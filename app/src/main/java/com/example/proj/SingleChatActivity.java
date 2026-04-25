package com.example.proj;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void loadMessages() {
        FirebaseDatabase.getInstance().getReference("ChatRooms").child(roomId).child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageList.clear();
                        Message lastMessage = null; // משתנה שיחזיק את ההודעה האחרונה

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Message msg = ds.getValue(Message.class);
                            if (msg != null) {
                                messageList.add(msg);
                                lastMessage = msg; // בסוף הלולאה, זה יהיה המסר הכי חדש
                            }
                        }

                        // מיון לפי זמן
                        Collections.sort(messageList, (m1, m2) -> Long.compare(m1.timestamp, m2.timestamp));
                        adapter.notifyDataSetChanged();

                        // --- לוגיקת ההתראות ---
                        // בודקים: 1. האם יש הודעות? 2. האם ההודעה האחרונה היא לא ממני?
                        if (lastMessage != null && !lastMessage.senderId.equals(myId)) {
                            showNotification("Partner", lastMessage.content);
                        }

                        // גלילה אוטומטית לסוף הרשימה כשמגיעה הודעה
                        chatListView.setSelection(messageList.size() - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            if (!isConnected) {
                Toast.makeText(context, "looks like there is no internet connection", Toast.LENGTH_LONG).show();
            }
        }
    };


    private void showNotification(String sender, String message) {
        String channelId = "chat_notifications";
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // יצירת ערוץ (חובה מגרסה אנדרואיד 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Messages", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        // מה יקרה כשילחצו על ההתראה
        Intent intent = new Intent(this, SingleChatActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // בניית ההתראה
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.stat_notify_chat) // הוספת אייקון מערכת בסיסי
                .setContentTitle("new message :) ")
                .setContentText(message)
                .setAutoCancel(true) // נעלם בלחיצה
                .setContentIntent(pendingIntent);

        manager.notify(1, builder.build());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // מאזינים לשינויים בסטטוס הקישוריות
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        // חשוב לבטל כדי לא לבזבז משאבים
        unregisterReceiver(networkReceiver);
    }

    private void sendMessage(String text) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ChatRooms")
                .child(roomId).child("messages");
        Message msg = new Message(myId, text);
        ref.push().setValue(msg);
    }
}