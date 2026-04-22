package com.example.proj;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.proj.BuildConfig;
import com.google.ai.client.generativeai.Chat;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.Part;
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.ai.client.generativeai.type.TextPart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kotlin.Result;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class GeminiChatManager {
    public static final String SYSTEM_PROMPT =
            "אתה עוזר אישי חכם. " +
                    "התפקיד שלך הוא לעזור למשתמש בשאלות יומיומיות. " +
                    "ענה תמיד בעברית, שמור על תשובות קצרות, ידידותיות וענייניות.";


    private static GeminiChatManager instance;
    private GenerativeModel gemini;
    private Chat chat;
    private final String TAG = "GeminiChatManager";

    /**
     * Initializes the Gemini model and starts a chat session.
     */
    private void startChat() {
        chat = gemini.startChat(Collections.emptyList());
    }

    /**
     * Private constructor to initialize the Gemini model with a system prompt.
     *
     * @param systemPrompt The system prompt to initialize the model.
     */
    private GeminiChatManager(String systemPrompt) {
        List<Part> parts = new ArrayList<Part>();
        parts.add(new TextPart(systemPrompt));
        gemini = new GenerativeModel(
                "gemini-2.0-flash",
                BuildConfig.GEMINI_API_KEY,
                null,
                null,
                new RequestOptions(),
                null,
                null,
                new Content(parts)
        );
        startChat();
    }

    /**
     * Returns the singleton instance of {@code GeminiChatManager}.
     *
     * @return The singleton instance of {@code GeminiChatManager}.
     */
    public static GeminiChatManager getInstance(String systemPrompt) {
        if (instance == null) {
            instance = new GeminiChatManager(systemPrompt);
        }
        return instance;
    }

    /**
     * Sends a chat message to the Gemini model and receives a text response.
     *
     * @param prompt   The text prompt to send to the model.
     * @param callback The callback to receive the response or error.
     */
    public void sendChatMessage(String prompt, GeminiCallback callback) {
        chat.sendMessage(prompt,
                new Continuation<GenerateContentResponse>() {
                    @NonNull
                    @Override
                    public CoroutineContext getContext() {
                        return EmptyCoroutineContext.INSTANCE;
                    }

                    @Override
                    public void resumeWith(@NonNull Object result) {
                        if (result instanceof Result.Failure) {
                            Log.i(TAG, "Error: " + ((Result.Failure) result).exception.getMessage());
                            callback.onFailure(((Result.Failure) result).exception);
                        } else {
                            Log.i(TAG, "Success: " + ((GenerateContentResponse) result).getText());
                            callback.onSuccess(((GenerateContentResponse) result).getText());
                        }
                    }
                });
    }
}
