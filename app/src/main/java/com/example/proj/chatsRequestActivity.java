package com.example.proj;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Activity responsible for displaying and managing incoming chat requests.
 * It filters requests from Firebase to show only those directed to the current user
 * that are still in a "pending" status.
 */
public class chatsRequestActivity extends AppCompatActivity {

    /** ListView to display the list of incoming chat requests. */
    ListView lvFollowRequests;

    /** Data source containing the list of chat requests retrieved from the database. */
    ArrayList<chatRequest> requestList;

    /** Custom adapter used to bind the chatRequest data to the ListView rows. */
    custom_lv_request_adapter adapter;

    /**
     * Initializes the activity, sets the content view, and triggers the data loading process.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_request);
        lvFollowRequests = findViewById(R.id.lvFollowRequests);
        requestList = new ArrayList<>();

        loadRequests();
    }

    /**
     * Connects to Firebase Realtime Database to load chat requests.
     * It queries the "ChatRequests" node and filters results where "toUserId"
     * matches the current user's ID. Only requests with status 0 (WAITING) are added to the list.
     */
    private void loadRequests() {
        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ChatRequests");

        // Filter: Get only requests sent to me (toUserId == myId)
        ref.orderByChild("toUserId").equalTo(myId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    chatRequest req = ds.getValue(chatRequest.class);
                    // Show only pending requests (status == 0)
                    if (req != null && req.status == 0) {
                        requestList.add(req);
                    }
                }

                // Check if the list is empty and notify the user
                if (requestList.isEmpty()) {
                    Toast.makeText(chatsRequestActivity.this, "No pending requests at the moment", Toast.LENGTH_SHORT).show();
                }

                // Initialize and set the adapter with the filtered list
                adapter = new custom_lv_request_adapter(chatsRequestActivity.this, requestList);
                lvFollowRequests.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(chatsRequestActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Increments a specific user's score in the database.
     * Uses a Firebase Transaction to ensure thread safety and prevent data overwriting
     * when multiple updates occur simultaneously.
     *
     * @param userId The unique ID of the user whose score should be updated.
     * @param points The number of points to add to the user's current score.
     */
    public void addPointsToUser(String userId, int points) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("score");

        userRef.runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @NonNull
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData mutableData) {
                // Using Integer object to handle potential null values if the path doesn't exist yet
                Integer currentScore = mutableData.getValue(Integer.class);

                if (currentScore == null) {
                    mutableData.setValue(points);
                } else {
                    mutableData.setValue(currentScore + points);
                }
                return com.google.firebase.database.Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable com.google.firebase.database.DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                // Transaction completed logic can be added here if needed
            }
        });
    }

    /**
     * Inflates the menu resource into the existing menu.
     * @param menu The options menu in which you place your items.
     * @return True for the menu to be displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Handles action bar item clicks.
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String id = item.getTitle().toString();
        if (id.equals("requests")) {
            // Stay in or restart the current activity
            Intent intent = new Intent(this, chatsRequestActivity.class);
            startActivity(intent);
            return super.onOptionsItemSelected(item);
        } else {
            // Navigate back to the main activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return super.onOptionsItemSelected(item);
        }
    }
}