package com.example.proj;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.Chat;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.Part;
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.ai.client.generativeai.type.TextPart;

import java.util.ArrayList;

public class GeminiManager {
    private static GeminiManager instance;
    private GenerativeModel gemini;

    private GeminiManager (){
        gemini = new GenerativeModel("gemini-2.0-flash",
                BuildConfig.GEMINI_API_KEY);
    }

}
