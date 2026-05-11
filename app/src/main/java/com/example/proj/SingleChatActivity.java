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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Activity that hosts a real-time chat session between two users.
 * <p>
 * This class manages:
 * 1. Loading and displaying messages from a specific Firebase ChatRoom.
 * 2. Sending new messages.
 * 3. Notifying the user of incoming messages when the activity is active.
 * 4. Monitoring network connectivity to alert the user of connection loss.
 * 5. Handling runtime permissions for notifications on Android 13+.
 */
public class SingleChatActivity extends AppCompatActivity {

    /** UI component for typing messages. */
    EditText etMessage;

    /** Button to trigger message sending. */
    ImageButton btnSend;

    /** ListView to display the conversation history. */
    ListView chatListView;

    /** The ID of the current chat room and the current authenticated user. */
    String roomId, myId;

    /** List containing message objects for the current room. */
    ArrayList<Message> messageList;

    /** Adapter for binding message data to the chat ListView. */
    MessageAdapter adapter;

    /**
     * Initializes the activity, sets up the message adapter, and handles
     * notification permissions for newer Android versions.
     */
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

        // Permission check for Android 13 (Tiramisu) and above for notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    /**
     * Sets up a Realtime Database listener for the "messages" node of the current room.
     * <p>
     * Clears and repopulates the local list on every change, sorts messages by
     * timestamp, and triggers a notification if the latest message was sent by
     * another user.
     */
    private void loadMessages() {
        FirebaseDatabase.getInstance().getReference("ChatRooms").child(roomId).child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageList.clear();
                        Message lastMessage = null;

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Message msg = ds.getValue(Message.class);
                            if (msg != null) {
                                messageList.add(msg);
                                lastMessage = msg;
                            }
                        }

                        // Chronological sorting
                        Collections.sort(messageList, (m1, m2) -> Long.compare(m1.timestamp, m2.timestamp));
                        adapter.notifyDataSetChanged();

                        // Notification logic: Notify if there's a new message from the other participant
                        if (lastMessage != null && !lastMessage.senderId.equals(myId)) {
                            showNotification(lastMessage.content);
                        }

                        // Auto-scroll to the bottom of the list
                        chatListView.setSelection(messageList.size() - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    /**
     * BroadcastReceiver to monitor network connectivity changes.
     * Alerts the user via Toast if the internet connection is lost.
     */
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

    /**
     * Displays a local system notification for a new message.
     * <p>
     * Handles the creation of a NotificationChannel for Android Oreo (8.0) and above.
     *
     * @param message The content of the incoming message to display in the notification.
     */
    private void showNotification(String message) {
        String channelId = "chat_notifications";
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Required for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Messages", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, SingleChatActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle("new message :) ")
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        manager.notify(1, builder.build());
    }

    /**
     * Registers the network connectivity receiver when the activity starts.
     */
    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    /**
     * Unregisters the network connectivity receiver when the activity stops to save resources.
     */
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(networkReceiver);
    }

    /**
     * Pushes a new {@link Message} object to the Firebase Realtime Database.
     *
     * @param text The text content of the message to be sent.
     */
    private void sendMessage(String text) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ChatRooms")
                .child(roomId).child("messages");
        Message msg = new Message(myId, text);
        ref.push().setValue(msg);
    }
}