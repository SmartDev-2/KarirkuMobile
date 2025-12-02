package com.tem2.karirku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class EditSertifikatActivity extends AppCompatActivity {

    ImageView btnBack;
    Button btnTambah;
    ListView listSertifikat;
    ArrayList<SertifikatModel> list = new ArrayList<>();

    SertifikatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sertifikat);

        btnBack = findViewById(R.id.btnBack);
        btnTambah = findViewById(R.id.btnTambah);
        listSertifikat = findViewById(R.id.listSertifikat);

        btnBack.setOnClickListener(v -> finish());
        btnTambah.setOnClickListener(v -> showAddDialog());

        adapter = new SertifikatAdapter(this, list, position -> {
            list.remove(position);
            adapter.notifyDataSetChanged();
            saveStorage();
        });

        listSertifikat.setAdapter(adapter);

        loadStorage();
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_tambah_sertifikat, null);

        EditText inputNama = view.findViewById(R.id.inputNama);
        EditText inputPenerbit = view.findViewById(R.id.inputPenerbit);
        EditText inputTahun = view.findViewById(R.id.inputTahun);
        Button save = view.findViewById(R.id.btnSave);

        builder.setView(view);
        AlertDialog alert = builder.create();
        alert.show();

        save.setOnClickListener(v -> {
            String nama = inputNama.getText().toString().trim();
            String penerbit = inputPenerbit.getText().toString().trim();
            String tahun = inputTahun.getText().toString().trim();

            if (nama.isEmpty() || penerbit.isEmpty() || tahun.isEmpty()) {
                Toast.makeText(this, "Semua data harus diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            list.add(new SertifikatModel(nama, penerbit, tahun));
            adapter.notifyDataSetChanged();
            saveStorage();
            alert.dismiss();
        });
    }

    private void saveStorage() {
        try {
            JSONArray arr = new JSONArray();

            for (SertifikatModel s : list) {
                JSONObject o = new JSONObject();
                o.put("nama", s.nama);
                o.put("penerbit", s.penerbit);
                o.put("tahun", s.tahun);
                arr.put(o);
            }

            SharedPreferences prefs = getSharedPreferences("profil", MODE_PRIVATE);
            prefs.edit().putString("sertifikat_list", arr.toString()).apply();

            String summary = list.size() == 0
                    ? "Belum ada sertifikat"
                    : list.size() + " sertifikat";

            prefs.edit().putString("sertifikat_summary", summary).apply();

        } catch (Exception ignored) {}
    }

    private void loadStorage() {
        try {
            SharedPreferences prefs = getSharedPreferences("profil", MODE_PRIVATE);
            String json = prefs.getString("sertifikat_list", "[]");

            JSONArray arr = new JSONArray(json);
            list.clear();

            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new SertifikatModel(
                        o.getString("nama"),
                        o.getString("penerbit"),
                        o.getString("tahun")
                ));
            }

            adapter.notifyDataSetChanged();

        } catch (Exception ignored) {}
    }
}
