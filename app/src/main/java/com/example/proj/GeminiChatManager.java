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
    public static final String SYSTEM_PROMPT ="your name is Joe, and your job is to help teenagers study be" +
            "friendly and nice and explain every topic the best you can ";



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
                "gemini-2.5-flash",
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
            //חיבור ייחודי ויחיד עם צאט של גוגל
        }
        return instance;
    }

    /**
     * Sends a chat message to the Gemini model and receives a text response.
     *
     * @param prompt   The text prompt to send to the model.
     * @param callback The callback to receive the response or error.
     */
    /**
     * Sends a chat message to the Gemini model and receives a text response.
     *
     * @param prompt   The text prompt to send to the model.
     * @param callback The callback to receive the response or error.
     */
    public void sendChatMessage(String prompt, GeminiCallback callback) {
        try {
            chat.sendMessage(prompt,
                    new Continuation<GenerateContentResponse>() {
                //משום שלוקח לגמיני כמה רגעים לענות אנחנו במקביל נמשיך לעבוד כרגיל עד שתגיע תשובה
                        @NonNull
                        @Override
                        public CoroutineContext getContext() {
                            return EmptyCoroutineContext.INSTANCE;
                        }

                        @Override
                        public void resumeWith(@NonNull Object result) {
                            try {
                                if (result instanceof Result.Failure) {
                                    Throwable exception = ((Result.Failure) result).exception;
                                    Log.e(TAG, "Error from Gemini API: " + exception.getMessage());
                                    callback.onFailure(exception);
                                } else {
                                    GenerateContentResponse response = (GenerateContentResponse) result;
                                    String text = response.getText();
                                    if (text != null) {
                                        Log.i(TAG, "Success: " + text);
                                        callback.onSuccess(text);
                                    } else {
                                        callback.onFailure(new Exception("Response text is null"));
                                    }
                                }
                            } catch (Exception e) {
                                // תופס שגיאות שקורות בזמן עיבוד התשובה (כמו ה-MissingFieldException שראינו)
                                Log.e(TAG, "Error processing response: " + e.getMessage());
                                callback.onFailure(e);
                            }
                        }
                    });
        } catch (Exception e) {
            // תופס שגיאות שקורות עוד לפני שליחת ההודעה
            Log.e(TAG, "Error initiating send: " + e.getMessage());
            callback.onFailure(e);
        }
    }
}
