package com.syang.whisper.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.syang.whisper.R;
import com.syang.whisper.model.User;

import java.util.List;

public class AddAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView email;
        private Button accept;
        private Button deny;

        public ViewHolder(final View parent, TextView email, Button accept, Button deny) {
            super(parent);
            this.email = email;
            this.accept = accept;
            this.deny = deny;
        }

        public static ViewHolder newInstance(View parent) {
            TextView email = (TextView)parent.findViewById(R.id.email);
            Button accept = (Button)parent.findViewById(R.id.accept);
            Button deny = (Button)parent.findViewById(R.id.deny);
            return new ViewHolder(parent, email, accept, deny);
        }

        public void setUserEmail(CharSequence text) {
            email.setText(text);
        }
    }

    private List<User> mPendingFriends;

    public AddAdapter(List<User> pendingFriends) {
        this.mPendingFriends = pendingFriends;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.add_item, parent, false);
        final ViewHolder holder = ViewHolder.newInstance(view);

        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        String itemText = mPendingFriends.get(position).getEmail();
        holder.setUserEmail(itemText);
    }

    @Override
    public int getItemCount() {
        return mPendingFriends == null ? 0 : mPendingFriends.size();
    }

    public void setPendingFriends(List<User> pendingFriends) {
        this.mPendingFriends = pendingFriends;
    }
}
