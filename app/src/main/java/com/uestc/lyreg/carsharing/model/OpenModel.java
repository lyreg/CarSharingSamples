package com.uestc.lyreg.carsharing.model;

import android.util.Log;

import com.uestc.lyreg.carsharing.entity.AuthRequest;
import com.uestc.lyreg.carsharing.entity.AuthResponse;
import com.uestc.lyreg.carsharing.entity.DistRequest;
import com.uestc.lyreg.carsharing.entity.OpenRequest;
import com.uestc.lyreg.carsharing.network.Network;
import com.uestc.lyreg.carsharing.network.api.CarSharingService;
import com.uestc.lyreg.carsharing.presenter.EnrollPresenter;
import com.uestc.lyreg.carsharing.presenter.OpenMvpOps;
import com.uestc.lyreg.carsharing.utils.HexStringConvert;
import com.uestc.lyreg.carsharing.utils.SecurityUtils;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.SecretKey;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/5/25.
 *
 * @Author lyreg
 */
public class OpenModel implements OpenMvpOps.ModelOps {
    private final String TAG = getClass().getSimpleName();

    private OpenMvpOps.RequiredPresenterOps mPresenter;

    //car sharing service reference
    private CarSharingService mCarSharingApi;

    private Subscription mSubscription;

    private String mRandomN1;
    private String mRandomN2;
    private SecretKey mSessionKey;

    public OpenModel(OpenMvpOps.RequiredPresenterOps mPresenter) {
        this.mPresenter = mPresenter;
        mCarSharingApi = Network.getCarSharingApi();
    }

