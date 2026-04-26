package com.example.proj;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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
            //בכל לחיצה על איבר בlistview
            ChatRoom selected = myRooms.get(position); // מציאת החדר המתאים בעזרת האינדקס של הlistview
            Intent intent = new Intent(getActivity(), SingleChatActivity.class);
            intent.putExtra("roomId", selected.roomId);
            //נותנים לintent מידע על החדר כדי שידע איזה נתונים לעלות
            startActivity(intent);
        });

        // 3. הגדרת מאזין ללחיצה (OnClickListener)
        aiChatBtn.setOnClickListener(new View.OnClickListener() {
            //מאזין ללחיצה על כפתור צאט עם AI
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
        // 1. הפניה לרשימת ה-IDs המאוחדת של המשתמש
        DatabaseReference myChatsIdsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(myId)
                .child("myChatRooms");

        // 2. האזנה לרשימת ה-IDs (אם יתווסף צ'אט חדש, זה יתעדכן אוטומטית)
        myChatsIdsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myRooms.clear(); // מנקים את הרשימה הנוכחית כדי לבנות אותה מחדש

                if (!snapshot.exists()) {
                    // אם אין למשתמש צ'אטים בכלל
                    Toast.makeText(getContext(), "No chats yet!", Toast.LENGTH_LONG).show();
                    adapter.notifyDataSetChanged();
                    return;
                }

                // 3. מעבר על כל ה-IDs שקיימים ברשימה של המשתמש
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String roomId = ds.getValue(String.class);

                    if (roomId != null) {
                        // 4. שליפת פרטי החדר המלאים מתוך ענף ChatRooms לפי ה-ID
                        FirebaseDatabase.getInstance().getReference("ChatRooms")
                                .child(roomId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot roomSnapshot) {
                                        ChatRoom room = roomSnapshot.getValue(ChatRoom.class);
                                        if (room != null) {
                                            myRooms.add(room);
                                            // מעדכנים את האדפטר בכל פעם שחדר "מגיע" מהשרת
                                            adapter.notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e("Firebase", "Error fetching room: " + error.getMessage());
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading chat IDs: " + error.getMessage());
            }
        });
    }

}