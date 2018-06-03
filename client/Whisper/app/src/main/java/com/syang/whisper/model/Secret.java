package com.syang.whisper.model;

import java.security.PublicKey;

public class Secret {
    private PublicKey publicKey;
    private String xxteaKey;
    private String aesKey;
    private String aesIV;

    public Secret(PublicKey publicKey, String aesKey, String aesIV) {
        this.publicKey = publicKey;
        this.aesKey = aesKey;
        this.aesIV = aesIV;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getAesKey() {
        return aesKey;
    }

    public String getAesIV() {
        return aesIV;
    }
}
