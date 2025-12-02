package com.tem2.karirku;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class profilFragment extends Fragment {

    private static final String TAG = "ProfilFragment";
    private static final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    private TextView tvName, tvEmail, tvJumlahDisimpan, tvJumlahDilamar;
    private CircleImageView imgProfile;
    private SessionManager sessionManager;
    private BroadcastReceiver jobsCountReceiver;
    private boolean isReceiverRegistered = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView dipanggil");

        View view = inflater.inflate(R.layout.fragment_profil, container, false);

        try {
            if (getContext() != null) {
                sessionManager = new SessionManager(getContext());
            } else {
                Log.e(TAG, "Context is null");
                return view;
            }

            tvName = view.findViewById(R.id.tvNama);
            tvEmail = view.findViewById(R.id.tvSelengkapnya);
            imgProfile = view.findViewById(R.id.imgProfile);
            tvJumlahDisimpan = view.findViewById(R.id.tvJumlahDisimpan);
            tvJumlahDilamar = view.findViewById(R.id.tvJumlahDilamar);

            if (tvName != null) tvName.setText("Memuat...");
            if (tvEmail != null) tvEmail.setText("Memuat...");
            if (tvJumlahDisimpan != null) tvJumlahDisimpan.setText("0");
            if (tvJumlahDilamar != null) tvJumlahDilamar.setText("0");

            loadUserData();
            loadSavedJobsCount();
            loadAppliedJobsCount();

            setupClickListeners(view);
            setupBroadcastReceiver();

        } catch (Exception e) {
            Log.e(TAG, "Error dalam onCreateView: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Terjadi kesalahan saat memuat profil", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void setupBroadcastReceiver() {
        try {
            jobsCountReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if ("UPDATE_SAVED_JOBS_COUNT".equals(action)) {
                        Log.d(TAG, "Broadcast received, updating saved jobs count");
                        loadSavedJobsCount();
                    } else if ("UPDATE_APPLIED_JOBS_COUNT".equals(action)) {
                        Log.d(TAG, "Broadcast received, updating applied jobs count");
                        loadAppliedJobsCount();
                    }
                }
            };

            if (getContext() != null) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("UPDATE_SAVED_JOBS_COUNT");
                filter.addAction("UPDATE_APPLIED_JOBS_COUNT");
                getContext().registerReceiver(jobsCountReceiver, filter);
                isReceiverRegistered = true;
                Log.d(TAG, "Broadcast receiver registered for both saved and applied jobs");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up broadcast receiver: " + e.getMessage());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView dipanggil");

        if (isReceiverRegistered && jobsCountReceiver != null && getContext() != null) {
            try {
                getContext().unregisterReceiver(jobsCountReceiver);
                isReceiverRegistered = false;
                Log.d(TAG, "Broadcast receiver unregistered");
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receiver: " + e.getMessage());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume dipanggil");

        try {
            loadSavedJobsCount();
            loadAppliedJobsCount();
            loadUserData();
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage());
        }
    }

    private void loadSavedJobsCount() {
        try {
            if (sessionManager == null || tvJumlahDisimpan == null) {
                Log.e(TAG, "SessionManager atau tvJumlahDisimpan null");
                return;
            }

            int userId = sessionManager.getUserId();
            if (userId == 0) {
                Log.w(TAG, "User ID is 0");
                if (tvJumlahDisimpan != null) {
                    tvJumlahDisimpan.setText("0");
                }
                return;
            }

            String getPencakerUrl = SUPABASE_URL + "/rest/v1/pencaker?id_pengguna=eq." + userId + "&select=id_pencaker";

            RequestQueue queue = Volley.newRequestQueue(requireContext());
            JsonArrayRequest getPencakerRequest = new JsonArrayRequest(Request.Method.GET, getPencakerUrl, null,
                    response -> {
                        try {
                            if (response.length() > 0) {
                                JSONObject pencaker = response.getJSONObject(0);
                                int pencakerId = pencaker.getInt("id_pencaker");

                                String countUrl = SUPABASE_URL + "/rest/v1/favorit_lowongan" +
                                        "?id_pencaker=eq." + pencakerId +
                                        "&select=id_lowongan";

                                JsonArrayRequest countRequest = new JsonArrayRequest(Request.Method.GET, countUrl, null,
                                        countResponse -> {
                                            int count = countResponse.length();
                                            if (tvJumlahDisimpan != null) {
                                                tvJumlahDisimpan.setText(String.valueOf(count));
                                            }
                                            Log.d(TAG, "Jumlah lowongan disimpan: " + count + " for pencaker: " + pencakerId);
                                        },
                                        error -> {
                                            Log.e(TAG, "Gagal memuat jumlah disimpan: " + error.toString());
                                            if (tvJumlahDisimpan != null) {
                                                tvJumlahDisimpan.setText("0");
                                            }
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

                                queue.add(countRequest);
                            } else {
                                Log.e(TAG, "No pencaker found for user: " + userId);
                                if (tvJumlahDisimpan != null) {
                                    tvJumlahDisimpan.setText("0");
                                }
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing pencaker data: " + e.getMessage());
                            if (tvJumlahDisimpan != null) {
                                tvJumlahDisimpan.setText("0");
                            }
                        }
                    },
                    error -> {
                        Log.e(TAG, "Error loading pencaker ID: " + error.toString());
                        if (tvJumlahDisimpan != null) {
                            tvJumlahDisimpan.setText("0");
                        }
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

            queue.add(getPencakerRequest);
        } catch (Exception e) {
            Log.e(TAG, "Error in loadSavedJobsCount: " + e.getMessage(), e);
            if (tvJumlahDisimpan != null) {
                tvJumlahDisimpan.setText("0");
            }
        }
    }

    private void loadAppliedJobsCount() {
        try {
            if (sessionManager == null || tvJumlahDilamar == null) {
                Log.e(TAG, "SessionManager atau tvJumlahDilamar null");
                return;
            }

            int userId = sessionManager.getUserId();
            if (userId == 0) {
                Log.w(TAG, "User ID is 0");
                if (tvJumlahDilamar != null) {
                    tvJumlahDilamar.setText("0");
                }
                return;
            }

            String getPencakerUrl = SUPABASE_URL + "/rest/v1/pencaker?id_pengguna=eq." + userId + "&select=id_pencaker";

            RequestQueue queue = Volley.newRequestQueue(requireContext());
            JsonArrayRequest getPencakerRequest = new JsonArrayRequest(Request.Method.GET, getPencakerUrl, null,
                    response -> {
                        try {
                            if (response.length() > 0) {
                                JSONObject pencaker = response.getJSONObject(0);
                                int pencakerId = pencaker.getInt("id_pencaker");

                                String countUrl = SUPABASE_URL + "/rest/v1/lamaran" +
                                        "?id_pencaker=eq." + pencakerId +
                                        "&select=id_lamaran";

                                JsonArrayRequest countRequest = new JsonArrayRequest(Request.Method.GET, countUrl, null,
                                        countResponse -> {
                                            int count = countResponse.length();
                                            if (tvJumlahDilamar != null) {
                                                tvJumlahDilamar.setText(String.valueOf(count));
                                            }
                                            Log.d(TAG, "Jumlah lowongan dilamar: " + count + " for pencaker: " + pencakerId);
                                        },
                                        error -> {
                                            Log.e(TAG, "Gagal memuat jumlah dilamar: " + error.toString());
                                            if (tvJumlahDilamar != null) {
                                                tvJumlahDilamar.setText("0");
                                            }
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

                                queue.add(countRequest);
                            } else {
                                Log.e(TAG, "No pencaker found for user: " + userId);
                                if (tvJumlahDilamar != null) {
                                    tvJumlahDilamar.setText("0");
                                }
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing pencaker data: " + e.getMessage());
                            if (tvJumlahDilamar != null) {
                                tvJumlahDilamar.setText("0");
                            }
                        }
                    },
                    error -> {
                        Log.e(TAG, "Error loading pencaker ID: " + error.toString());
                        if (tvJumlahDilamar != null) {
                            tvJumlahDilamar.setText("0");
                        }
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

            queue.add(getPencakerRequest);
        } catch (Exception e) {
            Log.e(TAG, "Error in loadAppliedJobsCount: " + e.getMessage(), e);
            if (tvJumlahDilamar != null) {
                tvJumlahDilamar.setText("0");
            }
        }
    }

    private void loadUserData() {
        try {
            if (sessionManager == null) {
                Log.e(TAG, "SessionManager is null");
                return;
            }

            int userId = sessionManager.getUserId();
            Log.d(TAG, "Load profile for user ID: " + userId);

            String url = SUPABASE_URL + "/rest/v1/pencaker?id_pengguna=eq." + userId + "&select=id_pencaker,nama_lengkap,email_pencaker,no_hp,tanggal_lahir,gender,alamat,pengalaman_tahun,foto_profil_url,foto_profil_path";

            RequestQueue queue = Volley.newRequestQueue(requireContext());

            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            if (response.length() > 0) {
                                JSONObject pencaker = response.getJSONObject(0);

                                String name = pencaker.optString("nama_lengkap", "-");
                                String email = pencaker.optString("email_pencaker", "-");
                                String fotoUrl = pencaker.optString("foto_profil_url", "");

                                if (tvName != null) {
                                    tvName.setText(name);
                                }
                                if (tvEmail != null) {
                                    tvEmail.setText(email);
                                }

                                if (!fotoUrl.isEmpty() && imgProfile != null) {
                                    Log.d(TAG, "Load image from: " + fotoUrl);

                                    Glide.with(requireContext())
                                            .load(fotoUrl)
                                            .circleCrop()
                                            .error(R.drawable.ic_profile_placeholder)
                                            .into(imgProfile);
                                } else if (imgProfile != null) {
                                    imgProfile.setImageResource(R.drawable.ic_profile_placeholder);
                                }

                                Log.d(TAG, "Data loaded from pencaker table");

                            } else {
                                Log.d(TAG, "No data found in pencaker table for user ID: " + userId);
                                loadUserDataFromPengguna();
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing pencaker data: " + e.getMessage());
                            loadUserDataFromPengguna();
                        }
                    },
                    error -> {
                        Log.e(TAG, "Volley error loading pencaker data: " + error.toString());
                        loadUserDataFromPengguna();
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
        } catch (Exception e) {
            Log.e(TAG, "Error in loadUserData: " + e.getMessage(), e);
            loadUserDataFromPengguna();
        }
    }

    private void loadUserDataFromPengguna() {
        try {
            if (sessionManager == null) return;

            int userId = sessionManager.getUserId();
            Log.d(TAG, "Fallback: Load profile from pengguna table for user ID: " + userId);

            String url = SUPABASE_URL + "/rest/v1/pengguna?id=eq." + userId + "&select=nama_lengkap,email,foto_url";

            RequestQueue queue = Volley.newRequestQueue(requireContext());

            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            if (response.length() > 0) {
                                JSONObject user = response.getJSONObject(0);

                                String name = user.optString("nama_lengkap", "-");
                                String email = user.optString("email", "-");
                                String fotoUrl = user.optString("foto_url", "");

                                if (tvName != null) {
                                    tvName.setText(name);
                                }
                                if (tvEmail != null) {
                                    tvEmail.setText(email);
                                }

                                if (!fotoUrl.isEmpty() && imgProfile != null) {
                                    String fullImageUrl = SUPABASE_URL + "/storage/v1/object/public/" + fotoUrl;
                                    Log.d(TAG, "Load image from pengguna table: " + fullImageUrl);

                                    Glide.with(requireContext())
                                            .load(fullImageUrl)
                                            .circleCrop()
                                            .error(R.drawable.ic_profile_placeholder)
                                            .into(imgProfile);
                                }

                                Log.d(TAG, "Fallback data loaded from pengguna table");
                            } else {
                                if (tvName != null) tvName.setText("User");
                                if (tvEmail != null) tvEmail.setText("user@example.com");
                                if (imgProfile != null) imgProfile.setImageResource(R.drawable.ic_profile_placeholder);
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing pengguna data: " + e.getMessage());
                        }
                    },
                    error -> {
                        Log.e(TAG, "Volley error loading pengguna data: " + error.toString());
                        if (tvName != null) tvName.setText("User");
                        if (tvEmail != null) tvEmail.setText("user@example.com");
                        if (imgProfile != null) imgProfile.setImageResource(R.drawable.ic_profile_placeholder);
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
        } catch (Exception e) {
            Log.e(TAG, "Error in loadUserDataFromPengguna: " + e.getMessage(), e);
        }
    }

    private void setupClickListeners(View view) {
        try {
            view.findViewById(R.id.dilamarContainer).setOnClickListener(v -> {
                try {
                    startActivity(new Intent(getActivity(), DilamarActivity.class));
                } catch (Exception e) {
                    Log.e(TAG, "Error opening DilamarActivity: " + e.getMessage());
                    Toast.makeText(getContext(), "Tidak dapat membuka halaman", Toast.LENGTH_SHORT).show();
                }
            });

            view.findViewById(R.id.disimpanContainer).setOnClickListener(v -> {
                try {
                    startActivity(new Intent(getActivity(), DisimpanActivity.class));
                } catch (Exception e) {
                    Log.e(TAG, "Error opening DisimpanActivity: " + e.getMessage());
                    Toast.makeText(getContext(), "Tidak dapat membuka halaman", Toast.LENGTH_SHORT).show();
                }
            });

            view.findViewById(R.id.itemCV).setOnClickListener(v -> {
                try {
                    startActivity(new Intent(getActivity(), CvActivity.class));
                } catch (Exception e) {
                    Log.e(TAG, "Error opening CvActivity: " + e.getMessage());
                    Toast.makeText(getContext(), "Tidak dapat membuka halaman", Toast.LENGTH_SHORT).show();
                }
            });

            view.findViewById(R.id.itemPreferensiKerja).setOnClickListener(v -> {
                try {
                    startActivity(new Intent(getActivity(), UserDataActivity.class));
                } catch (Exception e) {
                    Log.e(TAG, "Error opening UserDataActivity: " + e.getMessage());
                    Toast.makeText(getContext(), "Tidak dapat membuka halaman", Toast.LENGTH_SHORT).show();
                }
            });

            view.findViewById(R.id.btnSetting).setOnClickListener(v -> {
                try {
                    startActivity(new Intent(getActivity(), UpdateAkunActivity.class));
                } catch (Exception e) {
                    Log.e(TAG, "Error opening UpdateDataActivity: " + e.getMessage());
                    Toast.makeText(getContext(), "Tidak dapat membuka halaman", Toast.LENGTH_SHORT).show();
                }
            });

//            view.findViewById(R.id.btnSetting).setOnClickListener(v -> {
//                Toast.makeText(getActivity(), "Fitur settings", Toast.LENGTH_SHORT).show();
//            });

            view.findViewById(R.id.btnLengkapiProfil).setOnClickListener(v -> {
                try {
                    startActivity(new Intent(getActivity(), LengkapiProfilActivity.class));
                } catch (Exception e) {
                    Log.e(TAG, "Error opening LengkapiProfilActivity: " + e.getMessage());
                    Toast.makeText(getContext(), "Tidak dapat membuka halaman", Toast.LENGTH_SHORT).show();
                }
            });

            view.findViewById(R.id.btnKeluar).setOnClickListener(v -> {
                try {
                    if (sessionManager != null) {
                        sessionManager.clearSession();
                    }
                    startActivity(new Intent(getActivity(), MainActivity.class));
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error during logout: " + e.getMessage());
                    Toast.makeText(getContext(), "Error saat logout", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in setupClickListeners: " + e.getMessage());
        }
    }
}