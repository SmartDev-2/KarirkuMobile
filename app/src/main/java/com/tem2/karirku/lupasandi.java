package com.tem2.karirku;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class lupasandi extends AppCompatActivity {

    private EditText edtPasswordBaru, edtPasswordKonfirmasi;
    private ImageView imgTogglePasswordBaru, imgTogglePasswordKonfirmasi, btnBack;
    private MaterialButton btnUbahPassword;

    private boolean isPasswordBaruVisible = false;
    private boolean isPasswordKonfirmasiVisible = false;

    private String email;

    private final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private final String SUPABASE_API_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lupasandi);

        // Get email from intent
        email = getIntent().getStringExtra("email");
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Email tidak valid!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        edtPasswordBaru = findViewById(R.id.pswrdbaru);
        edtPasswordKonfirmasi = findViewById(R.id.edt_Password);
        imgTogglePasswordBaru = findViewById(R.id.showpaswrd);
        imgTogglePasswordKonfirmasi = findViewById(R.id.imgTogglePassword);
        btnUbahPassword = findViewById(R.id.btnUbahpassword);
        btnBack = findViewById(R.id.btnBack);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Toggle password visibility - Password Baru
        imgTogglePasswordBaru.setOnClickListener(v -> {
            if (isPasswordBaruVisible) {
                edtPasswordBaru.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                imgTogglePasswordBaru.setImageResource(R.drawable.ic_eyeoff);
            } else {
                edtPasswordBaru.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                imgTogglePasswordBaru.setImageResource(R.drawable.ic_eyeon);
            }
            edtPasswordBaru.setSelection(edtPasswordBaru.getText().length());
            isPasswordBaruVisible = !isPasswordBaruVisible;
        });

        // Toggle password visibility - Password Konfirmasi
        imgTogglePasswordKonfirmasi.setOnClickListener(v -> {
            if (isPasswordKonfirmasiVisible) {
                edtPasswordKonfirmasi.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                imgTogglePasswordKonfirmasi.setImageResource(R.drawable.ic_eyeoff);
            } else {
                edtPasswordKonfirmasi.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                imgTogglePasswordKonfirmasi.setImageResource(R.drawable.ic_eyeon);
            }
            edtPasswordKonfirmasi.setSelection(edtPasswordKonfirmasi.getText().length());
            isPasswordKonfirmasiVisible = !isPasswordKonfirmasiVisible;
        });

        // Ubah password button
        btnUbahPassword.setOnClickListener(v -> ubahPassword());
    }

    private void ubahPassword() {
        String passwordBaru = edtPasswordBaru.getText().toString().trim();
        String passwordKonfirmasi = edtPasswordKonfirmasi.getText().toString().trim();

        // Validasi input
        if (passwordBaru.isEmpty() || passwordKonfirmasi.isEmpty()) {
            Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validasi panjang password
        if (passwordBaru.length() < 6) {
            Toast.makeText(this, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validasi password sama
        if (!passwordBaru.equals(passwordKonfirmasi)) {
            Toast.makeText(this, "Password tidak sama!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update password ke database
        updatePasswordDatabase(passwordBaru);
    }

    private void updatePasswordDatabase(String passwordBaru) {
        // Encode email untuk URL
        String encodedEmail = email.replace("@", "%40").replace("+", "%2B");
        String url = SUPABASE_URL + "/rest/v1/pengguna?email=eq." + encodedEmail;

        Log.d("UPDATE_PASSWORD", "URL: " + url);
        Log.d("UPDATE_PASSWORD", "Email: " + email);
        Log.d("UPDATE_PASSWORD", "New Password: " + passwordBaru);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("password", passwordBaru);
        } catch (JSONException e) {
            Toast.makeText(this, "Terjadi kesalahan membuat data!", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest updateRequest = new JsonObjectRequest(Request.Method.PATCH, url, jsonBody,
                response -> {
                    Log.d("UPDATE_PASSWORD", "Success Response: " + response.toString());
                    Toast.makeText(this, "Password berhasil diubah!", Toast.LENGTH_LONG).show();

                    // Kembali ke halaman login
                    Intent intent = new Intent(lupasandi.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    if (error.networkResponse != null) {
                        Log.e("UPDATE_PASSWORD_ERROR",
                                "Code: " + error.networkResponse.statusCode +
                                        " | Body: " + new String(error.networkResponse.data));
                    } else {
                        Log.e("UPDATE_PASSWORD_ERROR", "Volley Error: " + error.toString());
                    }
                    Toast.makeText(this, "Gagal mengubah password!", Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                if (response != null && (response.statusCode == 200 || response.statusCode == 204)) {
                    return Response.success(
                            new JSONObject(),
                            HttpHeaderParser.parseCacheHeaders(response)
                    );
                }
                return super.parseNetworkResponse(response);
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                headers.put("Content-Type", "application/json");
                // Penting: return=representation untuk mendapat response data yang diupdate
                headers.put("Prefer", "return=representation");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(updateRequest);
    }
}