    /**
     * Sent from {@link EnrollPresenter#onDestory(boolean)}
     * should stop/kill operations that could be running
     * and are't needed anymore
     */
    @Override
    public void onDestroy() {
        // destory retrofit
        if(mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    @Override
    public void open(final X509Certificate cert,
                     final KeyPair keyPair,
                     final String serialNum,
                     final String openRequest) {

        mSubscription = Observable.create(
                new Observable.OnSubscribe<AuthRequest>() {
                    @Override
                    public void call(Subscriber<? super AuthRequest> subscriber) {
                        AuthRequest mAuthRequest = genAuthRequest(cert, keyPair, serialNum, openRequest);
                        if(mAuthRequest != null) {
                            subscriber.onNext(mAuthRequest);
                            subscriber.onCompleted();
                        }
                    }
                })
                .flatMap(new Func1<AuthRequest, Observable<AuthResponse>>() {
                    @Override
                    public Observable<AuthResponse> call(AuthRequest request) {
                        return mCarSharingApi.auth(request);
                    }
                })
                .flatMap(new Func1<AuthResponse, Observable<DistRequest>>() {
                    @Override
                    public Observable<DistRequest> call(final AuthResponse authResponse) {
                        return Observable.create(new Observable.OnSubscribe<DistRequest>() {
                            @Override
                            public void call(Subscriber<? super DistRequest> subscriber) {
                                DistRequest mDistRequest = genDistRequest(cert, keyPair, authResponse);
                                if(mDistRequest != null) {
                                    subscriber.onNext(mDistRequest);
                                    subscriber.onCompleted();
                                }
                            }
                        });
                    }
                })
                .flatMap(new Func1<DistRequest, Observable<ResponseBody>>() {
                    @Override
                    public Observable<ResponseBody> call(DistRequest request) {
                        return mCarSharingApi.distribution(request);
                    }
                })
                .flatMap(new Func1<ResponseBody, Observable<OpenRequest>>() {
                    @Override
                    public Observable<OpenRequest> call(final ResponseBody responseBody) {
                        return Observable.create(new Observable.OnSubscribe<OpenRequest>() {
                            @Override
                            public void call(Subscriber<? super OpenRequest> subscriber) {
                                OpenRequest mOpenRequest = genOpenRequest(openRequest, "2096");
                                if(mOpenRequest != null) {
                                    subscriber.onNext(mOpenRequest);
                                    subscriber.onCompleted();
                                }
                            }
                        });
                    }
                })
                .flatMap(new Func1<OpenRequest, Observable<ResponseBody>>() {
                    @Override
                    public Observable<ResponseBody> call(OpenRequest request) {
                        return mCarSharingApi.opendoor(request);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {
                        Log.v(TAG, "Open Door Completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        mPresenter.onError(e.getMessage());
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        Log.e(TAG, "onNext before mPresenter.onOpenSuccess()");
                        mPresenter.onOpenSuccess();
                    }
                });

    }

    private AuthRequest genAuthRequest(final X509Certificate cert,
                                       final KeyPair keyPair,
                                       final String serialNum,
                                       final String openRequest) {

        AuthRequest request = null;

        try {
            mRandomN1 = SecurityUtils.generateRandom(6);
            RSAPublicKey serverPubKey = (RSAPublicKey) cert.getPublicKey();
            String randomN1Hex = HexStringConvert.hexToString(
                    SecurityUtils.encryptByPublicKey(mRandomN1.getBytes(), serverPubKey)
            );

            String serialNumHex = HexStringConvert.hexToString(
                    SecurityUtils.encryptByPublicKey(serialNum.getBytes(), serverPubKey)
            );

            String openReqHex = HexStringConvert.hexToString(
                    SecurityUtils.encryptByPublicKey(openRequest.getBytes(), serverPubKey)
            );

            request = new AuthRequest(randomN1Hex, serialNumHex, openReqHex);

        } catch (Exception e) {
            e.printStackTrace();
            mPresenter.onError(e.getMessage());
        }

        return request;
    }

    private DistRequest genDistRequest(final X509Certificate cert,
                                       final KeyPair keyPair,
                                       AuthResponse response) {
        DistRequest request = null;

        try {

            if(parseAuthResponse(keyPair, response)) {
                mSessionKey = SecurityUtils.generateDesKey(56);
                RSAPublicKey serverPubKey = (RSAPublicKey) cert.getPublicKey();
                PrivateKey clientPriKey = keyPair.getPrivate();

                String randomN2Hex = HexStringConvert.hexToString(
                        SecurityUtils.encryptByPublicKey(mRandomN2.getBytes(), serverPubKey)
                );

                String sessionkeyHex = HexStringConvert.hexToString(
                        SecurityUtils.encryptByPublicKey(mSessionKey.getEncoded(), serverPubKey)
                );

                byte[] sessionkeyHash = SecurityUtils.MD5Digest(mSessionKey.getEncoded());

                String hash = HexStringConvert.hexToString(
                        SecurityUtils.sign(sessionkeyHash, clientPriKey)
                );

                request = new DistRequest(randomN2Hex, sessionkeyHex, hash);

            } else {
                throw new Exception("认证失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            mPresenter.onError(e.getMessage());
        }

        return request;
    }

    private boolean parseAuthResponse(KeyPair keypair, AuthResponse response)
            throws Exception {

        PrivateKey clientPriKey = keypair.getPrivate();

        byte[] rawRand1 = HexStringConvert.stringToHex( response.getRand1());
        byte[] rawRand2 = HexStringConvert.stringToHex( response.getRand2());

        String rand1 = new String(
                SecurityUtils.decryptByPrivateKey(rawRand1, clientPriKey)
        );

        String rand2 = new String(
                SecurityUtils.decryptByPrivateKey(rawRand2, clientPriKey)
        );

        mRandomN2 = rand2;

        return rand1.equals(mRandomN1);
    }

    private OpenRequest genOpenRequest(String openRequest, String cardId) {
        OpenRequest request = null;

        try {

            String openRequestHex = HexStringConvert.hexToString(
                    SecurityUtils.encryptByDesKey(openRequest, mSessionKey)
            );

            String cardIdHex = HexStringConvert.hexToString(
                    SecurityUtils.encryptByDesKey(cardId, mSessionKey)
            );

            request = new OpenRequest(openRequestHex, cardIdHex);

        } catch (Exception e) {
            e.printStackTrace();
            mPresenter.onError(e.getMessage());
        }

        return request;
    }

}
