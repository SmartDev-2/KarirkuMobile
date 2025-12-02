package com.tem2.karirku;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class NotificationDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);

        // Back button
        ImageView imgBack = findViewById(R.id.imgBack);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Get data from intent
        String title = getIntent().getStringExtra("notification_title");
        String message = getIntent().getStringExtra("notification_message");
        String time = getIntent().getStringExtra("notification_time");
        String type = getIntent().getStringExtra("notification_type");

        // Set data to views
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvMessage = findViewById(R.id.tvDetailMessage);
        TextView tvTime = findViewById(R.id.tvDetailTime);
        ImageView imgIcon = findViewById(R.id.imgDetailIcon);

        tvTitle.setText(title);
        tvMessage.setText(message);
        tvTime.setText(time);

        // Set icon based on type
        int iconRes = getIconForType(type);
        imgIcon.setImageResource(iconRes);
    }

    private int getIconForType(String type) {
        if (type == null) return R.drawable.notification;

        switch (type) {
            case "lamaran":
                return R.drawable.iconloker;
            case "system":
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
}