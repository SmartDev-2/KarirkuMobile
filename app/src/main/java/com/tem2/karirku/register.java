package com.tem2.karirku;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class register extends AppCompatActivity {

    private EditText edtNama, edtEmail, edtPassword, edtNotlp;
    private ImageView imgTogglePassword;
    private boolean isPasswordVisible = false;

    private final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private final String SUPABASE_API_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView masukText = findViewById(R.id.textView5);
        edtNama = findViewById(R.id.edt_nama);
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_Password);
        edtNotlp = findViewById(R.id.edt_notlp);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);

        masukText.setOnClickListener(v -> {
            startActivity(new Intent(register.this, MainActivity.class));
            finish();
        });

        imgTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        findViewById(R.id.btnRegister).setOnClickListener(v -> registerUser());
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            imgTogglePassword.setImageResource(R.drawable.ic_eyeoff);
        } else {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            imgTogglePassword.setImageResource(R.drawable.ic_eyeon);
        }
        edtPassword.setSelection(edtPassword.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }

    private void registerUser() {
        String nama = edtNama.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String nohp = edtNotlp.getText().toString().trim();

        if (nama.isEmpty() || email.isEmpty() || password.isEmpty() || nohp.isEmpty()) {
            Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show();
            return;
        }

        checkEmailExists(nama, email, password, nohp);
    }

    private void checkEmailExists(String nama, String email, String password, String nohp) {

        String url = SUPABASE_URL + "/rest/v1/pengguna?email=eq." + email + "&select=id_pengguna";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (response.length() > 0) {
                        Toast.makeText(this, "Email sudah digunakan!", Toast.LENGTH_SHORT).show();
                    } else {
                        saveUserToDatabase(nama, email, password, nohp);
                    }
                },
                error -> {
                    saveUserToDatabase(nama, email, password, nohp);
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

    private void saveUserToDatabase(String nama, String email, String password, String nohp) {

        String url = SUPABASE_URL + "/rest/v1/pengguna";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("nama_lengkap", nama);
            jsonBody.put("email", email);
            jsonBody.put("password", password);
            jsonBody.put("no_hp", nohp);
            jsonBody.put("role", "pencaker");
        } catch (JSONException e) {
            Toast.makeText(this, "Terjadi kesalahan membuat data!", Toast.LENGTH_LONG).show();
            return;
        }

        JsonObjectRequest dbRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(register.this, MainActivity.class));
                    finish();
                },
                error -> {
                    if (error.networkResponse != null) {
                        Log.e("REGISTER_DB_ERROR",
                                "Code: " + error.networkResponse.statusCode +
                                        " | Body: " + new String(error.networkResponse.data));
                    } else {
                        Log.e("REGISTER_DB_ERROR", "Volley Error: " + error.toString());
                    }
                    Toast.makeText(this, "Gagal menyimpan data!", Toast.LENGTH_LONG).show();
                }
        ) {

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                if (response != null && (response.statusCode == 200 || response.statusCode == 201)) {
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

                // FIX PALING PENTING: supaya tidak 409 dan selalu success
                headers.put("Prefer", "return=representation");

                return headers;
            }
        };

        Volley.newRequestQueue(this).add(dbRequest);
    }
}
