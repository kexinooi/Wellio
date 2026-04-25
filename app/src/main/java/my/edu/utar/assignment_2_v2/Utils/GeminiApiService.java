package my.edu.utar.assignment_2_v2.Utils;

import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GeminiApiService {

    private final GenerativeModelFutures model;
    private final Executor executor;

    public GeminiApiService() {
        // FIXED: Switched to the stable gemini-2.5-flash model to avoid the 404 error and preview quota limits
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
        String prompt = "You are Wellio, an academic performance analyst for university students. " +
                "Analyze the following academic workload data and provide 3 specific, actionable insights:\n\n" +
                "DATA:\n" + academicData + "\n\n" +
                "GUIDELINES:\n" +
                "1. Focus on workload management and study strategies\n" +
                "2. Identify potential stress points or bottlenecks\n" +
                "3. Suggest specific time management techniques\n" +
                "4. Keep insights concise (2-3 sentences each)\n" +
                "5. Be analytical and practical, not generic\n" +
                "6. Format as bullet points with clear headings\n\n" +
                "Provide your analysis:";
        Content content = new Content.Builder().addText(prompt).build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                callback.onResult(result.getText());
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError("Academic AI Error: " + t.getMessage());
            }
        }, executor);
    }

    public void getMoodInsights(String moodData, GeminiCallback callback) {
        String prompt = "You are Wellio, a mental wellbeing specialist for university students. " +
                "Analyze the following mood pattern data and provide personalized insights:\n\n" +
                "DATA:\n" + moodData + "\n\n" +
                "GUIDELINES:\n" +
                "1. Identify mood patterns and trends\n" +
                "2. Connect mood changes to academic stress factors\n" +
                "3. Provide 3 specific wellbeing recommendations\n" +
                "4. Focus on emotional regulation and stress management\n" +
                "5. Keep insights supportive and actionable\n" +
                "6. Format as clear, empathetic bullet points\n\n" +
                "Provide your analysis:";
        Content content = new Content.Builder().addText(prompt).build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                callback.onResult(result.getText());
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError("Mood AI Error: " + t.getMessage());
            }
        }, executor);
    }

    public void getSleepInsights(String sleepData, GeminiCallback callback) {
        String prompt = "You are Wellio, a sleep and recovery specialist for university students. " +
                "Analyze the following sleep pattern data and provide optimization recommendations:\n\n" +
                "DATA:\n" + sleepData + "\n\n" +
                "GUIDELINES:\n" +
                "1. Identify sleep quality and consistency issues\n" +
                "2. Connect sleep patterns to academic performance\n" +
                "3. Provide 3 specific sleep improvement strategies\n" +
                "4. Consider student lifestyle constraints\n" +
                "5. Focus on practical, implementable solutions\n" +
                "6. Format as clear, actionable recommendations\n\n" +
                "Provide your analysis:";
        Content content = new Content.Builder().addText(prompt).build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                callback.onResult(result.getText());
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError("Sleep AI Error: " + t.getMessage());
            }
        }, executor);
    }

    public interface GeminiCallback {
        void onResult(String result);
        void onError(String error);
    }
}