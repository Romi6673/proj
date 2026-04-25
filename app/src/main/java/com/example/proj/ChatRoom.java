package com.example.proj;

import java.util.HashMap;

public class ChatRoom {
    public String roomId;
    public String guideUserId;   // ה-ID של המדריך
    public String studentUserId; // ה-ID של החניך
    public String subject;
    public HashMap<String, Message> messages; // המפתח יהיה ה-ID של ההודעה מפיירבייס

    public ChatRoom() {
        this.messages = new HashMap<>(); // חובה לאתחל כדי למנוע קריסה
    }

    public ChatRoom(String roomId, String guideUserId, String studentUserId, String subject) {
        this.roomId = roomId;
        this.guideUserId = guideUserId;
        this.studentUserId = studentUserId;
        this.messages = new HashMap<>();
    }
}