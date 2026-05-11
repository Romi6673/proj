package com.example.proj;

/**
 * Represents a chat request sent from one user to another.
 * This class is used to manage the state of a connection request,
 * including who sent it, who is receiving it, the subject, and the current status.
 */
public class chatRequest {

    /** Unique identifier for the request. */
    public String requestId;

    /** The ID of the user who initiated the request. */
    public String fromUserId;

    /** The display name of the user who initiated the request. */
    public String fromUserName;

    /** The ID of the user to whom the request is being sent. */
    public String toUserId;

    /** The subject or message content of the chat request. */
    public String subject;

    /**
     * The current status of the request:
     * 0 = WAITING (Pending response)
     * 1 = DECLINED (Request was rejected)
     * 2 = ACCEPTED (Request was approved)
     */
    public int status;

    /**
     * Default constructor required for calls to DataSnapshot.getValue(chatRequest.class).
     * Necessary for Firebase Realtime Database integration.
     */
    public chatRequest() {}

    /**
     * Constructs a new chatRequest with the specified details.
     *
     * @param requestId    Unique ID for the request.
     * @param fromUserId   ID of the sender.
     * @param fromUserName Name of the sender.
     * @param toUserId     ID of the recipient.
     * @param subject      The subject of the request.
     * @param status       The initial status (usually 0 for WAITING).
     */
    public chatRequest(String requestId, String fromUserId, String fromUserName, String toUserId, String subject, int status) {
        this.requestId = requestId;
        this.fromUserId = fromUserId;
        this.fromUserName = fromUserName;
        this.toUserId = toUserId;
        this.subject = subject;
        this.status = status;
    }

    // Getters and Setters are recommended for better data encapsulation

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getFromUserId() { return fromUserId; }
    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }

    public String getFromUserName() { return fromUserName; }
    public void setFromUserName(String fromUserName) { this.fromUserName = fromUserName; }

    public String getToUserId() { return toUserId; }
    public void setToUserId(String toUserId) { this.toUserId = toUserId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}
