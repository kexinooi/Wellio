package my.edu.utar.assignment_2_v2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import my.edu.utar.assignment_2_v2.Utils.Firebase;
import my.edu.utar.assignment_2_v2.Utils.GeminiApiService;
import my.edu.utar.assignment_2_v2.model.Mood;

public class MoodTrendsFragment extends Fragment {
    private static final String TAG = "MoodTrendsFragment";
    private List<Mood> moodList = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
    private GeminiApiService geminiApiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_trends, container, false);
        
        geminiApiService = new GeminiApiService();
        loadMoodData(view);
        return view;
    }

    private void loadMoodData(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Get mood data for last 30 days
        Calendar thirtyDaysAgo = Calendar.getInstance();
        thirtyDaysAgo.add(Calendar.DAY_OF_MONTH, -30);

        Firebase.getInstance().getUserMoodLogsInRange(user.getUid(), thirtyDaysAgo.getTimeInMillis(), System.currentTimeMillis())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    moodList.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                        Mood mood = doc.toObject(Mood.class);
                        mood.setId(doc.getId());
                        moodList.add(mood);
                    }
                    updateMoodAnalytics(view);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load mood data", e));
    }

    private void updateMoodAnalytics(View view) {
        if (moodList.isEmpty()) {
            Log.d(TAG, "No mood data available");
            return;
        }

        // Calculate mood statistics
        Map<String, Integer> moodCounts = new HashMap<>();
        Map<String, Integer> feelingCounts = new HashMap<>();
        List<Double> sleepHoursList = new ArrayList<>();
        
        for (Mood mood : moodList) {
            // Count moods
            String moodType = mood.getMood();
            if (moodType != null) {
                moodCounts.put(moodType, moodCounts.getOrDefault(moodType, 0) + 1);
            }
            
            // Count feelings
            String feelings = mood.getFeel();
            if (feelings != null && !feelings.isEmpty()) {
                String[] feelingArray = feelings.split(",\\s*");
                for (String feeling : feelingArray) {
                    if (!feeling.trim().isEmpty()) {
                        feelingCounts.put(feeling.trim(), feelingCounts.getOrDefault(feeling.trim(), 0) + 1);
                    }
                }
            }
            
            // Collect sleep hours
            if (mood.getSleepHours() > 0) {
                sleepHoursList.add(mood.getSleepHours());
            }
        }

        // Log analytics for now (until layout is updated)
        Log.d(TAG, "Mood counts: " + moodCounts.toString());
        Log.d(TAG, "Feeling counts: " + feelingCounts.toString());
        Log.d(TAG, "Total moods analyzed: " + moodList.size());

        // Generate AI-powered mood insights
        generateMoodInsights(moodCounts, feelingCounts, sleepHoursList);
    }

    private void generateMoodInsights(Map<String, Integer> moodCounts, Map<String, Integer> feelingCounts, List<Double> sleepHoursList) {
        // Prepare mood data for AI analysis
        StringBuilder moodData = new StringBuilder();
        moodData.append("Mood Analysis for Last 30 Days:\n\n");
        
        // Mood distribution
        moodData.append("MOOD DISTRIBUTION:\n");
        for (Map.Entry<String, Integer> entry : moodCounts.entrySet()) {
            moodData.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" times\n");
        }
        
        // Top feelings
        moodData.append("\nTOP FEELINGS:\n");
        List<Map.Entry<String, Integer>> sortedFeelings = new ArrayList<>(feelingCounts.entrySet());
        for (int i = 0; i < sortedFeelings.size() - 1; i++) {
            for (int j = i + 1; j < sortedFeelings.size(); j++) {
                if (sortedFeelings.get(j).getValue() > sortedFeelings.get(i).getValue()) {
                    Map.Entry<String, Integer> temp = sortedFeelings.get(i);
                    sortedFeelings.set(i, sortedFeelings.get(j));
                    sortedFeelings.set(j, temp);
                }
            }
        }
        int limit = Math.min(5, sortedFeelings.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> entry = sortedFeelings.get(i);
            moodData.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" times\n");
        }
        
        // Recent mood trend
        moodData.append("\nRECENT PATTERN:\n");
        if (moodList.size() >= 3) {
            Mood recent = moodList.get(0);
            Mood previous = moodList.get(1);
            Mood earlier = moodList.get(2);
            
            moodData.append("Latest mood: ").append(recent.getMood());
            if (recent.getFeel() != null && !recent.getFeel().isEmpty()) {
                moodData.append(" (").append(recent.getFeel()).append(")");
            }
            moodData.append("\nPrevious: ").append(previous.getMood());
            moodData.append("\nEarlier: ").append(earlier.getMood());
        }
        
        // Sleep statistics
        moodData.append("\nSLEEP STATISTICS:\n");
        if (!sleepHoursList.isEmpty()) {
            double avgSleep = calculateAverage(sleepHoursList);
            double minSleep = findMin(sleepHoursList);
            double maxSleep = findMax(sleepHoursList);
            
            moodData.append("Average sleep: ").append(String.format("%.1f", avgSleep)).append(" hours\n");
            moodData.append("Sleep range: ").append(String.format("%.1f", minSleep)).append(" - ").append(String.format("%.1f", maxSleep)).append(" hours\n");
            moodData.append("Sleep entries: ").append(sleepHoursList.size()).append("\n");
        } else {
            moodData.append("No sleep data recorded\n");
        }
        
        // Total entries and frequency
        moodData.append("\nSTATISTICS:\n");
        moodData.append("Total mood entries: ").append(moodList.size()).append("\n");
        moodData.append("Average frequency: ").append(moodList.size() / 30.0).append(" entries per day\n");
        
        // Call Gemini API for insights
        geminiApiService.getMoodInsights(moodData.toString(), new GeminiApiService.GeminiCallback() {
            @Override
            public void onResult(String result) {
                Log.d(TAG, "AI Mood Insights: " + result);
                // TODO: Update UI with AI insights when layout is ready
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "AI Mood Insights Error: " + error);
            }
        });
        
        // Generate sleep insights separately
        generateSleepInsights(sleepHoursList);
    }
    
    private void generateSleepInsights(List<Double> sleepHoursList) {
        if (sleepHoursList.isEmpty()) {
            Log.d(TAG, "No sleep data available for insights");
            return;
        }
        
        // Prepare sleep data for AI analysis
        StringBuilder sleepData = new StringBuilder();
        sleepData.append("Sleep Pattern Analysis for Last 30 Days:\n\n");
        
        // Calculate sleep statistics
        double avgSleep = calculateAverage(sleepHoursList);
        double minSleep = findMin(sleepHoursList);
        double maxSleep = findMax(sleepHoursList);
        
        // Sleep distribution
        long goodSleep = 0, fairSleep = 0, poorSleep = 0;
        for (Double hours : sleepHoursList) {
            if (hours >= 7) {
                goodSleep++;
            } else if (hours >= 5) {
                fairSleep++;
            } else {
                poorSleep++;
            }
        }
        
        sleepData.append("SLEEP DISTRIBUTION:\n");
        sleepData.append("- Good sleep (≥7h): ").append(goodSleep).append(" nights\n");
        sleepData.append("- Fair sleep (5-6.9h): ").append(fairSleep).append(" nights\n");
        sleepData.append("- Poor sleep (<5h): ").append(poorSleep).append(" nights\n");
        
        sleepData.append("\nSLEEP STATISTICS:\n");
        sleepData.append("Average: ").append(String.format("%.1f", avgSleep)).append(" hours\n");
        sleepData.append("Range: ").append(String.format("%.1f", minSleep)).append(" - ").append(String.format("%.1f", maxSleep)).append(" hours\n");
        sleepData.append("Total recorded nights: ").append(sleepHoursList.size()).append("\n");
        
        // Sleep consistency
        sleepData.append("\nSLEEP CONSISTENCY:\n");
        double variance = calculateVariance(sleepHoursList, avgSleep);
        double stdDev = Math.sqrt(variance);
        
        if (stdDev < 1) {
            sleepData.append("Very consistent sleep pattern (low variance)\n");
        } else if (stdDev < 2) {
            sleepData.append("Moderately consistent sleep pattern\n");
        } else {
            sleepData.append("Irregular sleep pattern (high variance)\n");
        }
        
        // Recent sleep trend
        sleepData.append("\nRECENT SLEEP TREND:\n");
        int recentDays = Math.min(7, sleepHoursList.size());
        if (recentDays >= 3) {
            List<Double> recent = sleepHoursList.subList(0, recentDays);
            double recentAvg = calculateAverage(recent);
            sleepData.append("Last ").append(recentDays).append(" days average: ").append(String.format("%.1f", recentAvg)).append(" hours\n");
            
            if (recentAvg > avgSleep + 0.5) {
                sleepData.append("Sleep improving recently\n");
            } else if (recentAvg < avgSleep - 0.5) {
                sleepData.append("Sleep declining recently\n");
            } else {
                sleepData.append("Sleep stable recently\n");
            }
        }
        
        // Call Gemini API for sleep insights
        geminiApiService.getSleepInsights(sleepData.toString(), new GeminiApiService.GeminiCallback() {
            @Override
            public void onResult(String result) {
                Log.d(TAG, "AI Sleep Insights: " + result);
                // TODO: Update UI with AI sleep insights when layout is ready
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "AI Sleep Insights Error: " + error);
            }
        });
    }
    
    // Helper methods for sleep statistics
    private double calculateAverage(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Double value : values) {
            sum += value;
        }
        return sum / values.size();
    }
    
    private double findMin(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        double min = values.get(0);
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) < min) {
                min = values.get(i);
            }
        }
        return min;
    }
    
    private double findMax(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        double max = values.get(0);
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) > max) {
                max = values.get(i);
            }
        }
        return max;
    }
    
    private double calculateVariance(List<Double> values, double mean) {
        if (values.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Double value : values) {
            sum += Math.pow(value - mean, 2);
        }
        return sum / values.size();
    }
}
