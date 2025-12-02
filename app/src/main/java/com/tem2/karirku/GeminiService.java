package com.tem2.karirku;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;

public class GeminiService {

    private static final String GEMINI_API_KEY = "AIzaSyCeGPTMO3_qxoZ4e-KiMyYooQ9l_rxrh5c";

    // Pakai model terbaru (flash = paling cepat & gratis)
    private static final String MODEL_NAME = "gemini-1.5-flash";

    private GenerativeModelFutures model;
    private final Executor mainExecutor;

    public GeminiService(Context context) {

        // Gunakan model terbaru (WAJIB)
        GenerativeModel generativeModel = new GenerativeModel(
                MODEL_NAME,
                GEMINI_API_KEY
        );

        this.model = GenerativeModelFutures.from(generativeModel);

        // Executor untuk UI thread
        this.mainExecutor = new MainThreadExecutor();
    }

    public void getHRDResponse(String userMessage, GeminiCallback callback) {

        // Sistem prompt untuk HRD Assistant
        String systemPrompt =
                "Anda adalah ASISTEN HRD PROFESIONAL dari platform Karirku.\n" +
                        "Jawab hanya tentang:\n" +
                        "- Lowongan kerja\n" +
                        "- Tips interview\n" +
                        "- CV & resume\n" +
                        "- Skill karier\n" +
                        "- Rekrutmen\n" +
                        "- Dunia kerja\n\n" +
                        "Jika pertanyaan di luar topik, jawab:\n" +
                        "\"Maaf, saya hanya dapat membantu dalam topik HRD dan karier.\"\n\n" +
                        "Pertanyaan user:\n" + userMessage;

        try {
            Content content = new Content.Builder()
                    .addText(systemPrompt)
                    .build();

            ListenableFuture<GenerateContentResponse> response =
                    model.generateContent(content);

            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {

                    String text = result.getText();
                    if (text != null && !text.isEmpty()) {
                        callback.onSuccess(text.trim());
                    } else {
                        callback.onError("AI tidak memberikan jawaban.");
                    }
                }

                @Override
                public void onFailure(Throwable t) {
//                    callback.onError("Error koneksi: " + t.getMessage());
                    callback.onError("Responses AI");
                }

            }, mainExecutor);

        } catch (Exception e) {
            callback.onError("Error internal: " + e.getMessage());
        }
    }

    // Executor agar callback dijalankan di UI thread
    private static class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());
        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }
    }

    // Callback Interface
    public interface GeminiCallback {
        void onSuccess(String response);
        void onError(String error);
    }
}
