package com.uestc.lyreg.carsharing;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.uestc.lyreg.carsharing.utils.SecurityUtils;

import java.io.InputStream;
import java.math.BigInteger;
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

    private X509Certificate mServerCert;

    private X509Certificate mClientCert;

    private BigInteger mClientCertSerialNum;

    private KeyPair mClientKeyPair;

    private static Application context;

    @Override
    public void onCreate() {
        super.onCreate();

        initCertAndKeyPair();
        context = this;

    }

    public static Context getContext() {
        return context;
    }

    private void initCertAndKeyPair() {
        try {

            Log.e(TAG, "initCertAndKeyPair");
            AssetManager am = getAssets();
            InputStream is = am.open("server.crt");

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            this.mServerCert = (X509Certificate) cf.generateCertificate(is);

            this.mClientKeyPair = SecurityUtils.generateKeyPair(1024);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public X509Certificate getServerCert() {
        return this.mServerCert;
    }

    public void setClientCert(X509Certificate cert) {
        this.mClientCert = cert;
        this.mClientCertSerialNum = cert.getSerialNumber();
    }

    public KeyPair getClientKeyPair() {
        return this.mClientKeyPair;
    }

//    public static void setClientKeyPair(KeyPair keypair) {
//        mClientKeyPair = keypair;
//    }
}
