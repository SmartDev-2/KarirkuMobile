package com.tem2.karirku;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LOGIN_TIME = "login_time";

    public SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    private static final long SESSION_TIMEOUT = 7 * 24 * 60 * 60 * 1000;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void createSession(JSONObject user, String authToken) {
        try {
            editor.putInt(KEY_USER_ID, user.optInt("id_pengguna"));
            editor.putString(KEY_USER_NAME, user.optString("nama_lengkap"));
            editor.putString(KEY_USER_EMAIL, user.optString("email"));
            editor.putString(KEY_USER_PHONE, user.optString("no_hp"));
            editor.putString(KEY_USER_ROLE, user.optString("role", "pencaker"));
            editor.putString(KEY_AUTH_TOKEN, authToken);
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
            editor.apply();

            Log.d("SESSION", "Session created for: " + user.optString("nama_lengkap"));
        } catch (Exception e) {
            Log.e("SESSION", "Failed to create session: " + e.getMessage());
        }
    }

    public void createSession(JSONObject user) {
        createSession(user, "");
    }

    public boolean isLoggedIn() {
        boolean loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);

        if (!loggedIn) {
            return false;
        }

        long loginTime = prefs.getLong(KEY_LOGIN_TIME, 0);
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - loginTime;

        if (timeDiff > SESSION_TIMEOUT) {
            Log.d("SESSION", "Session expired (7 days)");
            clearSession();
            return false;
        }

        return true;
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, 0);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public String getUserPhone() {
        return prefs.getString(KEY_USER_PHONE, "");
    }

    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, "pencaker");
    }

    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, "");
    }

    public void updateUserProfile(String name, String phone) {
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_PHONE, phone);
        editor.apply();
        Log.d("SESSION", "Profile updated - Name: " + name + ", Phone: " + phone);
    }

    public void updateUserEmail(String email) {
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
        Log.d("SESSION", "Email updated to: " + email);
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
        Log.d("SESSION", "Session cleared");
    }

    public JSONObject getSessionData() {
        JSONObject data = new JSONObject();
        try {
            data.put("user_id", getUserId());
            data.put("user_name", getUserName());
            data.put("user_email", getUserEmail());
            data.put("user_phone", getUserPhone());
            data.put("user_role", getUserRole());
            data.put("is_logged_in", isLoggedIn());
        } catch (Exception e) {
            Log.e("SESSION", "Error getting session data: " + e.getMessage());
        }
        return data;
    }

    public boolean isSessionExpiringSoon() {
        long loginTime = prefs.getLong(KEY_LOGIN_TIME, 0);
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - loginTime;
        long timeLeft = SESSION_TIMEOUT - timeDiff;

        return timeLeft < (24 * 60 * 60 * 1000);
    }

    public void renewSession() {
        editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
        editor.apply();
        Log.d("SESSION", "Session renewed");
    }
}