package com.syang.whisper.model;

import com.syang.whisper.security.RSA;

import org.xxtea.XXTEA;

public class Chat {
    private MessageList mMessageList;
    private ChatSecret mSecret;
    private User friend;

    public Chat(MessageList messageList) {
        this.mMessageList = messageList;
    }

    public User getFriend() {
        return friend;
    }

    public void setFriend(User friend) {
        this.friend = friend;
    }

    public ChatSecret getSecret() {
        return mSecret;
    }

    public void setSecret(ChatSecret secret) {
        this.mSecret = secret;
    }

    public MessageList getMessageList() {
        return mMessageList;
    }

    private void setMessageList(MessageList messageList) {
        this.mMessageList = messageList;
    }

    public String getChatSecretMsg(String plainData) {
        return XXTEA.encryptToBase64String(plainData, mSecret.getKey());
    }

    public byte[] getChatSecretMsg(byte[] plainData) {
        return XXTEA.encrypt(plainData, mSecret.getKey());
    }

    public String getChatPlainMsg(String encryptData) {
        return XXTEA.decryptBase64StringToString(encryptData, mSecret.getKey());
    }

    public byte[] getChatPlainMsg(byte[] encryptData) {
        return XXTEA.decrypt(encryptData, mSecret.getKey());
    }

    public boolean checkSignature(String signature, String hash) {
        return RSA.Base64Decrypt(signature, friend.getPublicKey()).equals(hash);
    }
}
