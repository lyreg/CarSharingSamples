package com.uestc.lyreg.carsharing;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.uestc.lyreg.carsharing.utils.HexStringConvert;
import com.uestc.lyreg.carsharing.utils.Preferences;
import com.uestc.lyreg.carsharing.utils.SecurityUtils;

import org.spongycastle.util.encoders.Base64;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.SecretKey;

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

//        testAuth();
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



    private void testAuth() {
        String rand1 = "30d576209672923bdd23b7d8f545aaf3fbbc186d0415b680409edaa17379e5ecc343c2" +
                "4436c35ebe097a64206cd30f1dbeb073ec84b08e6846576865bf07348acd8e28866748d821514ecc" +
                "bfbbe647b8e8c9443a09a0de19dc069fa2398e92461eafd6d9b70fcfa1d8d76b9e8bc2e364692b47" +
                "7dfa975d4f5fc1c069985930b6";

        String rand2 = "1270a38b707dd13f5068ad8ed3cf49847e0c7ebcd98ba2703f475fe98947a70bee4f75" +
                "0c89f63b0ae1dd5f3b4310d1fb608011f8939dea6ba996c79c757bf09e673e3be39c60ef963208d6" +
                "4859d3dfb35162aff632bb391e9cb2258504d71f750a5a695a0c0640893de7d65856ace61eb61349" +
                "e818255ee2f7f164f8ef82dac9";

        try {
            byte[] data = SecurityUtils.decryptByPrivateKey(HexStringConvert.stringToHex(rand1), mClientKeyPair.getPrivate());

            Log.e(TAG, "rand1 => " + new String(data));

            data = SecurityUtils.decryptByPrivateKey(HexStringConvert.stringToHex(rand2), mClientKeyPair.getPrivate());

            Log.e(TAG, "rand2 => " + new String(data));


            Log.e(TAG, "publickey => " + HexStringConvert.hexToString(mClientKeyPair.getPublic().getEncoded()));
            Log.e(TAG, "publickey => " + Base64.toBase64String(mClientKeyPair.getPublic().getEncoded()));

            SecretKey mSessionKey = SecurityUtils.generateDesKey(56);
            rand2 = SecurityUtils.generateRandom(6);
            RSAPublicKey serverPubKey = (RSAPublicKey) mServerCert.getPublicKey();
            PrivateKey clientPriKey = mClientKeyPair.getPrivate();

            String randomN2Hex = HexStringConvert.hexToString(
                    SecurityUtils.encryptByPublicKey(data, serverPubKey)
            );

            String sessionkeyHex = HexStringConvert.hexToString(
                    SecurityUtils.encryptByPublicKey(mSessionKey.getEncoded(), serverPubKey)
            );

            byte[] sessionkeyHash = SecurityUtils.MD5Digest(mSessionKey.getEncoded());

            String hash = HexStringConvert.hexToString(
                    SecurityUtils.sign(sessionkeyHash, clientPriKey)
            );

            Log.e(TAG, "rand2 => " + new String(data));
            Log.e(TAG, "sessionkey => " + HexStringConvert.hexToString(mSessionKey.getEncoded()));
            Log.e(TAG, "sessionhash => " + HexStringConvert.hexToString(sessionkeyHash));

            Log.e(TAG, "rand2_jiami => " + randomN2Hex);
            Log.e(TAG, "sessionkey_jiami => " + sessionkeyHex);
            Log.e(TAG, "hash => " + hash);

            boolean verify = SecurityUtils.verify(sessionkeyHash, HexStringConvert.stringToHex(hash), mClientKeyPair.getPublic());

            Log.e(TAG, "verify => " + verify);

            String serialNum = "486769bace5c4a2baf954945fa782685";

            String serialNumjiami = HexStringConvert.hexToString(
                    SecurityUtils.encryptByPublicKey(HexStringConvert.stringToHex(serialNum), serverPubKey)
            );

            Log.e(TAG, "serialNum => " + serialNum);
            Log.e(TAG, "serrialNum_Cipher => " + serialNumjiami);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
