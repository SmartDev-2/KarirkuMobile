package com.tem2.karirku;

import android.content.Context;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;

public class PenghargaanAdapter extends BaseAdapter {

    Context context;
    ArrayList<PenghargaanModel> list;
    DeleteListener listener;

    interface DeleteListener {
        void onDelete(int position);
    }

    public PenghargaanAdapter(Context ctx, ArrayList<PenghargaanModel> list, DeleteListener listener) {
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
                    .inflate(R.layout.item_penghargaan, parent, false);
        }

        TextView txtNama = convertView.findViewById(R.id.txtNama);
        TextView txtTahun = convertView.findViewById(R.id.txtTahun);
        ImageView btnDelete = convertView.findViewById(R.id.btnDelete);

        PenghargaanModel data = list.get(position);

        txtNama.setText(data.nama);
        txtTahun.setText(data.tahun);

        btnDelete.setOnClickListener(v -> listener.onDelete(position));

        return convertView;
    }
}
