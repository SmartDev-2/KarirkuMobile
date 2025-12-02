package com.tem2.karirku;

public class NotificationItem {
    private String id;
    private String title;
    private String message;
    private String time;
    private String type;
    private boolean isRead;
    private int iconRes;

    public NotificationItem(String id, String title, String message, String time, String type, boolean isRead, int iconRes) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.time = time;
        this.type = type;
        this.isRead = isRead;
        this.iconRes = iconRes;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public String getType() { return type; }
    public boolean isRead() { return isRead; }
    public int getIconRes() { return iconRes; }
    public void setRead(boolean read) { isRead = read; }
}