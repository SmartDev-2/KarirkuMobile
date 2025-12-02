package com.tem2.karirku;

import java.util.Calendar;

public class Message {
    private String id;
    private String senderId;
    private String senderName;
    private String content;
    private Calendar timestamp;
    private boolean isSentByMe;

    public Message(String id, String senderId, String senderName, String content, Calendar timestamp, boolean isSentByMe) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.timestamp = timestamp;
        this.isSentByMe = isSentByMe;
    }

    // Getters
    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getContent() { return content; }
    public Calendar getTimestamp() { return timestamp; }
    public boolean isSentByMe() { return isSentByMe; }
}