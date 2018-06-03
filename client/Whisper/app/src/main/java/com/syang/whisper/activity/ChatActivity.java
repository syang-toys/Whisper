package com.syang.whisper.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.github.bassaer.chatmessageview.model.Message;
import com.github.bassaer.chatmessageview.util.ChatBot;
import com.github.bassaer.chatmessageview.view.ChatView;


import com.syang.whisper.R;
import com.syang.whisper.model.User;

import java.util.Random;

public class ChatActivity extends Activity {

    private ChatView mChatView;
    private User user;
    private User me;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        user = (User)getIntent().getSerializableExtra("user_data");
        Log.v("Demo", user.getEmail());
    }
}