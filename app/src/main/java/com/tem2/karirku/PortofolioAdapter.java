package com.tem2.karirku;

import android.content.Context;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;

public class PortofolioAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> list;
    DeleteListener listener;

    interface DeleteListener {
        void onDelete(int position);
    }

    public PortofolioAdapter(Context ctx, ArrayList<String> list, DeleteListener listener) {
        this.context = ctx;
        this.list = list;
        this.listener = listener;
    }

    @Override
    public int getCount() { return list.size(); }

    @Override
    public Object getItem(int position) { return list.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_link_portofolio, parent, false);
        }

        TextView txtLink = convertView.findViewById(R.id.txtLink);
        ImageView btnDelete = convertView.findViewById(R.id.btnDelete);

        txtLink.setText(list.get(position));

        btnDelete.setOnClickListener(v -> listener.onDelete(position));

        return convertView;
    }
}
