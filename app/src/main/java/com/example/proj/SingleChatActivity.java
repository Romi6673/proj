package com.example.proj;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class SingleChatActivity extends AppCompatActivity {
    EditText etMessage;
    ImageButton btnSend;
    ListView chatListView;
    String roomId, myId;
    ArrayList<Message> messageList;
    MessageAdapter adapter; // צריך ליצור אדפטר פשוט להודעות

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat);

        roomId = getIntent().getStringExtra("roomId");
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        chatListView = findViewById(R.id.chatListView);

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(this, messageList, myId);
        chatListView.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadMessages();

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
                etMessage.setText("");
            }
        });
    }

    private void loadMessages() {
        FirebaseDatabase.getInstance().getReference("ChatRooms").child(roomId).child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            messageList.add(ds.getValue(Message.class));
                        }
                        // מיון לפי זמן
                        Collections.sort(messageList, (m1, m2) -> Long.compare(m1.timestamp, m2.timestamp));
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void sendMessage(String text) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ChatRooms")
                .child(roomId).child("messages");
        Message msg = new Message(myId, text);
        ref.push().setValue(msg);
    }
}