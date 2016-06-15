package com.uestc.lyreg.carsharing.presenter;

/**
 * Created by Administrator on 2016/6/15.
 *
 * @Author lyreg
 */
public class ErrorProcessOps {

    /**
     * View mandatory methods. Available to Presenter
     * Presenter -> View
     */
    interface RequiredViewOps {
        void showToast(String msg);
        void showAlert(String msg);
    }

    /**
     * Operations offered from Presenter to View
     * View -> Presenter
     */
    interface PresenterOps {
        void onConfigurationChanged(RequiredViewOps view);
        void onDestory(boolean isChangingConfig);
//        void open(X509Certificate serverCrt, KeyPair keyPair, String serialNum, String openRequest);
        
    }

}
