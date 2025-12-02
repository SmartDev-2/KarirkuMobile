package com.tem2.karirku;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

public class beranda extends AppCompatActivity {

    private LinearLayout navHome, navScan, navChat, navProfile;
    private ImageView iconHome, iconScan, iconChat, iconProfile;
    private TextView textHome, textScan, textChat, textProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_beranda);

        // Inisialisasi container navigasi
        navHome = findViewById(R.id.nav_home);
        navScan = findViewById(R.id.nav_scan);
        navChat = findViewById(R.id.nav_chat);
        navProfile = findViewById(R.id.nav_profile);

        // Inisialisasi ikon
        iconHome = findViewById(R.id.icon_home);
        iconScan = findViewById(R.id.icon_scan);
        iconChat = findViewById(R.id.icon_chat);
        iconProfile = findViewById(R.id.icon_profile);

        // Inisialisasi teks
        textHome = findViewById(R.id.text_home);
        textScan = findViewById(R.id.text_scan);
        textChat = findViewById(R.id.text_chat);
        textProfile = findViewById(R.id.text_profile);

        setupCustomBottomNav();

        // Load fragment default saat pertama kali dibuka
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            setActiveTab(navHome);
        }

        // PERBAIKI BAGIAN INI - Syntax yang benar:
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.frameLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupCustomBottomNav() {
        View.OnClickListener listener = v -> {
            Fragment fragment = null;
            LinearLayout activeTab = null;

            int id = v.getId();
            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
                activeTab = navHome;
            } else if (id == R.id.nav_scan) {
                fragment = new scancvFragment();
                activeTab = navScan;
            } else if (id == R.id.nav_chat) {
                fragment = new chatFragment();
                activeTab = navChat;
            } else if (id == R.id.nav_profile) {
                fragment = new profilFragment();
                activeTab = navProfile;
            }

            if (fragment != null && activeTab != null) {
                loadFragment(fragment);
                setActiveTab(activeTab);
            }
        };

        navHome.setOnClickListener(listener);
        navScan.setOnClickListener(listener);
        navChat.setOnClickListener(listener);
        navProfile.setOnClickListener(listener);
    }

    private void setActiveTab(LinearLayout activeTab) {
        resetTabs();
        int colorSelected = ContextCompat.getColor(this, R.color.nav_color_selected);

        if (activeTab == navHome) {
            iconHome.setColorFilter(colorSelected);
            textHome.setTextColor(colorSelected);
        } else if (activeTab == navScan) {
            iconScan.setColorFilter(colorSelected);
            textScan.setTextColor(colorSelected);
        } else if (activeTab == navChat) {
            iconChat.setColorFilter(colorSelected);
            textChat.setTextColor(colorSelected);
        } else if (activeTab == navProfile) {
            iconProfile.setColorFilter(colorSelected);
            textProfile.setTextColor(colorSelected);
        }
    }

    private void resetTabs() {
        int colorUnselected = ContextCompat.getColor(this, R.color.nav_color_unselected);

        iconHome.setColorFilter(colorUnselected);
        textHome.setTextColor(colorUnselected);
        iconScan.setColorFilter(colorUnselected);
        textScan.setTextColor(colorUnselected);
        iconChat.setColorFilter(colorUnselected);
        textChat.setTextColor(colorUnselected);
        iconProfile.setColorFilter(colorUnselected);
        textProfile.setTextColor(colorUnselected);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commit();
    }
}