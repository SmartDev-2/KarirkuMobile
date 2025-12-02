package com.tem2.karirku;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PdfViewerActivity extends AppCompatActivity {

    ImageView pdfImage;
    PdfRenderer renderer;
    PdfRenderer.Page page;
    private File pdfFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        pdfImage = findViewById(R.id.pdfImage);

        String path = getIntent().getStringExtra("path");

        if (path != null) {
            if (path.startsWith("http")) {
                // Download dari URL Supabase
                downloadAndDisplayPdf(path);
            } else {
                // File lokal
                displayLocalPdf(path);
            }
        } else {
            Toast.makeText(this, "Path tidak valid", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void downloadAndDisplayPdf(String url) {
        // Show loading indicator (you can add ProgressDialog here)
        Toast.makeText(this, "Downloading PDF...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                Log.d("PDF_VIEWER", "📥 Downloading from: " + url);

                // Download PDF menggunakan OkHttp
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    // Save to cache directory
                    String fileName = "temp_cv_" + System.currentTimeMillis() + ".pdf";
                    pdfFile = new File(getCacheDir(), fileName);

                    FileOutputStream fos = new FileOutputStream(pdfFile);
                    InputStream inputStream = response.body().byteStream();

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }

                    fos.close();
                    inputStream.close();

                    Log.d("PDF_VIEWER", "✅ PDF downloaded: " + pdfFile.getAbsolutePath());

                    // Display PDF di UI thread
                    runOnUiThread(() -> displayPdf(pdfFile));

                } else {
                    String error = "HTTP " + response.code();
                    Log.e("PDF_VIEWER", "❌ Download failed: " + error);

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Gagal download PDF: " + error, Toast.LENGTH_LONG).show();
                        finish();
                    });
                }

            } catch (Exception e) {
                Log.e("PDF_VIEWER", "❌ Download error: " + e.getMessage(), e);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        }).start();
    }

    private void displayLocalPdf(String path) {
        File file = new File(path);
        if (file.exists()) {
            pdfFile = file;
            displayPdf(file);
        } else {
            Toast.makeText(this, "File tidak ditemukan: " + path, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayPdf(File file) {
        try {
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(
                    file,
                    ParcelFileDescriptor.MODE_READ_ONLY
            );

            renderer = new PdfRenderer(fd);

            // Render halaman pertama
            page = renderer.openPage(0);

            // Create bitmap dengan resolusi tinggi
            int width = page.getWidth() * 2;  // 2x resolution
            int height = page.getHeight() * 2;

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            // Render page ke bitmap
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            // Tampilkan di ImageView
            pdfImage.setImageBitmap(bitmap);

            Log.d("PDF_VIEWER", "✅ PDF displayed: " + renderer.getPageCount() + " pages");

            if (renderer.getPageCount() > 1) {
                Toast.makeText(this, "Menampilkan halaman 1 dari " + renderer.getPageCount(), Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e("PDF_VIEWER", "❌ Error displaying PDF: " + e.getMessage(), e);
            Toast.makeText(this, "Gagal membuka PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cleanup resources
        try {
            if (page != null) {
                page.close();
            }
            if (renderer != null) {
                renderer.close();
            }

            // Hapus file temporary
            if (pdfFile != null && pdfFile.exists() && pdfFile.getName().startsWith("temp_cv_")) {
                boolean deleted = pdfFile.delete();
                Log.d("PDF_VIEWER", "🗑️ Temp file deleted: " + deleted);
            }
        } catch (Exception e) {
            Log.e("PDF_VIEWER", "Error cleanup: " + e.getMessage());
        }
    }
}