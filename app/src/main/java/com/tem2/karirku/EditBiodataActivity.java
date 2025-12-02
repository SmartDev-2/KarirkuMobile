package com.tem2.karirku;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;

import java.util.Calendar;

public class EditBiodataActivity extends AppCompatActivity {

    ImageView btnBack;
    EditText inputNama, inputDomisili;
    TextView inputTanggalLahir;
    Spinner spinnerGender;
    Button btnCancel, btnSave;

    String[] listGender = {"Laki-laki", "Perempuan"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_biodata);

        // bind UI
        btnBack = findViewById(R.id.btnBack);
        inputNama = findViewById(R.id.inputNama);
        inputDomisili = findViewById(R.id.inputDomisili);
        inputTanggalLahir = findViewById(R.id.inputTanggalLahir);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);

        // Back
        btnBack.setOnClickListener(v -> finish());

        // Gender Spinner
        spinnerGender.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, listGender));

        // Tanggal Lahir Picker
        inputTanggalLahir.setOnClickListener(v -> openDatePicker());

        // Load saved data
        loadData();

        // Cancel
        btnCancel.setOnClickListener(v -> finish());

        // Save
        btnSave.setOnClickListener(v -> {
            saveData();
            Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    // ============================
    // DATE PICKER
    // ============================
    private void openDatePicker() {
        Calendar cal = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    String date = day + "/" + (month + 1) + "/" + year;
                    inputTanggalLahir.setText(date);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    // ============================
    // LOAD DATA
    // ============================
    private void loadData() {
        SharedPreferences prefs = getSharedPreferences("profil", MODE_PRIVATE);

        inputNama.setText(prefs.getString("bio_nama", ""));
        inputDomisili.setText(prefs.getString("bio_domisili", ""));
        inputTanggalLahir.setText(prefs.getString("bio_tanggal", ""));

        int genderIndex = prefs.getInt("bio_gender_index", 0);
        spinnerGender.setSelection(genderIndex);
    }

    // ============================
    // SAVE DATA
    // ============================
    private void saveData() {
        SharedPreferences prefs = getSharedPreferences("profil", MODE_PRIVATE);
        SharedPreferences.Editor e = prefs.edit();

        String nama = inputNama.getText().toString().trim();
        String tanggal = inputTanggalLahir.getText().toString().trim();
        String domisili = inputDomisili.getText().toString().trim();
        String gender = listGender[spinnerGender.getSelectedItemPosition()];

        e.putString("bio_nama", nama);
        e.putString("bio_tanggal", tanggal);
        e.putString("bio_domisili", domisili);
        e.putString("bio_gender", gender);
        e.putInt("bio_gender_index", spinnerGender.getSelectedItemPosition());

        // Summary untuk profilFragment
        String summary = nama.isEmpty() ? "Belum diisi" : nama;
        e.putString("biodata_summary", summary);

        e.apply();
    }
}
