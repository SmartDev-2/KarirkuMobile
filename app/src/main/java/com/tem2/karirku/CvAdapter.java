package com.tem2.karirku;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CvAdapter extends RecyclerView.Adapter<CvAdapter.ViewHolder> {

    Context context;
    ArrayList<CvModel> list;

    public CvAdapter(Context context, ArrayList<CvModel> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cv, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CvModel model = list.get(position);

        holder.tvNamaCV.setText(model.fileName);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PdfViewerActivity.class);
            intent.putExtra("path", model.filePath);
            intent.putExtra("fileName", model.fileName);
            context.startActivity(intent);
        });

        // Optional: Add delete button
        if (holder.btnDelete != null) {
            holder.btnDelete.setOnClickListener(v -> {
                // TODO: Implement delete CV
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaCV;
        ImageView btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNamaCV = itemView.findViewById(R.id.tvNamaCV);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}