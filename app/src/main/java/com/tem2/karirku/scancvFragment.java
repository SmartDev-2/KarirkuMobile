package com.tem2.karirku;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class scancvFragment extends Fragment {

    private static final String TAG = "SCAN_CV_DEBUG";
    private static final int PICK_PDF_REQUEST = 1001;
    private static final int CAMERA_REQUEST = 1002;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private ImageView btnUploadPDF;
    private Button btnCamera;
    private TextRecognizer textRecognizer;
    private Handler timeoutHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scancv, container, false);

        Log.d(TAG, "========================================");
        Log.d(TAG, "📱 scancvFragment onCreate");
        Log.d(TAG, "========================================");

        // Initialize PDFBox
        try {
            PDFBoxResourceLoader.init(requireContext());
            Log.d(TAG, "✅ PDFBox initialized");
        } catch (Exception e) {
            Log.e(TAG, "❌ PDFBox init failed: " + e.getMessage());
        }

        // Initialize ML Kit Text Recognition
        try {
            textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            Log.d(TAG, "✅ TextRecognizer initialized SUCCESS");
        } catch (Exception e) {
            Log.e(TAG, "❌ TextRecognizer initialization FAILED: " + e.getMessage());
            e.printStackTrace();
        }

        // Initialize views
        btnUploadPDF = view.findViewById(R.id.btnUploadPDF);
        btnCamera = view.findViewById(R.id.btnCamera);

        if (btnUploadPDF == null) {
            Log.e(TAG, "❌ btnUploadPDF is NULL! Check XML id");
        } else {
            Log.d(TAG, "✅ btnUploadPDF found");
        }

        if (btnCamera == null) {
            Log.e(TAG, "❌ btnCamera is NULL! Check XML id");
        } else {
            Log.d(TAG, "✅ btnCamera found");
        }

        // Set click listeners
        if (btnUploadPDF != null) {
            btnUploadPDF.setOnClickListener(v -> {
                Log.d(TAG, "📄 Upload PDF clicked");
                openFileChooser();
            });
        }

        if (btnCamera != null) {
            btnCamera.setOnClickListener(v -> {
                Log.d(TAG, "📷 Camera button clicked");
                openCamera();
            });
        }

        timeoutHandler = new Handler(Looper.getMainLooper());

        return view;
    }

    private void openCamera() {
        Log.d(TAG, "========================================");
        Log.d(TAG, "📸 openCamera() START");
        Log.d(TAG, "========================================");

        // Check camera permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "⚠️ Camera permission NOT granted - requesting...");
            requestPermissions(
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        } else {
            Log.d(TAG, "✅ Camera permission already GRANTED");
            launchCamera();
        }
    }

    private void launchCamera() {
        Log.d(TAG, "🎬 Launching camera intent...");
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            Log.d(TAG, "✅ Camera app available - starting...");
            Toast.makeText(getContext(), "📸 Membuka kamera...", Toast.LENGTH_SHORT).show();
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        } else {
            Log.e(TAG, "❌ No camera app available");
            Toast.makeText(getContext(), "❌ Kamera tidak tersedia di device ini", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "========================================");
        Log.d(TAG, "📋 Permission Result");
        Log.d(TAG, "Request Code: " + requestCode);
        Log.d(TAG, "Results: " + (grantResults.length > 0 ? grantResults[0] : "empty"));
        Log.d(TAG, "========================================");

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "✅ Permission GRANTED by user");
                launchCamera();
            } else {
                Log.w(TAG, "❌ Permission DENIED by user");
                Toast.makeText(getContext(), "❌ Izin kamera diperlukan untuk scan CV", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openFileChooser() {
        Log.d(TAG, "📂 Opening file chooser for PDF");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Pilih CV (PDF)"), PICK_PDF_REQUEST);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error opening file chooser: " + e.getMessage());
            Toast.makeText(getContext(), "❌ Tidak bisa membuka file picker", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "========================================");
        Log.d(TAG, "📬 onActivityResult CALLED");
        Log.d(TAG, "Request Code: " + requestCode);
        Log.d(TAG, "Result Code: " + resultCode + " (RESULT_OK=" + Activity.RESULT_OK + ")");
        Log.d(TAG, "Data: " + (data != null ? "NOT NULL" : "NULL"));
        Log.d(TAG, "========================================");

        if (resultCode != Activity.RESULT_OK) {
            Log.w(TAG, "⚠️ Result code is not OK - user cancelled?");
            Toast.makeText(getContext(), "Batal", Toast.LENGTH_SHORT).show();
            return;
        }

        if (data == null) {
            Log.e(TAG, "❌ Intent data is NULL");
            Toast.makeText(getContext(), "❌ Tidak ada data dari camera/file picker", Toast.LENGTH_SHORT).show();
            return;
        }

        // Handle PDF upload
        if (requestCode == PICK_PDF_REQUEST) {
            Log.d(TAG, "📄 Handling PDF upload");
            Uri pdfUri = data.getData();

            if (pdfUri != null) {
                Log.d(TAG, "PDF URI: " + pdfUri);
                handlePdfFile(pdfUri);
            } else {
                Log.e(TAG, "❌ PDF URI is null");
                Toast.makeText(getContext(), "❌ Gagal membaca file PDF", Toast.LENGTH_SHORT).show();
            }
        }
        // Handle camera result
        else if (requestCode == CAMERA_REQUEST) {
            Log.d(TAG, "📸 Handling camera result");

            Bundle extras = data.getExtras();
            if (extras == null) {
                Log.e(TAG, "❌ Extras bundle is NULL");
                Toast.makeText(getContext(), "❌ Gagal mengambil foto. Coba lagi.", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "📦 Extras keys: " + extras.keySet());

            Object dataObj = extras.get("data");
            if (dataObj == null) {
                Log.e(TAG, "❌ 'data' key in extras is NULL");
                Toast.makeText(getContext(), "❌ Foto tidak tersedia. Coba lagi.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!(dataObj instanceof Bitmap)) {
                Log.e(TAG, "❌ 'data' is not a Bitmap, type: " + dataObj.getClass().getName());
                Toast.makeText(getContext(), "❌ Format gambar tidak valid", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap imageBitmap = (Bitmap) dataObj;
            Log.d(TAG, "✅ Bitmap received!");
            Log.d(TAG, "   Size: " + imageBitmap.getWidth() + "x" + imageBitmap.getHeight());
            Log.d(TAG, "   Config: " + imageBitmap.getConfig());
            Log.d(TAG, "   ByteCount: " + imageBitmap.getByteCount());

            processCameraImage(imageBitmap);
        }
    }

    private void processCameraImage(Bitmap bitmap) {
        Log.d(TAG, "========================================");
        Log.d(TAG, "🔄 processCameraImage() START");
        Log.d(TAG, "========================================");

        if (bitmap == null) {
            Log.e(TAG, "❌ Bitmap is NULL");
            Toast.makeText(getContext(), "❌ Gambar tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "🔍 Memproses gambar CV...", Toast.LENGTH_SHORT).show();

        // Reinitialize if needed
        if (textRecognizer == null) {
            Log.e(TAG, "❌ TextRecognizer is NULL! Reinitializing...");
            try {
                textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                Log.d(TAG, "✅ TextRecognizer reinitialized");
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to reinitialize: " + e.getMessage());
                Toast.makeText(getContext(), "❌ OCR engine error. Restart app.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        try {
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            Log.d(TAG, "✅ InputImage created");

            // Set timeout 30 detik
            timeoutHandler.postDelayed(() -> {
                Log.e(TAG, "⏱️ OCR TIMEOUT after 30 seconds");
                Toast.makeText(getContext(), "⏱️ OCR timeout. Coba:\n• Foto lebih jelas\n• Gunakan Upload PDF", Toast.LENGTH_LONG).show();
            }, 30000);

            Log.d(TAG, "🚀 Starting OCR process...");

            textRecognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        timeoutHandler.removeCallbacksAndMessages(null); // Cancel timeout

                        String recognizedText = visionText.getText();

                        Log.d(TAG, "========================================");
                        Log.d(TAG, "✅✅✅ OCR SUCCESS! ✅✅✅");
                        Log.d(TAG, "========================================");
                        Log.d(TAG, "📝 Text length: " + recognizedText.length());

                        if (recognizedText.length() > 0) {
                            Log.d(TAG, "📝 First 500 chars:");
                            Log.d(TAG, recognizedText.substring(0, Math.min(500, recognizedText.length())));
                            Log.d(TAG, "========================================");
                        }

                        if (recognizedText.isEmpty()) {
                            Log.w(TAG, "⚠️ Text is EMPTY");
                            Toast.makeText(getContext(),
                                    "❌ Tidak ada text terdeteksi\n\n" +
                                            "💡 Tips:\n" +
                                            "• Pastikan foto jelas (tidak blur)\n" +
                                            "• Gunakan pencahayaan yang baik\n" +
                                            "• Foto dari jarak dekat\n" +
                                            "• Text harus kontras dengan background\n\n" +
                                            "Atau gunakan Upload PDF",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "✅ Text terdeteksi! Mencari keyword...", Toast.LENGTH_SHORT).show();
                            extractKeywordsFromText(recognizedText, "kamera");
                        }
                    })
                    .addOnFailureListener(e -> {
                        timeoutHandler.removeCallbacksAndMessages(null); // Cancel timeout

                        Log.e(TAG, "========================================");
                        Log.e(TAG, "❌❌❌ OCR FAILED! ❌❌❌");
                        Log.e(TAG, "========================================");
                        Log.e(TAG, "Error class: " + e.getClass().getName());
                        Log.e(TAG, "Error message: " + e.getMessage());
                        e.printStackTrace();

                        Toast.makeText(getContext(),
                                "❌ Gagal memproses gambar\n\n" +
                                        "Error: " + e.getMessage() + "\n\n" +
                                        "Solusi:\n" +
                                        "• Restart app\n" +
                                        "• Update Google Play Services\n" +
                                        "• Gunakan Upload PDF",
                                Toast.LENGTH_LONG).show();
                    })
                    .addOnCompleteListener(task -> {
                        Log.d(TAG, "🏁 OCR task COMPLETED. Success: " + task.isSuccessful());
                    });

        } catch (Exception e) {
            Log.e(TAG, "========================================");
            Log.e(TAG, "❌ EXCEPTION in processCameraImage");
            Log.e(TAG, "========================================");
            Log.e(TAG, "Exception: " + e.getClass().getName());
            Log.e(TAG, "Message: " + e.getMessage());
            e.printStackTrace();

            Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handlePdfFile(Uri pdfUri) {
        InputStream inputStream = null;
        PDDocument document = null;

        try {
            String fileName = getFileName(pdfUri);
            Log.d(TAG, "📄 Processing PDF: " + fileName);
            Toast.makeText(getContext(), "📄 Memproses: " + fileName, Toast.LENGTH_SHORT).show();

            inputStream = requireContext().getContentResolver().openInputStream(pdfUri);

            if (inputStream == null) {
                throw new Exception("Cannot open input stream");
            }

            document = PDDocument.load(inputStream);
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);

            Log.d(TAG, "✅ PDF parsed. Text length: " + text.length());

            if (text.isEmpty()) {
                Toast.makeText(getContext(), "❌ PDF tidak mengandung text", Toast.LENGTH_SHORT).show();
                return;
            }

            extractKeywordsFromText(text, "PDF");

        } catch (Exception e) {
            Log.e(TAG, "❌ PDF parse error: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(getContext(), "❌ Gagal membaca PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            // Clean up resources
            try {
                if (document != null) {
                    document.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error closing resources: " + e.getMessage());
            }
        }
    }

    private void extractKeywordsFromText(String text, String source) {
        Log.d(TAG, "========================================");
        Log.d(TAG, "🔍 Extracting keywords from " + source);
        Log.d(TAG, "========================================");

        String[] keywords = {
                "teknologi", "technology", "software", "developer", "programmer", "IT", "coding",
                "engineer", "java", "python", "web", "mobile", "android", "ios", "react", "vue",
                "desain", "design", "UI", "UX", "graphic", "grafis",
                "keuangan", "finance", "accounting", "akuntan", "akuntansi",
                "perbankan", "bank", "banking",
                "produksi", "production", "manufaktur", "manufacturing",
                "administrasi", "admin", "administrative",
                "teknik", "engineering", "engineer",
                "pertanian", "agriculture", "agrikultur",
                "pendidikan", "education", "guru", "teacher", "pengajar"
        };

        List<String> matchedKeywords = new ArrayList<>();
        String textLower = text.toLowerCase(Locale.ROOT);

        for (String keyword : keywords) {
            if (textLower.contains(keyword.toLowerCase(Locale.ROOT))) {
                if (!matchedKeywords.contains(keyword)) {
                    matchedKeywords.add(keyword);
                    Log.d(TAG, "✅ Keyword match: " + keyword);
                }
            }
        }

        Log.d(TAG, "📊 Total matched: " + matchedKeywords.size());

        if (matchedKeywords.isEmpty()) {
            Log.w(TAG, "⚠️ No keywords matched");
            Toast.makeText(getContext(),
                    "❌ Tidak ada kata kunci cocok dari " + source + "\n\n" +
                            "Kata kunci yang dicari:\n" +
                            "Teknologi, Desain, Keuangan, Perbankan,\n" +
                            "Produksi, Admin, Teknik, Pertanian, Pendidikan\n\n" +
                            "Cek Home untuk semua lowongan",
                    Toast.LENGTH_LONG).show();

            // Clear keywords
            if (CVKeywordManager.getInstance() != null) {
                CVKeywordManager.getInstance().clearKeywords();
            }
        } else {
            // Save keywords
            if (CVKeywordManager.getInstance() != null) {
                CVKeywordManager.getInstance().setKeywords(matchedKeywords);
            }

            String keywordText = String.join(", ", matchedKeywords);
            Log.d(TAG, "✅ Keywords saved: " + keywordText);

            Toast.makeText(getContext(),
                    "✅ CV berhasil dipindai!\n\n" +
                            "🎯 Keyword: " + keywordText + "\n\n" +
                            "👉 Klik tab 'Home' untuk lowongan cocok",
                    Toast.LENGTH_LONG).show();
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        android.database.Cursor cursor = null;

        try {
            cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    result = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting filename: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (result == null) {
            result = uri.getLastPathSegment();
        }

        return result != null ? result : "unknown.pdf";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Clean up handler
        if (timeoutHandler != null) {
            timeoutHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Close text recognizer
        if (textRecognizer != null) {
            textRecognizer.close();
            Log.d(TAG, "📚 TextRecognizer closed");
        }
    }
}