package com.syang.whisper.model;

import com.syang.whisper.security.RSA;
import com.syang.whisper.security.XXTea;

import java.security.PublicKey;

public class ChatSecret {
    private String key;
    private PublicKey mPublicKey;

    public ChatSecret(String key) {
        this.key = key;
    }

    public void updateKey(String key2) {
        this.key = this.key + key2;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setPublicKey(PublicKey publicKey) {
        mPublicKey = publicKey;
    }

    public String getChatSecretMsg(String plainData) {
        return XXTea.Base64Encrypt(plainData, key);
    }

    public byte[] getChatSecretMsg(byte[] plainData) {
        return XXTea.encrypt(plainData, key);
    }

    public String getChatPlainMsg(String encryptData) {
        return XXTea.Base64Decrypt(encryptData, key);
    }

    public boolean checkSignature(String signature, String hash) {
        return RSA.Base64Decrypt(signature, mPublicKey).equals(hash);
    }

}
