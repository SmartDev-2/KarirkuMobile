package com.tem2.karirku;

public class ChatListItem {
    private String name;
    private String lastMessage;
    private String time;
    private int profileRes;

    public ChatListItem(String name, String lastMessage, String time, int profileRes) {
        this.name = name;
        this.lastMessage = lastMessage;
        this.time = time;
        this.profileRes = profileRes;
    }

    public String getName() { return name; }
    public String getLastMessage() { return lastMessage; }
    public String getTime() { return time; }
    public int getProfileRes() { return profileRes; }
}
