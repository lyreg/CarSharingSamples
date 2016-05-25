package com.uestc.lyreg.carsharing.presenter;

import com.uestc.lyreg.carsharing.model.OpenModel;

import java.lang.ref.WeakReference;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

/**
 * Created by Administrator on 2016/5/25.
 *
 * @Author lyreg
 */
public class OpenPresenter
        implements OpenMvpOps.PresenterOps,
        OpenMvpOps.RequiredPresenterOps {


    //Layer View reference
    private WeakReference<OpenMvpOps.RequiredViewOps> mView;

    //Layer Model reference
    private OpenMvpOps.ModelOps mModel;

    //Configuration change state
    private boolean mIsChangingConfig;

    public OpenPresenter(OpenMvpOps.RequiredViewOps mView) {
        this.mView = new WeakReference<>(mView);
        this.mModel = new OpenModel(this);
    }

    /**
     * Sent from Activity after a configuration changes
     * @param view View reference
     */
    @Override
    public void onConfigurationChanged(OpenMvpOps.RequiredViewOps view) {
        this.mView = new WeakReference<>(view);
    }

    @Override
    public void onDestory(boolean isChangingConfig) {
        mView = null;
        mIsChangingConfig = isChangingConfig;
        if(!isChangingConfig) {
            mModel.onDestroy();
        }
    }

    @Override
    public void open(X509Certificate serverCrt, KeyPair keyPair, String serialNum, String openRequest) {
        if(serverCrt != null) {
            mModel.open(serverCrt, keyPair, serialNum, openRequest);
        } else {
            onError("No server Cert!");
        }
    }

    @Override
    public void onOpenSuccess() {
        if(mView.get() != null) {
            mView.get().showToast("Open成功");
        }
    }

    @Override
    public void onError(String errorMsg) {
        if(mView.get() != null) {
            mView.get().showAlert(errorMsg);
        }
    }
}
