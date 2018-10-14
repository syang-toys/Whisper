package com.syang.whisper;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.EventLog;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.github.bassaer.chatmessageview.model.Message;
import com.syang.whisper.activity.ChatActivity;
import com.syang.whisper.activity.MainActivity;
import com.syang.whisper.model.Chat;
import com.syang.whisper.model.MessageList;
import com.syang.whisper.model.MyMessageStatusFormatter;
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
    private final Map<Integer, ChatSecret> chatSecretMap = new TreeMap<>();
    private final Map<Integer, Chat> chatMap = new TreeMap<>();

    private MainActivity mMainActivity;
    private ChatActivity mChatActivity;

    private Socket socket;
    {
        try {
            socket = IO.socket("http://10.131.229.156:3000/");
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
            SecureSocket.mDefaultPublicKey = mServerPublicKey;
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
        return chatSecretMap;
    }

    public Map<Integer, Chat> getChatMap() {
        return chatMap;
    }

    public void setChatActivity(ChatActivity chatActivity) {
        mChatActivity = chatActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        mMainActivity = mainActivity;
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
                mMainActivity.notifyFriendsUpdate();
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
                Chat chat = new Chat(new MessageList());
                chat.setSecret(secret);
                chat.setFriend(findFriend(id));
                chatMap.put(id, chat);
                emitReplyInitialChatting(id, key2, new Ack() {
                    @Override
                    public void call(Object... args) {

                    }
                });
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
                chatMap.get(id).getSecret().updateKey(key2);
            } catch (Exception ex) {
            }
        }
    };

    private Emitter.Listener onTextMsgReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String id = SecureSocket.clientDecrypt((String)args[0], (String)args[1]);
            Chat chat = chatMap.get(Integer.valueOf(id));

            String content = chat.getChatPlainMsg((String)args[2]);
            String signature = (String)args[3];


            if (chat.checkSignature(signature, Hash.SHA256Hash(content.getBytes()))) {
                appendMsg(Integer.valueOf(id), content);
                Log.v("Demo", "Success!");
            } else {
                Log.v("Demo", "Failed!");
            }
        }
    };

    private Emitter.Listener onFileMsgReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            int id = Integer.valueOf(SecureSocket.clientDecrypt((String)args[0], (String)args[1]));
            Chat chat = chatMap.get(id);

            String fileName = chat.getChatPlainMsg((String)args[2]);
            byte[] content = chat.getChatPlainMsg((byte [])args[3]);
            String signature = (String)args[4];

            if (chat.checkSignature(signature, Hash.SHA256Hash(content))) {
                appendMsg(Integer.valueOf(id), fileName, content);
                Log.v("Demo", "Success!");
            } else {
                Log.v("Demo", "Failed!");

                AccessibilityEvent event;
                event.getEventType() == 32;
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

    private void appendMsg(int id, String text) {
        Chat chat = chatMap.get(id);
        if (chat != null) {

            Message message = new Message.Builder()
                    .setUser(chat.getFriend())
                    .setRight(false)
                    .setText(text)
                    .setStatusIconFormatter(new MyMessageStatusFormatter(getApplicationContext()))
                    .setStatusTextFormatter(new MyMessageStatusFormatter(getApplicationContext()))
                    .setStatusStyle(com.github.bassaer.chatmessageview.model.Message.Companion.getSTATUS_ICON())
                    .setStatus(MyMessageStatusFormatter.STATUS_DELIVERED)
                    .build();

            chat.getMessageList().add(message);
            if (mChatActivity != null && mChatActivity.getChat().getFriend().equals(chat.getFriend())) {
                mChatActivity.recvMsg(message);
            }
        }
    }

    private void appendMsg(int id, String fileName, byte[] data) {
        Log.v("Demo", fileName);
        Chat chat = chatMap.get(id);
        Bitmap bitmap;
        if (chat == null) {
            return;
        }
        if (fileName.equals("PICTURE: PNG")) {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            fileName = null;
        } else  {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.plain);
        }

        Message message1= new Message.Builder()
                .setRight(false)
                .setText(Message.Type.PICTURE.name())
                .setUser(chat.getFriend())
                .hideIcon(false)
                .setPicture(bitmap)
                .setType(Message.Type.PICTURE)
                .setStatusIconFormatter(new MyMessageStatusFormatter(getApplicationContext()))
                .setStatusStyle(Message.Companion.getSTATUS_ICON())
                .setStatus(MyMessageStatusFormatter.STATUS_DELIVERED)
                .build();

        chat.getMessageList().add(message1);

        if (mChatActivity != null && mChatActivity.getChat().getFriend().equals(chat.getFriend())) {
            mChatActivity.recvMsg(message1);
        }

        if (fileName != null) {
            try {
                saveToFile(fileName, data);
            } catch (IOException ex) {
                Log.e("Demo", ex.getMessage());
                return;
            }

            Message message2 = new Message.Builder()
                    .setUser(chat.getFriend())
                    .setRight(false)
                    .setText(fileName)
                    .setStatusIconFormatter(new MyMessageStatusFormatter(getApplicationContext()))
                    .setStatusTextFormatter(new MyMessageStatusFormatter(getApplicationContext()))
                    .setStatusStyle(com.github.bassaer.chatmessageview.model.Message.Companion.getSTATUS_ICON())
                    .setStatus(MyMessageStatusFormatter.STATUS_DELIVERED)
                    .build();
            chat.getMessageList().add(message2);

            if (mChatActivity != null && mChatActivity.getChat().getFriend().equals(chat.getFriend())) {
                mChatActivity.recvMsg(message2);
            }
        }
    }

    private void saveToFile(String fileName, byte[] content) throws IOException {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(content);
        fos.close();
    }
}
