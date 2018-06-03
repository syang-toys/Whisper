package com.syang.whisper.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.syang.whisper.R;
import com.syang.whisper.adapter.FriendsAdapter;
import com.syang.whisper.adapter.RecyclerAdapter;
import com.syang.whisper.model.User;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {

    private List<User> friends;
    private FriendsAdapter mFriendsAdapter;
    private RecyclerView recyclerView;

    public FriendsFragment() {
    }

    public static FriendsFragment newInstance() {
        return new FriendsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_friends, container, false);
        setupRecyclerView(recyclerView);
        return recyclerView;
    }


    private void setupRecyclerView(RecyclerView recyclerView) {
        List<User> users = new ArrayList<>();
        users.add(new User(9, RandomStringUtils.random(5)));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFriendsAdapter = new FriendsAdapter(users);
        recyclerView.setAdapter(mFriendsAdapter);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            List<User> users = new ArrayList<>();
            users.add(new User(9, RandomStringUtils.random(5)));
            users.add(new User(9, RandomStringUtils.random(5)));
            users.add(new User(9, RandomStringUtils.random(5)));

            if(mFriendsAdapter != null) {
                mFriendsAdapter.setFriends(users);
                mFriendsAdapter.notifyDataSetChanged();


            }
        }
    }

}
