package com.example.proj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class custom_lv_request_adapter extends BaseAdapter {
    Context context;
    ArrayList<chatRequest> requests;
    private LayoutInflater inflater;


    public custom_lv_request_adapter(Context context, ArrayList<chatRequest> requests) {
        this.context = context;
        this.requests = requests;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() { return requests.size(); }

    @Override
    public Object getItem(int i) { return requests.get(i); }

    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.custom_lv_requests, viewGroup, false);
        }

        chatRequest req = requests.get(i);

        TextView tvUserName = view.findViewById(R.id.tvUserName);
        TextView tvSubject = view.findViewById(R.id.tvSubject);
        ImageView ivProfilePic = view.findViewById(R.id.ivProfileImage);
        Button btnAccept = view.findViewById(R.id.btnAccept);
        Button btnDecline = view.findViewById(R.id.btnDecline);

        tvSubject.setText("Wants help with: " + req.subject);

        // שליפת פרטי השולח לפי ה-ID ששמור בבקשה
        FirebaseDatabase.getInstance().getReference("Users").child(req.fromUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users sender = snapshot.getValue(Users.class);
                        if (sender != null) {
                            tvUserName.setText(sender.userName);

                            // טעינת התמונה של השולח
                            if (sender.profilePicUrl != null && !sender.profilePicUrl.isEmpty()) {
                                Glide.with(context).load(sender.profilePicUrl).into(ivProfilePic);
                            } else {
                                ivProfilePic.setImageResource(R.drawable.outline_account_circle_24);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        btnAccept.setOnClickListener(v -> updateStatus(req, 2)); // שלחי את האובייקט req עצמו
        btnDecline.setOnClickListener(v -> updateStatus(req, 1));

        return view;
    }



    // וזו הפונקציה המתוקנת (שימי לב שהיא מקבלת chatRequest):
    private void updateStatus(chatRequest req, int newStatus) {
        DatabaseReference reqRef = FirebaseDatabase.getInstance().getReference("ChatRequests").child(req.requestId);
        reqRef.child("status").setValue(newStatus).addOnSuccessListener(unused -> {
            if (newStatus == 2) {
                createChatRoom(req);

                // בדיקה האם הcontext הוא במסך של בקשות צאט כדי שיהיה אפשרי לעשות לו
                // המרה ולהפעיל עליו פונקציה של הchatRequestActivity
                if (context instanceof chatsRequestActivity) {
                    ((chatsRequestActivity) context).addPointsToUser(req.toUserId, 50);
                }
            }
        });
    }



    private void createChatRoom(chatRequest req) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        String roomId = rootRef.child("ChatRooms").push().getKey();
        //פנייה לענף chatroom ויצירת מזהה ייחודי חדש עבור חדר חדש.

        // יצירת אובייקט החדר
        ChatRoom newRoom = new ChatRoom(roomId, req.toUserId, req.fromUserId, req.subject);

        // 1. שמירת החדר בענף הכללי ChatRooms
        rootRef.child("ChatRooms").child(roomId).setValue(newRoom).addOnSuccessListener(unused -> {

            // 2. עדכון הרשימה של הmychats אצל המדריך (זה שאישר את הבקשה - toUserId)
            addRoomToUserList(req.toUserId, roomId);

            // 3. עדכון הרשימה של הmychats אצל התלמיד (זה ששלח את הבקשה - fromUserId)
            addRoomToUserList(req.fromUserId, roomId);
        });
    }

    // פונקציית עזר כללית שמעדכנת את רשימת הצ'אטים של משתמש ספציפי
    private void addRoomToUserList(String userId, String roomId) {
        DatabaseReference userChatsRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(userId).child("myChatRooms"); // השם הגנרי החדש שביקשת

        userChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> chats = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        chats.add(ds.getValue(String.class));
                    }
                }
                if (!chats.contains(roomId)) {
                    chats.add(roomId);
                    userChatsRef.setValue(chats);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}