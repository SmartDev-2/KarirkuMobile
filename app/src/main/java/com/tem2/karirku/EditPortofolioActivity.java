package com.tem2.karirku;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import org.json.JSONArray;

import java.util.ArrayList;

public class EditPortofolioActivity extends AppCompatActivity {

    ImageView btnBack;
    Button btnTambah;
    ListView listLink;

    ArrayList<String> list = new ArrayList<>();
    PortofolioAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_portofolio);

        btnBack = findViewById(R.id.btnBack);
        btnTambah = findViewById(R.id.btnTambah);
        listLink = findViewById(R.id.listLink);

        btnBack.setOnClickListener(v -> finish());
        btnTambah.setOnClickListener(v -> showAddDialog());

        adapter = new PortofolioAdapter(this, list, position -> {
            list.remove(position);
            adapter.notifyDataSetChanged();
            saveStorage();
        });

        listLink.setAdapter(adapter);

        loadStorage();
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_tambah_portofolio, null);

        EditText inputLink = view.findViewById(R.id.inputLink);
        Button save = view.findViewById(R.id.btnSave);

        builder.setView(view);
        AlertDialog alert = builder.create();
        alert.show();

        save.setOnClickListener(v -> {
            String link = inputLink.getText().toString().trim();

            if (link.isEmpty()) {
                Toast.makeText(this, "Link tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!link.startsWith("http")) {
                Toast.makeText(this, "Link wajib diawali http atau https", Toast.LENGTH_SHORT).show();
                return;
            }

            list.add(link);
            adapter.notifyDataSetChanged();

            saveStorage();
            alert.dismiss();
        });
    }

    private void saveStorage() {
        try {
            JSONArray arr = new JSONArray();
            for (String s : list) arr.put(s);

            SharedPreferences prefs = getSharedPreferences("profil", MODE_PRIVATE);
            prefs.edit().putString("portofolio_list", arr.toString()).apply();

            String summary = (list.size() == 0)
                    ? "Belum ada link"
                    : list.size() + " link portofolio";

            prefs.edit().putString("portofolio_summary", summary).apply();

        } catch (Exception ignored) {}
    }

    private void loadStorage() {
        try {
            SharedPreferences prefs = getSharedPreferences("profil", MODE_PRIVATE);
            String json = prefs.getString("portofolio_list", "[]");

            JSONArray arr = new JSONArray(json);

            list.clear();
            for (int i = 0; i < arr.length(); i++) {
                list.add(arr.getString(i));
            }

            adapter.notifyDataSetChanged();

        } catch (Exception ignored) {}
    }
}
