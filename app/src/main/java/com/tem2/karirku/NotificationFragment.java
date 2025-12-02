package com.tem2.karirku;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotificationFragment extends Fragment {

    private RecyclerView rvNotifications;
    private NotificationAdapter notificationAdapter;
    private List<NotificationItem> notificationList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefresh;
    private SessionManager sessionManager;

    private static final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        Log.d("NOTIFICATION", "========================================");
        Log.d("NOTIFICATION", "📱 NotificationFragment loaded");
        Log.d("NOTIFICATION", "========================================");

        // ✅ Initialize SessionManager
        sessionManager = new SessionManager(requireContext());

        // Check if user logged in
        if (!sessionManager.isLoggedIn()) {
            Log.e("NOTIFICATION", "❌ User not logged in!");
            Toast.makeText(getContext(), "Session expired, please login", Toast.LENGTH_SHORT).show();
            return view;
        }

        Log.d("NOTIFICATION", "✅ User logged in: " + sessionManager.getUserName());
        Log.d("NOTIFICATION", "   User ID: " + sessionManager.getUserId());

        rvNotifications = view.findViewById(R.id.rvNotifications);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        notificationAdapter = new NotificationAdapter(getContext(), notificationList);
        rvNotifications.setAdapter(notificationAdapter);

        TextView tvMarkAllRead = view.findViewById(R.id.tvMarkAllRead);
        tvMarkAllRead.setOnClickListener(v -> {
            markAllAsRead();
        });

        // Pull to refresh
        swipeRefresh.setOnRefreshListener(() -> {
            loadNotifications();
        });

        // Load notifications
        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        // ✅ Get user ID from SessionManager
        int currentUserId = sessionManager.getUserId();

        if (currentUserId == 0) {
            Log.e("NOTIFICATION", "❌ User ID is 0, session invalid");
            Toast.makeText(getContext(), "Session expired, please login again", Toast.LENGTH_SHORT).show();
            swipeRefresh.setRefreshing(false);
            return;
        }

        Log.d("NOTIFICATION", "📥 Loading notifications for user ID: " + currentUserId);

        // URL dengan filter user_id dan order by created
        String url = SUPABASE_URL + "/rest/v1/notifikasi" +
                "?id_pengguna=eq." + currentUserId +
                "&select=*" +
                "&order=dibuat_pada.desc";

        Log.d("NOTIFICATION", "Request URL: " + url);

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("NOTIFICATION", "✅ Response received: " + response.length() + " items");
                    notificationList.clear();
                    parseNotifications(response);
                    notificationAdapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);

                    if (notificationList.isEmpty()) {
                        Toast.makeText(getContext(), "Tidak ada notifikasi", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("NOTIFICATION", "❌ Error loading notifications: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("NOTIFICATION", "Status code: " + error.networkResponse.statusCode);
                    }
                    Toast.makeText(getContext(), "Gagal memuat notifikasi", Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
                }) {
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

    private void parseNotifications(JSONArray response) {
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);

                String id = obj.optString("id_notifikasi");

                // TIDAK ADA KOLOM JUDUL - ambil dari pesan atau buat berdasarkan tipe
                String message = obj.optString("pesan", "");
                String type = obj.optString("tipe", "general");
                boolean isRead = obj.optBoolean("sudah_dibaca", false);  // ganti is_read -> sudah_dibaca
                String createdAt = obj.optString("dibuat_pada", "");

                // Ekstrak judul dari pesan atau buat berdasarkan tipe
                String title = extractTitleFromMessage(message, type);

                // Format waktu
                String timeAgo = formatTimeAgo(createdAt);

                // Icon berdasarkan tipe
                int iconRes = getIconForType(type);

                NotificationItem item = new NotificationItem(
                        id, title, message, timeAgo, type, isRead, iconRes
                );

                notificationList.add(item);

                Log.d("NOTIFICATION", "  - " + title + " (" + timeAgo + ")");
            }

            Log.d("NOTIFICATION", "📊 Parsed " + notificationList.size() + " notifications");

        } catch (Exception e) {
            Log.e("NOTIFICATION", "❌ Parse error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method baru untuk ekstrak judul dari pesan
    private String extractTitleFromMessage(String message, String type) {
        if (message == null || message.isEmpty()) {
            return "Notifikasi";
        }

        switch (type) {
            case "lamaran":
                return "Lowongan Baru";
            case "system":
                // Cek apakah ini notifikasi status lamaran
                if (message.contains("[STATUS LAMARAN]")) {
                    if (message.contains("🎉")) return "Lamaran Diterima";
                    if (message.contains("❌")) return "Lamaran Ditolak";
                    if (message.contains("📝")) return "Status Lamaran Berubah";
                    return "Status Lamaran";
                } else {
                    return "Sistem";
                }
            case "pesan":
                return "Pesan";
            case "interview":
                return "Undangan Interview";
            case "reminder":
                return "Pengingat";
            default:
                // Ambil 3-5 kata pertama sebagai judul
                String[] words = message.split("\\s+");
                if (words.length > 5) {
                    // Menggunakan Arrays.copyOfRange
                    return String.join(" ", Arrays.copyOfRange(words, 0, 5)) + "...";
                } else if (words.length > 0) {
                    return message;
                } else {
                    return "Notifikasi";
                }
        }
    }

    private String formatTimeAgo(String timestamp) {
        try {
            // Parse timestamp from Supabase (format: 2024-11-06T14:30:00)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(timestamp);

            if (date == null) return "Baru saja";

            long diff = System.currentTimeMillis() - date.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (seconds < 60) {
                return "Baru saja";
            } else if (minutes < 60) {
                return minutes + " menit lalu";
            } else if (hours < 24) {
                return hours + " jam lalu";
            } else if (days < 7) {
                return days + " hari lalu";
            } else {
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
                return displayFormat.format(date);
            }
        } catch (Exception e) {
            Log.e("NOTIFICATION", "Date parse error: " + e.getMessage());
            return "Baru saja";
        }
    }

    private int getIconForType(String type) {
        switch (type) {
            case "lamaran":  // Tipe untuk lowongan baru
                return R.drawable.iconloker;
            case "system":   // Tipe untuk status lamaran
                return R.drawable.ic_application_status;
            case "pesan":
                return R.drawable.notification;
            case "interview":
                return R.drawable.ic_calendar;
            case "job_recommendation":
                return R.drawable.ic_job;
            case "reminder":
                return R.drawable.ic_reminder;
            default:
                return R.drawable.notification;
        }
    }

    private void markAllAsRead() {
        // ✅ Get user ID from SessionManager
        int currentUserId = sessionManager.getUserId();

        if (currentUserId == 0) {
            Toast.makeText(getContext(), "Session invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("NOTIFICATION", "📝 Marking all notifications as read for user: " + currentUserId);

        // Call Supabase function - update dengan kolom yang benar
        String url = SUPABASE_URL + "/rest/v1/notifikasi?id_pengguna=eq." + currentUserId;

        JSONObject body = new JSONObject();
        try {
            body.put("sudah_dibaca", true);  // ganti is_read -> sudah_dibaca
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PATCH, url, body,
                response -> {
                    Log.d("NOTIFICATION", "✅ All notifications marked as read");

                    // Update local list
                    for (NotificationItem item : notificationList) {
                        item.setRead(true);
                    }
                    notificationAdapter.notifyDataSetChanged();

                    Toast.makeText(getContext(), "✅ Semua notifikasi ditandai dibaca", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("NOTIFICATION", "❌ Error marking as read: " + error.toString());
                    Toast.makeText(getContext(), "Gagal menandai notifikasi", Toast.LENGTH_SHORT).show();
                }) {
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

        queue.add(request);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh saat fragment visible lagi
        Log.d("NOTIFICATION", "♻️ Fragment resumed, refreshing...");
        loadNotifications();
    }
}