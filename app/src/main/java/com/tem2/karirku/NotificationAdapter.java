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

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<NotificationItem> notificationList;
    private Context context;

    public NotificationAdapter(Context context, List<NotificationItem> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem item = notificationList.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvMessage.setText(item.getMessage());
        holder.tvTime.setText(item.getTime());
        holder.imgIcon.setImageResource(item.getIconRes());

        // Tampilkan indicator untuk notifikasi belum dibaca
        if (item.isRead()) {
            holder.indicatorUnread.setVisibility(View.GONE);
        } else {
            holder.indicatorUnread.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            // Tandai sebagai sudah dibaca
            item.setRead(true);
            notifyItemChanged(position);

            // Buka detail notifikasi
            Intent intent = new Intent(context, NotificationDetailActivity.class);
            intent.putExtra("notification_id", item.getId());
            intent.putExtra("notification_title", item.getTitle());
            intent.putExtra("notification_message", item.getMessage());
            intent.putExtra("notification_time", item.getTime());
            intent.putExtra("notification_type", item.getType());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public void setData(List<NotificationItem> newList) {
        notificationList = newList;
        notifyDataSetChanged();
    }

    public void markAllAsRead() {
        for (NotificationItem item : notificationList) {
            item.setRead(true);
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        ImageView imgIcon;
        View indicatorUnread;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
            imgIcon = itemView.findViewById(R.id.imgNotificationIcon);
            indicatorUnread = itemView.findViewById(R.id.indicatorUnread);
        }
    }
}