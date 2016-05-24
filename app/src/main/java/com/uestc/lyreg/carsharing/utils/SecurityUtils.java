package com.uestc.lyreg.carsharing.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.util.Log;

import com.uestc.lyreg.carsharing.BaseApplication;

import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.X500NameBuilder;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import org.spongycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.spongycastle.pkcs.PKCS10CertificationRequest;
import org.spongycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.spongycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.spongycastle.util.encoders.Base64;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;

/**
 * Created by Administrator on 2016/5/23.
 *
 * @Author lyreg
 */
public class SecurityUtils {

    private static final String TAG         = "SecurityUtils";

    private static final String DES         = "DES";
    private static final String RSA         = "RSA";
    private static final String PROVIDER    = "SC";
    private static final String MD5         = "MD5";
    private static final String MD5WITHRSA  = "MD5withRSA";

    public static final String CLIENTKEY   = "clientKeyPair";


    static {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    public static SecretKey generateDesKey(int keysize)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyGenerator kg = KeyGenerator.getInstance(DES, PROVIDER);
        SecureRandom random = new SecureRandom();
        kg.init(56, random);
        SecretKey key = kg.generateKey();
        return key;
    }

    public static byte[] encryptByDesKey(String plain, SecretKey key)
            throws Exception {
        return encryptByDesKey(plain.getBytes(), key);
    }

    public static byte[] encryptByDesKey(byte[] plain, SecretKey key)
            throws Exception {
        Cipher cipher = Cipher.getInstance(DES, PROVIDER);
        SecureRandom random = new SecureRandom();
        cipher.init(Cipher.ENCRYPT_MODE, key, random);

        byte[] cipherText = cipher.doFinal(plain);
        return cipherText;
    }

    public static byte[] decryptByDesKey(byte[] ciphertext, SecretKey key)
            throws Exception {
        Cipher cipher = Cipher.getInstance(DES, PROVIDER);
        SecureRandom random = new SecureRandom();

        cipher.init(Cipher.DECRYPT_MODE, key, random);

        byte[] plain = cipher.doFinal(ciphertext);
        return plain;
    }

    public static byte[] MD5Digest(byte[] msg) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance(MD5, PROVIDER);

        md5.update(msg);
        byte[] digests = md5.digest();

        return digests;
    }

    public static KeyPair generateKeyPair(int keysize)
            throws NoSuchProviderException, NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA, PROVIDER);
        kpg.initialize(keysize, new SecureRandom());

        KeyPair keyPair = kpg.generateKeyPair();
        return keyPair;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static KeyPair generateRsaKeyPairByKeyStore(Context context, int keysize)
            throws Exception {

        final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 10);

        KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA, "AndroidKeyStore");
        generator.initialize(new KeyPairGeneratorSpec.Builder(BaseApplication.getContext())
                .setAlias(CLIENTKEY)
                .setSubject(new X500Principal("CN=" + CLIENTKEY))
                .setSerialNumber(BigInteger.ONE)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build()
        );

        return generator.generateKeyPair();
    }

    public static String genCSR(KeyPair kp, String countryname, String state,
                         String localityname, String organization, String connonname, String email)
            throws Exception{
        X500NameBuilder x500NameBld = new X500NameBuilder(BCStyle.INSTANCE);

        x500NameBld.addRDN(BCStyle.C, countryname);
        x500NameBld.addRDN(BCStyle.ST, state);
        x500NameBld.addRDN(BCStyle.L, localityname);
        x500NameBld.addRDN(BCStyle.O, organization);
        x500NameBld.addRDN(BCStyle.CN, connonname);
        x500NameBld.addRDN(BCStyle.E, email);

        X500Name subject = x500NameBld.build();

        return genCSR(kp, subject);
    }

    public static String genCSR(KeyPair kp, X500Name subject)
            throws Exception {
        PKCS10CertificationRequestBuilder requestBuilder =
                new JcaPKCS10CertificationRequestBuilder(subject, kp.getPublic());

        PKCS10CertificationRequest req = requestBuilder.build(
                new JcaContentSignerBuilder(MD5WITHRSA).setProvider(PROVIDER).
                        build(kp.getPrivate()));

        if(!req.isSignatureValid(new JcaContentVerifierProviderBuilder()
                .setProvider(PROVIDER).build(kp.getPublic()))) {
            throw new Exception("genCSR failed => " + MD5WITHRSA + " : Failed verify check.");
        } else {
            byte[] encoded = req.getEncoded();
            String csr = "-----BEGIN CERTIFICATE REQUEST-----\n";
            csr += new String(Base64.encode(encoded));
            csr += "\n-----END CERTIFICATE REQUEST-----\n";

            return csr;
        }
    }

    public static byte[] sign(byte[] raw, RSAPrivateKey privateKey)
            throws Exception {
        Signature signature = Signature.getInstance(MD5WITHRSA, PROVIDER);
        signature.initSign(privateKey, new SecureRandom());

        signature.update(raw);

        return signature.sign();
    }

    public static boolean verify(byte[] raw, byte[] sign, RSAPublicKey publicKey)
            throws Exception {
        Signature signature = Signature.getInstance(MD5WITHRSA, PROVIDER);
        signature.initVerify(publicKey);
        signature.update(raw);

        return signature.verify(sign);
    }

    public static byte[] encryptByPublicKey(byte[] plaintext, RSAPublicKey publicKey)
            throws Exception {
        Cipher cipher = Cipher.getInstance(RSA, PROVIDER);
        SecureRandom random = new SecureRandom();
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, random);

        // 模长
        int key_len = publicKey.getModulus().bitLength() / 8;
        // 加密数据长度 <= 模长-11
        byte[][] datas = splitArray(plaintext, key_len - 11);
        Log.e(TAG, "enc datas len is " + datas.length);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for(byte[] s : datas) {
            out.write(cipher.doFinal(s));
        }
        out.close();

        return out.toByteArray();
    }

    public static byte[] decryptByPrivateKey(byte[] ciphertext, RSAPrivateKey privateKey)
            throws Exception {
        Cipher cipher = Cipher.getInstance(RSA, PROVIDER);
        SecureRandom random = new SecureRandom();
        cipher.init(Cipher.DECRYPT_MODE, privateKey, random);
        //模长
        int key_len = privateKey.getModulus().bitLength() / 8;
        //如果密文长度大于模长则要分组解密
        byte[][] arrays = splitArray(ciphertext, key_len);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for(byte[] arr : arrays){
            out.write(cipher.doFinal(arr));
        }
        out.close();

        return out.toByteArray();
    }

    /**
     *拆分数组
     */
    public static byte[][] splitArray(byte[] data, int len){
        int x = data.length / len;
        int y = data.length % len;
        int z = 0;
        if(y!=0){
            z = 1;
        }
        byte[][] arrays = new byte[x+z][];
        byte[] arr;
        for(int i=0; i<x+z; i++){
            arr = new byte[len];
            if(i==x+z-1 && y!=0){
                System.arraycopy(data, i*len, arr, 0, y);
            }else{
                System.arraycopy(data, i*len, arr, 0, len);
            }
            arrays[i] = arr;
        }
        return arrays;
    }
}
