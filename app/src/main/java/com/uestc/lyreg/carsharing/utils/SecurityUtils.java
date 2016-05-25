package com.uestc.lyreg.carsharing.utils;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.util.Log;

import com.uestc.lyreg.carsharing.BaseApplication;

import org.spongycastle.asn1.misc.MiscObjectIdentifiers;
import org.spongycastle.asn1.misc.NetscapeCertType;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.X500NameBuilder;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.asn1.x509.BasicConstraints;
import org.spongycastle.asn1.x509.ExtendedKeyUsage;
import org.spongycastle.asn1.x509.Extension;
import org.spongycastle.asn1.x509.ExtensionsGenerator;
import org.spongycastle.asn1.x509.KeyPurposeId;
import org.spongycastle.asn1.x509.KeyUsage;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.X509v3CertificateBuilder;
import org.spongycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import org.spongycastle.pkcs.PKCS10CertificationRequest;
import org.spongycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.spongycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.spongycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAKey;
import java.util.Calendar;
import java.util.Date;

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
//        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        Security.addProvider(new BouncyCastleProvider());
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

    public static KeyPair generateKeyPairAndStore(int keysize)
            throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA, PROVIDER);
        kpg.initialize(keysize, new SecureRandom());

        KeyPair keyPair = kpg.generateKeyPair();

        final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        String issuer = "C=CN,E=lyreg@163.com";
        String subject = issuer;
        Certificate cert = generateV3(issuer, subject,
                BigInteger.ZERO, new Date(System.currentTimeMillis() - 1000
                        * 60 * 60 * 24),
                new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24
                        * 365 * 32),
                keyPair.getPublic(),//待签名的公钥
                keyPair.getPrivate());

        cert.verify(keyPair.getPublic());

        keyStore.setKeyEntry(CLIENTKEY, keyPair.getPrivate(), null, new Certificate[] {cert});

        return keyPair;
    }

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

    public static KeyPair recoverKeyPairFromKeyStore(String alias)
            throws Exception {
        final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

//        keyStore.deleteEntry(CLIENTKEY);
        KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(CLIENTKEY, null);
        if(entry == null) {
            return null;
        }

        Log.e(TAG, "entry is not null");
        KeyPair kp = new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());
        return kp;
    }

    public static Certificate generateV3(String issuer, String subject,
                                         BigInteger serial, Date notBefore, Date notAfter,
                                         PublicKey publicKey, PrivateKey privKey)
            throws OperatorCreationException, CertificateException, IOException {

        X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                new X500Name(issuer), serial, notBefore, notAfter,
                new X500Name(subject), publicKey);
        ContentSigner sigGen = new JcaContentSignerBuilder("SHA1withRSA")
                .setProvider("BC").build(privKey);
        //privKey:使用自己的私钥进行签名，CA证书
//        if (extensions != null)
//            for (Extension ext : extensions) {
//                builder.addExtension(new ASN1ObjectIdentifier(ext.getOid()),
//                        ext.isCritical(),
//                        ASN1Primitive.fromByteArray(ext.getValue()));
//            }
        X509CertificateHolder holder = builder.build(sigGen);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream is1 = new ByteArrayInputStream(holder.toASN1Structure()
                .getEncoded());
        X509Certificate theCert = (X509Certificate) cf.generateCertificate(is1);
        is1.close();
        return theCert;
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

        KeyUsage keyUsage = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.nonRepudiation | KeyUsage.keyEncipherment | KeyUsage.dataEncipherment | KeyUsage.keyAgreement);
        ExtendedKeyUsage extKeyUsage = new ExtendedKeyUsage(KeyPurposeId.id_kp_clientAuth);
        NetscapeCertType netCertType = new NetscapeCertType(NetscapeCertType.objectSigning | NetscapeCertType.smime | NetscapeCertType.sslClient);

        ContentSigner signer = new keyStoreContentSigner(CLIENTKEY);


        PKCS10CertificationRequestBuilder requestBuilder =
                new JcaPKCS10CertificationRequestBuilder(subject, kp.getPublic());

