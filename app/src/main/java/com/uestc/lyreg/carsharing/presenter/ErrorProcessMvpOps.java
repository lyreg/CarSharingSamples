package com.uestc.lyreg.carsharing.presenter;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

/**
 * Created by Administrator on 2016/6/15.
 *
 * @Author lyreg
 */
public class ErrorProcessMvpOps {

    /**
     * View mandatory methods. Available to Presenter
     * Presenter -> View
     */
    public interface RequiredViewOps {
        void showToast(String msg);
        void showAlert(String msg);
    }

    /**
     * Operations offered from Presenter to View
     * View -> Presenter
     */
    public interface PresenterOps {
        void onConfigurationChanged(RequiredViewOps view);
        void onDestory(boolean isChangingConfig);

        // 异常流程1：明文注册请求
        void errorProcessOne(KeyPair keyPair);
        // 异常流程2：模拟开门请求(错误用户证书序列号)
        void errorProcessTwo(X509Certificate serverCrt, KeyPair keyPair, String openRequest);
        // 异常流程3：模拟开门指令(错误会话密钥)
        void errorProcessThree(X509Certificate serverCrt, KeyPair keyPair, String openRequest);
    }

}
