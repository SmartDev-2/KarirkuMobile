package com.tem2.karirku;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private ImageView icGoogle;
    private ImageView splashScreen;
    private RelativeLayout loginForm;
    private SessionManager sessionManager;

    private final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d("MAIN_ACTIVITY", "========================================");
        Log.d("MAIN_ACTIVITY", "ðŸ“± MainActivity onCreate");
        Log.d("MAIN_ACTIVITY", "========================================");

        // âœ… Initialize SessionManager
        sessionManager = new SessionManager(this);

        // âœ… Check if already logged in
        if (sessionManager.isLoggedIn()) {
            Log.d("MAIN_ACTIVITY", "âœ… User already logged in");
            Log.d("MAIN_ACTIVITY", "   User: " + sessionManager.getUserName());
            Log.d("MAIN_ACTIVITY", "   Email: " + sessionManager.getUserEmail());
            Log.d("MAIN_ACTIVITY", "ðŸ”„ Redirecting to beranda...");

            // Auto-redirect to beranda
            startActivity(new Intent(this, beranda.class));
            finish();
            return;
        }

        Log.d("MAIN_ACTIVITY", "âŒ No active session, showing login screen");

        // Inisialisasi view untuk splash screen
        splashScreen = findViewById(R.id.splashScreen);
        loginForm = findViewById(R.id.loginForm);

        setupSplashScreen();
    }

    private void setupSplashScreen() {
        loginForm.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Sembunyikan splash screen
                splashScreen.setVisibility(View.GONE);

                // Tampilkan login form
                loginForm.setVisibility(View.VISIBLE);

                // Setup login logic setelah splash selesai
                setupLoginLogic();
            }
        }, 1000); // 1 DETIK
    }

    private void setupLoginLogic() {
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_Password);
        icGoogle = findViewById(R.id.ic_google);

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi semua field!", Toast.LENGTH_SHORT).show();
            } else {
                loginManual(email, password);
            }
        });

        icGoogle.setOnClickListener(v -> loginWithGoogle());

        // Tambahkan click listener untuk daftar text
        findViewById(R.id.textView5).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, register.class);
            startActivity(intent);
        });

        TextView lupaPassword = findViewById(R.id.textView2);
        lupaPassword.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Emailubahpasword.class));
        });


        // Toggle password visibility
        ImageView imgTogglePassword = findViewById(R.id.imgTogglePassword);
        imgTogglePassword.setOnClickListener(v -> {
            if (edtPassword.getInputType() == 129) { // Password hidden
                edtPassword.setInputType(1); // Text visible
                imgTogglePassword.setImageResource(R.drawable.eyeon);
            } else {
                edtPassword.setInputType(129); // Password hidden
                imgTogglePassword.setImageResource(R.drawable.eyeoff);
            }
            edtPassword.setSelection(edtPassword.getText().length());
        });
    }

    // LOGIN MANUAL VIA REST API
    private void loginManual(String email, String password) {
        String url = SUPABASE_URL + "/rest/v1/pengguna?email=eq." + email + "&select=*";

        Log.d("LOGIN_DEBUG", "Request URL: " + url);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("LOGIN_DEBUG", "Response: " + response.toString());

                    if (response.length() > 0) {
                        try {
                            JSONObject user = response.getJSONObject(0);
                            String pass = user.optString("password", "");

                            // ⛔ HAPUS emailVerified karena kolomnya sudah tidak ada
                            // boolean emailVerified = user.optBoolean("email_verified", false);

                            if (pass.equals(password)) {

                                Log.d("LOGIN_DEBUG", "========================================");
                                Log.d("LOGIN_DEBUG", "Login successful!");
                                Log.d("LOGIN_DEBUG", "========================================");

                                // ✔ Simpan session
                                sessionManager.createSession(user);

                                Log.d("LOGIN_DEBUG", "✔ Session created for: " + user.optString("nama_lengkap"));
                                Log.d("LOGIN_DEBUG", "   User ID: " + user.optInt("id_pengguna"));
                                Log.d("LOGIN_DEBUG", "   Email: " + user.optString("email"));
                                Log.d("LOGIN_DEBUG", "➡ Redirecting to beranda...");

                                Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, beranda.class));
                                finish();

                            } else {
                                Log.w("LOGIN_DEBUG", "Password incorrect");
                                Toast.makeText(this, "Password salah", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            Log.e("LOGIN_ERROR", "JSON parsing error: " + e.getMessage());
                            Toast.makeText(this, "Parsing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w("LOGIN_DEBUG", "❌ Email tidak ditemukan");
                        Toast.makeText(this, "❌ Email tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("LOGIN_ERROR", "Volley error: " + error.toString());
                    Toast.makeText(this, "❌ Gagal koneksi ke server", Toast.LENGTH_SHORT).show();
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

        queue.add(request);
    }


    // ðŸ”¹ LOGIN VIA GOOGLE SUPABASE
    private void loginWithGoogle() {
        String redirectUrl = SUPABASE_URL + "/auth/v1/authorize?provider=google"
                + "&redirect_to=" + Uri.encode("karirku://auth-callback");

        Log.d("LOGIN_GOOGLE", "Redirect ke: " + redirectUrl);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl));
        startActivity(browserIntent);
    }

    // ðŸ”¹ Tangkap callback dari Supabase (Google OAuth)
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri data = intent.getData();
        if (data != null) {
            String fullUri = data.toString();
            Log.d("SUPABASE_CALLBACK", "Data: " + fullUri);

            if (fullUri.contains("access_token")) {
                String token = fullUri.substring(fullUri.indexOf("access_token=") + 13);
                if (token.contains("&")) token = token.substring(0, token.indexOf("&"));

                Log.d("SUPABASE_CALLBACK", "âœ… Token received: " + token.substring(0, 20) + "...");

                // TODO: Fetch user data from Supabase Auth with token
                // Then create session with that data
                // For now, create minimal session
                JSONObject tempUser = new JSONObject();
                try {
                    tempUser.put("id_pengguna", 0);
                    tempUser.put("nama_lengkap", "Google User");
                    tempUser.put("email", "google@user.com");
                    tempUser.put("role", "pencaker");
                    sessionManager.createSession(tempUser, token);
                } catch (Exception e) {
                    Log.e("SUPABASE_CALLBACK", "Error creating temp session: " + e.getMessage());
                }

                Toast.makeText(this, "âœ… Login Google sukses!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, beranda.class));
                finish();
            } else {
                Log.e("SUPABASE_CALLBACK", "No access token in callback");
                Toast.makeText(this, "Gagal ambil token Google!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}