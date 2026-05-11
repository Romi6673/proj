package com.example.proj;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity that handles user login.
 * <p>
 * This class allows users to authenticate using an email and password via Firebase.
 * Upon successful login, it updates the application's persistent state to skip
 * the login screen in the future and redirects the user to the {@link MainActivity}.
 */
public class LoginActivity extends AppCompatActivity {

    /** EditText for entering the user's email address. */
    EditText emailEditText;

    /** EditText for entering the user's password. */
    EditText passwordEditText;

    /** Button to trigger the authentication process. */
    Button btnSignIn;

    /**
     * Initializes the activity, sets up the UI components, and attaches
     * the click listener to the sign-in button.
     *
     * @param savedInstanceState If the activity is being re-initialized, this contains the data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        btnSignIn = findViewById(R.id.btnSignIn);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect_user(v);
            }
        });
    }

    /**
     * Orchestrates the user authentication process.
     * <p>
     * 1. Validates that the input fields are not empty.<br>
     * 2. Uses {@link FBRef#refAuth} to attempt a sign-in with Firebase.<br>
     * 3. On success: Updates login status and moves to {@link MainActivity}.<br>
     * 4. On failure: Displays a Toast with the error message.
     *
     * @param v The view that triggered this method (usually the sign-in button).
     */
    public void connect_user(View v) {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Basic validation to ensure fields are not empty
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Attempt to sign in using the centralized FirebaseAuth reference
        FBRef.refAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Authentication successful
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                        // Save the login state locally
                        updateLoginStatus();

                        // Navigate to the main dashboard
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Authentication failed (e.g., wrong password or user doesn't exist)
                        Toast.makeText(LoginActivity.this, "Authentication failed: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Saves a flag to {@link SharedPreferences} indicating the user has successfully logged in.
     * <p>
     * This creates a local file named "PREFERENCE" that is used by the app's
     * entry point to decide whether to show the login screen or skip to the main screen.
     */
    private void updateLoginStatus() {
        SharedPreferences preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Mark that the user has completed the installation/login process
        editor.putString("FirstTimeInstall", "Yes");
        editor.apply();
    }

    /**
     * Navigates the user to the registration screen.
     *
     * @param view The text or button view clicked.
     */
    public void signUpOnClick(View view) {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
    }
}