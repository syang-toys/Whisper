package com.syang.whisper.utils;

import android.util.Base64;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAUtil {
    public static PublicKey getPublicKey(String pemStr) {
        try {
            pemStr = pemStr.replace("-----BEGIN PUBLIC KEY-----\n", "");
            pemStr = pemStr.replace("-----END PUBLIC KEY-----", "");
            byte[] keyData = Base64.decode(pemStr, Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyData);
            return KeyFactory.getInstance("RSA").generatePublic(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PrivateKey getPrivateKey(String pemStr) {
        try {
            pemStr = pemStr.replace("-----BEGIN RSA PRIVATE KEY-----\n", "");
            pemStr = pemStr.replace("-----END RSA PRIVATE KEY-----", "");
            byte[] keyData = Base64.decode(pemStr, Base64.DEFAULT);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyData);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
