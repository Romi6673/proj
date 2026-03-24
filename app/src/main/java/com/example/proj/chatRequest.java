package com.example.proj;
public class chatRequest {
    public String requestId;
    public String fromUserId;
    public String fromUserName; // כדאי להוסיף כדי שלא נצטרך לחפש את השם כל פעם
    public String toUserId;
    public String subject;
    public int status; // 0=WAITING, 1=DECLINED, 2=ACCEPTED

    public chatRequest() {} // בנאי ריק חובה

    public chatRequest(String requestId, String fromUserId, String fromUserName, String toUserId, String subject, int status) {
        this.requestId = requestId;
        this.fromUserId = fromUserId;
        this.fromUserName = fromUserName;
        this.toUserId = toUserId;
        this.subject = subject;
        this.status = status;
    }
}
