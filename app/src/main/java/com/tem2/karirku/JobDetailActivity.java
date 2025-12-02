package com.tem2.karirku;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JobDetailActivity extends AppCompatActivity {

    private static final String TAG = "JOB_DETAIL";
    private static final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    private ImageView btnBack;
    private TextView tvJobTitle, tvCompanyName, tvLocation, tvJobDescription, tvRequirements;
    private TextView tvTag1, tvTag2, tvTag3, tvJobTypeValue, tvSalary, tvWorkingHours, tvExpertise;
    private MaterialButton btnWhatsApp;
    private Button btnApply;

    private Job currentJob;
    private SessionManager sessionManager;
    private RequestQueue requestQueue;
    private int currentPencakerId = 0;
    private String currentCvUrl = "";
    private boolean hasApplied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        try {
            sessionManager = new SessionManager(this);
            requestQueue = Volley.newRequestQueue(this);
            initViews();
            loadPencakerAndCV();
            loadJobData();
            setupClickListeners();
        } catch (Exception e) {
            Log.e(TAG, "❌ ERROR in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Terjadi kesalahan saat memuat detail lowongan", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvJobTitle = findViewById(R.id.tvJobTitle);
        tvCompanyName = findViewById(R.id.tvCompanyName);
        tvLocation = findViewById(R.id.tvLocation);
        tvJobDescription = findViewById(R.id.tvJobDescription);
        tvRequirements = findViewById(R.id.tvRequirements);
        tvTag1 = findViewById(R.id.tvTag1);
        tvTag2 = findViewById(R.id.tvTag2);
        tvTag3 = findViewById(R.id.tvTag3);
        tvJobTypeValue = findViewById(R.id.tvJobTypeValue);
        tvSalary = findViewById(R.id.tvSalary);
        tvWorkingHours = findViewById(R.id.tvWorkingHours);
        tvExpertise = findViewById(R.id.tvExpertise);
        btnWhatsApp = findViewById(R.id.btnWhatsApp);
        btnApply = findViewById(R.id.btnApply);
    }

    private void loadPencakerAndCV() {
        if (!sessionManager.isLoggedIn()) {
            Log.e(TAG, "❌ User not logged in");
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int userId = sessionManager.getUserId();
        Log.d(TAG, "🔍 Loading pencaker data for user ID: " + userId);

        String pencakerUrl = SUPABASE_URL + "/rest/v1/pencaker?select=id_pencaker&id_pengguna=eq." + userId;

        JsonArrayRequest pencakerRequest = new JsonArrayRequest(
                Request.Method.GET,
                pencakerUrl,
                null,
                response -> {
                    Log.d(TAG, "📨 Pencaker API Response: " + response.toString());
                    try {
                        if (response.length() > 0) {
                            JSONObject pencakerData = response.getJSONObject(0);
                            currentPencakerId = pencakerData.getInt("id_pencaker");
                            Log.d(TAG, "✅ Loaded pencaker ID: " + currentPencakerId);
                            loadCVUrl();
                            checkIfAlreadyApplied();
                        } else {
                            Log.e(TAG, "❌ No pencaker data found for user");
                            runOnUiThread(() -> {
                                Toast.makeText(JobDetailActivity.this, "Data profil tidak lengkap. Silakan lengkapi profil terlebih dahulu.", Toast.LENGTH_LONG).show();
                                btnApply.setEnabled(false);
                                btnApply.setAlpha(0.5f);
                                btnApply.setText("Lamar (Lengkapi Profil)");
                            });
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error parsing pencaker data: " + e.getMessage());
                        runOnUiThread(() -> {
                            Toast.makeText(JobDetailActivity.this, "Error memuat data profil", Toast.LENGTH_LONG).show();
                            btnApply.setEnabled(false);
                            btnApply.setAlpha(0.5f);
                            btnApply.setText("Lamar (Error)");
                        });
                    }
                },
                error -> {
                    Log.e(TAG, "❌ Volley error loading pencaker: " + error.toString());
                    runOnUiThread(() -> {
                        Toast.makeText(JobDetailActivity.this, "Gagal memuat data profil", Toast.LENGTH_LONG).show();
                        btnApply.setEnabled(false);
                        btnApply.setAlpha(0.5f);
                        btnApply.setText("Lamar (Error Load Profil)");
                    });
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

        requestQueue.add(pencakerRequest);
    }

    private void checkIfAlreadyApplied() {
        if (currentPencakerId == 0 || currentJob == null) {
            Log.d(TAG, "⚠️ Cannot check application status: pencakerId or job is null");
            return;
        }

        String url = SUPABASE_URL + "/rest/v1/lamaran?select=id_lamaran&id_pencaker=eq." + currentPencakerId + "&id_lowongan=eq." + currentJob.getIdLowongan();

        Log.d(TAG, "🔍 Checking application status: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            hasApplied = true;
                            runOnUiThread(() -> {
                                btnApply.setEnabled(false);
                                btnApply.setAlpha(0.5f);
                                btnApply.setText("✓ Telah Dilamar");
                                Log.d(TAG, "✅ User has already applied for this job");
                            });
                        } else {
                            hasApplied = false;
                            Log.d(TAG, "✅ User has not applied for this job yet");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error checking application status: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "❌ Error checking application status: " + error.toString());
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

        requestQueue.add(request);
    }

    private void loadCVUrl() {
        if (currentPencakerId == 0) {
            Log.e(TAG, "❌ Cannot load CV: Invalid pencaker ID");
            runOnUiThread(() -> {
                Toast.makeText(JobDetailActivity.this, "Data profil tidak valid", Toast.LENGTH_LONG).show();
                btnApply.setEnabled(false);
                btnApply.setAlpha(0.5f);
                btnApply.setText("Lamar (Profil Invalid)");
            });
            return;
        }

        String cvUrl = SUPABASE_URL + "/rest/v1/cv?select=*&id_pencaker=eq." + currentPencakerId;
        Log.d(TAG, "🔍 CV API URL: " + cvUrl);

        JsonArrayRequest cvRequest = new JsonArrayRequest(
                Request.Method.GET,
                cvUrl,
                null,
                response -> {
                    Log.d(TAG, "📨 CV API Response: " + response.toString());
                    try {
                        if (response.length() > 0) {
                            JSONObject cvData = response.getJSONObject(0);

                            if (cvData.has("cv_url")) {
                                currentCvUrl = cvData.getString("cv_url");
                            } else if (cvData.has("url")) {
                                currentCvUrl = cvData.getString("url");
                            } else if (cvData.has("file_url")) {
                                currentCvUrl = cvData.getString("file_url");
                            } else {
                                for (String key : new String[]{"cv_url", "url", "file_url", "document_url"}) {
                                    if (cvData.has(key) && cvData.getString(key) != null &&
                                            !cvData.getString(key).isEmpty()) {
                                        currentCvUrl = cvData.getString(key);
                                        break;
                                    }
                                }
                            }

                            String fileName = cvData.optString("nama_file", "");
                            String uploadedAt = cvData.optString("uploaded_at", "");

                            runOnUiThread(() -> {
                                if (currentCvUrl != null && !currentCvUrl.trim().isEmpty()) {
                                    Log.d(TAG, "✅ Loaded CV URL: " + currentCvUrl);

                                    if (!hasApplied) {
                                        btnApply.setEnabled(true);
                                        btnApply.setAlpha(1.0f);
                                        btnApply.setText("Lamar Pekerjaan Ini");
                                    }

                                    Toast.makeText(JobDetailActivity.this,
                                            "CV siap: " + (fileName.isEmpty() ? getFileNameFromUrl(currentCvUrl) : fileName),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.w(TAG, "⚠️ CV URL is empty in database");
                                    btnApply.setEnabled(false);
                                    btnApply.setAlpha(0.5f);
                                    btnApply.setText("Lamar (Upload CV Dulu)");
                                    Toast.makeText(JobDetailActivity.this,
                                            "CV tidak valid. Silakan upload ulang CV.", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            runOnUiThread(() -> {
                                Log.w(TAG, "⚠️ No CV found for pencaker ID: " + currentPencakerId);
                                btnApply.setEnabled(false);
                                btnApply.setAlpha(0.5f);
                                btnApply.setText("Lamar (Upload CV Dulu)");
                                Toast.makeText(JobDetailActivity.this,
                                        "Silakan upload CV terlebih dahulu sebelum melamar", Toast.LENGTH_LONG).show();
                            });
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error parsing CV data: " + e.getMessage());
                        runOnUiThread(() -> {
                            Toast.makeText(JobDetailActivity.this, "Error memuat data CV", Toast.LENGTH_LONG).show();
                            btnApply.setEnabled(false);
                            btnApply.setAlpha(0.5f);
                            btnApply.setText("Lamar (Error)");
                        });
                    }
                },
                error -> {
                    Log.e(TAG, "❌ Volley error loading CV: " + error.toString());
                    runOnUiThread(() -> {
                        Toast.makeText(JobDetailActivity.this, "Gagal memuat data CV", Toast.LENGTH_LONG).show();
                        btnApply.setEnabled(false);
                        btnApply.setAlpha(0.5f);
                        btnApply.setText("Lamar (Error Load CV)");
                    });
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

        requestQueue.add(cvRequest);
    }

    private void loadJobData() {
        try {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("JOB_DATA")) {
                currentJob = (Job) intent.getSerializableExtra("JOB_DATA");

                if (currentJob != null) {
                    Log.d(TAG, "========================================");
                    Log.d(TAG, "Loading job data: " + currentJob.toString());
                    Log.d(TAG, "========================================");

                    tvJobTitle.setText(currentJob.getJobTitle() != null ? currentJob.getJobTitle() : "Judul tidak tersedia");
                    tvCompanyName.setText(currentJob.getCompanyName() != null ? currentJob.getCompanyName() : "Perusahaan tidak tersedia");
                    tvLocation.setText(currentJob.getLocation() != null ? currentJob.getLocation() : "Lokasi tidak tersedia");

                    tvTag1.setText(currentJob.getTag1() != null ? currentJob.getTag1() : "");
                    tvTag2.setText(currentJob.getTag2() != null ? currentJob.getTag2() : "");
                    tvTag3.setText(currentJob.getTag3() != null ? currentJob.getTag3() : "");

                    tvTag1.setVisibility(currentJob.getTag1() != null && !currentJob.getTag1().isEmpty() ? View.VISIBLE : View.GONE);
                    tvTag2.setVisibility(currentJob.getTag2() != null && !currentJob.getTag2().isEmpty() ? View.VISIBLE : View.GONE);
                    tvTag3.setVisibility(currentJob.getTag3() != null && !currentJob.getTag3().isEmpty() ? View.VISIBLE : View.GONE);

                    tvJobTypeValue.setText(currentJob.getTipePekerjaan() != null ? currentJob.getTipePekerjaan() : "Full Time");
                    tvSalary.setText(currentJob.getGajiRange() != null ? currentJob.getGajiRange() : "Dirahasiakan");
                    tvWorkingHours.setText(currentJob.getModeKerja() != null ? currentJob.getModeKerja() : "Fleksibel");
                    tvExpertise.setText(currentJob.getTag1() != null ? currentJob.getTag1() : "Menyesuaikan");

                    String description = currentJob.getDeskripsi();
                    if (description != null && !description.isEmpty()) {
                        tvJobDescription.setText(description);
                    } else {
                        tvJobDescription.setText("Lowongan " + currentJob.getJobTitle() +
                                " di " + currentJob.getCompanyName() +
                                " membuka kesempatan bagi profesional yang berpengalaman di bidang " +
                                (currentJob.getTag1() != null ? currentJob.getTag1() : "terkait") + ".\n\n" +
                                "Bergabunglah dengan tim kami yang dinamis dan berkembang pesat.");
                    }

                    String kualifikasi = currentJob.getKualifikasi();
                    if (kualifikasi != null && !kualifikasi.isEmpty()) {
                        tvRequirements.setText(kualifikasi);
                    } else {
                        StringBuilder defaultRequirements = new StringBuilder();
                        if (currentJob.getTag1() != null && !currentJob.getTag1().isEmpty()) {
                            defaultRequirements.append("• Pengalaman di bidang ").append(currentJob.getTag1()).append("\n");
                        }
                        if (currentJob.getTag2() != null && !currentJob.getTag2().isEmpty()) {
                            defaultRequirements.append("• Memahami ").append(currentJob.getTag2()).append("\n");
                        }
                        if (currentJob.getTag3() != null && !currentJob.getTag3().isEmpty()) {
                            defaultRequirements.append("• Dapat bekerja ").append(currentJob.getTag3()).append("\n");
                        }
                        if (currentJob.getLocation() != null && !currentJob.getLocation().isEmpty()) {
                            defaultRequirements.append("• Berdomisili di ").append(currentJob.getLocation()).append("\n");
                        }

                        if (defaultRequirements.length() == 0) {
                            defaultRequirements.append("• Pengalaman di bidang terkait\n");
                            defaultRequirements.append("• Kemampuan komunikasi yang baik\n");
                            defaultRequirements.append("• Dapat bekerja dalam tim\n");
                            defaultRequirements.append("• Memiliki motivasi tinggi");
                        }

                        tvRequirements.setText(defaultRequirements.toString());
                    }

                    // TAMBAHAN: Load company logo
                    loadCompanyLogo();

                    if (currentJob.getNoTelp() == null || currentJob.getNoTelp().isEmpty()) {
                        fetchNoTelpFromDatabase();
                    } else {
                        setupWhatsAppButton();
                    }

                    if (currentPencakerId != 0) {
                        checkIfAlreadyApplied();
                    }
                } else {
                    Toast.makeText(this, "Data lowongan tidak tersedia", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Terjadi kesalahan saat membuka detail lowongan", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error in loadJobData: " + e.getMessage(), e);
            Toast.makeText(this, "Gagal memuat data lowongan", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * TAMBAHAN: Method untuk memuat logo perusahaan di halaman detail
     */
    private void loadCompanyLogo() {
        if (currentJob == null) {
            Log.w(TAG, "⚠️ currentJob is null, cannot load logo");
            return;
        }

        String logoUrl = currentJob.getLogoUrl();
        String logoPath = currentJob.getLogoPath();

        Log.d(TAG, "🖼️ Loading company logo for: " + currentJob.getCompanyName());
        Log.d(TAG, "   Logo URL: " + logoUrl);
        Log.d(TAG, "   Logo Path: " + logoPath);

        ImageView imgCompanyLogo = findViewById(R.id.imgCompanyLogo);

        // Prioritaskan logo_url jika ada
        if (logoUrl != null && !logoUrl.trim().isEmpty()) {
            Log.d(TAG, "✅ Using logo_url: " + logoUrl);
            Glide.with(this)
                    .load(logoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.iconloker)
                    .error(R.drawable.iconloker)
                    .into(imgCompanyLogo);
        }
        // Jika logo_url tidak ada, coba gunakan logo_path
        else if (logoPath != null && !logoPath.trim().isEmpty()) {
            String builtUrl = buildLogoUrlFromPath(logoPath);
            Log.d(TAG, "🔄 Built URL from path: " + builtUrl);
            Glide.with(this)
                    .load(builtUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.iconloker)
                    .error(R.drawable.iconloker)
                    .into(imgCompanyLogo);
        }
        // Jika tidak ada logo, gunakan default
        else {
            Log.d(TAG, "⚠️ No logo available, using default");
            imgCompanyLogo.setImageResource(R.drawable.iconloker);
        }
    }

    /**
     * TAMBAHAN: Build URL dari logo_path
     */
    private String buildLogoUrlFromPath(String logoPath) {
        return "https://tkjnbelcgfwpbhppsnrl.supabase.co/storage/v1/object/public/" + logoPath;
    }

    private void fetchNoTelpFromDatabase() {
        if (currentJob == null) {
            Log.e(TAG, "❌ currentJob is null, cannot fetch telephone");
            return;
        }

        if (currentJob.getIdLowongan() == 0) {
            Log.e(TAG, "❌ Cannot fetch no_telp: Invalid job ID (0)");
            runOnUiThread(() -> {
                currentJob.setNoTelp("");
                setupWhatsAppButton();
            });
            return;
        }

        String url = SUPABASE_URL + "/rest/v1/lowongan?select=no_telp&id_lowongan=eq." + currentJob.getIdLowongan();

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            JSONObject jobData = response.getJSONObject(0);
                            String phoneNumber = jobData.optString("no_telp", "");

                            runOnUiThread(() -> {
                                if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                                    currentJob.setNoTelp(phoneNumber);
                                    Log.d(TAG, "✅ Fetched no_telp from database: " + phoneNumber);
                                } else {
                                    Log.w(TAG, "⚠️ No telephone number found for job ID: " + currentJob.getIdLowongan());
                                    currentJob.setNoTelp("");
                                }
                                setupWhatsAppButton();
                            });
                        } else {
                            runOnUiThread(() -> {
                                Log.e(TAG, "❌ No data found for job ID: " + currentJob.getIdLowongan());
                                currentJob.setNoTelp("");
                                setupWhatsAppButton();
                            });
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error fetching no_telp: " + e.getMessage());
                        runOnUiThread(() -> {
                            currentJob.setNoTelp("");
                            setupWhatsAppButton();
                        });
                    }
                },
                error -> {
                    Log.e(TAG, "❌ Error fetching no_telp: " + error.toString());
                    runOnUiThread(() -> {
                        currentJob.setNoTelp("");
                        setupWhatsAppButton();
                    });
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

        requestQueue.add(request);
    }

    private void setupWhatsAppButton() {
        if (currentJob == null) return;

        String phoneNumber = currentJob.getNoTelp();

        Log.d(TAG, "========================================");
        Log.d(TAG, "🔍 WHATSAPP SETUP DEBUG");
        Log.d(TAG, "Job ID: " + currentJob.getIdLowongan());
        Log.d(TAG, "Company: " + currentJob.getCompanyName());
        Log.d(TAG, "Job Title: " + currentJob.getJobTitle());
        Log.d(TAG, "Raw Phone: '" + phoneNumber + "'");
        Log.d(TAG, "Phone Length: " + (phoneNumber != null ? phoneNumber.length() : "null"));
        Log.d(TAG, "Phone Empty: " + (phoneNumber == null || phoneNumber.trim().isEmpty()));
        Log.d(TAG, "========================================");

        btnWhatsApp.setVisibility(View.VISIBLE);

        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            btnWhatsApp.setEnabled(true);
            btnWhatsApp.setAlpha(1.0f);
            btnWhatsApp.setText("💬 Hubungi via WhatsApp");
            Log.d(TAG, "✅ WhatsApp button ENABLED");
        } else {
            btnWhatsApp.setEnabled(false);
            btnWhatsApp.setAlpha(0.5f);
            btnWhatsApp.setText("WhatsApp (Tidak Tersedia)");
            Log.d(TAG, "❌ WhatsApp button DISABLED - no phone number");
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnWhatsApp.setOnClickListener(v -> {
            if (btnWhatsApp.isEnabled()) {
                openWhatsApp();
            } else {
                Toast.makeText(this, "Fitur WhatsApp tidak tersedia untuk lowongan ini", Toast.LENGTH_SHORT).show();
            }
        });

        btnApply.setOnClickListener(v -> {
            if (hasApplied) {
                Toast.makeText(this, "Anda sudah melamar lowongan ini", Toast.LENGTH_LONG).show();
                return;
            }

            if (!btnApply.isEnabled()) {
                String currentText = btnApply.getText().toString();
                if (currentText.contains("Upload CV")) {
                    Toast.makeText(this, "Silakan upload CV terlebih dahulu sebelum melamar", Toast.LENGTH_LONG).show();
                } else if (currentText.contains("Profil")) {
                    Toast.makeText(this, "Silakan lengkapi profil terlebih dahulu", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Tidak dapat melamar saat ini", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            showLamaranBottomSheet();
        });
    }

    private void showLamaranBottomSheet() {
        if (currentJob == null) {
            Toast.makeText(this, "Data lowongan tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentPencakerId == 0) {
            Toast.makeText(this, "Data profil tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentCvUrl == null || currentCvUrl.trim().isEmpty()) {
            Toast.makeText(this, "CV tidak tersedia. Silakan upload CV terlebih dahulu.", Toast.LENGTH_LONG).show();
            return;
        }

        if (hasApplied) {
            Toast.makeText(this, "Anda sudah melamar lowongan ini", Toast.LENGTH_LONG).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottomsheet_lamaran, null);
        dialog.setContentView(view);

        TextInputEditText edtCatatan = view.findViewById(R.id.edtCatatanHRD);
        MaterialButton btnKirim = view.findViewById(R.id.btnKirimLamaran);
        MaterialButton btnBatal = view.findViewById(R.id.btnBatalLamaran);

        String fileName = getFileNameFromUrl(currentCvUrl);
        Toast.makeText(this, "CV yang akan dikirim: " + fileName, Toast.LENGTH_LONG).show();

        btnKirim.setOnClickListener(v -> {
            String catatan = edtCatatan.getText() != null ? edtCatatan.getText().toString().trim() : "";

            if (catatan.isEmpty()) {
                catatan = "Tidak ada catatan tambahan";
            }

            Toast.makeText(JobDetailActivity.this, "Mengirim lamaran dengan CV: " + fileName, Toast.LENGTH_SHORT).show();
            uploadLamaran(catatan);
            dialog.dismiss();
        });

        btnBatal.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void uploadLamaran(String catatan) {
        if (currentJob == null || currentPencakerId == 0 || currentCvUrl == null || currentCvUrl.trim().isEmpty()) {
            Toast.makeText(this, "Data tidak lengkap untuk mengirim lamaran", Toast.LENGTH_SHORT).show();
            return;
        }

        if (hasApplied) {
            Toast.makeText(this, "Anda sudah melamar lowongan ini", Toast.LENGTH_LONG).show();
            return;
        }

        String url = SUPABASE_URL + "/rest/v1/lamaran";

        JSONObject body = new JSONObject();
        try {
            body.put("id_lowongan", currentJob.getIdLowongan());
            body.put("id_pencaker", currentPencakerId);
            body.put("cv_url", currentCvUrl);
            body.put("catatan", catatan);
            body.put("status", "diproses");

            Log.d(TAG, "📤 Uploading lamaran data: " + body.toString());
        } catch (JSONException e) {
            Log.e(TAG, "❌ Error creating JSON: " + e.getMessage());
            Toast.makeText(this, "Error menyiapkan data lamaran", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "✅ Lamaran berhasil dikirim! Response: " + response);

                        hasApplied = true;
                        runOnUiThread(() -> {
                            btnApply.setEnabled(false);
                            btnApply.setAlpha(0.5f);
                            btnApply.setText("✓ Telah Dilamar");
                            Toast.makeText(JobDetailActivity.this, "✅ Lamaran berhasil dikirim!", Toast.LENGTH_LONG).show();
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            Log.d(TAG, "📨 Response Status Code: " + statusCode);

                            if (statusCode == 201 || statusCode == 204) {
                                Log.d(TAG, "✅ DATA BERHASIL MASUK KE SUPABASE! Status: " + statusCode);

                                hasApplied = true;
                                runOnUiThread(() -> {
                                    btnApply.setEnabled(false);
                                    btnApply.setAlpha(0.5f);
                                    btnApply.setText("Telah Dilamar");
                                    Toast.makeText(JobDetailActivity.this, "✅ Lamaran berhasil dikirim!", Toast.LENGTH_LONG).show();
                                });
                                return;
                            }

                            String errorBody = error.networkResponse.data != null ?
                                    new String(error.networkResponse.data) : "No error body";
                            Log.e(TAG, "❌ Real Error - Status: " + statusCode + ", Body: " + errorBody);

                            runOnUiThread(() -> {
                                if (statusCode == 409) {
                                    hasApplied = true;
                                    btnApply.setEnabled(false);
                                    btnApply.setAlpha(0.5f);
                                    btnApply.setText("✓ Telah Dilamar");
                                    Toast.makeText(JobDetailActivity.this, "❌ Anda sudah melamar lowongan ini sebelumnya", Toast.LENGTH_LONG).show();
                                } else if (statusCode == 400) {
                                    Toast.makeText(JobDetailActivity.this, "❌ Data tidak valid", Toast.LENGTH_LONG).show();
                                } else if (statusCode == 401) {
                                    Toast.makeText(JobDetailActivity.this, "❌ Tidak diizinkan - silakan login ulang", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(JobDetailActivity.this, "❌ Gagal mengirim lamaran (Error: " + statusCode + ")", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            Log.e(TAG, "❌ Network error: " + error.toString());
                            runOnUiThread(() -> {
                                Toast.makeText(JobDetailActivity.this, "❌ Error jaringan - periksa koneksi internet", Toast.LENGTH_LONG).show();
                            });
                        }
                    }
                }
        ) {
            @Override
            public byte[] getBody() {
                try {
                    return body.toString().getBytes("utf-8");
                } catch (Exception e) {
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

    private String getFileNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "Unknown File";
        }
        try {
            String[] parts = url.split("/");
            return parts[parts.length - 1];
        } catch (Exception e) {
            return "CV File";
        }
    }

    private void openWhatsApp() {
        if (currentJob == null) {
            Toast.makeText(this, "❌ Data lowongan tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        String phoneNumber = currentJob.getNoTelp();

        Log.d(TAG, "🎯 OPENING WHATSAPP");
        Log.d(TAG, "Company: " + currentJob.getCompanyName());
        Log.d(TAG, "Job: " + currentJob.getJobTitle());
        Log.d(TAG, "Original Phone: '" + phoneNumber + "'");

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Toast.makeText(this, "❌ Nomor WhatsApp tidak tersedia untuk lowongan ini", Toast.LENGTH_SHORT).show();
            return;
        }

        String cleanNumber = phoneNumber.replaceAll("[^0-9]", "");
        Log.d(TAG, "Cleaned Phone (numeric only): " + cleanNumber);

        if (cleanNumber.isEmpty()) {
            Toast.makeText(this, "❌ Format nomor tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cleanNumber.startsWith("62")) {
            Log.d(TAG, "✅ Phone already in international format (62)");
        } else if (cleanNumber.startsWith("0")) {
            cleanNumber = "62" + cleanNumber.substring(1);
            Log.d(TAG, "🔄 Converted 0 to 62 format: " + cleanNumber);
        } else {
            cleanNumber = "62" + cleanNumber;
            Log.d(TAG, "➕ Added 62 prefix: " + cleanNumber);
        }

        cleanNumber = cleanNumber.replaceAll("[^0-9]", "");

        if (cleanNumber.length() < 12) {
            Toast.makeText(this, "❌ Nomor telepon terlalu pendek", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "❌ Phone number too short: " + cleanNumber);
            return;
        }

        String message = "Halo, saya tertarik dengan lowongan *" + currentJob.getJobTitle() +
                "* di *" + currentJob.getCompanyName() + "*. " +
                "Bisakah saya mendapatkan informasi lebih lanjut?";

        String url = "https://wa.me/" + cleanNumber + "?text=" + Uri.encode(message);

        Log.d(TAG, "🔗 Final WhatsApp URL: " + url);
        Log.d(TAG, "📞 Final Phone Number: " + cleanNumber);
        Log.d(TAG, "💬 Message: " + message);

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            Log.d(TAG, "✅ WhatsApp opened successfully");
        } catch (Exception e) {
            Toast.makeText(this, "❌ Gagal membuka WhatsApp", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "❌ Error opening WhatsApp: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "JobDetailActivity destroyed");
    }
}