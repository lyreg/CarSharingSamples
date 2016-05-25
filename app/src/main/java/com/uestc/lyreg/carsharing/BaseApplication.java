package com.uestc.lyreg.carsharing;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.uestc.lyreg.carsharing.utils.Preferences;
import com.uestc.lyreg.carsharing.utils.SecurityUtils;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Created by Administrator on 2016/5/24.
 *
 * @Author lyreg
 */
public class BaseApplication extends Application {
    private final String TAG = getClass().getSimpleName();

    private static final String CLIENTCRTSERIALNUM = "clientcrtserialnum";

    private X509Certificate mServerCert;

    private X509Certificate mClientCert;

    private String mClientCertSerialNum;

    private KeyPair mClientKeyPair;

    public static Application mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        initCertAndKeyPair();

        mContext = this;
    }

    private void initCertAndKeyPair() {
        try {

            Log.e(TAG, "initCertAndKeyPair");
            AssetManager am = getAssets();
            InputStream is = am.open("server.crt");

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            this.mServerCert = (X509Certificate) cf.generateCertificate(is);


            this.mClientKeyPair = SecurityUtils.recoverKeyPairFromKeyStore(SecurityUtils.CLIENTKEY);
            if(mClientKeyPair == null) {
                Log.e(TAG, "generateKeyPairAndStore");
                mClientKeyPair = SecurityUtils.generateKeyPairAndStore(1024);
            }

            this.mClientCertSerialNum = Preferences.
                    getSettingsParam(getBaseContext(), CLIENTCRTSERIALNUM, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Context getContext() {
        return mContext;
    }

    public X509Certificate getServerCert() {
        return this.mServerCert;
    }

    public void setClientCert(X509Certificate cert) {
        this.mClientCert = cert;
//        this.mClientCertSerialNum = cert.getSerialNumber();
    }

    public void setClientCrtSerialNum(String serialNum) {
        mClientCertSerialNum = serialNum;

        Preferences.setSettingsParam(getBaseContext(), CLIENTCRTSERIALNUM, serialNum);
    }

    public String getClientCrtSerialNum() {
        return mClientCertSerialNum;
    }

    public KeyPair getClientKeyPair() {
        return this.mClientKeyPair;
    }

}
