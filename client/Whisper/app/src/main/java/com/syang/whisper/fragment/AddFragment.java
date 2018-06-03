package com.syang.whisper.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.syang.whisper.R;
import com.syang.whisper.adapter.AddAdapter;
import com.syang.whisper.adapter.FriendsAdapter;
import com.syang.whisper.model.User;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;


public class AddFragment extends Fragment {
    private List<User> mPendingFriends;
    private AddAdapter mAddAdapter;
    private RecyclerView mRecyclerView;

    public AddFragment() {
    }

    public static AddFragment newInstance() {
        return new AddFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_add, container, false);
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.add_recycler_view);
        //SearchView search = (SearchView)layout.findViewById(R.id.searchView);
        setupRecyclerView(mRecyclerView);
        return layout;
    }


    private void setupRecyclerView(RecyclerView recyclerView) {
        mPendingFriends = new ArrayList<>();
        mPendingFriends.add(new User(1, "33333333"));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAddAdapter = new AddAdapter(mPendingFriends);
        recyclerView.setAdapter(mAddAdapter);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            List<User> users = new ArrayList<>();
            users.add(new User(9, RandomStringUtils.random(5)));
            users.add(new User(9, RandomStringUtils.random(5)));
            users.add(new User(9, RandomStringUtils.random(5)));

            if(mAddAdapter != null) {
                mAddAdapter.setPendingFriends(users);
                mAddAdapter.notifyDataSetChanged();
            }
        }
    }
}