//        PKCS10CertificationRequest req = requestBuilder.build(
//                new JcaContentSignerBuilder(MD5WITHRSA).setProvider(PROVIDER).
//                        build(kp.getPrivate()));

        requestBuilder.addAttribute(MiscObjectIdentifiers.netscapeCertType, netCertType);

        // Configure certificate extensions
        ExtensionsGenerator extGen = new ExtensionsGenerator();
        extGen.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
        extGen.addExtension(Extension.keyUsage, false, keyUsage);
        extGen.addExtension(Extension.extendedKeyUsage, false, extKeyUsage);


        PKCS10CertificationRequest req = requestBuilder.build(signer);

//        if(!req.isSignatureValid(new JcaContentVerifierProviderBuilder()
//                .build(kp.getPublic()))) {
//            throw new Exception("genCSR failed => " + MD5WITHRSA + " : Failed verify check.");
//        } else {
//            byte[] encoded = req.getEncoded();
//            String csr = "-----BEGIN CERTIFICATE REQUEST-----\n";
//            csr += new String(Base64.encode(encoded));
//            csr += "\n-----END CERTIFICATE REQUEST-----\n";
//
//            return csr;
//        }

        return new String(Base64.encode(req.getEncoded()));
    }

    public static byte[] sign(byte[] raw, PrivateKey privateKey)
            throws Exception {
        Signature signature = Signature.getInstance(MD5WITHRSA);
        signature.initSign(privateKey, new SecureRandom());

        signature.update(raw);

        return signature.sign();
    }

    public static boolean verify(byte[] raw, byte[] sign, PublicKey publicKey)
            throws Exception {
        Signature signature = Signature.getInstance(MD5WITHRSA);
        signature.initVerify(publicKey);
        signature.update(raw);

        return signature.verify(sign);
    }

    public static byte[] encryptByPublicKey(byte[] plaintext, PublicKey publicKey)
            throws Exception {
        Cipher cipher = Cipher.getInstance(RSA);
        SecureRandom random = new SecureRandom();
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, random);

        // 模长
        int key_len = ((RSAKey)publicKey).getModulus().bitLength() / 8;
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

    public static byte[] decryptByPrivateKey(byte[] ciphertext, PrivateKey privateKey)
            throws Exception {
        Cipher cipher = Cipher.getInstance(RSA);
        SecureRandom random = new SecureRandom();
        cipher.init(Cipher.DECRYPT_MODE, (Key) privateKey, random);
        //模长
        int key_len = ((RSAKey)privateKey).getModulus().bitLength() / 8;
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

    public static String generateRandom(int length) {
        int num = 1;
        double random = Math.random();
        if(random < 0.1) {
            random += 0.1;
        }

        for(int i=0; i < length; i++) {
            num = num * 10;
        }

        int raw = (int) (random * num);

        return raw + "";
    }


    public static class keyStoreContentSigner implements ContentSigner {

        private AlgorithmIdentifier algorithmIdentifier;
        private String algorithmText;
        private ByteArrayOutputStream dataStream;
        private String keyAlias;

        public keyStoreContentSigner(String keyAlias)
        {
            algorithmText = MD5WITHRSA;
            algorithmIdentifier = new DefaultSignatureAlgorithmIdentifierFinder().find(algorithmText);
            dataStream = new ByteArrayOutputStream();
            this.keyAlias = keyAlias;
        }

        public void setAlgorithm(String algorithmText)
        {
            this.algorithmText = algorithmText;
            algorithmIdentifier = new DefaultSignatureAlgorithmIdentifierFinder().find(algorithmText);
        }

        @Override
        public AlgorithmIdentifier getAlgorithmIdentifier() {
            return algorithmIdentifier;
        }

        /**
         * Returns a stream that will accept data for the purpose of calculating
         * a signature. Use org.spongycastle.util.io.TeeOutputStream if you want to accumulate
         * the data on the fly as well.
         *
         * @return an OutputStream
         */
        @Override
        public OutputStream getOutputStream() {
            return dataStream;
        }

        /**
         * Returns a signature based on the current data written to the stream, since the
         * start or the last call to getSignature().
         *
         * @return bytes representing the signature.
         */
        @Override
        public byte[] getSignature() {
            byte[] data;
            byte[] signature = null;
            KeyStore ks;

            try {
                ks = KeyStore.getInstance("AndroidKeyStore");
                ks.load(null);

                data = dataStream.toByteArray();
                dataStream.flush();

                Signature s = Signature.getInstance(algorithmText);
                KeyStore.Entry entry = ks.getEntry(keyAlias, null);
                PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();

                s.initSign(privateKey);
                s.update(data);
                signature = s.sign();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return signature;
        }
    }
}
