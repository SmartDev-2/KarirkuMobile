package com.tem2.karirku;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class ProfilKarirkuActivity extends AppCompatActivity {

    ImageView btnBack;
    TextView tvNamaValue, tvTanggalValue, tvGenderValue, tvDomisiliValue,
            tvPendidikanValue, tvSkillValue, tvTentangValue,
            tvPenghargaanValue, tvSertifikatValue, tvPortofolioValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil_karirku);

        bindViews();
        loadData();

        btnBack.setOnClickListener(v -> finish());
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);

        tvNamaValue = findViewById(R.id.tvNamaValue);
        tvTanggalValue = findViewById(R.id.tvTanggalValue);
        tvGenderValue = findViewById(R.id.tvGenderValue);
        tvDomisiliValue = findViewById(R.id.tvDomisiliValue);

        tvPendidikanValue = findViewById(R.id.tvPendidikanValue);
        tvSkillValue = findViewById(R.id.tvSkillValue);
        tvTentangValue = findViewById(R.id.tvTentangValue);
        tvPenghargaanValue = findViewById(R.id.tvPenghargaanValue);
        tvSertifikatValue = findViewById(R.id.tvSertifikatValue);
        tvPortofolioValue = findViewById(R.id.tvPortofolioValue);
    }

    private void loadData() {
        SharedPreferences prefs = getSharedPreferences("profil", MODE_PRIVATE);

        tvNamaValue.setText(prefs.getString("bio_nama", "-"));
        tvTanggalValue.setText(prefs.getString("bio_tanggal", "-"));
        tvGenderValue.setText(prefs.getString("bio_gender", "-"));
        tvDomisiliValue.setText(prefs.getString("bio_domisili", "-"));

        tvPendidikanValue.setText(prefs.getString("pendidikan_summary", "Belum diisi"));
        tvSkillValue.setText(prefs.getString("skill_summary", "Belum diisi"));
        tvTentangValue.setText(prefs.getString("tentang_full", "Belum diisi"));
        tvPenghargaanValue.setText(prefs.getString("penghargaan_summary", "Belum diisi"));
        tvSertifikatValue.setText(prefs.getString("sertifikat_summary", "Belum diisi"));
        tvPortofolioValue.setText(prefs.getString("portofolio_summary", "Belum diisi"));
    }
}
