package com.example.proj;

import static com.example.proj.GeminiChatManager.SYSTEM_PROMPT;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class AiChatActivity extends AppCompatActivity {
    private TextView textViewChat;
    private EditText eTUserInput;
    private GeminiChatManager chatManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        textViewChat = findViewById(R.id.textViewChat);
        eTUserInput = findViewById(R.id.eTUserInput);

        textViewChat.setMovementMethod(new ScrollingMovementMethod());

        chatManager = GeminiChatManager.getInstance(SYSTEM_PROMPT);

    }

    public void sendUserInput(View view) {
        //InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        String userInput = eTUserInput.getText().toString();
        if (userInput.isEmpty()) {
            Toast.makeText(this, "Please enter your answer", Toast.LENGTH_LONG).show();
            return;
        } else {
            textViewChat.append("\n" + "you:" + "\n" + userInput + "\n");
            eTUserInput.setText("");
            processUserInput(userInput);
        }
    }

    private void processUserInput(String userInput) {
        chatManager.sendChatMessage(userInput, new GeminiCallback () {
            @Override
            public void onSuccess(String result) {
                textViewChat.append("\n" + result + "\n");

            }

            @Override
            public void onFailure(Throwable throwable) {
                runOnUiThread(() -> {
                    textViewChat.append("Sorry , the server is in high demand ." +
                            "come back in a minute");
                });
            }
        });
    }

}