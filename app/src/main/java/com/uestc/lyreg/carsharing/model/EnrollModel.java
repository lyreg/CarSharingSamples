package com.uestc.lyreg.carsharing.model;

import android.util.Log;

import com.uestc.lyreg.carsharing.entity.RegRequest;
import com.uestc.lyreg.carsharing.entity.RegResponse;
import com.uestc.lyreg.carsharing.network.Network;
import com.uestc.lyreg.carsharing.network.api.CarSharingService;
import com.uestc.lyreg.carsharing.presenter.EnrollMvpOps;
import com.uestc.lyreg.carsharing.presenter.EnrollPresenter;
import com.uestc.lyreg.carsharing.utils.HexStringConvert;
import com.uestc.lyreg.carsharing.utils.SecurityUtils;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.SecretKey;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/5/23.
 *
 * @Author lyreg
 */
public class EnrollModel implements EnrollMvpOps.ModelOps {

    private final String TAG = getClass().getSimpleName();

    //Presenter reference
    private EnrollMvpOps.RequiredPresenterOps mPresenter;

    //car sharing service reference
    private CarSharingService mCarSharingApi;

    private Subscription mSubscription;

    public EnrollModel(EnrollMvpOps.RequiredPresenterOps mPresenter) {
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
    public void enroll(final X509Certificate cert, final KeyPair keyPair, final String username, final String password) {

//        RegRequest mRegRequest = generateRequest(cert, username, password);
//
//        if(mRegRequest != null) {
//
//            mSubscription = mCarSharingApi.registration(mRegRequest)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Subscriber<RegResponse>() {
//                        @Override
//                        public void onCompleted() {
//                            Log.v(TAG, "onCompleted");
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                            mPresenter.onError(e.getMessage());
//                        }
//
//                        @Override
//                        public void onNext(RegResponse regResponse) {
//                            Log.e(TAG, regResponse.getSerialNum());
//                            Log.e(TAG, regResponse.getCert());
//                            mPresenter.onEnrollSuccess();
//                        }
//                    });
//
//        }

        mSubscription = Observable.create(new Observable.OnSubscribe<RegRequest>() {
                    @Override
                    public void call(Subscriber<? super RegRequest> subscriber) {
                        RegRequest mRegRequest = generateRequest(cert, keyPair, username, password);
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
                        Log.v(TAG, "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        mPresenter.onError(e.getMessage());
                    }

                    @Override
                    public void onNext(RegResponse regResponse) {
                        Log.e(TAG, regResponse.getSerialNum());
                        Log.e(TAG, regResponse.getCert());
                        mPresenter.onEnrollSuccess();
                    }
                });
    }

    private RegRequest generateRequest(X509Certificate cert, KeyPair keyPair, String username, String password) {
        RegRequest request = null;
        try {
            SecretKey rawSessionKey = SecurityUtils.generateDesKey(56);
            RSAPublicKey serverPubKey = (RSAPublicKey) cert.getPublicKey();
            String sessionkeyHex = HexStringConvert.hexToString(
                    SecurityUtils.encryptByPublicKey(rawSessionKey.getEncoded(), serverPubKey)
            );

            String usernameHex = HexStringConvert.hexToString(
                    SecurityUtils.encryptByDesKey(username, rawSessionKey)
            );
            String passwordHex = HexStringConvert.hexToString(
                    SecurityUtils.encryptByDesKey(SecurityUtils.MD5Digest(password.getBytes()), rawSessionKey)
            );
            String rawhash = username + password;
            String hashHex = HexStringConvert.hexToString(
                    SecurityUtils.MD5Digest(rawhash.getBytes())
            );

//            KeyPair keyPair = SecurityUtils.generateKeyPair(1024);

//            final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
//            keyStore.load(null);
//            final KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(SecurityUtils.CLIENTKEY, null);
//            KeyPair keyPair = new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());

//            KeyPair keyPair = BaseApplication

            String csr = SecurityUtils.genCSR(keyPair, "CN",
                    "SiChuan", "ChengDu", "UESTC", username, "lyreg@163.com");

            request = new RegRequest(sessionkeyHex, usernameHex, passwordHex, hashHex, csr);

        } catch (Exception e) {
            e.printStackTrace();
            mPresenter.onError(e.getMessage());
        }

        return request;
    }
}
