package com.syang.whisper.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.syang.whisper.R;
import com.syang.whisper.WhisperApplication;
import com.syang.whisper.model.User;
import com.syang.whisper.request.SecureSocket;
import com.syang.whisper.utils.RSAUtil;

import org.json.JSONObject;

import java.security.PublicKey;
import java.util.List;

import io.socket.client.Ack;

public class AddAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView email;
        private Button accept;
        private Button deny;

        private ViewHolder(final View parent, TextView email, Button accept, Button deny) {
            super(parent);
            this.email = email;
            this.accept = accept;
            this.deny = deny;
        }

        private static ViewHolder newInstance(View parent) {
            TextView email = (TextView)parent.findViewById(R.id.email);
            Button accept = (Button)parent.findViewById(R.id.accept);
            Button deny = (Button)parent.findViewById(R.id.deny);
            return new ViewHolder(parent, email, accept, deny);
        }

        private void setUserEmail(CharSequence text) {
            email.setText(text);
        }
    }

    private List<String> mPendingFriends;
    private WhisperApplication app;

    public AddAdapter(WhisperApplication app) {
        this.app = app;
        this.mPendingFriends = app.getPendingFriends();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.add_item, parent, false);
        final ViewHolder holder = ViewHolder.newInstance(view);

        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = holder.getAdapterPosition();
                final String email = mPendingFriends.get(pos);
                app.emitAcceptFriendRequest(email, new Ack() {
                    @Override
                    public void call(Object... args) {
                        final String data = SecureSocket.clientDecrypt((String) args[0], (String) args[1]);
                        try {
                            final JSONObject friend = new JSONObject(data);
                            PublicKey publicKey = RSAUtil.getPublicKey(friend.getString("publicKey"));
                            User user = new User(friend.getInt("id"), friend.getString("email"), publicKey);
                            app.getFriends().add(user);
                            app.getPendingFriends().remove(pos);
                            notifyDataSetChanged();
                        } catch (Exception ex) {
                            Log.e("DEBUG SYMBOL", ex.getMessage());
                        }
                    }
                });
            }
        });

        holder.deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = holder.getAdapterPosition();
                final String email = mPendingFriends.get(pos);
                app.emitDenyFriendRequest(email);
                app.getPendingFriends().remove(pos);
                notifyDataSetChanged();
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        String email = mPendingFriends.get(position);
        holder.setUserEmail(email);
    }

    @Override
    public int getItemCount() {
        return mPendingFriends == null ? 0 : mPendingFriends.size();
    }

    public void setPendingFriends(List<String> pendingFriends) {
        this.mPendingFriends = pendingFriends;
    }
}
