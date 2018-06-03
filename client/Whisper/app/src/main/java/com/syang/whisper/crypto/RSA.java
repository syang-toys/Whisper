package com.syang.whisper.crypto;

import android.util.Base64;

import java.security.Key;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;

public class RSA {
    private static final String ALGORITHM = "RSA/NONE/PKCS1Padding";

    public static String
    Base64Encrypt(String data, Key key) {
        return Base64.encodeToString(encrypt(data, key), Base64.DEFAULT);
    }

    public static String Base64Decrypt(String data, Key key) {
        return decrypt(Base64.decode(data, Base64.DEFAULT), key);
    }

    public static byte[] encrypt(String data, Key key) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data.getBytes());
        } catch (Exception ex) {
            return null;
        }
    }

    public static String decrypt(byte[] data, Key key) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(data));
        } catch (Exception ex) {
            return "";
        }
    }
}
