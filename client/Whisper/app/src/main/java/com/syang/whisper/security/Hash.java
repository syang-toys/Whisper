package com.syang.whisper.security;

import android.util.Base64;

import java.security.MessageDigest;

public class Hash {
    public static String MD5Hash(byte[] data) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(data);
            return Base64.encodeToString(md5.digest(), Base64.DEFAULT);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String SHA256Hash(byte[] data) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(data);
            return Base64.encodeToString(sha256.digest(), Base64.DEFAULT);
        } catch (Exception ex) {
            return null;
        }
    }
}
