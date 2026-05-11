package com.example.proj;

/**
 * Represents a single message within a chat room.
 * This class is designed to be compatible with Firebase Realtime Database.
 */
public class Message {

    /** The unique ID of the user who sent the message. */
    public String senderId;

    /** The text content of the message. */
    public String content;

    /**
     * The time the message was sent in milliseconds.
     * Using a long allows for easy chronological sorting in the UI.
     */
    public long timestamp;

    /**
     * Default constructor required for calls to DataSnapshot.getValue(Message.class).
     * Necessary for Firebase serialization.
     */
    public Message() {}

    /**
     * Constructs a new Message and automatically assigns the current system time as the timestamp.
     *
     * @param senderId The ID of the user sending the message.
     * @param content  The text message to be sent.
     */
    public Message(String senderId, String content) {
        this.senderId = senderId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }
}