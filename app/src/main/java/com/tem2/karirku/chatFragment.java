package com.tem2.karirku;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class chatFragment extends Fragment {

    private RecyclerView rvChatMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvChatTitle;

    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private String currentUserId = "user1";
    private GeminiService geminiService;

    // Untuk menangani typing indicator
    private Message typingMessage;
    private int typingMessagePosition = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_chat_detail, container, false);

        // Initialize Gemini Service dengan context
        geminiService = new GeminiService(getContext());

        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadBotWelcomeMessage();

        showKeyboard();
        return view;
    }

    private void initViews(View view) {
        rvChatMessages = view.findViewById(R.id.rvChatMessages);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        tvChatTitle = view.findViewById(R.id.tvChatTitle);

        // Update title untuk menunjukkan ini adalah AI HRD
        tvChatTitle.setText("HRD Assistant");
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        rvChatMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChatMessages.setAdapter(chatAdapter);

        // Auto scroll ketika keyboard muncul
        rvChatMessages.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    rvChatMessages.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (messageList.size() > 0) {
                                rvChatMessages.scrollToPosition(messageList.size() - 1);
                            }
                        }
                    }, 100);
                }
            }
        });
    }

    private void setupClickListeners() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void showKeyboard() {
        etMessage.postDelayed(new Runnable() {
            @Override
            public void run() {
                etMessage.requestFocus();
                // ✅ PERBAIKAN: Gunakan requireContext() bukan getActivity()
                if (getContext() != null) {
                    InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(etMessage, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            }
        }, 200);
    }

    private void loadBotWelcomeMessage() {
        Message welcomeMessage = new Message(
                "1",
                "bot",
                "HRD Assistant",
                "👋 Halo! Saya **Asisten HRD Karirku**.\n\n" +
                        "Saya siap membantu Anda dengan:\n" +
                        "• 🔍 Pencarian lowongan kerja\n" +
                        "• 💼 Tips wawancara & CV\n" +
                        "• 🚀 Pengembangan karier\n" +
                        "• 💰 Negosiasi gaji\n" +
                        "• 📈 Perencanaan karier\n\n" +
                        "Silakan tanyakan apa saja seputar dunia kerja dan HRD!",
                Calendar.getInstance(),
                false
        );

        messageList.add(welcomeMessage);
        chatAdapter.notifyDataSetChanged();

        if (messageList.size() > 0) {
            rvChatMessages.scrollToPosition(messageList.size() - 1);
        }
    }

    private void sendMessage() {
        String messageContent = etMessage.getText().toString().trim();
        if (!messageContent.isEmpty()) {
            // Buat pesan user
            Message newMessage = new Message(
                    String.valueOf(System.currentTimeMillis()),
                    currentUserId,
                    "Anda",
                    messageContent,
                    Calendar.getInstance(),
                    true
            );

            messageList.add(newMessage);
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            etMessage.setText("");
            rvChatMessages.scrollToPosition(messageList.size() - 1);

            // Jaga keyboard tetap terbuka
            etMessage.requestFocus();
            // ✅ PERBAIKAN: Gunakan requireContext() bukan getActivity()
            if (getContext() != null) {
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etMessage, InputMethodManager.SHOW_IMPLICIT);
                }
            }

            // Dapatkan respons dari Gemini AI
            getGeminiResponse(messageContent);
        }
    }

    private void getGeminiResponse(String userMessage) {
        // Tampilkan typing indicator
        showTypingIndicator();

        // Panggil Gemini API
        geminiService.getHRDResponse(userMessage, new GeminiService.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                // Sembunyikan typing indicator
                hideTypingIndicator();

                // Tambahkan respons AI
                Message botMessage = new Message(
                        String.valueOf(System.currentTimeMillis()),
                        "bot",
                        "HRD Assistant",
                        response,
                        Calendar.getInstance(),
                        false
                );

                messageList.add(botMessage);
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                rvChatMessages.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onError(String error) {
                // Sembunyikan typing indicator
                hideTypingIndicator();

                // Tampilkan pesan error yang user-friendly
                Message errorMessage = new Message(
                        String.valueOf(System.currentTimeMillis()),
                        "bot",
                        "HRD Assistant",
                        "⚠️ Maaf, saat ini saya sedang mengalami gangguan.\n\n" +
                                "Silakan coba lagi dalam beberapa saat atau gunakan fitur lain di aplikasi Karirku.",
                        Calendar.getInstance(),
                        false
                );

                messageList.add(errorMessage);
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                rvChatMessages.scrollToPosition(messageList.size() - 1);

                // ✅ PERBAIKAN: Tambah null check untuk Toast
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showTypingIndicator() {
        // Hapus typing indicator sebelumnya jika ada
        hideTypingIndicator();

        typingMessage = new Message(
                "typing",
                "bot",
                "HRD Assistant",
                "HRD Assistant sedang mengetik...",
                Calendar.getInstance(),
                false
        );

        messageList.add(typingMessage);
        typingMessagePosition = messageList.size() - 1;
        chatAdapter.notifyItemInserted(typingMessagePosition);
        rvChatMessages.scrollToPosition(typingMessagePosition);
    }

    private void hideTypingIndicator() {
        if (typingMessage != null && typingMessagePosition != -1) {
            messageList.remove(typingMessagePosition);
            chatAdapter.notifyItemRemoved(typingMessagePosition);
            typingMessage = null;
            typingMessagePosition = -1;
        }
    }
}