package com.tem2.karirku;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.NetworkResponse;
import com.android.volley.toolbox.StringRequest;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private Context context;
    private List<Job> jobList;
    private List<Integer> savedJobIds = new ArrayList<>();
    private SessionManager sessionManager;
    private int currentUserId;
    private int currentPencakerId;
    private boolean isSavedMode;

    private static final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    public JobAdapter(Context context, List<Job> jobList, boolean isSavedMode) {
        this.context = context;
        this.jobList = jobList;
        this.isSavedMode = isSavedMode;
        this.sessionManager = new SessionManager(context);
        this.currentUserId = sessionManager.getUserId();
        this.currentPencakerId = 0;

        if (!isSavedMode) {
            loadPencakerId();
        }
    }

    public JobAdapter(Context context, List<Job> jobList) {
        this(context, jobList, false);
    }

    private void loadPencakerId() {
        if (currentUserId == 0) {
            Log.e("FAVORITE", "User ID is 0, cannot load pencaker ID");
            return;
        }

        String url = SUPABASE_URL + "/rest/v1/pencaker?id_pengguna=eq." + currentUserId + "&select=id_pencaker";

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            JSONObject pencaker = response.getJSONObject(0);
                            currentPencakerId = pencaker.getInt("id_pencaker");
                            Log.d("FAVORITE", "Loaded pencaker ID: " + currentPencakerId + " for user: " + currentUserId);
                            loadSavedJobs();
                        } else {
                            Log.e("FAVORITE", "No pencaker found for user: " + currentUserId);
                        }
                    } catch (JSONException e) {
                        Log.e("FAVORITE", "Error parsing pencaker data: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("FAVORITE", "Error loading pencaker ID: " + error.toString());
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

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);

        Log.d("JOB_ADAPTER", "Binding job - ID: " + job.getIdLowongan() + ", Title: " + job.getJobTitle());

        holder.tvCompanyName.setText(job.getCompanyName());
        holder.tvLocation.setText(job.getLocation());
        holder.tvJobTitle.setText(job.getJobTitle());
        holder.tvPostedTime.setText(job.getPostedTime());
        holder.tvApplicants.setText(job.getApplicants());

        holder.tvTag1.setText(job.getTag1());
        holder.tvTag2.setText(job.getTag2());
        holder.tvTag3.setText(job.getTag3());

        holder.tvTag1.setVisibility(job.getTag1().isEmpty() ? View.GONE : View.VISIBLE);
        holder.tvTag2.setVisibility(job.getTag2().isEmpty() ? View.GONE : View.VISIBLE);
        holder.tvTag3.setVisibility(job.getTag3().isEmpty() ? View.GONE : View.VISIBLE);

        loadCompanyLogo(holder.imgCompany, job);

        boolean isSaved = savedJobIds.contains(job.getIdLowongan());
        updateSaveButton(holder.btnSave, isSaved);

        holder.btnSave.setOnClickListener(v -> {
            Log.d("FAVORITE", "Tombol save diklik - Job ID: " + job.getIdLowongan() + ", User ID: " + currentUserId + ", Already Saved: " + isSaved);

            if (currentUserId == 0) {
                Toast.makeText(context, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentPencakerId == 0) {
                Toast.makeText(context, "Error: Data profil tidak lengkap", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isSaved) {
                removeFromFavorites(job.getIdLowongan(), holder, position);
            } else {
                addToFavorites(job, holder);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, JobDetailActivity.class);
            intent.putExtra("JOB_DATA", job);
            context.startActivity(intent);
        });
    }

    private void loadCompanyLogo(ImageView imageView, Job job) {
        String logoUrl = job.getLogoUrl();
        String logoPath = job.getLogoPath();

        Log.d("JOB_ADAPTER", "Loading logo for company: " + job.getCompanyName());
        Log.d("JOB_ADAPTER", "   Logo URL: " + logoUrl);
        Log.d("JOB_ADAPTER", "   Logo Path: " + logoPath);

        if (logoUrl != null && !logoUrl.trim().isEmpty()) {
            Log.d("JOB_ADAPTER", "Using logo_url: " + logoUrl);
            Glide.with(context)
                    .load(logoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.iconloker)
                    .error(R.drawable.iconloker)
                    .circleCrop()
                    .into(imageView);
        }
        else if (logoPath != null && !logoPath.trim().isEmpty()) {
            String builtUrl = buildLogoUrlFromPath(logoPath);
            Log.d("JOB_ADAPTER", "Built URL from path: " + builtUrl);
            Glide.with(context)
                    .load(builtUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.iconloker)
                    .error(R.drawable.iconloker)
                    .circleCrop()
                    .into(imageView);
        }
        else {
            Log.d("JOB_ADAPTER", "No logo available, using default");
            imageView.setImageResource(R.drawable.iconloker);
        }
    }

    private String buildLogoUrlFromPath(String logoPath) {
        return "https://tkjnbelcgfwpbhppsnrl.supabase.co/storage/v1/object/public/" + logoPath;
    }

    private void updateSaveButton(ImageView btnSave, boolean isSaved) {
        if (isSaved) {
            btnSave.setImageResource(R.drawable.icsimpan_active);
        } else {
            btnSave.setImageResource(R.drawable.ic_simpan);
        }
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public void setData(List<Job> newList) {
        this.jobList = newList;
        notifyDataSetChanged();
    }

    private void addToFavorites(Job job, JobViewHolder holder) {
        int jobId = job.getIdLowongan();

        if (savedJobIds.contains(jobId)) {
            Log.d("FAVORITE", "Lowongan sudah disimpan secara lokal, skip API call");
            Toast.makeText(context, "Lowongan sudah disimpan", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("FAVORITE", "Menyimpan lowongan ke favorit - Job ID: " + jobId + ", Pencaker ID: " + currentPencakerId);

        String url = SUPABASE_URL + "/rest/v1/favorit_lowongan";

        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put("id_pencaker", currentPencakerId);
            bodyJson.put("id_lowongan", jobId);
            Log.d("FAVORITE", "Request Body: " + bodyJson.toString());
        } catch (JSONException e) {
            Log.e("FAVORITE", "Error creating JSON: " + e.getMessage());
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                bodyJson,
                response -> {
                    Log.d("FAVORITE", "Berhasil menyimpan ke favorit");
                    savedJobIds.add(jobId);
                    updateSaveButton(holder.btnSave, true);
                    Toast.makeText(context, "Lowongan disimpan", Toast.LENGTH_SHORT).show();
                    sendSavedCountUpdateBroadcast();
                },
                error -> {
                    Log.e("FAVORITE", "Gagal menyimpan favorit: " + error.toString());

                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        String errorBody = error.networkResponse.data != null ?
                                new String(error.networkResponse.data) : "No error body";

                        Log.d("FAVORITE", "Error Details - Status: " + statusCode + ", Body: " + errorBody);

                        if (statusCode == 409) {
                            Log.d("FAVORITE", "Error 409 - Lowongan sudah ada di database, update UI");
                            savedJobIds.add(jobId);
                            updateSaveButton(holder.btnSave, true);
                            Toast.makeText(context, "Lowongan sudah disimpan", Toast.LENGTH_SHORT).show();
                            sendSavedCountUpdateBroadcast();
                            loadSavedJobs();
                        } else if (statusCode == 400) {
                            Toast.makeText(context, "Data tidak valid", Toast.LENGTH_SHORT).show();
                        } else if (statusCode == 401) {
                            Toast.makeText(context, "Tidak diizinkan - silakan login ulang", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Gagal menyimpan (Error: " + statusCode + ")", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Error jaringan - periksa koneksi internet", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
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

    private void removeFromFavorites(int jobId, JobViewHolder holder, int position) {
        if (!savedJobIds.contains(jobId)) {
            Log.d("FAVORITE", "Lowongan sudah dihapus secara lokal, skip API call");
            Toast.makeText(context, "Lowongan sudah dihapus", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = SUPABASE_URL + "/rest/v1/favorit_lowongan" +
                "?id_pencaker=eq." + currentPencakerId +
                "&id_lowongan=eq." + jobId;

        Log.d("FAVORITE", "Menghapus dari favorit - URL: " + url);

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    Log.d("FAVORITE", "Berhasil menghapus dari favorit");
                    savedJobIds.remove((Integer) jobId);

                    if (isSavedMode) {
                        jobList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, jobList.size());
                        Toast.makeText(context, "Lowongan dihapus dari disimpan", Toast.LENGTH_SHORT).show();
                    } else {
                        updateSaveButton(holder.btnSave, false);
                        Toast.makeText(context, "Lowongan dihapus dari disimpan", Toast.LENGTH_SHORT).show();
                    }

                    sendSavedCountUpdateBroadcast();
                },
                error -> {
                    Log.e("FAVORITE", "Gagal menghapus favorit: " + error.toString());

                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        String errorBody = error.networkResponse.data != null ?
                                new String(error.networkResponse.data) : "No error body";

                        Log.d("FAVORITE", "Delete Error - Status: " + statusCode + ", Body: " + errorBody);

                        if (statusCode == 204) {
                            Log.d("FAVORITE", "204 No Content - Data berhasil dihapus");
                            savedJobIds.remove((Integer) jobId);

                            if (isSavedMode) {
                                jobList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, jobList.size());
                            } else {
                                updateSaveButton(holder.btnSave, false);
                            }
                            Toast.makeText(context, "Lowongan dihapus dari disimpan", Toast.LENGTH_SHORT).show();
                            sendSavedCountUpdateBroadcast();
                        } else if (statusCode == 404) {
                            Log.d("FAVORITE", "Error 404 - Data sudah dihapus, update UI");
                            savedJobIds.remove((Integer) jobId);

                            if (isSavedMode) {
                                jobList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, jobList.size());
                            } else {
                                updateSaveButton(holder.btnSave, false);
                            }
                            Toast.makeText(context, "Lowongan dihapus dari disimpan", Toast.LENGTH_SHORT).show();
                            sendSavedCountUpdateBroadcast();
                        } else {
                            Toast.makeText(context, "Gagal menghapus lowongan (Error: " + statusCode + ")", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Error jaringan - periksa koneksi internet", Toast.LENGTH_SHORT).show();
                    }
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

    private void loadSavedJobs() {
        if (currentPencakerId == 0) {
            Log.d("FAVORITE", "Pencaker ID 0, skip load saved jobs");
            return;
        }

        String url = SUPABASE_URL + "/rest/v1/favorit_lowongan" +
                "?id_pencaker=eq." + currentPencakerId +
                "&select=id_lowongan";

        Log.d("FAVORITE", "Loading saved jobs from: " + url);

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        savedJobIds.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            int jobId = obj.getInt("id_lowongan");
                            savedJobIds.add(jobId);
                        }
                        Log.d("FAVORITE", "Loaded " + savedJobIds.size() + " saved jobs: " + savedJobIds);
                        notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e("FAVORITE", "Error parsing saved jobs: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("FAVORITE", "Gagal load saved jobs: " + error.toString());
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

    private void sendSavedCountUpdateBroadcast() {
        try {
            Intent intent = new Intent("UPDATE_SAVED_JOBS_COUNT");
            context.sendBroadcast(intent);
            Log.d("BROADCAST", "Broadcast sent for saved jobs count update");
        } catch (Exception e) {
            Log.e("BROADCAST", "Error sending broadcast: " + e.getMessage());
        }
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCompany, btnSave;
        TextView tvCompanyName, tvLocation, tvJobTitle, tvPostedTime, tvApplicants;
        TextView tvTag1, tvTag2, tvTag3;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);

            imgCompany = itemView.findViewById(R.id.imgCompanyLogo);
            btnSave = itemView.findViewById(R.id.btnSave);
            tvCompanyName = itemView.findViewById(R.id.tvCompanyName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvPostedTime = itemView.findViewById(R.id.tvPostedTime);
            tvApplicants = itemView.findViewById(R.id.tvApplicants);
            tvTag1 = itemView.findViewById(R.id.tvTag1);
            tvTag2 = itemView.findViewById(R.id.tvTag2);
            tvTag3 = itemView.findViewById(R.id.tvTag3);
        }
    }
}