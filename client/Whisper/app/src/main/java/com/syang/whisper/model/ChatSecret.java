package com.syang.whisper.model;

import com.syang.whisper.security.RSA;
import com.syang.whisper.security.XXTea;

import java.security.PublicKey;

public class ChatSecret {
    private String key;

    public ChatSecret(String key) {
        this.key = key;
    }

    public void updateKey(String key2) {
        this.key = this.key + key2;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
