package com.syang.whisper.security;

import org.xxtea.XXTEA;

public class XXTea {
    public static String Base64Encrypt(String data, String key) {
        return XXTEA.encryptToBase64String(data, key);
    }

    public static String Base64Decrypt(String data, String key) {
        return XXTEA.decryptBase64StringToString(data, key);
    }

    public static byte[] encrypt(String data, String key) {
        return XXTEA.encrypt(data, key);
    }

    public static byte[] encrypt(byte[] data, String key) {
        return XXTEA.encrypt(data, key);
    }


    public static String decrypt(byte[] data, String key) {
        return XXTEA.decryptToString(data, key);
    }
}
