package com.example.proj;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.app.AlertDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fragment responsible for searching and filtering potential tutors.
 * <p>
 * This fragment:
 * 1. Loads the current user's "Weak Subjects" to populate a search filter.
 * 2. Queries Firebase for other users who have those subjects marked as "Strong."
 * 3. Displays results in a ListView.
 * 4. Allows the user to view a tutor's profile in a dialog and send a {@link chatRequest}.
 */
public class SearchFragment extends Fragment {

    /** ListView to display the results of the search. */
    ListView userListView;

    /** Spinner for selecting which subject to filter by. */
    Spinner filterSpinner;

    /** List of users that match the current search criteria. */
    ArrayList<Users> suggestedUsers = new ArrayList<>();

    /** List of subject names the current user needs help with. */
    ArrayList<String> myWeakSubjectsList = new ArrayList<>();

    /** Custom adapter for rendering the user list. */
    Custom_Listview_users adapter;

    /** The unique ID of the currently authenticated user. */
    String currentUserId;

    /** Components for the custom User Detail dialog. */
    AlertDialog.Builder adb;
    LinearLayout userDetailDialog;
    ImageView dialogProfileImage;
    TextView dialogUsername, dialogBio, dialogScore;

    /**
     * Initializes the UI and sets up the search/filter logic.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        userListView = view.findViewById(R.id.userListView);
        filterSpinner = view.findViewById(R.id.filterSpinner);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize the custom AlertDialog for user details
        userDetailDialog = (LinearLayout) inflater.inflate(R.layout.other_user_review, null);
        dialogProfileImage = userDetailDialog.findViewById(R.id.imgProfile);
        dialogUsername = userDetailDialog.findViewById(R.id.tvUsername);
        dialogBio = userDetailDialog.findViewById(R.id.tvBio);
        dialogScore = userDetailDialog.findViewById(R.id.tvPoints);
        Button btnClose = userDetailDialog.findViewById(R.id.btnClose);

        adb = new AlertDialog.Builder(this.getContext());
        adb.setView(userDetailDialog);
        adb.setCancelable(true);

        final AlertDialog dialog = adb.create();
        btnClose.setOnClickListener(v -> dialog.dismiss());

        // Setup listener for clicking a user in the search results
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Users selectedUser = suggestedUsers.get(position);
                showUserDetails(selectedUser, dialog);
            }
        });

        loadMyWeakSubjectsIntoSpinner();

        // Filter search results whenever a new subject is selected in the Spinner
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSubject = myWeakSubjectsList.get(position);
                searchUsersBySubject(selectedSubject);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    /**
     * Fetches the current user's profile from Firebase to identify which subjects
     * are marked as 'true' in the weakSubjects map.
     */
    private void loadMyWeakSubjectsIntoSpinner() {
        FBRef.refUsers.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users currentUser = snapshot.getValue(Users.class);
                myWeakSubjectsList.clear();

                if (currentUser != null && currentUser.weakSubjects != null) {
                    for (Map.Entry<String, Boolean> entry : currentUser.weakSubjects.entrySet()) {
                        if (entry.getValue()) {
                            myWeakSubjectsList.add(entry.getKey());
                        }
                    }
                }

                if (myWeakSubjectsList.isEmpty()) {
                    myWeakSubjectsList.add("No weak subjects defined");
                }

                // Populate the spinner with the user's weak subjects
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, myWeakSubjectsList);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                filterSpinner.setAdapter(spinnerAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * Populates the custom dialog with a selected user's details and shows it.
     *
     * @param user   The tutor whose details should be shown.
     * @param dialog The AlertDialog instance to be displayed.
     */
    private void showUserDetails(Users user, AlertDialog dialog) {
        dialogUsername.setText(user.userName);
        dialogBio.setText(user.bio != null ? user.bio : "No bio available");
        dialogScore.setText(String.valueOf(user.score));

        // Use the close button as the "Action" button for sending requests
        Button btnAction = userDetailDialog.findViewById(R.id.btnClose);
        btnAction.setText("Send Chat Request");

        btnAction.setOnClickListener(v -> {
            sendRequest(user);
            dialog.dismiss();
        });

        // Load profile picture
        if (user.profilePicUrl != null && !user.profilePicUrl.isEmpty()) {
            Glide.with(this).load(user.profilePicUrl).into(dialogProfileImage);
        } else {
            dialogProfileImage.setImageResource(R.drawable.outline_account_circle_24);
        }

        dialog.show();
    }

    /**
     * Creates a new pending chat request in the "ChatRequests" node of the database.
     *
     * @param toUser The user receiving the request.
     */
    private void sendRequest(Users toUser) {
        String fromId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ChatRequests");
        String requestId = ref.push().getKey();

        // Default 'fromUserName' is set to "Student" until profile retrieval is enhanced
        chatRequest newRequest = new chatRequest(requestId, fromId, "Student", toUser.userId, "General", 0);

        ref.child(requestId).setValue(newRequest).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Request Sent to " + toUser.userName, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Searches all users in the database to find those who have the specified
     * subject marked as "Strong" and are not the current user.
     *
     * @param subject The subject name to filter by.
     */
    private void searchUsersBySubject(String subject) {
        FBRef.refUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                suggestedUsers.clear();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    Users otherUser = userSnap.getValue(Users.class);

                    // Logic: Match if subject is in otherUser's strongSubjects and is true
                    if (otherUser != null && !otherUser.userId.equals(currentUserId)) {
                        if (otherUser.strongSubjects != null &&
                                otherUser.strongSubjects.containsKey(subject) &&
                                otherUser.strongSubjects.get(subject)) {

                            suggestedUsers.add(otherUser);
                        }
                    }
                }

                if (isAdded() && getContext() != null) {
                    if (suggestedUsers.isEmpty()) {
                        Toast.makeText(getContext(), "No tutors found for " + subject, Toast.LENGTH_SHORT).show();
                    }

                    adapter = new Custom_Listview_users(getContext(), suggestedUsers);
                    userListView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}