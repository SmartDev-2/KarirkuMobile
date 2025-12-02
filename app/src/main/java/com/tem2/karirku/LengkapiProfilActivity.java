package com.tem2.karirku;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LengkapiProfilActivity extends AppCompatActivity {

    TextView summaryBiodata, summaryPendidikan, summarySkill, summaryTentangSaya,
            summaryPenghargaan, summarySertifikat, summaryPortofolio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lengkapi_profil);

        summaryBiodata = findViewById(R.id.summaryBiodata);
        summaryPendidikan = findViewById(R.id.summaryPendidikan);
        summarySkill = findViewById(R.id.summarySkill);
        summaryTentangSaya = findViewById(R.id.summaryTentangSaya);
        summaryPenghargaan = findViewById(R.id.summaryPenghargaan);
        summarySertifikat = findViewById(R.id.summarySertifikat);
        summaryPortofolio = findViewById(R.id.summaryPortofolio);

        // Navigation to each editor
        findViewById(R.id.cardBiodata).setOnClickListener(v ->
                startActivity(new Intent(this, EditBiodataActivity.class)));

        findViewById(R.id.cardPendidikan).setOnClickListener(v ->
                startActivity(new Intent(this, EditPendidikanActivity.class)));

        findViewById(R.id.cardSkill).setOnClickListener(v ->
                startActivity(new Intent(this, EditSkillActivity.class)));

        findViewById(R.id.cardTentangSaya).setOnClickListener(v ->
                startActivity(new Intent(this, EditTentangSayaActivity.class)));

        findViewById(R.id.cardPenghargaan).setOnClickListener(v ->
                startActivity(new Intent(this, EditPenghargaanActivity.class)));

        findViewById(R.id.cardSertifikat).setOnClickListener(v ->
                startActivity(new Intent(this, EditSertifikatActivity.class)));

        findViewById(R.id.cardPortofolio).setOnClickListener(v ->
                startActivity(new Intent(this, EditPortofolioActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSummary();
    }

    private void loadSummary() {
        SharedPreferences prefs = getSharedPreferences("profil", MODE_PRIVATE);

        summaryBiodata.setText(prefs.getString("biodata_summary", "Belum diisi"));
        summaryPendidikan.setText(prefs.getString("pendidikan_summary", "Belum diisi"));
        summarySkill.setText(prefs.getString("skill_summary", "Belum diisi"));
        summaryTentangSaya.setText(prefs.getString("tentang_summary", "Belum diisi"));
        summaryPenghargaan.setText(prefs.getString("penghargaan_summary", "Belum diisi"));
        summarySertifikat.setText(prefs.getString("sertifikat_summary", "Belum diisi"));
        summaryPortofolio.setText(prefs.getString("portofolio_summary", "Belum diisi"));
    }
}
