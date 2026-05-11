package com.example.proj;

import static com.example.proj.FBRef.refAuth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Activity responsible for handling new user registration.
 * <p>
 * This class manages the sign-up flow, which includes:
 * 1. Validating user input (email and password).
 * 2. Creating a new authentication account via Firebase Auth.
 * 3. Initializing a corresponding {@link Users} profile in the Realtime Database.
 * 4. Sending a verification email to the user.
 */
public class SignUpActivity extends AppCompatActivity {

    /** EditText field for the user's email address. */
    EditText emailEditText;

    /** EditText field for the user's chosen password. */
    EditText passwordEditText;

    /**
     * Initializes the activity and binds UI components.
     *
     * @param savedInstanceState If the activity is being re-initialized, this contains the data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
    }

    /**
     * Navigates the user back to the Main/Login screen.
     *
     * @param view The view that triggered the click.
     */
    public void loginOnClick(View view) {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Orchestrates the user registration process.
     * <p>
     * Workflow:
     * 1. Extracts and trims text from input fields.
     * 2. Shows a {@link ProgressDialog} to indicate background work.
     * 3. Calls {@code createUserWithEmailAndPassword} via {@link FBRef#refAuth}.
     * 4. On Auth success, saves a new {@link Users} object to the "Users" node in the database.
     * 5. Triggers an email verification and signs the user out until they verify.
     *
     * @param view The view that triggered the registration (the sign-up button).
     */
    public void add_user(View view) {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Basic validation for empty fields
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Please fill all fields", Toast.LENGTH_LONG).show();
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Creating user...");
        pd.show();

        // Create the user in Firebase Authentication
        refAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = refAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();

                            // Create a new User data object
                            Users newUser = new Users(email, password, uid);

                            // Save user profile to Realtime Database under their specific UID
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(uid)
                                    .setValue(newUser)
                                    .addOnCompleteListener(dbTask -> {
                                        pd.dismiss();
                                        if (dbTask.isSuccessful()) {
                                            // Send verification email to the new user
                                            firebaseUser.sendEmailVerification();
                                            Toast.makeText(SignUpActivity.this, "User created! Please verify email.", Toast.LENGTH_LONG).show();

                                            // Sign out to ensure they log in with a verified email
                                            refAuth.signOut();
                                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(SignUpActivity.this, "DB Error: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        pd.dismiss();
                        Toast.makeText(SignUpActivity.this, "Auth Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}

