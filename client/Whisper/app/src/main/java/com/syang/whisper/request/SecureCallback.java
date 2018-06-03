package com.syang.whisper.request;

import okhttp3.Callback;

public abstract class SecureCallback implements Callback {
    private String key;

    public void setSecretKey(String key) {
        this.key = key;
    }

    public String getSecretKey(){
        return key;
    }
}
