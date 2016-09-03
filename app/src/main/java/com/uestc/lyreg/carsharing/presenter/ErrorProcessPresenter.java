package com.uestc.lyreg.carsharing.presenter;

import android.util.Log;

import com.uestc.lyreg.carsharing.entity.AuthRequest;
import com.uestc.lyreg.carsharing.entity.AuthResponse;
import com.uestc.lyreg.carsharing.entity.OpenRequest;
import com.uestc.lyreg.carsharing.entity.RegRequest;
import com.uestc.lyreg.carsharing.entity.RegResponse;
import com.uestc.lyreg.carsharing.network.Network;
import com.uestc.lyreg.carsharing.network.api.CarSharingService;
import com.uestc.lyreg.carsharing.utils.HexStringConvert;
import com.uestc.lyreg.carsharing.utils.SecurityUtils;

import java.lang.ref.WeakReference;
import java.security.KeyPair;
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
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Administrator on 2016/6/15.
 *
 * @Author lyreg
 */
public class ErrorProcessPresenter
        implements ErrorProcessMvpOps.PresenterOps {

    private final String TAG = getClass().getSimpleName();

    //Layer View reference
    private WeakReference<ErrorProcessMvpOps.RequiredViewOps> mView;

    //Configuration change state
    private boolean mIsChangingConfig;

    //car sharing service reference
    private CarSharingService mCarSharingApi;

    private CompositeSubscription mCompositeSubscription;

    public ErrorProcessPresenter(ErrorProcessMvpOps.RequiredViewOps mView) {
        this.mView = new WeakReference<>(mView);
        mCarSharingApi = Network.getCarSharingApi();
        mCompositeSubscription = new CompositeSubscription();
    }

    @Override
    public void onConfigurationChanged(ErrorProcessMvpOps.RequiredViewOps view) {
        this.mView = new WeakReference<>(view);
    }

    @Override
    public void onDestory(boolean isChangingConfig) {
        mView = null;
        mIsChangingConfig = isChangingConfig;

        // destory retrofit
        if(mCompositeSubscription != null && !mCompositeSubscription.isUnsubscribed()) {
            mCompositeSubscription.unsubscribe();
            mCompositeSubscription.clear();
        }
    }

    @Override
    public void errorProcessOne(final KeyPair keyPair) {
        Subscription mSubscription =
                Observable.create(new Observable.OnSubscribe<RegRequest>() {
                    @Override
                    public void call(Subscriber<? super RegRequest> subscriber) {
                        RegRequest mRegRequest = generateRequest(keyPair, "Alice", "Alice's password");
                        subscriber.onNext(mRegRequest);
                        subscriber.onCompleted();
                    }
                })
                .flatMap(new Func1<RegRequest, Observable<RegResponse>>() {
                    @Override
                    public Observable<RegResponse> call(RegRequest request) {
                        return mCarSharingApi.registration(request);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RegResponse>() {
                    @Override
                    public void onCompleted() {
                        Log.v(TAG, "Enroll Completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(mView.get() != null) {
                            mView.get().showAlert(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(RegResponse regResponse) {
                        Log.e(TAG, regResponse.getSerialNum());
                        Log.e(TAG, regResponse.getCert());
                        if(mView.get() != null) {
                            mView.get().showToast("注册成功");
                        }
                    }
                });

        mCompositeSubscription.add(mSubscription);
    }

    private RegRequest generateRequest(KeyPair keyPair, String username, String password) {
        RegRequest request = null;
        try {
            SecretKey rawSessionKey = SecurityUtils.generateDesKey(56);
            Log.e(TAG, HexStringConvert.hexToString(rawSessionKey.getEncoded()));
            String sessionkeyHex = HexStringConvert.hexToString(
                    rawSessionKey.getEncoded()
            );

            String usernameHex = username;

            String passwordHex = password;

            String rawhash = username + password;
            String hashHex = HexStringConvert.hexToString(
                    SecurityUtils.MD5Digest(rawhash.getBytes())
            );

            String csr = SecurityUtils.genCSR(keyPair, "CN",
                    "SiChuan", "ChengDu", "UESTC", username, "lyreg@163.com");

            request = new RegRequest(sessionkeyHex, usernameHex, passwordHex, hashHex, csr);

        } catch (Exception e) {
            e.printStackTrace();
            mView.get().showAlert(e.getMessage());
        }
        return request;
    }

    @Override
    public void errorProcessTwo(final X509Certificate cert, final KeyPair keyPair, final String openRequest) {
        Subscription mSubscription = Observable.create(
                new Observable.OnSubscribe<AuthRequest>() {
                    @Override
                    public void call(Subscriber<? super AuthRequest> subscriber) {
                        AuthRequest mAuthRequest = genAuthRequest(cert, "1234567890", openRequest);
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<AuthResponse>() {
                    @Override
                    public void onCompleted() {
                        Log.v(TAG, "Open Door Completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(mView.get() != null) {
                            mView.get().showAlert(e.getMessage());
                        }
                    }


                    @Override
                    public void onNext(AuthResponse response) {
                        Log.e(TAG, "onNext before mPresenter.onOpenSuccess()");
                        if(mView.get() != null) {
                            mView.get().showToast("Open成功");
                        }
                    }
                });
        mCompositeSubscription.add(mSubscription);
    }

    private AuthRequest genAuthRequest(final X509Certificate cert,
                                       final String serialNum,
                                       final String openRequest) {

        AuthRequest request = null;

        try {
            String mRandomN1 = SecurityUtils.generateRandom(6);
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

            Log.e(TAG, "rand1 => " + mRandomN1);
            Log.e(TAG, "serialNum => " + serialNum);
            Log.e(TAG, "openreq => " + openRequest);

            request = new AuthRequest(randomN1Hex, serialNumHex, openReqHex);

        } catch (Exception e) {
            e.printStackTrace();
            mView.get().showAlert(e.getMessage());
        }

        return request;
    }

    @Override
    public void errorProcessThree(X509Certificate serverCrt, KeyPair keyPair, final String openRequest) {
        Subscription mSubscription = Observable.create(new Observable.OnSubscribe<OpenRequest>() {
                    @Override
                    public void call(Subscriber<? super OpenRequest> subscriber) {
                        OpenRequest mOpenRequest = genOpenRequest(openRequest, "2694");
                        if(mOpenRequest != null) {
                            subscriber.onNext(mOpenRequest);
                            subscriber.onCompleted();
                        }
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
                        if(mView.get() != null) {
                            mView.get().showAlert(e.getMessage());
                        }
                    }
                    @Override
                    public void onNext(ResponseBody response) {
                        Log.e(TAG, "onNext before mPresenter.onOpenSuccess()");
                        if(mView.get() != null) {
                            mView.get().showToast("Open成功");
                        }
                    }
                });
        mCompositeSubscription.add(mSubscription);
    }

    private OpenRequest genOpenRequest(String openRequest, String cardId) {
        OpenRequest request = null;

        try {

            SecretKey mSessionKey = SecurityUtils.generateDesKey(56);

            String openRequestHex = HexStringConvert.hexToString(
                    SecurityUtils.encryptByDesKey(openRequest, mSessionKey)
            );

            String cardIdHex = HexStringConvert.hexToString(
                    SecurityUtils.encryptByDesKey(cardId, mSessionKey)
            );

            request = new OpenRequest(openRequestHex, cardIdHex);

        } catch (Exception e) {
            e.printStackTrace();
            mView.get().showAlert(e.getMessage());
        }

        return request;
    }
}
