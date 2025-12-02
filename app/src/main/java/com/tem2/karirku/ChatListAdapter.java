package com.tem2.karirku;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
    private List<ChatListItem> chatList;
    private Context context;

    public ChatListAdapter(Context context, List<ChatListItem> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatListItem item = chatList.get(position);
        holder.tvName.setText(item.getName());
        holder.tvLastMessage.setText(item.getLastMessage());
        holder.tvTime.setText(item.getTime());
        holder.imgProfile.setImageResource(item.getProfileRes());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatDetailActivity.class);
            intent.putExtra("name", item.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return chatList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMessage, tvTime;
        ImageView imgProfile;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            imgProfile = itemView.findViewById(R.id.imgProfile);
        }
    }
}

