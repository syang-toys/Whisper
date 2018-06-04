package com.syang.whisper.security;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    private static final String ALGORITHM = "AES";
    private static final String PADDING = "AES/CBC/PKCS5Padding";

    public static String Base64Encrypt(String data, String key, String iv) {
        return Base64.encodeToString(encrypt(data, key, iv), Base64.DEFAULT);
    }

    public static String Base64Decrypt(String data, String key, String iv) {
        return decrypt(Base64.decode(data, Base64.DEFAULT), key, iv);
    }

    public static byte[] encrypt(String data, String key, String iv) {
        if (key == null) {
            key = "NOT_HACK_THE_KEY";
        }
        if (iv == null) {
            iv = "IT_IS_DEFAULT_IV";
        }
        IvParameterSpec mIV = new IvParameterSpec(iv.getBytes());
        SecretKeySpec mSecretKeySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
        try {
            Cipher cipher = Cipher.getInstance(PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, mSecretKeySpec, mIV);
            return cipher.doFinal(data.getBytes());
        } catch (Exception ex) {
            return null;
        }
    }

    public static String decrypt(byte[] data, String key, String iv) {
        if (key == null) {
            key = "NOT_HACK_THE_KEY";
        }
        if (iv == null) {
            iv = "IT_IS_DEFAULT_IV";
        }
        IvParameterSpec mIV = new IvParameterSpec(iv.getBytes());
        SecretKeySpec mSecretKeySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
        try {
            Cipher cipher = Cipher.getInstance(PADDING);
            cipher.init(Cipher.DECRYPT_MODE, mSecretKeySpec, mIV);
            return new String(cipher.doFinal(data));
        } catch (Exception ex) {
            return "";
        }
    }
}
