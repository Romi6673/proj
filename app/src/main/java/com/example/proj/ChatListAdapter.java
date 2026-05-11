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

/**
 * Adapter for displaying a list of chat rooms in a ListView.
 * This adapter manages the layout for each chat entry, displaying the subject and 
 * fetching the other participant's profile information from Firebase.
 */
public class ChatListAdapter extends BaseAdapter {
    Context context;
    ArrayList<ChatRoom> chatRooms;
    String myId;

    /**
     * Initializes the adapter with the context and the list of chat rooms.
     * It also identifies the current user's ID for filtering participants.
     *
     * @param context   The current context.
     * @param chatRooms The list of chat rooms to display.
     */
    public ChatListAdapter(Context context, ArrayList<ChatRoom> chatRooms) {
        this.context = context;
        this.chatRooms = chatRooms;
        this.myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    /**
     * Returns the size of the chat rooms list.
     * @return The number of chat rooms in the data set.
     */
    @Override
    public int getCount() { return chatRooms.size(); }

    /**
     * Returns the chat room at the specified position.
     * @param i Position of the item.
     * @return The ChatRoom object at the specified position.
     */
    @Override
    public Object getItem(int i) { return chatRooms.get(i); }

    /**
     * Returns the row ID of the item.
     * @param i Position of the item.
     * @return The row ID (the position).
     */
    @Override
    public long getItemId(int i) { return i; }

    /**
     * Creates or recycles a view for each chat room item.
     * Sets the subject and asynchronously loads the other user's name and profile image.
     *
     * @param i      Position of the item.
     * @param view   The old view to reuse, if possible.
     * @param parent The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.chat_list_item, parent, false);
        }

        ChatRoom room = chatRooms.get(i);
        ImageView ivProfile = view.findViewById(R.id.ivOtherUser);
        TextView tvSubjectChat = view.findViewById(R.id.tvSubjectChat);
        TextView tvName = view.findViewById(R.id.tvChatUserName);


        String otherUserId = room.guideUserId.equals(myId) ? room.studentUserId : room.guideUserId;
        tvSubjectChat.setText(room.subject);

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
