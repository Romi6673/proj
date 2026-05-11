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

/**
 * Custom adapter for a ListView that displays incoming {@link chatRequest} objects.
 * <p>
 * This adapter handles:
 * 1. Displaying request details (subject and sender info).
 * 2. Fetching sender profile data (name and image) asynchronously from Firebase.
 * 3. Handling Accept/Decline actions.
 * 4. Triggering ChatRoom creation and user scoring updates.
 */
public class custom_lv_request_adapter extends BaseAdapter {

    /** Context used for layout inflation and starting activities. */
    Context context;

    /** List of chat requests to be displayed. */
    ArrayList<chatRequest> requests;

    /** Used to instantiate the row layout XML into View objects. */
    private LayoutInflater inflater;

    /**
     * Constructs a new custom_lv_request_adapter.
     *
     * @param context  The current context (expected to be {@link chatsRequestActivity}).
     * @param requests The list of pending chat requests.
     */
    public custom_lv_request_adapter(Context context, ArrayList<chatRequest> requests) {
        this.context = context;
        this.requests = requests;
        this.inflater = LayoutInflater.from(context);
    }

    /** @return The number of requests in the list. */
    @Override
    public int getCount() { return requests.size(); }

    /** @return The {@link chatRequest} object at the specified position. */
    @Override
    public Object getItem(int i) { return requests.get(i); }

    /** @return The position as the item ID. */
    @Override
    public long getItemId(int i) { return i; }

    /**
     * Provides a view for each row in the list.
     * <p>
     * Performs a Firebase lookup to find the sender's details (userName and profilePicUrl)
     * based on the {@code fromUserId} stored in the request.
     *
     * @param i           The position of the item.
     * @param view        The recycled view.
     * @param viewGroup   The parent view group.
     * @return The populated view for the request row.
     */
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

        // Fetch sender details using the fromUserId saved in the request
        FirebaseDatabase.getInstance().getReference("Users").child(req.fromUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users sender = snapshot.getValue(Users.class);
                        if (sender != null) {
                            tvUserName.setText(sender.userName);

                            // Load sender's profile image using Glide
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

        // 2 = ACCEPTED, 1 = DECLINED
        btnAccept.setOnClickListener(v -> updateStatus(req, 2));
        btnDecline.setOnClickListener(v -> updateStatus(req, 1));

        return view;
    }

    /**
     * Updates the status of a chat request in the database.
     * <p>
     * If the status is set to 2 (Accepted):
     * 1. A new chat room is created.
     * 2. Reward points (50) are added to the recipient (the guide) via {@link chatsRequestActivity#addPointsToUser}.
     *
     * @param req       The request object being updated.
     * @param newStatus The new status code (1 for decline, 2 for accept).
     */
    private void updateStatus(chatRequest req, int newStatus) {
        DatabaseReference reqRef = FirebaseDatabase.getInstance().getReference("ChatRequests").child(req.requestId);
        reqRef.child("status").setValue(newStatus).addOnSuccessListener(unused -> {
            if (newStatus == 2) {
                createChatRoom(req);

                // Check if context is an instance of chatsRequestActivity to grant points
                if (context instanceof chatsRequestActivity) {
                    ((chatsRequestActivity) context).addPointsToUser(req.toUserId, 50);
                }
            }
        });
    }

    /**
     * Generates a new unique ChatRoom in Firebase.
     * <p>
     * This creates a {@link ChatRoom} object and links it to both the sender (student)
     * and the recipient (guide) in their respective "myChatRooms" lists.
     *
     * @param req The accepted chat request.
     */
    private void createChatRoom(chatRequest req) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        String roomId = rootRef.child("ChatRooms").push().getKey();

        // Create the room object
        ChatRoom newRoom = new ChatRoom(roomId, req.toUserId, req.fromUserId, req.subject);

        // 1. Save the room to the global ChatRooms node
        rootRef.child("ChatRooms").child(roomId).setValue(newRoom).addOnSuccessListener(unused -> {
            // 2. Add room ID to the guide's chat list (the user who accepted)
            addRoomToUserList(req.toUserId, roomId);

            // 3. Add room ID to the student's chat list (the user who sent the request)
            addRoomToUserList(req.fromUserId, roomId);
        });
    }

    /**
     * Synchronizes the user's private list of active chat rooms.
     * <p>
     * Fetches the current list of room IDs for the user, checks for duplicates,
     * and appends the new room ID.
     *
     * @param userId The ID of the user whose list is being updated.
     * @param roomId The ID of the room to add.
     */
    private void addRoomToUserList(String userId, String roomId) {
        DatabaseReference userChatsRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(userId).child("myChatRooms");

        userChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> chats = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        chats.add(ds.getValue(String.class));
                    }
                }
                // Only add if the room isn't already in the list
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