package com.example.proj;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.proj.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.Button;

import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * The main entry point of the application after login.
 * <p>
 * This activity manages the primary navigation via a {@link BottomNavigationView},
 * hosting the Profile, Chats, and Search fragments. It also acts as a security gatekeeper,
 * ensuring that the user is authenticated, their email is verified, and their
 * profile data is complete.
 */
public class MainActivity extends AppCompatActivity {

    /** ViewBinding instance for accessing layout views. */
    ActivityMainBinding binding;

    /** Spinner for selecting weak subjects (initialized but managed via adapters). */
    Spinner weakSubSpinner;

    /** Spinner for selecting strong subjects (initialized but managed via adapters). */
    Spinner strongSubSpinner;

    /** The bottom navigation bar for switching between main app sections. */
    private BottomNavigationView bottomNavigationView;

    /** The container where fragments are loaded. */
    private FrameLayout framelayout;

    /** Button to trigger username editing. */
    Button userNameEditBtn;

    /** Button to save or edit user bio. */
    Button bioEditBtn;


    /**
     * Initializes the activity, sets up ViewBinding, and configures navigation listeners.
     * It also performs initial checks for user authentication and email verification.
     *
     * @param savedInstanceState If the activity is being re-initialized, this contains the data.
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Default fragment to load on startup
        replace_fragment(new ProfileFragment());

        weakSubSpinner = findViewById(R.id.weakSubSpinner);
        strongSubSpinner = findViewById(R.id.strongSubSpinner);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        framelayout = findViewById(R.id.framelayout);
        bioEditBtn = findViewById(R.id.saveBioBtn);

        // Handle navigation item selection
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.profile) {
                replace_fragment(new ProfileFragment());
            } else if (itemId == R.id.chats) {
                replace_fragment(new ChatsFragment());
            } else if (itemId == R.id.search) {
                replace_fragment(new SearchFragment());
            } else {
                return false;
            }
            return true;
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Check if user has verified their email address
            if (user.isEmailVerified()) {
                checkUserProfileStatus();
            } else {
                // Force logout if email is not verified
                Toast.makeText(this, "Please verify your email first!", Toast.LENGTH_LONG).show();
                FirebaseAuth.getInstance().signOut();
                navigateToLogin();
            }
        } else {
            navigateToLogin();
        }
    }

    /**
     * Ensures that there is a logged-in user whenever the activity starts.
     */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FBRef.refAuth.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
        }
    }

    /**
     * Swaps the current fragment in the FrameLayout.
     *
     * @param fragment The new fragment to display.
     */
    private void replace_fragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.framelayout, fragment);
        fragmentTransaction.commit();
    }

    /**
     * Inflates the top options menu.
     * @param menu The menu object.
     * @return true to display the menu.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Handles clicks on the top options menu, specifically the "requests" activity navigation.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String id = item.getTitle().toString();
        if (id.equals("requests")) {
            Intent intent = new Intent(this, chatsRequestActivity.class);
            startActivity(intent);
            return super.onOptionsItemSelected(item);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Validates if the user's mandatory profile information is complete.
     *
     * @param user The user object retrieved from the database.
     * @return true if the profile contains a username, false otherwise.
     */
    private boolean isProfileComplete(Users user) {
        return user != null && user.userName != null && !user.userName.isEmpty();
    }

    /**
     * Listens to the user's data node in Firebase.
     * If the profile is missing a username, it hides the navigation bar and
     * locks the user into the ProfileFragment until they provide one.
     */
    private void checkUserProfileStatus() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(myId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    handleIncompleteProfile();
                    return;
                }

                Users myUser = snapshot.getValue(Users.class);

                if (myUser == null || !isProfileComplete(myUser)) {
                    handleIncompleteProfile();
                } else {
                    // Profile is complete, allow full navigation
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error: " + error.getMessage());
                if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                    FirebaseAuth.getInstance().signOut();
                    navigateToLogin();
                }
            }
        });
    }

    /**
     * Helper to show a message and lock navigation when the profile is incomplete.
     */
    private void handleIncompleteProfile() {
        Toast.makeText(MainActivity.this, "Please enter a user name by clicking edit profile", Toast.LENGTH_LONG).show();
        lockNavigation();
    }

    /**
     * Hides the navigation bar and forces the user to the Profile screen.
     */
    private void lockNavigation() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.framelayout, new ProfileFragment())
                .commit();
    }

    /**
     * Helper method to finish this activity and return to the Login screen.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}