package com.example.p2pen;

/**
 * Created by zhangbin on 2015/12/15.
 */

        import android.content.Context;
        import android.util.Base64;
        import android.util.Log;
        import android.widget.Toast;

        import java.io.BufferedInputStream;
        import java.io.BufferedOutputStream;
        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.io.OutputStream;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.security.Key;
        import java.security.KeyFactory;
        import java.security.KeyPair;
        import java.security.KeyPairGenerator;
        import java.security.MessageDigest;
        import java.security.NoSuchAlgorithmException;
        import java.security.interfaces.RSAPrivateKey;
        import java.security.interfaces.RSAPublicKey;
        import java.security.spec.PKCS8EncodedKeySpec;
        import java.security.spec.X509EncodedKeySpec;
        import java.util.Arrays;
        import java.util.Random;

        import javax.crypto.Cipher;
        import javax.net.ssl.HttpsURLConnection;

/**
 * Created by zhangbin on 2015/12/8.
 */
public class HttpRequest {

    static String KEY_ALGO = "RSA";
    static String KEY_ALGO_PAD = "RSA/None/PKCS1Padding";
    static RSAPublicKey publicKey;
    static RSAPrivateKey privateKey;
    static byte lastNum = -1;

    public static void initKeys() {
        KeyPairGenerator keyPairGen;
        try {
            keyPairGen = KeyPairGenerator.getInstance(KEY_ALGO);
            keyPairGen.initialize(512);

            KeyPair keyPair = keyPairGen.generateKeyPair();

            publicKey = (RSAPublicKey) keyPair.getPublic();
            privateKey = (RSAPrivateKey) keyPair.getPrivate();

        } catch (Exception e) {

        }
    }

    public static String getPublicKey() {
        return Base64.encodeToString(getBytesFromPubKey(), Base64.DEFAULT);
    }

    public static byte[] encryptByPublicKey (byte[] data, byte[] pKey) {
        try {
            X509EncodedKeySpec x509Key = new X509EncodedKeySpec(pKey);
            KeyFactory factory = KeyFactory.getInstance(KEY_ALGO);
            Key publicK = factory.generatePublic(x509Key);

            Cipher cipher  = Cipher.getInstance(KEY_ALGO_PAD);
            cipher.init(Cipher.ENCRYPT_MODE, publicK);

            return cipher.doFinal(data);
        } catch (Exception e) {

            return null;
        }
    }

    public static byte[] encryptByPrivateKey (byte[] data, byte[] priKey) {
        try {
            PKCS8EncodedKeySpec pkcs = new PKCS8EncodedKeySpec(priKey);
            KeyFactory factory = KeyFactory.getInstance(KEY_ALGO);
            Key privateK = factory.generatePrivate(pkcs);

            Cipher cipher  = Cipher.getInstance(KEY_ALGO_PAD);
            cipher.init(Cipher.ENCRYPT_MODE, privateK);

            return cipher.doFinal(data);
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] decryptByPrivateKey (byte[] data, byte[] priKey) {
        try {
            PKCS8EncodedKeySpec pkcs = new PKCS8EncodedKeySpec(priKey);
            KeyFactory factory = KeyFactory.getInstance(KEY_ALGO);
            Key privateK = factory.generatePrivate(pkcs);

            Cipher cipher = Cipher.getInstance(KEY_ALGO_PAD);
            cipher.init(Cipher.DECRYPT_MODE, privateK);

            return cipher.doFinal(data);
        } catch (Exception e) {

            return null;
        }
    }

    public static byte[] decryptByPublicKey (byte[] data, byte[] pKey) {
        try {
            X509EncodedKeySpec x509Key = new X509EncodedKeySpec(pKey);
            KeyFactory factory = KeyFactory.getInstance(KEY_ALGO);
            Key publicK = factory.generatePublic(x509Key);

            Cipher cipher  = Cipher.getInstance(KEY_ALGO_PAD);
            cipher.init(Cipher.DECRYPT_MODE, publicK);

            return cipher.doFinal(data);
        } catch (Exception e) {
            return null;
        }
    }


    public static byte[] getBytesFromPriKey () {
        return privateKey.getEncoded();
    }

    public static byte[] getBytesFromPubKey () {
        return publicKey.getEncoded();
    }

    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            String t = Integer.toHexString(0xff & b);
            if (t.length() == 1) {
                builder.append("0");
            }
            builder.append(t);
        }
        return builder.toString();
    }

    public static byte[] hexStringToBytes(String str) {
        int num = str.length();
        byte[] bs = new byte[num];
        /*for (int i = 0; i < num; i += 2) {
            bs[i] = Byte.valueOf(str.substring(i, i + 2), 16);
            Log.d("sockettest", bs[i] + " ");
        }*/

        for (int i = 0; i < num; i += 2) {
            bs[i] = Byte.valueOf(str.substring(i, i + 1), 16);
            bs[i + 1] = Byte.valueOf(str.substring(i + 1, i + 2), 16);
            //Log.d("sockettest", bs[i] + " ");
        }

        return bs;
    }

    public static boolean verify(byte num, byte[] data, byte[] valueOfSha1) {

        if (!Arrays.equals(HttpRequest.sign(data), valueOfSha1)) {
            return false;
        }

        if (lastNum != -1) {
            if (!((lastNum + 1) % 128 == num)) {
                return false;
            }
        }

        lastNum = num;

        return true;
    }

    public static byte[] sign(byte[] endata) {

        byte[] t = new byte[20];
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
            mDigest.update(endata);
            t = mDigest.digest();
        } catch (NoSuchAlgorithmException e) {

        }

        return t;
    }

    public static byte getLastNum() {
        if (lastNum == -1) {
            lastNum = (byte)Math.abs((new Random().nextInt()) % 128);
        }

        lastNum = (byte)((lastNum + 1) % 128);
        return lastNum;
    }

}

