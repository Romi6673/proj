package com.example.proj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatListAdapter extends BaseAdapter {
    Context context;
    ArrayList<ChatRoom> chatRooms;
    String myId;

    public ChatListAdapter(Context context, ArrayList<ChatRoom> chatRooms) {
        this.context = context;
        this.chatRooms = chatRooms;
        this.myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public int getCount() { return chatRooms.size(); }
    @Override
    public Object getItem(int i) { return chatRooms.get(i); }
    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.chat_list_item, parent, false);
        }

        ChatRoom room = chatRooms.get(i);
        ImageView ivProfile = view.findViewById(R.id.ivOtherUser);
        TextView tvName = view.findViewById(R.id.tvChatUserName);

        // זיהוי ה-ID של הצד השני
        String otherUserId = room.guideUserId.equals(myId) ? room.studentUserId : room.guideUserId;

        // שליפת פרטי המשתמש השני מה-Database
        FirebaseDatabase.getInstance().getReference("Users").child(otherUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users otherUser = snapshot.getValue(Users.class);
                        if (otherUser != null) {
                            tvName.setText(otherUser.userName);
                            if (otherUser.profilePicUrl != null && !otherUser.profilePicUrl.isEmpty()) {
                                Glide.with(context).load(otherUser.profilePicUrl).circleCrop().into(ivProfile);
                            } else {
                                ivProfile.setImageResource(R.drawable.outline_account_circle_24);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        return view;
    }
}