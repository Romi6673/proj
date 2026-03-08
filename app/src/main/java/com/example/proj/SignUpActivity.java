package com.example.proj;

import static com.example.proj.FBRef.refAuth;

import android.os.Bundle;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.Carousel;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import android.content.Intent;
import android.os.Bundle;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Button;
import android.widget.Toast;

import android.widget.Switch;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DatabaseReference;

import androidx.core.view.WindowInsetsCompat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignUpActivity extends AppCompatActivity {

    EditText emailEditText;
    EditText passwordEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
    }


    public void loginOnClick(View view) {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(intent);

    }

    public void add_user(View view) {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty())
        {
            Toast.makeText(SignUpActivity.this, "Please fill all fields", Toast.LENGTH_LONG).show();
        }

        else
        {
            ProgressDialog pd = new ProgressDialog(this);
            pd.setTitle("Connecting");
            pd.setMessage("Creating user...");
            pd.show();

            refAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            pd.dismiss();

                            if (task.isSuccessful())
                            {
                                FirebaseUser user = refAuth.getCurrentUser();
                                user.sendEmailVerification()
                                        .addOnCompleteListener(verifyTask -> {
                                            if (verifyTask.isSuccessful()) {
                                                Toast.makeText(SignUpActivity.this,
                                                        "Registration successful! Please check your email for verification.",
                                                        Toast.LENGTH_LONG).show();

                                                // התנתקות זמנית כדי שלא יוכל להיכנס עד שיאמת
                                                FBRef.refAuth.signOut();

                                                // מעבר למסך ההתחברות
                                                startActivity(new Intent(SignUpActivity.this, ProfileActivity.class));
                                                finish();

                                    Toast.makeText(SignUpActivity.this, " User created successfully! " +
                                        task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }

                            else
                            {
                                Toast.makeText(SignUpActivity.this, "Authentication failed: " +
                                        task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                }
            });
        }
}
}

