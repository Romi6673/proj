package com.example.proj;

import java.util.HashMap;

/**
 * Represents a chat room entity for communication between a guide and a student.
 * This class stores room identification, participant details, the subject of discussion,
 * and a collection of messages.
 */
public class ChatRoom {

    /** Unique identifier for the chat room. */
    public String roomId;

    /** The unique ID of the guide (מדריך) participating in the chat. */
    public String guideUserId;

    /** The unique ID of the student (חניך) participating in the chat. */
    public String studentUserId;

    /** The topic or subject of the chat room. */
    public String subject;

    /**
     * A map of messages in the room, where the key is the Firebase message ID
     * and the value is the Message object.
     */
    public HashMap<String, Message> messages;

    /**
     * Default constructor required for calls to DataSnapshot.getValue(ChatRoom.class).
     * Initializes the messages HashMap to prevent null pointer exceptions.
     */
    public ChatRoom() {
        this.messages = new HashMap<>();
    }

    /**
     * Constructs a new ChatRoom with the specified identifiers and subject.
     *
     * @param roomId        The unique ID for the room.
     * @param guideUserId   The ID of the guide.
     * @param studentUserId The ID of the student.
     * @param subject       The topic of the chat room.
     */
    public ChatRoom(String roomId, String guideUserId, String studentUserId, String subject) {
        this.roomId = roomId;
        this.guideUserId = guideUserId;
        this.studentUserId = studentUserId;
        this.subject = subject;
        this.messages = new HashMap<>();
    }
}