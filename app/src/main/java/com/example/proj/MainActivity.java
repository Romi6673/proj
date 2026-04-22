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

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    Spinner weakSubSpinner;
    Spinner strongSubSpinner;
    private BottomNavigationView bottomNavigationView;
    private FrameLayout framelayout;

    Button userNameEditBtn;
    Button bioEditBtn;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replace_fragment(new ProfileFragment());
        weakSubSpinner = findViewById(R.id.weakSubSpinner);
        strongSubSpinner = findViewById(R.id.strongSubSpinner);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        framelayout = findViewById(R.id.framelayout);
        //userNameEditBtn = findViewById(R.id.userNameEditBtn);
        bioEditBtn = findViewById(R.id.saveBioBtn);


        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId(); // נשמור את ה-ID במשתנה

            if (itemId == R.id.profile) {
                replace_fragment(new ProfileFragment());
            } else if (itemId == R.id.chats) {
                replace_fragment(new ChatsFragment());
            } else if (itemId == R.id.search) {
                replace_fragment(new SearchFragment());
            } else {
                return false; // במקרה ששום דבר לא נמצא
            }
            return true;
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            checkUserProfileStatus();
            if (user != null) {
                // בדיקה: האם המשתמש אימת את המייל שלו?
                if (user.isEmailVerified()) {
                    // רק אם המייל מאומת, נבדוק אם הפרופיל (שם ומקצועות) מושלם
                    checkUserProfileStatus();
                } else {
                    // המייל לא מאומת! נותנים הודעה, מתנתקים ושולחים למסך התחברות
                    Toast.makeText(this, "Please verify your email first!", Toast.LENGTH_LONG).show();
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                // אין משתמש מחובר בכלל - עוברים למסך התחברות
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FBRef.refAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void replace_fragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.framelayout, fragment);
        fragmentTransaction.commit();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String id = item.getTitle().toString();
        if (id.equals("requests")) {
            Intent intent = new Intent(this, chatsRequestActivity.class);
            startActivity(intent);

            return super.onOptionsItemSelected(item);
        }else{
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return super.onOptionsItemSelected(item);
        }

    }

    // פונקציה שבודקת האם הפרופיל מושלם לפי המודל שלך
    private boolean isProfileComplete(Users user) {
        // בדיקה שכל השדות שדרשת קיימים ולא ריקים
        return user != null &&
                user.userName != null && !user.userName.isEmpty() &&
                user.strongSubjects != null && !user.strongSubjects.isEmpty() &&
                user.weakSubjects != null && !user.weakSubjects.isEmpty();
    }

    private void checkUserProfileStatus() {
        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(myId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // אם המשתמש בכלל לא קיים ב-Database (משתמש חדש לגמרי)
                if (!snapshot.exists()) {
                    lockNavigation(); // פונקציה שתעביר לפרגמנט פרופיל
                    return;
                }

                Users myUser = snapshot.getValue(Users.class);

                if (myUser == null || !isProfileComplete(myUser)) {
                    lockNavigation();
                } else {
                    // הפרופיל מושלם - מציגים תפריט
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // במקום טואסט, נרשום את זה רק בלוג למפתחת (לך)
                Log.e("FirebaseError", "Error: " + error.getMessage());

                // אם זו שגיאת הרשאות, פשוט נשלח אותו להתחבר מחדש
                if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        });
    }

    // פונקציית עזר קטנה כדי שלא נשכפל קוד
    private void lockNavigation() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.framelayout, new ProfileFragment())
                .commit();
    }

}