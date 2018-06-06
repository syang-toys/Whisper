package com.syang.whisper.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.syang.whisper.R;
import com.syang.whisper.activity.ChatActivity;
import com.syang.whisper.model.User;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mItemTextView;

        public ViewHolder(final View parent, TextView itemTextView) {
            super(parent);
            mItemTextView = itemTextView;
        }

        public static ViewHolder newInstance(View parent) {
            TextView itemTextView = (TextView) parent.findViewById(R.id.itemTextView);
            return new ViewHolder(parent, itemTextView);
        }

        public void setItemText(CharSequence text) {
            mItemTextView.setText(text);
        }
    }
    private List<User> friends;

    public FriendsAdapter(List<User> friends) {
        this.friends = friends;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.friends_item, parent, false);
        final ViewHolder holder = ViewHolder.newInstance(view);
        holder.mItemTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = friends.get(holder.getAdapterPosition());
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("user_data", user);
                context.startActivity(intent);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        String itemText = friends.get(position).getEmail();
        holder.setItemText(itemText);
    }

    @Override
    public int getItemCount() {
        return friends == null ? 0 : friends.size();
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
    }
}
