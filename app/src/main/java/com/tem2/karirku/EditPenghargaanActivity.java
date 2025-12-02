package com.tem2.karirku;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class EditPenghargaanActivity extends AppCompatActivity {

    ImageView btnBack;
    Button btnTambah;
    ListView listPenghargaan;

    ArrayList<PenghargaanModel> list = new ArrayList<>();
    PenghargaanAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_penghargaan);

        btnBack = findViewById(R.id.btnBack);
        btnTambah = findViewById(R.id.btnTambah);
        listPenghargaan = findViewById(R.id.listPenghargaan);

        btnBack.setOnClickListener(v -> finish());
        btnTambah.setOnClickListener(v -> showAddDialog());

        adapter = new PenghargaanAdapter(this, list, position -> {
            list.remove(position);
            adapter.notifyDataSetChanged();
            saveStorage();
        });

        listPenghargaan.setAdapter(adapter);

        loadStorage();
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_tambah_penghargaan, null);

        EditText inputNama = view.findViewById(R.id.inputNama);
        EditText inputTahun = view.findViewById(R.id.inputTahun);
        Button save = view.findViewById(R.id.btnSave);

        builder.setView(view);
        AlertDialog alert = builder.create();
        alert.show();

        save.setOnClickListener(v -> {
            String nama = inputNama.getText().toString().trim();
            String tahun = inputTahun.getText().toString().trim();

            if (nama.isEmpty() || tahun.isEmpty()) {
                Toast.makeText(this, "Semua data harus diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            list.add(new PenghargaanModel(nama, tahun));
            adapter.notifyDataSetChanged();
            saveStorage();
            alert.dismiss();
        });
    }

    private void saveStorage() {
        try {
            JSONArray arr = new JSONArray();

            for (PenghargaanModel p : list) {
                JSONObject o = new JSONObject();
                o.put("nama", p.nama);
                o.put("tahun", p.tahun);
                arr.put(o);
            }

            SharedPreferences prefs = getSharedPreferences("profil", MODE_PRIVATE);
            prefs.edit().putString("penghargaan_list", arr.toString()).apply();

            String summary = list.size() == 0
                    ? "Belum ada penghargaan"
                    : list.size() + " penghargaan";

            prefs.edit().putString("penghargaan_summary", summary).apply();

        } catch (Exception ignored) {}
    }

    private void loadStorage() {
        try {
            SharedPreferences prefs = getSharedPreferences("profil", MODE_PRIVATE);
            String json = prefs.getString("penghargaan_list", "[]");

            JSONArray arr = new JSONArray(json);
            list.clear();

            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new PenghargaanModel(
                        o.getString("nama"),
                        o.getString("tahun")
                ));
            }

            adapter.notifyDataSetChanged();

        } catch (Exception ignored) {}
    }
}
