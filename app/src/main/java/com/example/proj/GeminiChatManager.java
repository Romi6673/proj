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

/**
 * Manager class for the Google Gemini AI integration.
 * <p>
 * This class handles the initialization of the generative model, maintains a
 * single chat session (Singleton pattern), and manages asynchronous communication
 * with the Gemini API.
 */
public class GeminiChatManager {

    /**
     * The initial system prompt that defines the AI's persona.
     * Instructs the model to act as "Joe," a helpful tutor for teenagers.
     */
    public static final String SYSTEM_PROMPT ="your name is Joe, and your job is to help teenagers study ," +
            " explain every study question with patience and be helpful and nice";

    /** The unique singleton instance of this manager. */
    private static GeminiChatManager instance;

    /** The underlying Google Generative AI model instance. */
    private GenerativeModel gemini;

    /** The active chat session which maintains history. */
    private Chat chat;

    /** Tag for logging and debugging. */
    private final String TAG = "GeminiChatManager";

    /**
     * Initializes the Gemini model and starts a chat session.
     * This session allows the model to "remember" previous parts of the conversation.
     */
    private void startChat() {
        chat = gemini.startChat(Collections.emptyList());
    }

    /**
     * Private constructor to initialize the Gemini model with a system prompt.
     *
     * @param systemPrompt The instructions provided to the model to define its behavior.
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
     * <p>
     * Ensures a unique and single connection with Google's chat services.
     *
     * @param systemPrompt The prompt to use if the manager needs to be initialized.
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
     * <p>
     * This operation is asynchronous; the app will continue to run normally
     * while waiting for the AI to process and return a response.
     *
     * @param prompt   The text prompt/message to send to the AI.
     * @param callback The {@link GeminiCallback} to receive the success or error result.
     */
    public void sendChatMessage(String prompt, GeminiCallback callback) {
        try {
            chat.sendMessage(prompt,
                    new Continuation<GenerateContentResponse>() {

                        /**
                         * Provides the coroutine context for the callback.
                         * @return An empty coroutine context.
                         */
                        @NonNull
                        @Override
                        public CoroutineContext getContext() {
                            return EmptyCoroutineContext.INSTANCE;
                        }

                        /**
                         * Called when the message processing is complete.
                         *
                         * @param result The result containing the AI's response or an exception.
                         */
                        @Override
                        public void resumeWith(@NonNull Object result) {
                            try {
                                if (result instanceof Result.Failure) {
                                    // Handles failure from the Gemini API
                                    Throwable exception = ((Result.Failure) result).exception;
                                    Log.e(TAG, "Error from Gemini API: " + exception.getMessage());
                                    callback.onFailure(exception);
                                } else {
                                    // Successfully received a response
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
                                // Catches errors occurring during response processing (e.g., parsing)
                                Log.e(TAG, "Error processing response: " + e.getMessage());
                                callback.onFailure(e);
                            }
                        }
                    });
        } catch (Exception e) {
            // Catches errors occurring before the message is even sent
            Log.e(TAG, "Error initiating send: " + e.getMessage());
            callback.onFailure(e);
        }
    }
}