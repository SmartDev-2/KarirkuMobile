package com.tem2.karirku;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerJobs;
    private JobAdapter jobAdapter;
    private List<Job> jobList = new ArrayList<>();
    private List<Job> filteredJobList = new ArrayList<>();
    private EditText searchEditText;
    private ImageView imgProfile;

    private TextView tabSemua, tabTerbaru, tabTerlama;
    private String currentSearchQuery = "";

    private LinearLayout searchHistoryContainer;
    private RecyclerView recyclerSearchHistory;
    private SearchHistoryAdapter searchHistoryAdapter;
    private List<String> searchHistoryList = new ArrayList<>();
    private SessionManager sessionManager;
    private RequestQueue requestQueue;

    // PERBAIKAN: URL mengambil data dari perusahaan termasuk nama_perusahaan dan logo
    private static final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co/rest/v1/lowongan?select=*,perusahaan(nama_perusahaan,logo_url,logo_path,id_perusahaan)";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        sessionManager = new SessionManager(requireContext());
        requestQueue = Volley.newRequestQueue(requireContext());

        int userId = sessionManager.getUserId();
        Log.d("HOME_FRAGMENT", "========================================");
        Log.d("HOME_FRAGMENT", "👤 User ID: " + userId);
        Log.d("HOME_FRAGMENT", "👤 User Name: " + sessionManager.getUserName());
        Log.d("HOME_FRAGMENT", "👤 User Email: " + sessionManager.getUserEmail());
        Log.d("HOME_FRAGMENT", "========================================");

        recyclerJobs = view.findViewById(R.id.recyclerJobs);
        recyclerJobs.setLayoutManager(new LinearLayoutManager(getContext()));

        searchEditText = view.findViewById(R.id.searchEditText);
        tabSemua = view.findViewById(R.id.tab_semua);
        tabTerbaru = view.findViewById(R.id.tab_terbaru);
        tabTerlama = view.findViewById(R.id.tab_terlama);
        imgProfile = view.findViewById(R.id.imgprofile);

        searchHistoryContainer = view.findViewById(R.id.searchHistoryContainer);
        recyclerSearchHistory = view.findViewById(R.id.recyclerSearchHistory);
        setupSearchHistoryRecycler();

        jobAdapter = new JobAdapter(getContext(), filteredJobList);
        recyclerJobs.setAdapter(jobAdapter);

        ImageView imgNotif = view.findViewById(R.id.imgnotif);
        imgNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotificationFragment();
            }
        });

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUserDataActivity();
            }
        });

        loadLowonganFromAPI();
        setupSearchListener();
        loadProfileImage();
        loadSearchHistory();

        View.OnClickListener tabClickListener = v -> {
            resetTabs();
            ((TextView) v).setBackgroundResource(R.drawable.selected);
            ((TextView) v).setTextColor(getResources().getColor(android.R.color.white));

            if (v.getId() == R.id.tab_semua) {
                applyFilterAndSort(false, false);
            } else if (v.getId() == R.id.tab_terbaru) {
                applyFilterAndSort(true, false);
            } else if (v.getId() == R.id.tab_terlama) {
                applyFilterAndSort(true, true);
            }
        };

        tabSemua.setOnClickListener(tabClickListener);
        tabTerbaru.setOnClickListener(tabClickListener);
        tabTerlama.setOnClickListener(tabClickListener);

        return view;
    }

    private void loadProfileImage() {
        int userId = sessionManager.getUserId();
        Log.d("PROFILE_IMAGE", "Load profile image for user ID: " + userId);

        String url = "https://tkjnbelcgfwpbhppsnrl.supabase.co/rest/v1/pencaker?id_pengguna=eq." + userId + "&select=foto_profil_url";

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            JSONObject pencaker = response.getJSONObject(0);
                            String fotoUrl = pencaker.optString("foto_profil_url", "");

                            if (!fotoUrl.isEmpty()) {
                                Log.d("PROFILE_IMAGE", "✅ Load image from pencaker: " + fotoUrl);

                                Glide.with(requireContext())
                                        .load(fotoUrl)
                                        .circleCrop()
                                        .error(R.drawable.ic_profile_placeholder)
                                        .into(imgProfile);
                            } else {
                                Log.d("PROFILE_IMAGE", "ℹ️ No profile image in pencaker table");
                                setDefaultProfileImage();
                            }
                        } else {
                            Log.d("PROFILE_IMAGE", "ℹ️ No data found in pencaker table");
                            setDefaultProfileImage();
                        }
                    } catch (Exception e) {
                        Log.e("PROFILE_IMAGE", "❌ Error parsing pencaker data: " + e.getMessage());
                        setDefaultProfileImage();
                    }
                },
                error -> {
                    Log.e("PROFILE_IMAGE", "❌ Volley error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("PROFILE_IMAGE", "Error status: " + error.networkResponse.statusCode);
                        Log.e("PROFILE_IMAGE", "Error response: " + new String(error.networkResponse.data));
                    }
                    setDefaultProfileImage();
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

    private void setDefaultProfileImage() {
        imgProfile.setImageResource(R.drawable.ic_profile_placeholder);
    }

    private void openUserDataActivity() {
        Intent intent = new Intent(getActivity(), UserDataActivity.class);
        startActivity(intent);
    }

    private void setupSearchHistoryRecycler() {
        recyclerSearchHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        searchHistoryAdapter = new SearchHistoryAdapter(searchHistoryList, new SearchHistoryAdapter.OnHistoryClickListener() {
            @Override
            public void onHistoryClick(String keyword) {
                searchEditText.setText(keyword);
                searchEditText.setSelection(keyword.length());
                hideSearchHistory();
                performSearch(keyword);
            }

            @Override
            public void onHistoryDelete(String keyword, int position) {
                deleteSearchHistory(keyword, position);
            }
        });
        recyclerSearchHistory.setAdapter(searchHistoryAdapter);
    }

    private void showSearchHistory() {
        if (!searchHistoryList.isEmpty()) {
            searchHistoryContainer.setVisibility(View.VISIBLE);
        }
    }

    private void hideSearchHistory() {
        searchHistoryContainer.setVisibility(View.GONE);
    }

    private void loadSearchHistory() {
        int userId = sessionManager.getUserId();

        if (userId == 0) {
            Log.e("SEARCH_HISTORY", "❌ User ID is 0! Cannot load history.");
            return;
        }

        String url = "https://tkjnbelcgfwpbhppsnrl.supabase.co/rest/v1/riwayat_pencarian?id_pengguna=eq." + userId
                + "&select=keyword&order=dibuat_pada.desc&limit=3";

        Log.d("SEARCH_HISTORY", "📥 Loading history for user ID: " + userId);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("SEARCH_HISTORY", "✅ Response received: " + response.toString());
                    searchHistoryList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject item = response.getJSONObject(i);
                            String keyword = item.getString("keyword");
                            searchHistoryList.add(keyword);
                            Log.d("SEARCH_HISTORY", "   → Loaded: " + keyword);
                        }
                        searchHistoryAdapter.updateData(searchHistoryList);
                        Log.d("SEARCH_HISTORY", "✅ Total loaded: " + searchHistoryList.size() + " history items");
                    } catch (JSONException e) {
                        Log.e("SEARCH_HISTORY", "❌ Error parsing JSON: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("SEARCH_HISTORY", "❌ Error loading history: " + error.toString());
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

        requestQueue.add(request);
    }

    private void saveSearchHistory(String keyword) {
        if (keyword.trim().isEmpty()) {
            Log.w("SEARCH_HISTORY", "⚠️ Keyword is empty, skipping save");
            return;
        }

        int userId = sessionManager.getUserId();

        if (userId == 0) {
            Log.e("SEARCH_HISTORY", "❌ User ID is 0! Cannot save history.");
            Toast.makeText(requireContext(), "Error: User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("SEARCH_HISTORY", "💾 Saving keyword: '" + keyword + "'");

        String url = "https://tkjnbelcgfwpbhppsnrl.supabase.co/rest/v1/riwayat_pencarian";

        JSONObject bodyObject = new JSONObject();
        try {
            bodyObject.put("id_pengguna", userId);
            bodyObject.put("keyword", keyword);
            Log.d("SEARCH_HISTORY", "📦 Request body: " + bodyObject.toString());
        } catch (JSONException e) {
            Log.e("SEARCH_HISTORY", "❌ Error creating JSON: " + e.getMessage());
            return;
        }

        final String requestBody = bodyObject.toString();

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.POST, url, null,
                response -> {
                    Log.d("SEARCH_HISTORY", "✅ Keyword saved successfully!");
                    loadSearchHistory();
                },
                error -> {
                    Log.e("SEARCH_HISTORY", "❌ Error saving keyword: " + error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                headers.put("Content-Type", "application/json");
                headers.put("Prefer", "return=representation");
                return headers;
            }

            @Override
            public byte[] getBody() {
                try {
                    return requestBody.getBytes("utf-8");
                } catch (Exception e) {
                    return null;
                }
            }
        };

        requestQueue.add(request);
    }

    private void deleteSearchHistory(String keyword, int position) {
        int userId = sessionManager.getUserId();

        String url = "https://tkjnbelcgfwpbhppsnrl.supabase.co/rest/v1/riwayat_pencarian?id_pengguna=eq." + userId
                + "&keyword=eq." + keyword;

        Log.d("SEARCH_HISTORY", "Deleting keyword: " + keyword);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.DELETE, url, null,
                response -> {
                    Log.d("SEARCH_HISTORY", "✓ Keyword deleted");
                    searchHistoryAdapter.removeItem(position);
                    if (searchHistoryList.isEmpty()) {
                        hideSearchHistory();
                    }
                    Toast.makeText(requireContext(), "Riwayat dihapus", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("SEARCH_HISTORY", "✗ Error deleting: " + error.toString());
                    Toast.makeText(requireContext(), "Gagal menghapus riwayat", Toast.LENGTH_SHORT).show();
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

        requestQueue.add(request);
    }

    private void applyFilterAndSort(boolean sortByDate, boolean oldestFirst) {
        List<Job> listToSort = new ArrayList<>(filteredJobList);

        if (sortByDate) {
            Collections.sort(listToSort, new Comparator<Job>() {
                @Override
                public int compare(Job j1, Job j2) {
                    if (oldestFirst) {
                        return j1.getPostedTime().compareTo(j2.getPostedTime());
                    } else {
                        return j2.getPostedTime().compareTo(j1.getPostedTime());
                    }
                }
            });
        }

        jobAdapter.setData(listToSort);
    }

    private void resetTabs() {
        tabSemua.setBackgroundResource(R.drawable.shape_stroke);
        tabSemua.setTextColor(getResources().getColor(R.color.gray));
        tabTerbaru.setBackgroundResource(R.drawable.shape_stroke);
        tabTerbaru.setTextColor(getResources().getColor(R.color.gray));
        tabTerlama.setBackgroundResource(R.drawable.shape_stroke);
        tabTerlama.setTextColor(getResources().getColor(R.color.gray));
    }

    private void openNotificationFragment() {
        NotificationFragment notificationFragment = new NotificationFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, notificationFragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupSearchListener() {
        searchEditText.setOnClickListener(v -> {
            Log.d("SEARCH_LISTENER", "🖱️ Search field clicked");
            if (!searchHistoryList.isEmpty() && searchEditText.getText().toString().trim().isEmpty()) {
                showSearchHistory();
                Log.d("SEARCH_LISTENER", "📋 Showing " + searchHistoryList.size() + " history items");
            }
        });

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                String keyword = searchEditText.getText().toString().trim();
                Log.d("SEARCH_LISTENER", "⌨️ Enter/Search pressed!");
                Log.d("SEARCH_LISTENER", "Keyword: '" + keyword + "'");

                if (!keyword.isEmpty()) {
                    Log.d("SEARCH_LISTENER", "💾 Calling saveSearchHistory()...");
                    saveSearchHistory(keyword);
                    hideSearchHistory();
                    searchEditText.clearFocus();
                } else {
                    Log.d("SEARCH_LISTENER", "⚠️ Keyword is empty, not saving");
                }
                return true;
            }
            return false;
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();

                if (currentSearchQuery.isEmpty() && searchEditText.hasFocus() && !searchHistoryList.isEmpty()) {
                    showSearchHistory();
                } else {
                    hideSearchHistory();
                }

                performSearch(currentSearchQuery);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void performSearch(String query) {
        filteredJobList.clear();

        if (query.isEmpty()) {
            filterJobsByKeywords();
            return;
        }

        Log.d("SEARCH_DEBUG", "🔍 Searching for: " + query);

        String queryLower = query.toLowerCase(Locale.ROOT);

        for (Job job : jobList) {
            boolean matchJudul = job.getJobTitle().toLowerCase(Locale.ROOT).contains(queryLower);
            boolean matchKategori = job.getTag1().toLowerCase(Locale.ROOT).contains(queryLower);
            boolean matchPerusahaan = job.getCompanyName().toLowerCase(Locale.ROOT).contains(queryLower);
            boolean matchLokasi = job.getLocation().toLowerCase(Locale.ROOT).contains(queryLower);

            boolean smartMatchKategori = KeywordMapper.isRelated(query, job.getTag1());
            boolean smartMatchJudul = KeywordMapper.isRelated(query, job.getJobTitle());

            if (matchJudul || matchKategori || matchPerusahaan || matchLokasi ||
                    smartMatchKategori || smartMatchJudul) {
                filteredJobList.add(job);
            }
        }

        Log.d("SEARCH_DEBUG", "📊 Found " + filteredJobList.size() + " results for '" + query + "'");

        if (filteredJobList.isEmpty()) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Tidak ada hasil untuk \"" + query + "\"", Toast.LENGTH_SHORT).show();
            }
        }

        resetTabs();
        tabSemua.setBackgroundResource(R.drawable.selected);
        tabSemua.setTextColor(getResources().getColor(android.R.color.white));
        applyFilterAndSort(false, false);
    }

    private void loadLowonganFromAPI() {
        RequestQueue queue = Volley.newRequestQueue(getContext());

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, SUPABASE_URL, null,
                response -> {
                    jobList.clear();
                    parseResponse(response);

                    if (currentSearchQuery.isEmpty()) {
                        filterJobsByKeywords();
                    } else {
                        performSearch(currentSearchQuery);
                    }
                },
                error -> {
                    Log.e("API_ERROR", "Volley error: " + error.toString());
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Gagal memuat data: " + error.toString(), Toast.LENGTH_LONG).show();
                    }
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

    // PERBAIKAN: Parsing response untuk mengambil nama_perusahaan dari perusahaan
    private void parseResponse(JSONArray response) {
        Log.d("SUPABASE_DATA", "📦 Total data dari Supabase: " + response.length());

        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject obj = response.getJSONObject(i);

                int idLowongan = obj.optInt("id_lowongan", 0);

                if (idLowongan == 0) {
                    idLowongan = obj.optInt("id", 0);
                }

                String judul = obj.optString("judul", "-");
                String lokasi = obj.optString("lokasi", "-");
                String kategori = obj.optString("kategori", "-");
                String tipe = obj.optString("tipe_pekerjaan", "-");
                String gaji = obj.optString("gaji_range", "-");

                // PERBAIKAN: Ambil nama_perusahaan dari tabel perusahaan
                String namaPerusahaan = "Perusahaan"; // default
                String deskripsi = obj.optString("deskripsi", "");
                String kualifikasi = obj.optString("kualifikasi", "");
                String noTelp = obj.optString("no_telp", "");
                String modeKerja = obj.optString("mode_kerja", "On-site");
                String benefit = obj.optString("benefit", "");
                String dibuatPada = formatPostedTime(obj.optString("dibuat_pada", ""));

                // TAMBAHAN: Ambil data logo perusahaan dan nama perusahaan
                String logoUrl = "";
                String logoPath = "";
                int idPerusahaan = 0;

                // Coba ambil dari objek perusahaan jika ada
                if (obj.has("perusahaan")) {
                    try {
                        JSONObject perusahaan = obj.getJSONObject("perusahaan");
                        namaPerusahaan = perusahaan.optString("nama_perusahaan", "Perusahaan"); // Ambil dari perusahaan
                        logoUrl = perusahaan.optString("logo_url", "");
                        logoPath = perusahaan.optString("logo_path", "");
                        idPerusahaan = perusahaan.optInt("id_perusahaan", 0);
                        Log.d("SUPABASE_DATA", "✅ Found company data - Name: " + namaPerusahaan + ", Logo URL: " + logoUrl);
                    } catch (JSONException e) {
                        Log.e("SUPABASE_DATA", "❌ Error parsing perusahaan data: " + e.getMessage());
                    }
                }

                Job job = new Job(
                        idLowongan,
                        namaPerusahaan, // Sekarang menggunakan nama dari tabel perusahaan
                        lokasi,
                        judul,
                        dibuatPada,
                        gaji + " Pendaftar",
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

                jobList.add(job);

                Log.d("SUPABASE_DATA", "✅ Loaded Job - ID: " + idLowongan +
                        ", Title: " + judul + ", Company: " + namaPerusahaan +
                        ", Logo URL: " + logoUrl);

            } catch (JSONException e) {
                Log.e("JSON_ERROR", "Parsing gagal: " + e.getMessage());
            }
        }

        Log.d("SUPABASE_DATA", "✅ Berhasil parsing " + jobList.size() + " lowongan");
    }

    private String formatPostedTime(String rawTime) {
        if (rawTime == null || rawTime.isEmpty()) {
            return "Baru saja";
        }

        try {
            if (rawTime.contains("T")) {
                String datePart = rawTime.split("T")[0];
                String[] parts = datePart.split("-");
                if (parts.length == 3) {
                    return parts[2] + "/" + parts[1] + "/" + parts[0];
                }
                return datePart;
            }
            return rawTime;
        } catch (Exception e) {
            Log.e("TIME_FORMAT", "Error formatting time: " + rawTime);
            return "Baru saja";
        }
    }

    private void filterJobsByKeywords() {
        CVKeywordManager keywordManager = CVKeywordManager.getInstance();

        if (keywordManager.hasScannedCV() && !keywordManager.getKeywords().isEmpty()) {
            List<String> keywords = keywordManager.getKeywords();
            filteredJobList.clear();

            Log.d("FILTER_DEBUG", "Keywords dari CV: " + keywords.toString());

            for (Job job : jobList) {
                boolean isMatch = false;

                for (String keyword : keywords) {
                    if (matchesKeyword(job, keyword)) {
                        isMatch = true;
                        break;
                    }
                }

                if (isMatch) {
                    filteredJobList.add(job);
                }
            }

            if (filteredJobList.isEmpty()) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "❌ Tidak ada lowongan yang cocok dengan keyword CV.\nMenampilkan semua lowongan.", Toast.LENGTH_LONG).show();
                }
                filteredJobList.addAll(jobList);
            } else {
                String matchInfo = "✅ Ditemukan " + filteredJobList.size() + " lowongan cocok dari " + jobList.size() + " total";
                if (getContext() != null) {
                    Toast.makeText(getContext(), matchInfo, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            filteredJobList.addAll(jobList);
        }

        resetTabs();
        tabSemua.setBackgroundResource(R.drawable.selected);
        tabSemua.setTextColor(getResources().getColor(android.R.color.white));
        applyFilterAndSort(false, false);
    }

    private boolean matchesKeyword(Job job, String keyword) {
        String keywordLower = keyword.toLowerCase(Locale.ROOT).trim();
        String kategoriLower = job.getTag1().toLowerCase(Locale.ROOT).trim();
        String judulLower = job.getJobTitle().toLowerCase(Locale.ROOT).trim();

        boolean exactMatchJudul = judulLower.contains(keywordLower);
        boolean exactMatchKategori = kategoriLower.contains(keywordLower);
        boolean smartMatchKategori = KeywordMapper.isRelated(keyword, job.getTag1());
        boolean smartMatchJudul = KeywordMapper.isRelated(keyword, job.getJobTitle());

        return exactMatchJudul || exactMatchKategori || smartMatchKategori || smartMatchJudul;
    }
}