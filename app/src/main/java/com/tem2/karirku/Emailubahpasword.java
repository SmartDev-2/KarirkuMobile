package com.tem2.karirku;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class Emailubahpasword extends AppCompatActivity {

    private EditText edtEmail;
    private MaterialButton btnCari;
    private ImageView btnBack;

    private final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private final String SUPABASE_API_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailubahpasword);

        // Initialize views
        edtEmail = findViewById(R.id.pswrdbaru);
        btnCari = findViewById(R.id.btnLengkapiProfil);
        btnBack = findViewById(R.id.btnBack);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Cari button - cek email
        btnCari.setOnClickListener(v -> cekEmailTerdaftar());
    }

    private void cekEmailTerdaftar() {
        String email = edtEmail.getText().toString().trim();

        // Validasi input
        if (email.isEmpty()) {
            Toast.makeText(this, "Masukkan email Anda!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validasi format email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Format email tidak valid!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cek ke database Supabase
        String url = SUPABASE_URL + "/rest/v1/pengguna?email=eq." + email + "&select=id_pengguna,email";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            // Email ditemukan
                            Toast.makeText(this, "Email ditemukan!", Toast.LENGTH_SHORT).show();

                            // Pindah ke halaman lupasandi dengan membawa email
                            Intent intent = new Intent(Emailubahpasword.this, lupasandi.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                            finish();
                        } else {
                            // Email tidak ditemukan
                            Toast.makeText(this, "Email tidak terdaftar!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("CEK_EMAIL_ERROR", "Parse error: " + e.getMessage());
                        Toast.makeText(this, "Terjadi kesalahan!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        Log.e("CEK_EMAIL_ERROR",
                                "Code: " + error.networkResponse.statusCode +
                                        " | Body: " + new String(error.networkResponse.data));
                    } else {
                        Log.e("CEK_EMAIL_ERROR", "Volley Error: " + error.toString());
                    }
                    Toast.makeText(this, "Gagal memeriksa email. Cek koneksi internet!", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}