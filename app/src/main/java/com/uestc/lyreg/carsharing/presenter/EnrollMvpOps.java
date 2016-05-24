package com.uestc.lyreg.carsharing.presenter;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

/**
 * Created by Administrator on 2016/5/20.
 *
 * @Author lyreg
 */
public interface EnrollMvpOps {

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
        void enroll(X509Certificate cert, KeyPair keyPair, String username, String password);
    }

    /**
     * Operations offered from Presenter to Model
     * Model -> Presenter
     */
    interface RequiredPresenterOps {
        void onEnrollSuccess();
        void onError(String errorMsg);
    }

    /**
     * Model operations offered to Presenter
     * Presenter -> Model
     */
    interface ModelOps {
        void enroll(X509Certificate cert, KeyPair keyPair, String username, String password);
        void onDestroy();
    }
}
