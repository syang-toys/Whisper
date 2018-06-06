package com.syang.whisper.request;

import com.syang.whisper.security.RSA;
import com.syang.whisper.security.XXTea;

import org.apache.commons.lang3.RandomStringUtils;

import java.security.PrivateKey;
import java.security.PublicKey;

public class SecureSocket {
    public static PublicKey mDefaultPublicKey;
    public static PrivateKey mDefaultPrivateKey;

    public static String[] clientEncrypt(String data) {
        String key = RandomStringUtils.random(32);
        String encryptData = XXTea.Base64Encrypt(data, key);
        String encryptKey = RSA.Base64Encrypt(key, mDefaultPublicKey);
        return new String[]{encryptKey, encryptData};
    }

    public static String clientDecrypt(String key, String data) {
        String decryptKey = RSA.Base64Decrypt(key, mDefaultPrivateKey);
        return XXTea.Base64Decrypt(data, decryptKey);
    }

    public static String clientSign(String content) {
        return RSA.Base64Encrypt(content, mDefaultPrivateKey);
    }

}