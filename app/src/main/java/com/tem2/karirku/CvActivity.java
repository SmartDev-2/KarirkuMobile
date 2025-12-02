package com.tem2.karirku;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.android.volley.DefaultRetryPolicy;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CvActivity extends AppCompatActivity {

    ImageView btnBack;
    RecyclerView recyclerRiwayatCV;
    ProgressBar progressBar;
    ArrayList<CvModel> list = new ArrayList<>();
    CvAdapter adapter;

    final int PICK_CV = 101;
    private int currentUserId;
    private SessionManager sessionManager;
    private Uri selectedFileUri;
    private String selectedFileName;

    private static final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";
    private static final String BUCKET_NAME = "cv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cv);

        // Initialize session manager
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        // Check if logged in
        if (!sessionManager.isLoggedIn() || currentUserId == 0) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadRiwayatCV();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        recyclerRiwayatCV = findViewById(R.id.recyclerRiwayatCV);
        progressBar = findViewById(R.id.progressBar);

        btnBack.setOnClickListener(v -> finish());

        recyclerRiwayatCV.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CvAdapter(this, list);
        recyclerRiwayatCV.setAdapter(adapter);

        findViewById(R.id.btnUploadCV).setOnClickListener(v -> pilihFile());
    }

    private void pilihFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"application/pdf", "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(intent, PICK_CV);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CV && resultCode == Activity.RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            selectedFileName = getFileName(selectedFileUri);

            Log.d("CV_UPLOAD", "📄 File selected: " + selectedFileName);
            Log.d("CV_UPLOAD", "📄 File URI: " + selectedFileUri.toString());

            // Upload CV ke Supabase Storage
            uploadCVToStorage();
        }
    }

    private String getFileName(Uri uri) {
        String result = "cv_file";
        try {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    result = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("CV_UPLOAD", "Error getting filename: " + e.getMessage());
        }
        return result;
    }

    private void uploadCVToStorage() {
        if (selectedFileUri == null || selectedFileName == null) {
            Toast.makeText(this, "File tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        Toast.makeText(this, "Mulai upload CV...", Toast.LENGTH_SHORT).show();

        // Upload in background thread
        new Thread(() -> {
            try {
                // Generate unique filename
                String timestamp = String.valueOf(System.currentTimeMillis());
                String uniqueFileName = currentUserId + "_" + timestamp + "_" + selectedFileName.replace(" ", "_");

                Log.d("CV_UPLOAD", "📤 Uploading file: " + uniqueFileName);

                // Get file from URI
                File file = getFileFromUri(selectedFileUri);
                if (file == null) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(this, "Gagal membaca file", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                Log.d("CV_UPLOAD", "📁 File size: " + file.length() + " bytes");

                // Upload to Supabase Storage
                String cvUrl = uploadFileToSupabaseStorage(file, uniqueFileName);

                if (cvUrl != null) {
                    Log.d("CV_UPLOAD", "✅ File uploaded to Storage: " + cvUrl);

                    // Save to database
                    runOnUiThread(() -> saveCvToDatabase(selectedFileName, cvUrl));
                } else {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(this, "Gagal upload file ke storage", Toast.LENGTH_SHORT).show();
                    });
                }

            } catch (Exception e) {
                Log.e("CV_UPLOAD", "❌ Upload error: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private File getFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Log.e("CV_UPLOAD", "❌ Cannot open input stream from URI");
                return null;
            }

            // Create temp file in cache
            File tempFile = new File(getCacheDir(), "temp_cv_" + System.currentTimeMillis());
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            Log.d("CV_UPLOAD", "📁 Temp file created: " + tempFile.getAbsolutePath() + " (" + tempFile.length() + " bytes)");

            return tempFile;

        } catch (Exception e) {
            Log.e("CV_UPLOAD", "❌ Error reading file: " + e.getMessage(), e);
            return null;
        }
    }

    private String uploadFileToSupabaseStorage(File file, String fileName) {
        try {
            // Determine MIME type based on file extension
            String mimeType = "application/octet-stream"; // Default MIME type
            if (fileName.toLowerCase().endsWith(".pdf")) {
                mimeType = "application/pdf";
            } else if (fileName.toLowerCase().endsWith(".doc")) {
                mimeType = "application/msword";
            } else if (fileName.toLowerCase().endsWith(".docx")) {
                mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }

            Log.d("CV_UPLOAD", "📤 Uploading to storage: " + fileName + " (" + mimeType + ")");

            // Create request body
            RequestBody fileBody = RequestBody.create(
                    MediaType.parse(mimeType),
                    file
            );

            // Upload URL
            String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + fileName;

            Log.d("CV_UPLOAD", "🔗 Upload URL: " + uploadUrl);

            // Create request
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(uploadUrl)
                    .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                    .header("apikey", SUPABASE_API_KEY)
                    .post(fileBody)
                    .build();

            // Execute request
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();

            Log.d("CV_UPLOAD", "📨 Storage Response Code: " + response.code());
            Log.d("CV_UPLOAD", "📨 Storage Response Message: " + response.message());

            if (response.isSuccessful()) {
                // Return public URL
                String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + fileName;
                Log.d("CV_UPLOAD", "✅ File uploaded successfully: " + publicUrl);
                return publicUrl;
            } else {
                String error = response.body() != null ? response.body().string() : "Unknown error";
                Log.e("CV_UPLOAD", "❌ Storage upload failed: " + error);
                Log.e("CV_UPLOAD", "❌ Response Code: " + response.code());

                // Check specific error codes
                if (response.code() == 400) {
                    Log.e("CV_UPLOAD", "❌ Bad Request - Check bucket configuration");
                } else if (response.code() == 401) {
                    Log.e("CV_UPLOAD", "❌ Unauthorized - Check API key");
                } else if (response.code() == 403) {
                    Log.e("CV_UPLOAD", "❌ Forbidden - Check bucket permissions");
                } else if (response.code() == 413) {
                    Log.e("CV_UPLOAD", "❌ File too large");
                }
                return null;
            }

        } catch (Exception e) {
            Log.e("CV_UPLOAD", "❌ Upload exception: " + e.getMessage(), e);
            return null;
        }
    }

    private void saveCvToDatabase(String fileName, String cvUrl) {
        new Thread(() -> {
            try {
                String url = SUPABASE_URL + "/rest/v1/cv";

                JSONObject cvData = new JSONObject();
                cvData.put("id_pencaker", currentUserId);
                cvData.put("id_pengguna", currentUserId);
                cvData.put("nama_file", fileName);
                cvData.put("cv_url", cvUrl);

                Log.d("CV_UPLOAD", "💾 Saving to database: " + cvData.toString());

                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json"),
                        cvData.toString()
                );

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(url)
                        .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                        .header("apikey", SUPABASE_API_KEY)
                        .header("Content-Type", "application/json")
                        .header("Prefer", "return=minimal")
                        .post(body)
                        .build();

                OkHttpClient client = new OkHttpClient();
                Response response = client.newCall(request).execute();

                Log.d("CV_UPLOAD", "📨 Database Response Code: " + response.code());
                Log.d("CV_UPLOAD", "📨 Database Response Message: " + response.message());

                // OkHttp handle 201 dengan benar
                if (response.isSuccessful()) {
                    Log.d("CV_UPLOAD", "✅ CV saved to database successfully!");
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(CvActivity.this, "✅ CV berhasil diupload!", Toast.LENGTH_LONG).show();

                        selectedFileUri = null;
                        selectedFileName = null;
                        new android.os.Handler().postDelayed(() -> loadRiwayatCV(), 1000);
                    });
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e("CV_UPLOAD", "❌ Database save failed: " + errorBody);
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(CvActivity.this, "❌ Gagal menyimpan CV ke database", Toast.LENGTH_LONG).show();
                    });
                }

            } catch (Exception e) {
                Log.e("CV_UPLOAD", "❌ Database save exception: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(CvActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    // Method terpisah untuk handle success
    private void handleUploadSuccess() {
        runOnUiThread(() -> {
            showLoading(false);
            Toast.makeText(CvActivity.this, "✅ CV berhasil diupload!", Toast.LENGTH_LONG).show();

            // Clear selection
            selectedFileUri = null;
            selectedFileName = null;

            // Reload list CV
            new android.os.Handler().postDelayed(() -> loadRiwayatCV(), 1000);
        });
    }

    private void loadRiwayatCV() {
        showLoading(true);

        String url = SUPABASE_URL + "/rest/v1/cv" +
                "?or=(id_pencaker.eq." + currentUserId + ",id_pengguna.eq." + currentUserId + ")" +
                "&select=*" +
                "&order=uploaded_at.desc";

        Log.d("CV_LOAD", "🔍 Loading CV from: " + url);

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    list.clear();
                    parseCvData(response);
                    adapter.notifyDataSetChanged();
                    showLoading(false);
                    Log.d("CV_LOAD", "✅ Loaded " + list.size() + " CVs for user " + currentUserId);
                },
                error -> {
                    Log.e("CV_LOAD", "❌ Load failed: " + error.toString());
                    showLoading(false);
                    Toast.makeText(CvActivity.this, "Gagal memuat riwayat CV", Toast.LENGTH_SHORT).show();
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

    private void parseCvData(JSONArray response) {
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);

                String fileName = obj.optString("nama_file");
                String filePath = obj.optString("cv_url");

                CvModel cv = new CvModel(fileName, filePath);
                list.add(cv);

                Log.d("CV_PARSE", "📄 CV: " + fileName + " -> " + filePath);
            }
        } catch (JSONException e) {
            Log.e("CV_PARSE", "❌ Parse error: " + e.getMessage());
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        findViewById(R.id.btnUploadCV).setEnabled(!show);
    }
}