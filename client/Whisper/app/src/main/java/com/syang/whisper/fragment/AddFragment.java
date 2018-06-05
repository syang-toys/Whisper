package com.syang.whisper.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.syang.whisper.R;
import com.syang.whisper.WhisperApplication;
import com.syang.whisper.adapter.AddAdapter;
import com.syang.whisper.request.SecureSocket;

import io.socket.client.Ack;


public class AddFragment extends Fragment {
    private AddAdapter mAddAdapter;
    private WhisperApplication app;

    public AddFragment() {
    }

    public static AddFragment newInstance() {
        return new AddFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (WhisperApplication) getActivity().getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View layout = inflater.inflate(R.layout.fragment_add, container, false);
        final RecyclerView mRecyclerView = (RecyclerView) layout.findViewById(R.id.add_recycler_view);
        final SearchView mSearchView = (SearchView) layout.findViewById(R.id.searchView);
        setupSearchView(mSearchView);
        setupRecyclerView(mRecyclerView);
        return layout;
    }

    private void setupSearchView(SearchView searchView) {
        searchView.setIconified(false);
        searchView.onActionViewExpanded();
        // setup listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String email) {
                app.emitFriendRequest(email, new Ack() {
                    public void call(Object... args) {
                        final String status = SecureSocket.clientDecrypt((String) args[0], (String) args[1]);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), status, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAddAdapter = new AddAdapter(app);
        recyclerView.setAdapter(mAddAdapter);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (mAddAdapter != null) {
                mAddAdapter.notifyDataSetChanged();
            }
        }
    }
}
