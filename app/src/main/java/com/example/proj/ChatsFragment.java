package com.example.proj;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
public class ChatsFragment extends Fragment {
    ListView chatsListView;
    ArrayList<ChatRoom> myRooms;
    ChatListAdapter adapter; // האדפטר החדש
    String myId;
    Button aiChatBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        aiChatBtn = view.findViewById(R.id.aiChatBtn);
        chatsListView = view.findViewById(R.id.chatsListView);
        myRooms = new ArrayList<>();
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // הגדרת האדפטר כבר כאן
        adapter = new ChatListAdapter(getContext(), myRooms);
        chatsListView.setAdapter(adapter);

        loadMyChats();

        chatsListView.setOnItemClickListener((parent, v, position, id) -> {
            ChatRoom selected = myRooms.get(position);
            Intent intent = new Intent(getActivity(), SingleChatActivity.class);
            intent.putExtra("roomId", selected.roomId);
            startActivity(intent);
        });

        // 3. הגדרת מאזין ללחיצה (OnClickListener)
        aiChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // מעבר למסך ה-AI
                Intent intent = new Intent(getActivity(), AiChatActivity.class);
                startActivity(intent);
            }
        });


        return view;
    }

    private void loadMyChats() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ChatRooms");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myRooms.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ChatRoom room = ds.getValue(ChatRoom.class);
                    if (room != null && (room.guideUserId.equals(myId) || room.studentUserId.equals(myId))) {
                        myRooms.add(room);
                    }
                }
                adapter.notifyDataSetChanged(); // מעדכן את הרשימה המעוצבת
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


}