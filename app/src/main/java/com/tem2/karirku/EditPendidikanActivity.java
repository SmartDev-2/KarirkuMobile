package com.tem2.karirku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class EditPendidikanActivity extends AppCompatActivity {

    ImageView btnBack;
    Button btnTambah;
    ListView listPendidikan;

    ArrayList<PendidikanModel> list = new ArrayList<>();
    PendidikanAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pendidikan);

        btnBack = findViewById(R.id.btnBack);
        btnTambah = findViewById(R.id.btnTambah);
        listPendidikan = findViewById(R.id.listPendidikan);

        adapter = new PendidikanAdapter(this, list, position -> {
            list.remove(position);
            adapter.notifyDataSetChanged();
            saveToStorage();
        });

        listPendidikan.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnTambah.setOnClickListener(v -> showAddDialog());

        loadFromStorage();
    }

    private void showAddDialog() {

        // Pakai androidx AlertDialog (bukan android.app.AlertDialog)
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate dialog
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_tambah_pendidikan, null);

        EditText inputJenjang = view.findViewById(R.id.inputJenjang);
        EditText inputSekolah = view.findViewById(R.id.inputSekolah);
        EditText inputTahun = view.findViewById(R.id.inputTahun);
        Button save = view.findViewById(R.id.btnSave);

        builder.setView(view);
        AlertDialog alert = builder.create();
        alert.show();

        save.setOnClickListener(v -> {

            String jenjang = inputJenjang.getText().toString().trim();
            String sekolah = inputSekolah.getText().toString().trim();
            String tahun = inputTahun.getText().toString().trim();

            if (jenjang.isEmpty() || sekolah.isEmpty() || tahun.isEmpty()) {
                Toast.makeText(this, "Semua data harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            list.add(new PendidikanModel(jenjang, sekolah, tahun));
            adapter.notifyDataSetChanged();

            saveToStorage();
            alert.dismiss();
        });
    }

    private void saveToStorage() {
        try {
            JSONArray arr = new JSONArray();
            for (PendidikanModel p : list) {
                JSONObject o = new JSONObject();
                o.put("jenjang", p.jenjang);
                o.put("sekolah", p.sekolah);
                o.put("tahun", p.tahun);
                arr.put(o);
            }

            SharedPreferences prefs = getSharedPreferences("profil", MODE_PRIVATE);
            prefs.edit().putString("pendidikan_list", arr.toString()).apply();

            // summary untuk card utama
            String summary = list.size() == 0
                    ? "Belum diisi"
                    : list.size() + " pendidikan";

            prefs.edit().putString("pendidikan_summary", summary).apply();

        } catch (Exception ignored) {}
    }

    private void loadFromStorage() {
        try {
            SharedPreferences prefs = getSharedPreferences("profil", MODE_PRIVATE);
            String json = prefs.getString("pendidikan_list", "[]");

            JSONArray arr = new JSONArray(json);
            list.clear();

            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);

                list.add(new PendidikanModel(
                        o.getString("jenjang"),
                        o.getString("sekolah"),
                        o.getString("tahun")
                ));
            }

            adapter.notifyDataSetChanged();

        } catch (Exception ignored) {}
    }
}
