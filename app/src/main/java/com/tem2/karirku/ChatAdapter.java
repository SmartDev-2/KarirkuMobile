package com.tem2.karirku;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MY_MESSAGE = 1;
    private static final int TYPE_OTHER_MESSAGE = 2;

    private List<Message> messageList;

    public ChatAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        return message.isSentByMe() ? TYPE_MY_MESSAGE : TYPE_OTHER_MESSAGE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_MY_MESSAGE) {
            View view = inflater.inflate(R.layout.item_my_message, parent, false);
            return new MyMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_other_message, parent, false);
            return new OtherMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        if (holder instanceof MyMessageViewHolder) {
            ((MyMessageViewHolder) holder).bind(message);
        } else if (holder instanceof OtherMessageViewHolder) {
            ((OtherMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void addMessage(Message message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    // ViewHolder untuk pesan yang dikirim oleh user
    static class MyMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessage, tvTime;

        public MyMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMyMessage);
            tvTime = itemView.findViewById(R.id.tvMyTime);
        }

        public void bind(Message message) {
            tvMessage.setText(message.getContent());
            tvTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.getTimestamp().getTime()));
        }
    }

    // ViewHolder untuk pesan yang diterima dari orang lain
    static class OtherMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSenderName, tvMessage, tvTime;

        public OtherMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessage = itemView.findViewById(R.id.tvOtherMessage);
            tvTime = itemView.findViewById(R.id.tvOtherTime);
        }

        public void bind(Message message) {
            tvSenderName.setText(message.getSenderName());
            tvMessage.setText(message.getContent());
            tvTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.getTimestamp().getTime()));
        }
    }
}