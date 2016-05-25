package com.uestc.lyreg.carsharing.presenter;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

/**
 * Created by Administrator on 2016/5/25.
 *
 * @Author lyreg
 */
public interface OpenMvpOps {

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
        void open(X509Certificate serverCrt, KeyPair keyPair, String serialNum, String openRequest);
    }

    /**
     * Operations offered from Presenter to Model
     * Model -> Presenter
     */
    interface RequiredPresenterOps {
        void onOpenSuccess();
        void onError(String errorMsg);
    }

    /**
     * Model operations offered to Presenter
     * Presenter -> Model
     */
    interface ModelOps {
        void open(X509Certificate cert, KeyPair keyPair, String serialNum, String openRequest);
        void onDestroy();
    }
}
