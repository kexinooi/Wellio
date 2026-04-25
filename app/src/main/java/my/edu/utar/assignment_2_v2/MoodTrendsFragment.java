package my.edu.utar.assignment_2_v2;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.card.MaterialCardView;
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
    private View rootView;
    private LineChart moodLineChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_mood_trends, container, false);
        
        geminiApiService = new GeminiApiService();
        moodLineChart = rootView.findViewById(R.id.mood_line_chart);
        
        loadMoodData();
        return rootView;
    }

    private void loadMoodData() {
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
                    updateMoodAnalytics(rootView);
                    updateMoodLineChart();
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
    
    private void updateMoodLineChart() {
        if (moodLineChart == null || moodList.isEmpty()) return;
        
        // Convert mood strings to numeric values
        Map<String, Integer> moodValues = new HashMap<>();
        moodValues.put("Amazing", 5);
        moodValues.put("Good", 4);
        moodValues.put("Okay", 3);
        moodValues.put("Bad", 2);
        moodValues.put("Very Bad", 1);
        
        // Prepare data points sorted by date
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        // Sort mood list by date (oldest first)
        List<Mood> sortedMoods = new ArrayList<>(moodList);
        for (int i = 0; i < sortedMoods.size() - 1; i++) {
            for (int j = i + 1; j < sortedMoods.size(); j++) {
                if (sortedMoods.get(i).getTimestamp().after(sortedMoods.get(j).getTimestamp())) {
                    Mood temp = sortedMoods.get(i);
                    sortedMoods.set(i, sortedMoods.get(j));
                    sortedMoods.set(j, temp);
                }
            }
        }
        
        // Create entries for the last 30 days
        Calendar thirtyDaysAgo = Calendar.getInstance();
        thirtyDaysAgo.add(Calendar.DAY_OF_MONTH, -30);
        
        for (Mood mood : sortedMoods) {
            if (mood.getTimestamp().before(thirtyDaysAgo.getTime())) continue;
            
            String moodType = mood.getMood();
            if (moodType != null && moodValues.containsKey(moodType)) {
                entries.add(new Entry(entries.size(), moodValues.get(moodType)));
                labels.add(dateFormat.format(mood.getTimestamp()));
            }
        }
        
        if (entries.isEmpty()) return;
        
        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, "Mood Trend");
        dataSet.setColor(Color.parseColor("#6366F1"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(Color.parseColor("#6366F1"));
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setValueTextColor(Color.parseColor("#666666"));
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#E0E7FF"));
        
        // Create line data
        LineData lineData = new LineData(dataSet);
        
        // Configure chart
        moodLineChart.setData(lineData);
        moodLineChart.getDescription().setEnabled(false);
        moodLineChart.setDrawGridBackground(false);
        moodLineChart.setDrawBorders(false);
        moodLineChart.getLegend().setEnabled(false);
        moodLineChart.setTouchEnabled(true);
        moodLineChart.setPinchZoom(true);
        
        // Configure X axis
        XAxis xAxis = moodLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setTextColor(Color.parseColor("#666666"));
        xAxis.setTextSize(11f);
        
        // Configure Y axis
        YAxis yAxis = moodLineChart.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.setGridColor(Color.parseColor("#EEEEEE"));
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(6f);
        yAxis.setLabelCount(6, true);
        yAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                switch ((int) value) {
                    case 1: return "Very Bad";
                    case 2: return "Bad";
                    case 3: return "Okay";
                    case 4: return "Good";
                    case 5: return "Amazing";
                    default: return "";
                }
            }
        });
        yAxis.setTextColor(Color.parseColor("#666666"));
        yAxis.setTextSize(11f);
        
        // Hide right Y axis
        moodLineChart.getAxisRight().setEnabled(false);
        
        // Refresh chart
        moodLineChart.invalidate();
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
                updateInsightCards(result);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "AI Mood Insights Error: " + error);
            }
        });
        
        // Generate sleep insights separately
        generateSleepInsights(sleepHoursList);
    }
    
    private void updateInsightCards(String aiResponse) {
        if (rootView == null) return;
        
        // Find the insights section LinearLayout
        LinearLayout insightsSection = null;
        for (int i = 0; i < ((ViewGroup) rootView).getChildCount(); i++) {
            View child = ((ViewGroup) rootView).getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout linearLayout = (LinearLayout) child;
                // Check if this is the insights section (has TextView with "Insights" text)
                for (int j = 0; j < linearLayout.getChildCount(); j++) {
                    View subChild = linearLayout.getChildAt(j);
                    if (subChild instanceof LinearLayout) {
                        LinearLayout headerLayout = (LinearLayout) subChild;
                        for (int k = 0; k < headerLayout.getChildCount(); k++) {
                            View headerChild = headerLayout.getChildAt(k);
                            if (headerChild instanceof TextView) {
                                TextView tv = (TextView) headerChild;
                                if ("Insights".equals(tv.getText().toString())) {
                                    insightsSection = linearLayout;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (insightsSection == null) return;
        
        // Find the first 2 MaterialCardViews (insight cards) and update them
        int insightCardIndex = 0;
        for (int i = 0; i < insightsSection.getChildCount(); i++) {
            View child = insightsSection.getChildAt(i);
            if (child instanceof MaterialCardView && insightCardIndex < 2) {
                List<TextView> textViews = findTextViews(child);
                if (textViews.size() >= 2) {
                    // Parse AI response to get individual insights
                    String[] insights = parseInsights(aiResponse, insightCardIndex);
                    if (insights[0] != null) textViews.get(0).setText(insights[0]);
                    if (insights[1] != null) textViews.get(1).setText(insights[1]);
                }
                insightCardIndex++;
            }
        }
        
        // Find the next 3 MaterialCardViews (recommendation cards) and update them
        int recCardIndex = 0;
        for (int i = 0; i < insightsSection.getChildCount(); i++) {
            View child = insightsSection.getChildAt(i);
            if (child instanceof MaterialCardView && insightCardIndex >= 2 && recCardIndex < 3) {
                List<TextView> textViews = findTextViews(child);
                if (textViews.size() >= 2) {
                    // Parse AI response to get individual recommendations
                    String[] recommendations = parseRecommendations(aiResponse, recCardIndex);
                    if (recommendations[0] != null) textViews.get(0).setText(recommendations[0]);
                    if (recommendations[1] != null) textViews.get(1).setText(recommendations[1]);
                }
                recCardIndex++;
            }
        }
    }
    
    private String[] parseInsights(String aiResponse, int index) {
        String[] lines = aiResponse.split("\n");
        String title = null;
        String desc = null;
        
        // Simple parsing - in production, you'd want more sophisticated parsing
        if (index == 0) {
            // First insight
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].trim().length() > 0 && title == null) {
                    title = lines[i].trim();
                } else if (lines[i].trim().length() > 0 && title != null && desc == null) {
                    desc = lines[i].trim();
                    break;
                }
            }
        } else {
            // Second insight
            int found = 0;
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].trim().length() > 0) {
                    found++;
                    if (found > 2 && title == null) {
                        title = lines[i].trim();
                    } else if (found > 2 && title != null && desc == null) {
                        desc = lines[i].trim();
                        break;
                    }
                }
            }
        }
        
        return new String[]{title != null ? title : "Loading...", desc != null ? desc : "Loading..."};
    }
    
    private String[] parseRecommendations(String aiResponse, int index) {
        String[] lines = aiResponse.split("\n");
        String title = null;
        String desc = null;
        
        // Look for recommendation section
        boolean inRecSection = false;
        int recCount = 0;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].toLowerCase();
            if (line.contains("recommend") || line.contains("suggest") || line.contains("advice")) {
                inRecSection = true;
                continue;
            }
            
            if (inRecSection) {
                if (lines[i].trim().length() > 0) {
                    if (lines[i].trim().startsWith("-") || lines[i].trim().startsWith("*")) {
                        recCount++;
                        if (recCount == index + 1) {
                            title = lines[i].trim().substring(1).trim();
                            // Look for description on next line
                            if (i + 1 < lines.length && lines[i + 1].trim().length() > 0) {
                                desc = lines[i + 1].trim();
                            }
                            break;
                        }
                    }
                }
            }
        }
        
        return new String[]{title != null ? title : "Loading...", desc != null ? desc : "Loading..."};
    }
    
    private List<TextView> findTextViews(View view) {
        List<TextView> result = new ArrayList<>();
        if (view instanceof TextView) {
            result.add((TextView) view);
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                result.addAll(findTextViews(group.getChildAt(i)));
            }
        }
        return result;
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
    
    private String extractRecommendations(String aiResponse) {
        // Extract recommendations from AI response
        // Look for sections that contain actionable advice
        String[] lines = aiResponse.split("\n");
        StringBuilder recommendations = new StringBuilder();
        boolean inRecommendationsSection = false;
        
        for (String line : lines) {
            String lowerLine = line.toLowerCase();
            if (lowerLine.contains("recommend") || lowerLine.contains("suggest") || 
                lowerLine.contains("should") || lowerLine.contains("try") ||
                lowerLine.contains("tip") || lowerLine.contains("advice")) {
                inRecommendationsSection = true;
            }
            
            if (inRecommendationsSection) {
                if (line.trim().startsWith("-") || line.trim().startsWith("*") || 
                    line.trim().matches("\\d+\\.") || line.trim().length() > 0) {
                    recommendations.append(line).append("\n");
                }
            }
        }
        
        // If no specific recommendations found, return the last paragraph
        if (recommendations.length() == 0) {
            String[] paragraphs = aiResponse.split("\n\n");
            if (paragraphs.length > 0) {
                return paragraphs[paragraphs.length - 1];
            }
            return aiResponse;
        }
        
        return recommendations.toString();
    }
}
