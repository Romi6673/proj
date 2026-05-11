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

/**
 * A fragment that displays a list of active chat rooms for the current user.
 * It handles fetching chat IDs from the user's profile and then retrieving
 * the full room details from the database.
 */
public class ChatsFragment extends Fragment {

    /** ListView to display the list of chats. */
    ListView chatsListView;

    /** List containing the ChatRoom objects to be displayed. */
    ArrayList<ChatRoom> myRooms;

    /** Custom adapter for rendering ChatRoom objects in the ListView. */
    ChatListAdapter adapter;

    /** The unique ID of the currently authenticated user. */
    String myId;

    /** Button to navigate to the AI Chat activity. */
    Button aiChatBtn;

    /**
     * Called to have the fragment instantiate its user interface view.
     * Initializes UI components, the adapter, and sets up click listeners.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        aiChatBtn = view.findViewById(R.id.aiChatBtn);
        chatsListView = view.findViewById(R.id.chatsListView);
        myRooms = new ArrayList<>();
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize the adapter and attach it to the ListView
        adapter = new ChatListAdapter(getContext(), myRooms);
        chatsListView.setAdapter(adapter);

        loadMyChats();

        // Listener for clicking on a specific chat room in the list
        chatsListView.setOnItemClickListener((parent, v, position, id) -> {
            ChatRoom selected = myRooms.get(position);
            Intent intent = new Intent(getActivity(), SingleChatActivity.class);
            // Pass the room ID to the activity so it knows which messages to load
            intent.putExtra("roomId", selected.roomId);
            startActivity(intent);
        });

        // Listener for the AI Chat button
        aiChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AiChatActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    /**
     * Fetches the chat rooms associated with the current user from Firebase.
     * 1. Retrieves the list of chat room IDs from Users/{myId}/myChatRooms.
     * 2. For each ID, fetches the full ChatRoom object from the "ChatRooms" node.
     */
    private void loadMyChats() {
        // Reference to the list of chat IDs for the current user
        DatabaseReference myChatsIdsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(myId)
                .child("myChatRooms");

        // Listen for changes in the user's chat list
        myChatsIdsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myRooms.clear();

                if (!snapshot.exists()) {
                    Toast.makeText(getContext(), "No chats yet!", Toast.LENGTH_LONG).show();
                    adapter.notifyDataSetChanged();
                    return;
                }

                // Iterate through the list of room IDs
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String roomId = ds.getValue(String.class);

                    if (roomId != null) {
                        // Fetch the full details for each specific room ID
                        FirebaseDatabase.getInstance().getReference("ChatRooms")
                                .child(roomId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot roomSnapshot) {
                                        ChatRoom room = roomSnapshot.getValue(ChatRoom.class);
                                        if (room != null) {
                                            myRooms.add(room);
                                            // Update the UI as each room is loaded
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