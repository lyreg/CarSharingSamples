package com.uestc.lyreg.carsharing.presenter;

import com.uestc.lyreg.carsharing.EnrollActivity;
import com.uestc.lyreg.carsharing.model.EnrollModel;

import java.lang.ref.WeakReference;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

/**
 * Created by Administrator on 2016/5/20.
 *
 * @Author lyreg
 */
public class EnrollPresenter
        implements EnrollMvpOps.PresenterOps,
        EnrollMvpOps.RequiredPresenterOps {

    //Layer View reference
    private WeakReference<EnrollMvpOps.RequiredViewOps> mView;

    //Layer Model reference
    private EnrollMvpOps.ModelOps mModel;

    //Configuration change state
    private boolean mIsChangingConfig;



    public EnrollPresenter(EnrollMvpOps.RequiredViewOps mView) {
        this.mView = new WeakReference<>(mView);
        this.mModel = new EnrollModel(this);
    }

    /**
     * Sent from Activity after a configuration changes
     * @param view View reference
     */
    @Override
    public void onConfigurationChanged(EnrollMvpOps.RequiredViewOps view) {
        mView = new WeakReference<>(view);
    }

    /**
     * Receives {@link EnrollActivity#onDestroy()} event
     * @param isChangingConfig  Config change state
     */
    @Override
    public void onDestory(boolean isChangingConfig) {
        mView = null;
        mIsChangingConfig = isChangingConfig;
        if(!isChangingConfig) {
            mModel.onDestroy();
        }
    }

    /**
     * Called by user interaction from {@link EnrollActivity}
     * Enroll a new account
     * @param username username
     * @param password password
     */
    @Override
    public void enroll(X509Certificate cert, KeyPair keyPair, String username, String password) {
        if(cert != null) {
            mModel.enroll(cert, keyPair, username, password);
        } else {
            onError("No Server Cert!");
        }
    }

    /**
     * Called from {@link EnrollModel}
     * when a new account is enrolled successfully
     */
    @Override
    public void onEnrollSuccess() {
        if(mView.get() != null) {
            mView.get().showToast("注册成功");
        }
    }

    /**
     * Receives call from {@link EnrollModel}
     * when some errors happen
     */
    @Override
    public void onError(String errorMsg) {
        if(mView.get() != null) {
            mView.get().showAlert(errorMsg);
        }
    }
}
