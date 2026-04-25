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
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatRooms");
        String roomId = chatRef.push().getKey(); // יצירת מזהה לצ'אט

        ChatRoom newRoom = new ChatRoom(roomId, req.toUserId, req.fromUserId , req.subject); // המקבל הופך למדריך

        chatRef.child(roomId).setValue(newRoom).addOnSuccessListener(unused -> {
            // הוספת חדר חדש תחת הענף chatrooms
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(req.toUserId).child("myGuideChats");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                //המאזין מאזין לתקייה myGuidedChats כי לשם מצביע userRef

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<String> chats = new ArrayList<>(); // רשימת הצאטים לפני הוספת הצאט החדש
                    if (snapshot.exists()) { // בדיקה האם קיים myGuidedChats
                        for (DataSnapshot ds : snapshot.getChildren()) chats.add(ds.getValue(String.class));// הוספת הצאטים הקיימים לרשימת הצאטים
                    }
                    if (!chats.contains(roomId)) {
                        chats.add(roomId);
                        userRef.setValue(chats);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });
    }
}