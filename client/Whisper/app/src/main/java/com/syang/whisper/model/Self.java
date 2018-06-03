package com.syang.whisper.model;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class Self extends User {
    private RSAPrivateKey mPrivateKey;

    public Self() {
        super();
    }

    public Self(int id, String email, RSAPrivateKey privateKey) {
        super(id, email);
        this.mPrivateKey = privateKey;
    }

    public Self(int id, String email, RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        super(id, email);
        setPublicKey(publicKey);
        this.mPrivateKey = privateKey;
    }

    public void initial(int id, String email, RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        setId(id);
        setEmail(email);
        setPublicKey(publicKey);
        this.mPrivateKey = privateKey;
    }

    public RSAPrivateKey getPrivateKey() {
        return mPrivateKey;
    }

    public void setPrivateKey(RSAPrivateKey privateKey) {
        mPrivateKey = privateKey;
    }
}
