package com.example.proj;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.widget.TextView;

import com.example.proj.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseUser;

import android.widget.Button;

import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    Spinner weakSubSpinner;
    Spinner strongSubSpinner;

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

    private void replace_fragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.framelayout, fragment);
        fragmentTransaction.commit();
    }
}