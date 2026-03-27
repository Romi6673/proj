package com.example.proj;

public class Message {
    public String senderId; // מי שלח
    public String content;  // תוכן ההודעה
    public long timestamp;  // זמן שליחה (עדיף long עבור סדר כרונולוגי)

    public Message() {} // חובה

    public Message(String senderId, String content) {
        this.senderId = senderId;
        this.content = content;
        this.timestamp = System.currentTimeMillis(); // יוצר זמן נוכחי אוטומטית
    }
}