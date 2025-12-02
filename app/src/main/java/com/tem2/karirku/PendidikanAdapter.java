package com.tem2.karirku;

import android.content.Context;
import android.view.*;
import android.widget.*;
import java.util.ArrayList;

public class PendidikanAdapter extends BaseAdapter {

    Context ctx;
    ArrayList<PendidikanModel> data;
    OnDeleteListener listener;

    public interface OnDeleteListener {
        void onDelete(int position);
    }

    public PendidikanAdapter(Context ctx, ArrayList<PendidikanModel> data, OnDeleteListener listener) {
        this.ctx = ctx;
        this.data = data;
        this.listener = listener;
    }

    @Override
    public int getCount() { return data.size(); }

    @Override
    public Object getItem(int position) { return data.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(ctx).inflate(R.layout.item_pendidikan, parent, false);
        }

        TextView jenjang = convertView.findViewById(R.id.txtJenjang);
        TextView sekolah = convertView.findViewById(R.id.txtSekolah);
        TextView tahun = convertView.findViewById(R.id.txtTahun);
        Button btnHapus = convertView.findViewById(R.id.btnHapus);

        PendidikanModel p = data.get(pos);

        jenjang.setText(p.jenjang);
        sekolah.setText(p.sekolah);
        tahun.setText(p.tahun);

        btnHapus.setOnClickListener(v -> listener.onDelete(pos));

        return convertView;
    }
}
