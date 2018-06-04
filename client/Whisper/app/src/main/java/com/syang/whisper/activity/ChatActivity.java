package com.syang.whisper.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.bassaer.chatmessageview.model.Message;
import com.github.bassaer.chatmessageview.util.ChatBot;
import com.github.bassaer.chatmessageview.view.ChatView;


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
import java.util.Random;

import io.socket.client.Ack;

public class ChatActivity extends Activity {

    private static final int FILE_SELECTION_CODE = 0;
    private static final int READ_REQUEST_CODE = 100;

    private WhisperApplication app;
    private ChatView mChatView;
    private User friend;
    private User me;
    private ChatSecret mChatSecret;
    private MessageList mMessageList;


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

        initialChatSecret();
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

                String encryptFileName = mChatSecret.getChatSecretMsg(fileName);
                byte[] encryptContent = mChatSecret.getChatSecretMsg(content);
                String encryptSignature = mChatSecret.getChatSecretMsg(signature);

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
                //Add message list
                mMessageList.add(message);
            } catch (Exception ex) {
                Toast.makeText(this, R.string.read_file_error, Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initialChatSecret() {
        mChatSecret = app.getChatSecretMap().get(Integer.valueOf(friend.getId()));
        if (mChatSecret == null) {
            final String key1 = RandomStringUtils.random(16);
            final int id = Integer.valueOf(friend.getId());
            app.emitInitialChatting(id, key1, new Ack() {
                @Override
                public void call(Object... args) {

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
                // new message
                Message message = new Message.Builder()
                        .setUser(me)
                        .setRight(true)
                        .setText(mChatView.getInputText())
                        .hideIcon(false)
                        .setStatusIconFormatter(new MyMessageStatusFormatter(ChatActivity.this))
                        .setStatusTextFormatter(new MyMessageStatusFormatter(ChatActivity.this))
                        .setStatusStyle(Message.Companion.getSTATUS_ICON())
                        .setStatus(1)
                        .build();

                // send to server
                String encryptContent = mChatSecret.getChatSecretMsg(message.getText());
                String signature = Hash.SHA256Hash(message.getText().getBytes());
                String encryptSignature = mChatSecret.getChatSecretMsg(signature);
                app.emitTextMsg(Integer.valueOf(friend.getId()), encryptContent, encryptSignature);
                //Set to chat view
                mChatView.send(message);
                //Add message list
                mMessageList.add(message);
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
}