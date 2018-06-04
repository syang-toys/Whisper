package com.syang.whisper.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.syang.whisper.R;
import com.syang.whisper.WhisperApplication;
import com.syang.whisper.adapter.FriendsAdapter;
import com.syang.whisper.model.User;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {
    private FriendsAdapter mFriendsAdapter;
    private WhisperApplication app;

    public FriendsFragment() {
    }

    public static FriendsFragment newInstance() {
        return new FriendsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (WhisperApplication)getActivity().getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_friends, container, false);
        setupRecyclerView(recyclerView);
        return recyclerView;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFriendsAdapter = new FriendsAdapter(app.getFriends());
        recyclerView.setAdapter(mFriendsAdapter);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            if(mFriendsAdapter != null) {
                mFriendsAdapter.setFriends(app.getFriends());
                mFriendsAdapter.notifyDataSetChanged();
            }
        }
    }
}
