package com.syang.whisper;

import android.app.Application;

import com.syang.whisper.model.Self;
import com.syang.whisper.model.User;
import com.syang.whisper.request.Restful;
import com.syang.whisper.utils.RSAUtil;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.PublicKey;
import java.util.List;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;

public class WhisperApplication extends Application {
    private User self;
    private List<User> friends;

    private Socket socket;
    {
        try {
            socket = IO.socket("http://10.0.2.2:3000/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        self = new Self();
        setServerPublicKey();
    }

    private void setServerPublicKey(){
        PublicKey mServerPublicKey;
        try {
            InputStream is = getAssets().open("server_public_key.pem");
            byte[] data = new byte[is.available()];
            is.read(data);
            is.close();
            mServerPublicKey = RSAUtil.getPublicKey(new String(data));
            Restful.setServerPublicKey(mServerPublicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User getSelf() {
        return self;
    }

    public void setSelf(User self) {
        this.self = self;
    }
/*
    public void connectSocket() {
        socket.connect();
    }

    public void emitOnline() {
        socket.emit("online", );
    }

    public void emitOffline() {
        socket.emit("offline");
    }

    public void emitFriendRequest(String email, Ack ack) {
        socket.emit("friend request", encryptForServerSession(email), ack);
    }

    public void emitAcceptFriendRequest(String email, Ack ack) {
        socket.emit("accept friend request", encryptForServerSession(email), ack);
    }

    public void emitDenyFriendRequest(String email) {
        socket.emit("deny friend request", encryptForServerSession(email));
    }

    public void emitInitialTextMsg(String email, String encryptedKey, String encryptedIV, String encryptedContent, String encryptedSignature) {
        socket.emit("initial text msg", encryptForServerSession(email), encryptedKey, encryptedIV, encryptedContent, encryptedSignature);
    }

    public void emitTextMsg(String email, String encryptedContent, String encryptedSignature) {
        socket.emit("text msg", encryptForServerSession(email), encryptedContent, encryptedSignature);
    }

    public void emitInitialFileMsg(String email, String encryptedKey, String encryptedIV, String encryptedFileName, byte[] encryptedContent, String encryptedSignature) {
        socket.emit("initial file msg", encryptForServerSession(email), encryptedKey, encryptedIV, encryptedFileName, encryptedContent, encryptedSignature);
    }

    public void emitFileMsg(String email, String encryptedFileName, byte[] encryptedContent, String encryptedSignature) {
        socket.emit("file msg", encryptForServerSession(email), encryptedFileName, encryptedContent, encryptedSignature);
    }

    public void bindSocketEvents() {
        socket.on("test", onTest);
        socket.on("friends", onFriends);
        socket.on("friend online", onFriendOnline);
        socket.on("friend offline", onFriendOffline);
        socket.on("new friend request", onNewFriendRequest);
        socket.on("friend request accepted", onFriendRequestAccepted);
        socket.on("initial text msg received", onInitialTextMsgReceived);
        socket.on("text msg received", onTextMsgReceived);
        socket.on("initial file msg received", onInitialFileMsgReceived);
        socket.on("file msg received", onFileMsgReceived);
    }

    public void unbindSocketEvents() {
        socket.off("test", onTest);
        socket.off("friends", onFriends);
        socket.off("friend online", onFriendOnline);
        socket.off("friend offline", onFriendOffline);
        socket.off("new friend request", onNewFriendRequest);
        socket.off("friend request accepted", onFriendRequestAccepted);
        socket.off("initial text msg received", onInitialTextMsgReceived);
        socket.off("text msg received", onTextMsgReceived);
        socket.off("initial file msg received", onInitialFileMsgReceived);
        socket.off("file msg received", onFileMsgReceived);
    }*/
}
