package com.syang.whisper.model;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.github.bassaer.chatmessageview.model.IChatUser;

import java.io.Serializable;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class User implements IChatUser, Serializable {
    private int id;
    private Bitmap icon;
    private String email;
    private RSAPublicKey mPublicKey;

    public User(){
    }

    public User(int id, String email) {
        this.id = id;
        this.email = email;
    }

    public User(int id, String email, RSAPublicKey publicKey) {
        this(id, email);
        this.mPublicKey = publicKey;
    }

    @Nullable
    @Override
    public String getId() {
        return Integer.valueOf(id).toString();
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Nullable
    @Override
    public String getName() {
        return getEmail();
    }

    @Nullable
    @Override
    public Bitmap getIcon() {
        return this.icon;
    }

    @Override
    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public RSAPublicKey getPublicKey() {
        return mPublicKey;
    }

    public void setPublicKey(RSAPublicKey publicKey) {
        mPublicKey = publicKey;
    }


}
