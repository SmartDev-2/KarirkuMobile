package com.tem2.karirku;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.content.res.ColorStateList;
import android.graphics.Color;


import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;

import java.util.ArrayList;

public class EditSkillActivity extends AppCompatActivity {

    ImageView btnBack;
    EditText inputSkill;
    Button btnTambahSkill;
    ChipGroup skillChipGroup;

    ArrayList<String> listSkill = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_skill);

        btnBack = findViewById(R.id.btnBack);
        inputSkill = findViewById(R.id.inputSkill);
        btnTambahSkill = findViewById(R.id.btnTambahSkill);
        skillChipGroup = findViewById(R.id.skillChipGroup);

        btnBack.setOnClickListener(v -> finish());

        btnTambahSkill.setOnClickListener(v -> {
            String skill = inputSkill.getText().toString().trim();
            if (skill.isEmpty()) {
                Toast.makeText(this, "Skill tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            listSkill.add(skill);
            inputSkill.setText("");
            renderChips();
            saveStorage();
        });

        loadStorage();
        renderChips();
    }

    private void renderChips() {
        skillChipGroup.removeAllViews();

        for (int i = 0; i < listSkill.size(); i++) {
            int index = i;
            String skill = listSkill.get(i);

            Chip chip = new Chip(this);
            chip.setText(skill);
            chip.setCloseIconVisible(true);
            chip.setCheckable(false);

            // warna fix tanpa error
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#0CADF1")));
            chip.setTextColor(Color.WHITE);

            chip.setOnCloseIconClickListener(v -> {
                listSkill.remove(index);
                renderChips();
                saveStorage();
            });

            skillChipGroup.addView(chip);
        }
    }


    private void saveStorage() {
        try {
            JSONArray arr = new JSONArray();
            for (String s : listSkill) arr.put(s);

            SharedPreferences prefs = getSharedPreferences("profil", MODE_PRIVATE);
            prefs.edit().putString("skills", arr.toString()).apply();

            String summary = listSkill.size() == 0
                    ? "Belum diisi"
                    : listSkill.size() + " skill";

            prefs.edit().putString("skill_summary", summary).apply();

        } catch (Exception ignored) {}
    }

    private void loadStorage() {
        try {
            SharedPreferences prefs = getSharedPreferences("profil", MODE_PRIVATE);
            String json = prefs.getString("skills", "[]");

            JSONArray arr = new JSONArray(json);
            listSkill.clear();

            for (int i = 0; i < arr.length(); i++)
                listSkill.add(arr.getString(i));

        } catch (Exception ignored) {}
    }
}
