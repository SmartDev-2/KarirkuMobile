package com.tem2.karirku;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UpdateAkunActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword, edtWhatsapp;
    Button btnSimpan;
    private SessionManager sessionManager;
    private RequestQueue requestQueue;

    private static final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_akun);

        // ==== Inisialisasi ====
        sessionManager = new SessionManager(this);
        requestQueue = Volley.newRequestQueue(this);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtWhatsapp = findViewById(R.id.edtWhatsapp);
        btnSimpan = findViewById(R.id.btnSimpan);

        // ==== Load data user saat ini ====
        loadCurrentUserData();

        // ==== Tombol Simpan ====
        btnSimpan.setOnClickListener(v -> {
            updateUserProfile();
        });
    }

    private void loadCurrentUserData() {
        // Ambil data dari session
        String currentEmail = sessionManager.getUserEmail();
        String currentPhone = sessionManager.getUserPhone();

        edtEmail.setText(currentEmail);
        edtWhatsapp.setText(currentPhone);

        Log.d("UPDATE_AKUN", "Data user loaded - Email: " + currentEmail + ", Phone: " + currentPhone);
    }

    private void updateUserProfile() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String whatsapp = edtWhatsapp.getText().toString().trim();

        // Validasi
        if (email.isEmpty()) {
            edtEmail.setError("Email tidak boleh kosong");
            return;
        }
        if (whatsapp.isEmpty()) {
            edtWhatsapp.setError("Whatsapp tidak boleh kosong");
            return;
        }
        if (!password.isEmpty() && password.length() < 6) {
            edtPassword.setError("Password minimal 6 karakter");
            return;
        }

        // Tampilkan loading
        btnSimpan.setEnabled(false);
        btnSimpan.setText("Menyimpan...");

        // Dapatkan user ID dari session
        int userId = sessionManager.getUserId();
        if (userId == 0) {
            showError("Session expired, silakan login kembali");
            return;
        }

        // URL untuk update user
        String url = SUPABASE_URL + "/rest/v1/pengguna?id_pengguna=eq." + userId;

        Log.d("UPDATE_AKUN", "Updating user ID: " + userId);
        Log.d("UPDATE_AKUN", "Update data - Email: " + email + ", Phone: " + whatsapp + ", Password changed: " + !password.isEmpty());

        // Gunakan StringRequest untuk handle response kosong
        StringRequest request = new StringRequest(
                Request.Method.PATCH,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("UPDATE_AKUN", "Update successful - Response: " + response);

                        // Update session manager dengan data baru
                        updateSessionData(email, whatsapp);

                        Toast.makeText(UpdateAkunActivity.this, "Akun berhasil diperbarui", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("UPDATE_AKUN", "❌ Update error: " + error.toString());

                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            String responseBody = new String(error.networkResponse.data);
                            Log.e("UPDATE_AKUN", "Status code: " + statusCode);
                            Log.e("UPDATE_AKUN", "Response data: " + responseBody);

                            if (statusCode == 409) {
                                showError("Email sudah digunakan oleh akun lain");
                            } else if (statusCode == 400) {
                                showError("Data tidak valid: " + responseBody);
                            } else if (statusCode == 401) {
                                showError("Unauthorized - Silakan login kembali");
                            } else {
                                showError("Gagal memperbarui akun (Error " + statusCode + ")");
                            }
                        } else {
                            showError("Gagal memperbarui akun: " + error.getMessage());
                        }

                        resetButton();
                    }
                }
        ) {
            @Override
            public byte[] getBody() {
                // Build JSON body manually
                JSONObject updateData = new JSONObject();
                try {
                    updateData.put("email", email);
                    updateData.put("no_hp", whatsapp);

                    // Hanya update password jika diisi
                    if (!password.isEmpty()) {
                        updateData.put("password", password);
                    }

                    // Tambah timestamp update
                    updateData.put("diperbarui_pada", "now()");

                    Log.d("UPDATE_AKUN", "Request Body: " + updateData.toString());
                    return updateData.toString().getBytes("utf-8");

                } catch (Exception e) {
                    Log.e("UPDATE_AKUN", "Error creating request body: " + e.getMessage());
                    return null;
                }
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                headers.put("Content-Type", "application/json");
                headers.put("Prefer", "return=minimal");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void updateSessionData(String newEmail, String newPhone) {
        // Update session dengan data baru
        sessionManager.updateUserProfile(sessionManager.getUserName(), newPhone);

        // Update email secara manual di shared preferences
        sessionManager.prefs.edit().putString("user_email", newEmail).apply();

        Log.d("UPDATE_AKUN", "Session updated - Email: " + newEmail + ", Phone: " + newPhone);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        resetButton();
    }

    private void resetButton() {
        btnSimpan.setEnabled(true);
        btnSimpan.setText("SIMPAN PERUBAHAN");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }
}