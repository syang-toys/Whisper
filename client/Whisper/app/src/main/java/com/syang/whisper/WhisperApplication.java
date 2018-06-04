package com.syang.whisper;

import android.app.Application;
import android.os.Environment;

import com.syang.whisper.security.Hash;
import com.syang.whisper.model.ChatSecret;
import com.syang.whisper.model.Self;
import com.syang.whisper.model.User;
import com.syang.whisper.request.Restful;
import com.syang.whisper.request.SecureSocket;
import com.syang.whisper.utils.RSAUtil;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class WhisperApplication extends Application {
    private final User self = new Self();
    private final List<User> friendsList = new ArrayList<>();
    private final List<String> pendingFriends = new ArrayList<>();
    private final Map<Integer, ChatSecret> chatMap = new TreeMap<>();

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

    public void connectSocket() {
        socket.connect();
    }

    public List<User> getFriends() {
        return friendsList;
    }

    public List<String> getPendingFriends() {
        return pendingFriends;
    }

    public Map<Integer, ChatSecret> getChatSecretMap() {
        return chatMap;
    }

    public void emitOnline() {
        String email = self.getEmail();
        String[] encryptSecret = SecureSocket.clientEncrypt(email);
        socket.emit("online", encryptSecret[0], encryptSecret[1]);
    }

    public void emitOffline() {
        socket.emit("offline");
    }

    public void emitFriendRequest(String email, Ack ack) {
        String[] encryptSecret = SecureSocket.clientEncrypt(email);
        socket.emit("friend request", encryptSecret[0], encryptSecret[1], ack);
    }

    public void emitAcceptFriendRequest(String email, Ack ack) {
        String[] encryptSecret = SecureSocket.clientEncrypt(email);
        socket.emit("accept friend request", encryptSecret[0], encryptSecret[1], ack);
    }

    public void emitDenyFriendRequest(String email) {
        socket.emit("deny friend request");
    }

    public void emitInitialChatting(int id, String key1, Ack ack) {
        ChatSecret secret = new ChatSecret(key1);
        secret.setPublicKey(findFriend(id).getPublicKey());
        chatMap.put(id, secret);
        String data = String.format("{\"id\":\"%s\",\"key1\":\"%s\"}", id, key1);
        String[] encryptSecret = SecureSocket.clientEncrypt(data);
        socket.emit("initial chatting", encryptSecret[0], encryptSecret[1], ack);
    }

    public void emitReplyInitialChatting(int id, String key2, Ack ack) {
        String data = String.format("{\"id\":\"%s\",\"key2\":\"%s\"}", id, key2);
        String[] encryptSecret = SecureSocket.clientEncrypt(data);
        socket.emit("reply initial chatting", encryptSecret[0], encryptSecret[1], ack);
    }

    public void emitTextMsg(int id, String encryptedContent, String encryptedSignature) {
        String[] encryptSecret = SecureSocket.clientEncrypt(Integer.toString(id));
        socket.emit("text msg", encryptSecret[0], encryptSecret[1], encryptedContent, encryptedSignature);
    }

    public void emitFileMsg(int id, String encryptedFileName, byte[] encryptedContent, String encryptedSignature) {
        String[] encryptSecret = SecureSocket.clientEncrypt(Integer.toString(id));
        socket.emit("file msg", encryptSecret[0], encryptSecret[1], encryptedFileName, encryptedContent, encryptedSignature);
    }

    public void bindSocketEvents() {
        socket.on("friends", onFriends);
        socket.on("new friend request", onNewFriendRequest);
        socket.on("friend request accepted", onFriendRequestAccepted);
        socket.on("receive initial chatting", onReceiveInitialChatting);
        socket.on("initial chatting reply", onInitialChattingReply);
        socket.on("text msg received", onTextMsgReceived);
        socket.on("file msg received", onFileMsgReceived);
    }

    public void unbindSocketEvents() {
        socket.off("friends", onFriends);
        socket.off("new friend request", onNewFriendRequest);
        socket.off("friend request accepted", onFriendRequestAccepted);
        socket.off("receive initial chatting", onReceiveInitialChatting);
        socket.off("initial chatting reply", onInitialChattingReply);
        socket.off("text msg received", onTextMsgReceived);
        socket.off("file msg received", onFileMsgReceived);
    }

    private  Emitter.Listener onFriends = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                String data = SecureSocket.clientDecrypt((String)args[0], (String)args[1]);
                JSONArray friends = new JSONArray(data);
                for (int i = 0; i < friends.length(); i++) {
                    JSONObject friend = friends.getJSONObject(i);
                    PublicKey publicKey = RSAUtil.getPublicKey(friend.getString("publicKey"));
                    User user = new User(friend.getInt("id"), friend.getString("email"), publicKey);
                    friendsList.add(user);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onNewFriendRequest = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String email = SecureSocket.clientDecrypt((String) args[0], (String)args[1]);
            pendingFriends.add(email);
        }
    };

    private Emitter.Listener onFriendRequestAccepted = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String data = SecureSocket.clientDecrypt((String) args[0], (String) args[1]);
            try {
                JSONObject friend = new JSONObject(data);
                PublicKey publicKey = RSAUtil.getPublicKey(friend.getString("publicKey"));
                User user = new User(friend.getInt("id"), friend.getString("email"), publicKey);
                friendsList.add(user);
            } catch (Exception ex) {

            }
        }
    };

    private Emitter.Listener onReceiveInitialChatting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String data = SecureSocket.clientDecrypt((String) args[0], (String) args[1]);
            try {
                JSONObject obj = new JSONObject(data);
                int id = obj.getInt("id");
                String key1 = obj.getString("key1");
                String key2 = RandomStringUtils.random(16);
                ChatSecret secret = new ChatSecret(key1+key2);
                secret.setPublicKey(findFriend(id).getPublicKey());
                chatMap.put(id, secret);
                emitReplyInitialChatting(id, key2, null);
            } catch (Exception ex) {
            }
        }
    };

    private Emitter.Listener onInitialChattingReply = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String data = SecureSocket.clientDecrypt((String) args[0], (String) args[1]);
            try {
                JSONObject obj = new JSONObject(data);
                int id = obj.getInt("id");
                String key2 = obj.getString("key2");
                chatMap.get(id).updateKey(key2);
            } catch (Exception ex) {
            }
        }
    };

    private void saveToFile(String fileName, byte[] content) throws IOException {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(content);
        fos.close();
    }

    private Emitter.Listener onTextMsgReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String id = SecureSocket.clientDecrypt((String)args[0], (String)args[1]);
            ChatSecret secret = findChatSecret(Integer.valueOf(id));

            String content = secret.getChatPlainMsg((String)args[2]);
            String signature = secret.getChatPlainMsg((String)args[3]);

            if (secret.checkSignature(signature, Hash.SHA256Hash(content.getBytes()))) {

            }
        }
    };

    private Emitter.Listener onFileMsgReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String id = SecureSocket.clientDecrypt((String)args[0], (String)args[1]);
            ChatSecret secret = findChatSecret(Integer.valueOf(id));

            String fileName = secret.getChatPlainMsg((String)args[2]);
            String content = secret.getChatPlainMsg((String)args[3]);
            String signature = secret.getChatPlainMsg((String)args[4]);

            if (secret.checkSignature(signature, Hash.SHA256Hash(content.getBytes()))) {

            }
        }
    };

    public User findFriend(int id) {
        for (User friend : friendsList) {
            if (Integer.valueOf(friend.getId()) == id) {
                return friend;
            }
        }
        return null;
    }

    public User findFriend(String email) {
        for (User friend : friendsList) {
            if (email.equals(friend.getEmail())) {
                return friend;
            }
        }
        return null;
    }

    public ChatSecret findChatSecret(int id) {
        return chatMap.get(id);
    }

    public ChatSecret findChatSecret(String email) {
        User friend = findFriend(email);
        if (friend != null) {
            return chatMap.get(Integer.valueOf(friend.getId()));
        }
        return null;
    }
}
