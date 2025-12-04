package com.tem2.karirku;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class LengkapiProfilSemuaActivity extends AppCompatActivity {

    // Pendidikan Views
    private LinearLayout editPendidikanForm;
    private Button btnTambahPendidikan, btnCancelPendidikan, btnSavePendidikan;
    private EditText inputInstitusi, inputJurusan, inputTahun;
    private TextView summaryPendidikan;
    private ListView listPendidikan;
    private List<String> pendidikanList = new ArrayList<>();
    private ArrayAdapter<String> pendidikanAdapter;

    // Skill Views
    private LinearLayout editSkillForm;
    private Button btnEditSkill, btnCancelSkill, btnSaveSkill, btnTambahSkill;
    private EditText inputSkill;
    private TextView summarySkill;
    private ChipGroup skillChipGroup;
    private List<String> skillList = new ArrayList<>();

    // Tentang Saya Views
    private LinearLayout editTentangSayaForm;
    private Button btnEditTentangSaya, btnCancelTentangSaya, btnSaveTentangSaya;
    private EditText inputTentang;
    private TextView summaryTentangSaya, txtCounter;

    // Penghargaan Views
    private LinearLayout editPenghargaanForm;
    private Button btnTambahPenghargaan, btnCancelPenghargaan, btnSavePenghargaan;
    private EditText inputNamaPenghargaan, inputTahunPenghargaan, inputPenyelenggara;
    private TextView summaryPenghargaan;
    private ListView listPenghargaan;
    private List<String> penghargaanList = new ArrayList<>();
    private ArrayAdapter<String> penghargaanAdapter;

    // Sertifikat Views
    private LinearLayout editSertifikatForm;
    private Button btnTambahSertifikat, btnCancelSertifikat, btnSaveSertifikat;
    private EditText inputNamaSertifikat, inputPenerbitSertifikat, inputTahunSertifikat;
    private TextView summarySertifikat;
    private ListView listSertifikat;
    private List<String> sertifikatList = new ArrayList<>();
    private ArrayAdapter<String> sertifikatAdapter;

    // Portofolio Views
    private LinearLayout editPortofolioForm;
    private Button btnTambahPortofolio, btnCancelPortofolio, btnSavePortofolio;
    private EditText inputNamaPortofolio, inputUrlPortofolio;
    private TextView summaryPortofolio;
    private ListView listLink;
    private List<String> portofolioList = new ArrayList<>();
    private ArrayAdapter<String> portofolioAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lengkapi_profil_semua);

        initializeViews();
        setupAdapters();
        setupListeners();
        setupTextWatchers();

        Log.d("TEST", "Activity created successfully");
    }

    private void initializeViews() {
        // Pendidikan
        editPendidikanForm = findViewById(R.id.editPendidikanForm);
        btnTambahPendidikan = findViewById(R.id.btnTambahPendidikan);
        btnCancelPendidikan = findViewById(R.id.btnCancelPendidikan);
        btnSavePendidikan = findViewById(R.id.btnSavePendidikan);
        summaryPendidikan = findViewById(R.id.summaryPendidikan);
        listPendidikan = findViewById(R.id.listPendidikan);

        // Input fields Pendidikan
        inputInstitusi = findViewById(R.id.inputInstitusi);
        inputJurusan = findViewById(R.id.inputJurusan);
        inputTahun = findViewById(R.id.inputTahun);

        // Skill
        editSkillForm = findViewById(R.id.editSkillForm);
        btnEditSkill = findViewById(R.id.btnEditSkill);
        btnCancelSkill = findViewById(R.id.btnCancelSkill);
        btnSaveSkill = findViewById(R.id.btnSaveSkill);
        btnTambahSkill = findViewById(R.id.btnTambahSkill);
        inputSkill = findViewById(R.id.inputSkill);
        summarySkill = findViewById(R.id.summarySkill);
        skillChipGroup = findViewById(R.id.skillChipGroup);

        // Tentang Saya
        editTentangSayaForm = findViewById(R.id.editTentangSayaForm);
        btnEditTentangSaya = findViewById(R.id.btnEditTentangSaya);
        btnCancelTentangSaya = findViewById(R.id.btnCancelTentangSaya);
        btnSaveTentangSaya = findViewById(R.id.btnSaveTentangSaya);
        inputTentang = findViewById(R.id.inputTentang);
        summaryTentangSaya = findViewById(R.id.summaryTentangSaya);
        txtCounter = findViewById(R.id.txtCounter);

        // Penghargaan
        editPenghargaanForm = findViewById(R.id.editPenghargaanForm);
        btnTambahPenghargaan = findViewById(R.id.btnTambahPenghargaan);
        btnCancelPenghargaan = findViewById(R.id.btnCancelPenghargaan);
        btnSavePenghargaan = findViewById(R.id.btnSavePenghargaan);
        summaryPenghargaan = findViewById(R.id.summaryPenghargaan);
        listPenghargaan = findViewById(R.id.listPenghargaan);

        // Input fields Penghargaan
        inputNamaPenghargaan = findViewById(R.id.inputNamaPenghargaan);
        inputTahunPenghargaan = findViewById(R.id.inputTahunPenghargaan);
        inputPenyelenggara = findViewById(R.id.inputPenyelenggara);

        // Sertifikat
        editSertifikatForm = findViewById(R.id.editSertifikatForm);
        btnTambahSertifikat = findViewById(R.id.btnTambahSertifikat);
        btnCancelSertifikat = findViewById(R.id.btnCancelSertifikat);
        btnSaveSertifikat = findViewById(R.id.btnSaveSertifikat);
        summarySertifikat = findViewById(R.id.summarySertifikat);
        listSertifikat = findViewById(R.id.listSertifikat);

        // Input fields Sertifikat
        inputNamaSertifikat = findViewById(R.id.inputNamaSertifikat);
        inputPenerbitSertifikat = findViewById(R.id.inputPenerbitSertifikat);
        inputTahunSertifikat = findViewById(R.id.inputTahunSertifikat);

        // Portofolio
        editPortofolioForm = findViewById(R.id.editPortofolioForm);
        btnTambahPortofolio = findViewById(R.id.btnTambahPortofolio);
        btnCancelPortofolio = findViewById(R.id.btnCancelPortofolio);
        btnSavePortofolio = findViewById(R.id.btnSavePortofolio);
        summaryPortofolio = findViewById(R.id.summaryPortofolio);
        listLink = findViewById(R.id.listLink);

        // Input fields Portofolio
        inputNamaPortofolio = findViewById(R.id.inputNamaPortofolio);
        inputUrlPortofolio = findViewById(R.id.inputUrlPortofolio);
    }

    private void setupAdapters() {
        // Setup list adapters
        pendidikanAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pendidikanList);
        listPendidikan.setAdapter(pendidikanAdapter);

        penghargaanAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, penghargaanList);
        listPenghargaan.setAdapter(penghargaanAdapter);

        sertifikatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sertifikatList);
        listSertifikat.setAdapter(sertifikatAdapter);

        portofolioAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, portofolioList);
        listLink.setAdapter(portofolioAdapter);
    }

    private void setupListeners() {
        // =========== PENDIDIKAN ===========
        btnTambahPendidikan.setOnClickListener(v -> {
            Log.d("TEST", "Tambah Pendidikan clicked");
            listPendidikan.setVisibility(View.GONE);
            editPendidikanForm.setVisibility(editPendidikanForm.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);

            if (editPendidikanForm.getVisibility() == View.VISIBLE) {
                btnTambahPendidikan.setText("Tutup Form");
            } else {
                btnTambahPendidikan.setText("Tambah Pendidikan");
            }
        });

        btnCancelPendidikan.setOnClickListener(v -> {
            editPendidikanForm.setVisibility(View.GONE);
            listPendidikan.setVisibility(pendidikanList.isEmpty() ? View.GONE : View.VISIBLE);
            btnTambahPendidikan.setText("Tambah Pendidikan");
            clearPendidikanForm();
        });

        btnSavePendidikan.setOnClickListener(v -> {
            if (validatePendidikan()) {
                savePendidikan();
                editPendidikanForm.setVisibility(View.GONE);
                listPendidikan.setVisibility(View.VISIBLE);
                btnTambahPendidikan.setText("Tambah Pendidikan");
                Toast.makeText(this, "Pendidikan ditambahkan", Toast.LENGTH_SHORT).show();
            }
        });

        // =========== SKILL ===========
        btnEditSkill.setOnClickListener(v -> {
            Log.d("TEST", "Edit Skill clicked");
            editSkillForm.setVisibility(View.VISIBLE);
            btnEditSkill.setVisibility(View.GONE);
        });

        btnCancelSkill.setOnClickListener(v -> {
            editSkillForm.setVisibility(View.GONE);
            btnEditSkill.setVisibility(View.VISIBLE);
            inputSkill.setText("");
        });

        btnSaveSkill.setOnClickListener(v -> {
            saveSkills();
            editSkillForm.setVisibility(View.GONE);
            btnEditSkill.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Skill disimpan", Toast.LENGTH_SHORT).show();
        });

        btnTambahSkill.setOnClickListener(v -> {
            String skill = inputSkill.getText().toString().trim();
            if (!skill.isEmpty()) {
                addSkillChip(skill);
                inputSkill.setText("");
            }
        });

        // =========== TENTANG SAYA ===========
        btnEditTentangSaya.setOnClickListener(v -> {
            Log.d("TEST", "Edit Tentang Saya clicked");
            editTentangSayaForm.setVisibility(View.VISIBLE);
            btnEditTentangSaya.setVisibility(View.GONE);
        });

        btnCancelTentangSaya.setOnClickListener(v -> {
            editTentangSayaForm.setVisibility(View.GONE);
            btnEditTentangSaya.setVisibility(View.VISIBLE);
            inputTentang.setText("");
            txtCounter.setText("0/500");
        });

        btnSaveTentangSaya.setOnClickListener(v -> {
            String tentang = inputTentang.getText().toString().trim();
            if (!tentang.isEmpty()) {
                summaryTentangSaya.setText(tentang.length() > 50 ?
                        tentang.substring(0, 50) + "..." : tentang);
                editTentangSayaForm.setVisibility(View.GONE);
                btnEditTentangSaya.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Tentang Saya disimpan", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Tulis tentang diri Anda terlebih dahulu", Toast.LENGTH_SHORT).show();
            }
        });

        // =========== PENGHARGAAN ===========
        btnTambahPenghargaan.setOnClickListener(v -> {
            Log.d("TEST", "Tambah Penghargaan clicked");
            listPenghargaan.setVisibility(View.GONE);
            editPenghargaanForm.setVisibility(editPenghargaanForm.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);

            if (editPenghargaanForm.getVisibility() == View.VISIBLE) {
                btnTambahPenghargaan.setText("Tutup Form");
            } else {
                btnTambahPenghargaan.setText("Tambah Penghargaan");
            }
        });

        btnCancelPenghargaan.setOnClickListener(v -> {
            editPenghargaanForm.setVisibility(View.GONE);
            listPenghargaan.setVisibility(penghargaanList.isEmpty() ? View.GONE : View.VISIBLE);
            btnTambahPenghargaan.setText("Tambah Penghargaan");
            clearPenghargaanForm();
        });

        btnSavePenghargaan.setOnClickListener(v -> {
            if (validatePenghargaan()) {
                savePenghargaan();
                editPenghargaanForm.setVisibility(View.GONE);
                listPenghargaan.setVisibility(View.VISIBLE);
                btnTambahPenghargaan.setText("Tambah Penghargaan");
                Toast.makeText(this, "Penghargaan ditambahkan", Toast.LENGTH_SHORT).show();
            }
        });

        // =========== SERTIFIKAT ===========
        btnTambahSertifikat.setOnClickListener(v -> {
            Log.d("TEST", "Tambah Sertifikat clicked");
            listSertifikat.setVisibility(View.GONE);
            editSertifikatForm.setVisibility(editSertifikatForm.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);

            if (editSertifikatForm.getVisibility() == View.VISIBLE) {
                btnTambahSertifikat.setText("Tutup Form");
            } else {
                btnTambahSertifikat.setText("Tambah Sertifikat");
            }
        });

        btnCancelSertifikat.setOnClickListener(v -> {
            editSertifikatForm.setVisibility(View.GONE);
            listSertifikat.setVisibility(sertifikatList.isEmpty() ? View.GONE : View.VISIBLE);
            btnTambahSertifikat.setText("Tambah Sertifikat");
            clearSertifikatForm();
        });

        btnSaveSertifikat.setOnClickListener(v -> {
            if (validateSertifikat()) {
                saveSertifikat();
                editSertifikatForm.setVisibility(View.GONE);
                listSertifikat.setVisibility(View.VISIBLE);
                btnTambahSertifikat.setText("Tambah Sertifikat");
                Toast.makeText(this, "Sertifikat ditambahkan", Toast.LENGTH_SHORT).show();
            }
        });

        // =========== PORTOFOLIO ===========
        btnTambahPortofolio.setOnClickListener(v -> {
            Log.d("TEST", "Tambah Portofolio clicked");
            listLink.setVisibility(View.GONE);
            editPortofolioForm.setVisibility(editPortofolioForm.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);

            if (editPortofolioForm.getVisibility() == View.VISIBLE) {
                btnTambahPortofolio.setText("Tutup Form");
            } else {
                btnTambahPortofolio.setText("Tambah Link");
            }
        });

        btnCancelPortofolio.setOnClickListener(v -> {
            editPortofolioForm.setVisibility(View.GONE);
            listLink.setVisibility(portofolioList.isEmpty() ? View.GONE : View.VISIBLE);
            btnTambahPortofolio.setText("Tambah Link");
            clearPortofolioForm();
        });

        btnSavePortofolio.setOnClickListener(v -> {
            if (validatePortofolio()) {
                savePortofolio();
                editPortofolioForm.setVisibility(View.GONE);
                listLink.setVisibility(View.VISIBLE);
                btnTambahPortofolio.setText("Tambah Link");
                Toast.makeText(this, "Portofolio ditambahkan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTextWatchers() {
        inputTentang.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                txtCounter.setText(length + "/500");

                // Change color when reaching limit
                if (length >= 500) {
                    txtCounter.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else if (length >= 450) {
                    txtCounter.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                } else {
                    txtCounter.setTextColor(getResources().getColor(R.color.blue));
                }
            }
        });
    }

    // =========== HELPER METHODS ===========

    // Pendidikan Methods
    private boolean validatePendidikan() {
        if (inputInstitusi.getText().toString().trim().isEmpty()) {
            inputInstitusi.setError("Nama institusi tidak boleh kosong");
            return false;
        }
        if (inputJurusan.getText().toString().trim().isEmpty()) {
            inputJurusan.setError("Jurusan tidak boleh kosong");
            return false;
        }
        if (inputTahun.getText().toString().trim().isEmpty()) {
            inputTahun.setError("Tahun tidak boleh kosong");
            return false;
        }
        return true;
    }

    private void savePendidikan() {
        String institusi = inputInstitusi.getText().toString().trim();
        String jurusan = inputJurusan.getText().toString().trim();
        String tahun = inputTahun.getText().toString().trim();

        String pendidikan = institusi + " - " + jurusan + " (" + tahun + ")";
        pendidikanList.add(pendidikan);
        pendidikanAdapter.notifyDataSetChanged();
        summaryPendidikan.setText(pendidikanList.size() + " pendidikan");
        clearPendidikanForm();
    }

    private void clearPendidikanForm() {
        inputInstitusi.setText("");
        inputJurusan.setText("");
        inputTahun.setText("");
    }

    // Skill Methods
    private void addSkillChip(String skill) {
        if (!skillList.contains(skill)) {
            skillList.add(skill);

            Chip chip = new Chip(this);
            chip.setText(skill);
            chip.setCloseIconVisible(true);
            chip.setChipBackgroundColorResource(R.color.chip_background);

            chip.setOnCloseIconClickListener(v -> {
                skillChipGroup.removeView(chip);
                skillList.remove(skill);
                updateSkillSummary();
            });

            skillChipGroup.addView(chip);
            updateSkillSummary();
        }
    }

    private void saveSkills() {
        StringBuilder skillsText = new StringBuilder();
        for (int i = 0; i < skillList.size(); i++) {
            skillsText.append(skillList.get(i));
            if (i < skillList.size() - 1) {
                skillsText.append(", ");
            }
        }

        summarySkill.setText(skillsText.toString().isEmpty() ?
                "Belum diisi" : skillsText.toString());
    }

    private void updateSkillSummary() {
        summarySkill.setText(skillList.isEmpty() ? "Belum diisi" :
                skillList.size() + " skill ditambahkan");
    }

    // Penghargaan Methods
    private boolean validatePenghargaan() {
        if (inputNamaPenghargaan.getText().toString().trim().isEmpty()) {
            inputNamaPenghargaan.setError("Nama penghargaan tidak boleh kosong");
            return false;
        }
        if (inputTahunPenghargaan.getText().toString().trim().isEmpty()) {
            inputTahunPenghargaan.setError("Tahun tidak boleh kosong");
            return false;
        }
        return true;
    }

    private void savePenghargaan() {
        String nama = inputNamaPenghargaan.getText().toString().trim();
        String tahun = inputTahunPenghargaan.getText().toString().trim();
        String penyelenggara = inputPenyelenggara.getText().toString().trim();

        String penghargaan = nama + (penyelenggara.isEmpty() ? "" : " - " + penyelenggara) + " (" + tahun + ")";
        penghargaanList.add(penghargaan);
        penghargaanAdapter.notifyDataSetChanged();
        summaryPenghargaan.setText(penghargaanList.size() + " penghargaan");
        clearPenghargaanForm();
    }

    private void clearPenghargaanForm() {
        inputNamaPenghargaan.setText("");
        inputTahunPenghargaan.setText("");
        inputPenyelenggara.setText("");
    }

    // Sertifikat Methods
    private boolean validateSertifikat() {
        if (inputNamaSertifikat.getText().toString().trim().isEmpty()) {
            inputNamaSertifikat.setError("Nama sertifikat tidak boleh kosong");
            return false;
        }
        if (inputPenerbitSertifikat.getText().toString().trim().isEmpty()) {
            inputPenerbitSertifikat.setError("Penerbit tidak boleh kosong");
            return false;
        }
        return true;
    }

    private void saveSertifikat() {
        String nama = inputNamaSertifikat.getText().toString().trim();
        String penerbit = inputPenerbitSertifikat.getText().toString().trim();
        String tahun = inputTahunSertifikat.getText().toString().trim();

        String sertifikat = nama + " - " + penerbit + (tahun.isEmpty() ? "" : " (" + tahun + ")");
        sertifikatList.add(sertifikat);
        sertifikatAdapter.notifyDataSetChanged();
        summarySertifikat.setText(sertifikatList.size() + " sertifikat");
        clearSertifikatForm();
    }

    private void clearSertifikatForm() {
        inputNamaSertifikat.setText("");
        inputPenerbitSertifikat.setText("");
        inputTahunSertifikat.setText("");
    }

    // Portofolio Methods
    private boolean validatePortofolio() {
        if (inputNamaPortofolio.getText().toString().trim().isEmpty()) {
            inputNamaPortofolio.setError("Nama portofolio tidak boleh kosong");
            return false;
        }
        if (inputUrlPortofolio.getText().toString().trim().isEmpty()) {
            inputUrlPortofolio.setError("URL tidak boleh kosong");
            return false;
        }
        return true;
    }

    private void savePortofolio() {
        String nama = inputNamaPortofolio.getText().toString().trim();
        String url = inputUrlPortofolio.getText().toString().trim();

        String portofolio = nama + " - " + url;
        portofolioList.add(portofolio);
        portofolioAdapter.notifyDataSetChanged();
        summaryPortofolio.setText(portofolioList.size() + " link portofolio");
        clearPortofolioForm();
    }

    private void clearPortofolioForm() {
        inputNamaPortofolio.setText("");
        inputUrlPortofolio.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("TEST", "Activity destroyed");
    }
}