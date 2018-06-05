package com.syang.whisper.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.github.bassaer.chatmessageview.model.Message;
import com.github.bassaer.chatmessageview.view.ChatView;
import com.github.bassaer.chatmessageview.view.MessageView;
import com.syang.whisper.R;
import com.syang.whisper.WhisperApplication;
import com.syang.whisper.model.Chat;
import com.syang.whisper.model.ChatSecret;
import com.syang.whisper.model.MessageList;
import com.syang.whisper.model.MyMessageStatusFormatter;
import com.syang.whisper.model.User;
import com.syang.whisper.request.SecureSocket;
import com.syang.whisper.security.Hash;
import com.syang.whisper.utils.FileUtil;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.socket.client.Ack;

public class ChatActivity extends Activity {

    private static final int FILE_SELECTION_CODE = 0;
    private static final int READ_REQUEST_CODE = 100;

    private WhisperApplication app;
    private ChatView mChatView;
    private User friend;
    private User me;
    private Chat mChat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        app = (WhisperApplication)getApplication();
        mChatView = (ChatView)findViewById(R.id.chat_view);
        setChatViewListener(mChatView);

        me = app.getSelf();
        friend = (User)getIntent().getSerializableExtra("user_data");
        initUsers();

        initialChat();
        loadMessage();
    }

    @Override
    protected void onStart() {
        super.onStart();
        app.setChatActivity(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        initUsers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == FILE_SELECTION_CODE) {
            Uri uri = data.getData();
            File file = new File(FileUtil.getPath(this, uri));
            try {
                String fileName = file.getName();
                byte[] content = FileUtil.fullyReadFileToBytes(file);
                String signature = Hash.SHA256Hash(content);

                String encryptFileName = mChat.getChatSecretMsg(fileName);
                byte[] encryptContent = mChat.getChatSecretMsg(content);
                String encryptSignature = mChat.getChatSecretMsg(signature);

                app.emitFileMsg(Integer.valueOf(friend.getId()), encryptFileName, encryptContent, encryptSignature);
            } catch (Exception e) {
                Toast.makeText(this, R.string.read_file_error, Toast.LENGTH_LONG).show();
            }
        } else if (resultCode == RESULT_OK && requestCode == READ_REQUEST_CODE) {
            Uri uri = data.getData();
            try {
                Bitmap picture = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                Message message = new Message.Builder()
                        .setRight(true)
                        .setText(Message.Type.PICTURE.name())
                        .setUser(me)
                        .hideIcon(false)
                        .setPicture(picture)
                        .setType(Message.Type.PICTURE)
                        .setStatusIconFormatter(new MyMessageStatusFormatter(ChatActivity.this))
                        .setStatusStyle(Message.Companion.getSTATUS_ICON())
                        .setStatus(MyMessageStatusFormatter.STATUS_DELIVERED)
                        .build();

                mChatView.send(message);
                mChat.getMessageList().add(message);
            } catch (Exception ex) {
                Toast.makeText(this, R.string.read_file_error, Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initialChat() {
        final int id = Integer.valueOf(friend.getId());
        final Map<Integer, Chat> chatMap = app.getChatMap();
        mChat = chatMap.get(id);
        if (mChat == null) {
            mChat = new Chat(new MessageList());
            final String key1 = RandomStringUtils.random(16);
            ChatSecret secret = new ChatSecret(key1);
            mChat.setSecret(secret);
            mChat.setFriend(friend);
            chatMap.put(id, mChat);
            app.emitInitialChatting(id, key1, new Ack() {
                @Override
                public void call(Object... args) {
                    String message = SecureSocket.clientDecrypt((String)args[0], (String)args[1]);
                    switch (message) {
                        case "friend not online!": chatMap.remove(id); break;
                        case "exchange secret key!": break;
                    }
                }
            });
        }
    }

    private void initUsers() {
        this.me.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.face_1));
        this.friend.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.face_2));
    }

    private void setChatViewListener(final ChatView mChatView) {
        mChatView.setOnBubbleClickListener(new Message.OnBubbleClickListener() {
            @Override
            public void onClick(Message message) {
                Toast.makeText(ChatActivity.this, message.getText(), Toast.LENGTH_SHORT).show();
            }
        });

        mChatView.setOnBubbleLongClickListener(new Message.OnBubbleLongClickListener() {
            @Override
            public void onLongClick(Message message) {
                Toast.makeText(ChatActivity.this, "Removed this message \n" + message.getText(), Toast.LENGTH_SHORT).show();
                mChatView.getMessageView().remove(message);
            }
        });

        mChatView.setOnClickSendButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mChatView.getInputText().equals("")) {
                    return;
                }
                // new message
                Message message = new Message.Builder()
                        .setUser(me)
                        .setRight(true)
                        .setText(mChatView.getInputText())
                        .setStatusIconFormatter(new MyMessageStatusFormatter(ChatActivity.this))
                        .setStatusTextFormatter(new MyMessageStatusFormatter(ChatActivity.this))
                        .setStatusStyle(Message.Companion.getSTATUS_ICON())
                        .setStatus(1)
                        .build();
                // send to server
                String encryptContent = mChat.getChatSecretMsg(message.getText());
                String signature = Hash.SHA256Hash(message.getText().getBytes());
                String encryptSignature = mChat.getChatSecretMsg(signature);
                app.emitTextMsg(Integer.valueOf(friend.getId()), encryptContent, encryptSignature);
                //Set to chat view
                mChatView.send(message);
                //Add message list
                mChat.getMessageList().add(message);
                //Reset edit text
                mChatView.setInputText("");
            }
        });

        mChatView.setOnClickOptionButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    public void sendMsg(Message msg) {
        mChatView.send(msg);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private void openFileDir() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select file"), FILE_SELECTION_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ChatActivity.this, R.string.file_choose_error, Toast.LENGTH_LONG).show();
        }
    }

    private void loadMessage() {
        List<Message> messages = new ArrayList<>();
        MessageList mMessageList = mChat.getMessageList();
        if (mMessageList.size() > 0) {
            for (int i = 0; i < mMessageList.size(); i++) {
                Message message = mMessageList.get(i);
                if (message.getUser().equals(friend)) {
                    message.getUser().setIcon(friend.getIcon());
                } else {
                    message.getUser().setIcon(me.getIcon());
                }
                message.setStatusStyle(Message.Companion.getSTATUS_ICON_RIGHT_ONLY());
                message.setStatusIconFormatter(new MyMessageStatusFormatter(this));
                message.setStatus(MyMessageStatusFormatter.STATUS_DELIVERED);
                messages.add(message);
            }
        }
        MessageView messageView = mChatView.getMessageView();
        messageView.init(messages);
        messageView.setSelection(messageView.getCount() - 1);
    }

    private void showDialog() {
        final String[] items = { getString(R.string.send_picture), getString(R.string.send_file) };

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.options))
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        switch (position) {
                            case 0 :
                                openGallery();
                                break;
                            case 1:
                                openFileDir();
                                break;
                        }
                    }
                })
                .show();
    }

    /* get set function */
    public Chat getChat() {
        return mChat;
    }
}