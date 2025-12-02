package com.tem2.karirku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisimpanActivity extends AppCompatActivity {

    private RecyclerView recyclerJobs;
    private JobAdapter jobAdapter;
    private List<Job> savedJobList = new ArrayList<>();
    private SessionManager sessionManager;
    private int currentUserId;
    private int currentPencakerId;
    private TextView tvEmptyState;

    private static final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disimpan);

        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        if (!sessionManager.isLoggedIn() || currentUserId == 0) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadPencakerId();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerJobs = findViewById(R.id.recyclerJobs);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        recyclerJobs.setLayoutManager(new LinearLayoutManager(this));

        // Inisialisasi adapter dengan list kosong dulu
        jobAdapter = new JobAdapter(this, savedJobList, true);
        recyclerJobs.setAdapter(jobAdapter);

        // Sembunyikan empty state awalnya
        tvEmptyState.setVisibility(View.GONE);
    }

    private void loadPencakerId() {
        Log.d("DISIMPAN_DEBUG", "🔄 Memuat ID Pencaker untuk user: " + currentUserId);

        String url = SUPABASE_URL + "/rest/v1/pencaker?id_pengguna=eq." + currentUserId + "&select=id_pencaker";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            JSONObject pencaker = response.getJSONObject(0);
                            currentPencakerId = pencaker.getInt("id_pencaker");
                            Log.d("DISIMPAN_DEBUG", "✅ Berhasil mendapatkan ID Pencaker: " + currentPencakerId);
                            loadSavedJobs();
                        } else {
                            Log.e("DISIMPAN_DEBUG", "❌ Tidak ditemukan data pencaker untuk user: " + currentUserId);
                            Toast.makeText(DisimpanActivity.this, "Error: Profil pencaker tidak ditemukan", Toast.LENGTH_SHORT).show();
                            showEmptyState();
                        }
                    } catch (JSONException e) {
                        Log.e("DISIMPAN_DEBUG", "❌ Error parsing data pencaker: " + e.getMessage());
                        showEmptyState();
                    }
                },
                error -> {
                    Log.e("DISIMPAN_DEBUG", "❌ Error load ID Pencaker: " + error.toString());
                    Toast.makeText(DisimpanActivity.this, "Gagal memuat data profil", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                return headers;
            }
        };

        queue.add(request);
    }

    private void loadSavedJobs() {
        if (currentPencakerId == 0) {
            Log.e("DISIMPAN_DEBUG", "❌ ID Pencaker masih 0, tidak bisa load saved jobs");
            showEmptyState();
            return;
        }

        // PERBAIKAN: Ambil nama_perusahaan dari tabel perusahaan
        String url = SUPABASE_URL + "/rest/v1/favorit_lowongan" +
                "?id_pencaker=eq." + currentPencakerId +
                "&select=id_lowongan,lowongan(*,perusahaan(nama_perusahaan,logo_url,logo_path,id_perusahaan))";

        Log.d("DISIMPAN_DEBUG", "🔍 Loading saved jobs dari URL: " + url);

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("DISIMPAN_DEBUG", "✅ Response received: " + response.length() + " items");

                    if (response.length() == 0) {
                        Log.d("DISIMPAN_DEBUG", "📭 Tidak ada lowongan yang disimpan");
                        showEmptyState();
                        return;
                    }

                    savedJobList.clear();
                    parseSavedJobs(response);

                    if (savedJobList.isEmpty()) {
                        showEmptyState();
                        Toast.makeText(DisimpanActivity.this, "Belum ada lowongan yang disimpan", Toast.LENGTH_SHORT).show();
                    } else {
                        hideEmptyState();
                        jobAdapter.setData(savedJobList);
                        Log.d("DISIMPAN_DEBUG", "🎉 Berhasil memuat " + savedJobList.size() + " lowongan disimpan");
                        Toast.makeText(DisimpanActivity.this, "Ditemukan " + savedJobList.size() + " lowongan disimpan", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("DISIMPAN_DEBUG", "❌ Gagal load saved jobs: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("DISIMPAN_DEBUG", "Error code: " + error.networkResponse.statusCode);
                        Log.e("DISIMPAN_DEBUG", "Error data: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(DisimpanActivity.this, "Gagal memuat lowongan disimpan", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }

    private void parseSavedJobs(JSONArray response) {
        try {
            int validJobs = 0;
            int invalidJobs = 0;

            Log.d("DISIMPAN_DEBUG", "🔄 Parsing " + response.length() + " saved jobs");

            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject favoritObj = response.getJSONObject(i);

                    // PERBAIKAN: Ambil langsung object lowongan
                    JSONObject lowongan = favoritObj.getJSONObject("lowongan");

                    int idLowongan = lowongan.optInt("id_lowongan", 0);

                    if (idLowongan <= 0) {
                        // Coba alternatif field name
                        idLowongan = lowongan.optInt("id", 0);
                    }

                    if (idLowongan <= 0) {
                        invalidJobs++;
                        Log.w("DISIMPAN_DEBUG", "⚠️ Skip job dengan ID tidak valid: " + idLowongan);
                        continue;
                    }

                    // Ambil data dengan field yang sesuai
                    String judul = lowongan.optString("judul", "-");
                    String lokasi = lowongan.optString("lokasi", "-");
                    String kategori = lowongan.optString("kategori", "-");
                    String tipe = lowongan.optString("tipe_pekerjaan", "-");
                    String gaji = lowongan.optString("gaji_range", "-");
                    String deskripsi = lowongan.optString("deskripsi", "");
                    String kualifikasi = lowongan.optString("kualifikasi", "");
                    String modeKerja = lowongan.optString("mode_kerja", "On-site");
                    String benefit = lowongan.optString("benefit", "");
                    String noTelp = lowongan.optString("no_telp", "");

                    // PERBAIKAN: Ambil nama_perusahaan dari tabel perusahaan
                    String namaPerusahaan = "Perusahaan"; // default
                    String logoUrl = "";
                    String logoPath = "";
                    int idPerusahaan = 0;

                    // Ambil dari objek perusahaan jika ada
                    if (lowongan.has("perusahaan")) {
                        try {
                            JSONObject perusahaan = lowongan.getJSONObject("perusahaan");
                            namaPerusahaan = perusahaan.optString("nama_perusahaan", "Perusahaan");
                            logoUrl = perusahaan.optString("logo_url", "");
                            logoPath = perusahaan.optString("logo_path", "");
                            idPerusahaan = perusahaan.optInt("id_perusahaan", 0);
                            Log.d("DISIMPAN_DEBUG", "✅ Found company data - Name: " + namaPerusahaan + ", Logo URL: " + logoUrl);
                        } catch (JSONException e) {
                            Log.e("DISIMPAN_DEBUG", "❌ Error parsing perusahaan data: " + e.getMessage());
                        }
                    }

                    // Format waktu posting
                    String postedTime = "Baru saja";
                    String dibuatPada = lowongan.optString("dibuat_pada", "");
                    if (!dibuatPada.isEmpty()) {
                        postedTime = formatTimeAgo(dibuatPada);
                    }

                    // Format jumlah pendaftar (default)
                    String applicants = gaji + " Pendaftar";

                    Log.d("DISIMPAN_DEBUG", "📦 Parsed Job - ID: " + idLowongan +
                            ", Title: " + judul + ", Company: " + namaPerusahaan +
                            ", Logo URL: " + logoUrl);

                    // Buat objek Job dengan constructor yang lengkap
                    Job job = new Job(
                            idLowongan,
                            namaPerusahaan,
                            lokasi,
                            judul,
                            postedTime,
                            applicants,
                            kategori,
                            tipe,
                            gaji,
                            modeKerja,
                            deskripsi,
                            kualifikasi,
                            benefit,
                            noTelp,
                            logoUrl,
                            logoPath,
                            idPerusahaan
                    );

                    savedJobList.add(job);
                    validJobs++;

                } catch (JSONException e) {
                    invalidJobs++;
                    Log.e("DISIMPAN_DEBUG", "❌ Error parsing item " + i + ": " + e.getMessage());
                }
            }

            Log.d("DISIMPAN_DEBUG", "📊 Parsing Summary - Valid: " + validJobs + ", Invalid: " + invalidJobs);

        } catch (Exception e) {
            Log.e("DISIMPAN_DEBUG", "❌ Error parsing saved jobs: " + e.getMessage());
        }
    }

    private String formatTimeAgo(String dateTime) {
        try {
            if (dateTime == null || dateTime.isEmpty()) {
                return "Baru saja";
            }

            // Handle format ISO 8601 dari Supabase
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
            java.util.Date past = sdf.parse(dateTime.substring(0, 19));
            java.util.Date now = new java.util.Date();

            long diff = now.getTime() - past.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) {
                return days + " Hari lalu";
            } else if (hours > 0) {
                return hours + " Jam lalu";
            } else if (minutes > 0) {
                return minutes + " Menit lalu";
            } else {
                return "Baru saja";
            }
        } catch (Exception e) {
            Log.e("DISIMPAN_DEBUG", "Error parsing time: " + e.getMessage());
            return "Baru saja";
        }
    }

    private void showEmptyState() {
        runOnUiThread(() -> {
            recyclerJobs.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("Belum ada lowongan yang disimpan");
        });
    }

    private void hideEmptyState() {
        runOnUiThread(() -> {
            recyclerJobs.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("DISIMPAN_DEBUG", "onResume dipanggil");

        // Refresh data ketika activity di-resume
        if (currentPencakerId != 0) {
            loadSavedJobs();
        } else if (currentUserId != 0) {
            loadPencakerId();
        }
    }
}