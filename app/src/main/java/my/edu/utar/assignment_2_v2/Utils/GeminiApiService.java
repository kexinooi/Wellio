package my.edu.utar.assignment_2_v2.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.security.MessageDigest;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GeminiApiService {

    private final GenerativeModelFutures model;
    private final Executor executor;
    private final Context context;
    private static final String PREFS_NAME = "ai_cache_prefs";

    public GeminiApiService(Context context) {
        this.context = context.getApplicationContext();
        GenerativeModel gm = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-2.5-flash");
        this.model = GenerativeModelFutures.from(gm);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void getWellbeingRecommendations(String userInput, GeminiCallback callback) {
        // --- THE UPGRADED SMART PROMPT ---
        // --- THE UPGRADED SMART PROMPT ---
        String smartPrompt = "You are Wellio, an advanced academic wellbeing mentor for university students. " +
                "Read the student's input at the bottom and follow these strict rules:\n\n" +
                "1. MATCH LANGUAGE: You MUST reply in the exact same language the student used in their input.\n" +
                "2. SHORT INPUTS: If they just say 'happy', 'tired', 'stressed', etc., validate the feeling, give ONE specific productivity tip, and ask ONE question about their deadlines.\n" +
                "3. HEAVY WORKLOAD: If they describe a lot of work, provide a professional time-management strategy (e.g., Eisenhower Matrix, Time-blocking).\n" +
                "4. NO FLUFF: NEVER use generic advice like 'take a deep breath' or 'drink water'. Be analytical.\n" +
                "5. LENGTH: Maximum 3 to 4 short sentences.\n" +
                "6. FALLBACK: If the input is just 'hi', 'test', or gibberish, simply ask: 'How is your academic workload looking this week?'\n\n" +
                "--- STUDENT INPUT ---\n" +
                "\"" + userInput + "\"\n" +
                "---------------------";
        Content prompt = new Content.Builder().addText(smartPrompt).build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(prompt);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                callback.onResult(result.getText());
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError("Wellio AI Error: " + t.getMessage());
            }
        }, executor);
    }

    public void getAcademicInsights(String academicData, GeminiCallback callback) {
        String cacheKey = "academic_" + hashString(academicData);
        String cachedResult = getCachedResult(cacheKey);
        
        if (cachedResult != null) {
            callback.onResult(cachedResult);
            return;
        }
        
        String prompt = "You are Wellio, an academic performance analyst for university students. " +
                "Analyze the following academic workload data and provide exactly 1 insight and 1 recommendation:\n\n" +
                "DATA:\n" + academicData + "\n\n" +
                "GUIDELINES:\n" +
                "1. Provide exactly 1 insight with a title (max 10 words) and a description (1-2 sentences)\n" +
                "2. Provide exactly 1 recommendation with a title (max 10 words) and a description (1-2 sentences)\n" +
                "3. The description must directly relate to and expand on the title\n" +
                "4. Focus on workload management and study strategies\n" +
                "5. Be analytical and practical, not generic\n" +
                "6. Format as:\n" +
                "   INSIGHT:\n" +
                "   [Title]\n" +
                "   [Description]\n" +
                "   RECOMMENDATION:\n" +
                "   [Title]\n" +
                "   [Description]\n\n" +
                "Provide your analysis:";
        Content content = new Content.Builder().addText(prompt).build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                cacheResult(cacheKey, resultText);
                callback.onResult(resultText);
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError("Academic AI Error: " + t.getMessage());
            }
        }, executor);
    }

    public void getMoodInsights(String moodData, GeminiCallback callback) {
        String cacheKey = "mood_" + hashString(moodData);
        String cachedResult = getCachedResult(cacheKey);
        
        if (cachedResult != null) {
            callback.onResult(cachedResult);
            return;
        }
        
        String prompt = "You are Wellio, a mental wellbeing specialist for university students. " +
                "Analyze the following mood pattern data and provide exactly 1 insight and 1 recommendation:\n\n" +
                "DATA:\n" + moodData + "\n\n" +
                "GUIDELINES:\n" +
                "1. Provide exactly 1 insight with a title (max 10 words) and a description (1-2 sentences)\n" +
                "2. Provide exactly 1 recommendation with a title (max 10 words) and a description (1-2 sentences)\n" +
                "3. The description must directly relate to and expand on the title\n" +
                "4. Focus on emotional regulation and stress management\n" +
                "5. Keep insights supportive and actionable\n" +
                "6. Format as:\n" +
                "   INSIGHT:\n" +
                "   [Title]\n" +
                "   [Description]\n" +
                "   RECOMMENDATION:\n" +
                "   [Title]\n" +
                "   [Description]\n\n" +
                "Provide your analysis:";
        Content content = new Content.Builder().addText(prompt).build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                cacheResult(cacheKey, resultText);
                callback.onResult(resultText);
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError("Mood AI Error: " + t.getMessage());
            }
        }, executor);
    }

    public void getSleepInsights(String sleepData, GeminiCallback callback) {
        String cacheKey = "sleep_" + hashString(sleepData);
        String cachedResult = getCachedResult(cacheKey);
        
        if (cachedResult != null) {
            callback.onResult(cachedResult);
            return;
        }
        
        String prompt = "You are Wellio, a sleep and recovery specialist for university students. " +
                "Analyze the following sleep pattern data and provide exactly 1 insight and 1 recommendation:\n\n" +
                "DATA:\n" + sleepData + "\n\n" +
                "GUIDELINES:\n" +
                "1. Provide exactly 1 insight with a title (max 10 words) and a description (1-2 sentences)\n" +
                "2. Provide exactly 1 recommendation with a title (max 10 words) and a description (1-2 sentences)\n" +
                "3. The description must directly relate to and expand on the title\n" +
                "4. Focus on sleep quality and consistency\n" +
                "5. Consider student lifestyle constraints\n" +
                "6. Format as:\n" +
                "   INSIGHT:\n" +
                "   [Title]\n" +
                "   [Description]\n" +
                "   RECOMMENDATION:\n" +
                "   [Title]\n" +
                "   [Description]\n\n" +
                "Provide your analysis:";
        Content content = new Content.Builder().addText(prompt).build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                cacheResult(cacheKey, resultText);
                callback.onResult(resultText);
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError("Sleep AI Error: " + t.getMessage());
            }
        }, executor);
    }

    private String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return String.valueOf(input.hashCode());
        }
    }

    private String getCachedResult(String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(key, null);
    }

    private void cacheResult(String key, String result) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, result);
        editor.apply();
    }

    public interface GeminiCallback {
        void onResult(String result);
        void onError(String error);
    }
}