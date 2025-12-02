package com.tem2.karirku;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder> {

    private List<String> searchHistoryList;
    private OnHistoryClickListener listener;

    public interface OnHistoryClickListener {
        void onHistoryClick(String keyword);
        void onHistoryDelete(String keyword, int position);
    }

    public SearchHistoryAdapter(List<String> searchHistoryList, OnHistoryClickListener listener) {
        this.searchHistoryList = searchHistoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String keyword = searchHistoryList.get(position);
        holder.tvSearchKeyword.setText(keyword);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHistoryClick(keyword);
            }
        });

        holder.imgDeleteHistory.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHistoryDelete(keyword, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchHistoryList.size();
    }

    public void updateData(List<String> newList) {
        this.searchHistoryList = newList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        searchHistoryList.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSearchKeyword;
        ImageView imgDeleteHistory;

        ViewHolder(View itemView) {
            super(itemView);
            tvSearchKeyword = itemView.findViewById(R.id.tvSearchKeyword);
            imgDeleteHistory = itemView.findViewById(R.id.imgDeleteHistory);
        }
    }
}