package com.tem2.karirku;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {

    private RecyclerView rvChatMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvChatTitle;

    private ChatAdapter chatAdapter;
    private List<Message> messageList;

    // Data dummy untuk chat
    private String currentUserId = "user1";
    private String chatPartnerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        // Initialize views
        initViews();

        // Get chat partner name from intent
        chatPartnerName = getIntent().getStringExtra("name");
        if (chatPartnerName != null) {
            tvChatTitle.setText(chatPartnerName);
        }

        // Setup RecyclerView
        setupRecyclerView();

        // Setup click listeners
        setupClickListeners();

        // Load dummy messages
        loadDummyMessages();
    }

    private void initViews() {
        rvChatMessages = findViewById(R.id.rvChatMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        tvChatTitle = findViewById(R.id.tvChatTitle);
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));
        rvChatMessages.setAdapter(chatAdapter);
    }

    private void setupClickListeners() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void loadDummyMessages() {
        // Clear existing messages
        messageList.clear();

        // Add dummy messages based on chat partner
        if (chatPartnerName != null) {
            switch (chatPartnerName) {
                case "Sultan":
                    addSultanMessages();
                    break;
                case "HRD PT Maju Jaya":
                    addHRDMessages();
                    break;
                case "Admin Edusync":
                    addAdminEdusyncMessages();
                    break;
                default:
                    addDefaultMessages();
                    break;
            }
        } else {
            addDefaultMessages();
        }

        chatAdapter.notifyDataSetChanged();
        // Scroll to bottom
        if (messageList.size() > 0) {
            rvChatMessages.scrollToPosition(messageList.size() - 1);
        }
    }

    private void addSultanMessages() {
        messageList.add(new Message("1", "sultan", "Sultan", "Halo, apakah sudah mengisi form?",
                getTime(12, 25), false));
        messageList.add(new Message("2", "user1", "Anda", "Sudah, saya kirim kemarin",
                getTime(12, 28), true));
        messageList.add(new Message("3", "sultan", "Sultan", "Oke baik, akan saya cek",
                getTime(12, 30), false));
        messageList.add(new Message("4", "user1", "Anda", "Terima kasih banyak",
                getTime(12, 32), true));
    }

    private void addHRDMessages() {
        messageList.add(new Message("1", "hrd", "HRD PT Maju Jaya", "Selamat siang, boleh kirim CV-nya?",
                getTime(11, 10), false));
        messageList.add(new Message("2", "user1", "Anda", "Tentu, ini saya lampirkan CV dan portofolio",
                getTime(11, 15), true));
        messageList.add(new Message("3", "hrd", "HRD PT Maju Jaya", "Terima kasih, akan kami review",
                getTime(11, 20), false));
        messageList.add(new Message("4", "user1", "Anda", "Kira-kira kapan pengumuman selanjutnya?",
                getTime(11, 25), true));
    }

    private void addAdminEdusyncMessages() {
        messageList.add(new Message("1", "admin", "Admin Edusync", "Terima kasih sudah melamar!",
                getTimeYesterday(14, 30), false));
        messageList.add(new Message("2", "user1", "Anda", "Sama-sama, saya tunggu kabar selanjutnya",
                getTimeYesterday(15, 0), true));
        messageList.add(new Message("3", "admin", "Admin Edusync", "Kami akan hubungi dalam 3-5 hari kerja",
                getTimeYesterday(15, 15), false));
    }

    private void addDefaultMessages() {
        messageList.add(new Message("1", "partner", chatPartnerName != null ? chatPartnerName : "User", "Halo, ada yang bisa saya bantu?",
                getTime(10, 0), false));
        messageList.add(new Message("2", "user1", "Anda", "Halo, saya tertarik dengan lowongan ini",
                getTime(10, 5), true));
    }

    private Calendar getTime(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        return cal;
    }

    private Calendar getTimeYesterday(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        return cal;
    }

    private void sendMessage() {
        String messageContent = etMessage.getText().toString().trim();
        if (!messageContent.isEmpty()) {
            // Create new message
            Message newMessage = new Message(
                    String.valueOf(System.currentTimeMillis()),
                    currentUserId,
                    "Anda",
                    messageContent,
                    Calendar.getInstance(),
                    true
            );

            // Add to list and update adapter
            messageList.add(newMessage);
            chatAdapter.notifyItemInserted(messageList.size() - 1);

            // Clear input field
            etMessage.setText("");

            // Scroll to bottom
            rvChatMessages.scrollToPosition(messageList.size() - 1);
        }
    }
}