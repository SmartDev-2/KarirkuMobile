package com.tem2.karirku;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;

public class EditTentangSayaActivity extends AppCompatActivity {

    ImageView btnBack;
    EditText inputTentang;
    TextView txtCounter;
    Button btnSimpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tentang_saya);

        btnBack = findViewById(R.id.btnBack);
        inputTentang = findViewById(R.id.inputTentang);
        txtCounter = findViewById(R.id.txtCounter);
        btnSimpan = findViewById(R.id.btnSimpan);

        btnBack.setOnClickListener(v -> finish());

        loadData();
        setupCounter();

        btnSimpan.setOnClickListener(v -> {
            saveData();
            Toast.makeText(this, "Berhasil disimpan!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setupCounter() {
        inputTentang.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                txtCounter.setText(s.length() + "/500");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadData() {
        SharedPreferences prefs = getSharedPreferences("profil", MODE_PRIVATE);

        String tentang = prefs.getString("tentang_saya", "");
        inputTentang.setText(tentang);

        txtCounter.setText(tentang.length() + "/500");
    }

    private void saveData() {
        String tentang = inputTentang.getText().toString().trim();

        SharedPreferences prefs = getSharedPreferences("profil", MODE_PRIVATE);
        SharedPreferences.Editor e = prefs.edit();

        e.putString("tentang_saya", tentang);

        // summary untuk halaman profil
        String summary;
        if (tentang.length() == 0)
            summary = "Belum diisi";
        else if (tentang.length() > 30)
            summary = tentang.substring(0, 30) + "...";
        else
            summary = tentang;

        e.putString("tentang_summary", summary);
        e.apply();
    }
}
