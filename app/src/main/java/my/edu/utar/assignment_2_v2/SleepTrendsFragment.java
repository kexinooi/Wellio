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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import my.edu.utar.assignment_2_v2.Utils.Firebase;
import my.edu.utar.assignment_2_v2.Utils.GeminiApiService;
import my.edu.utar.assignment_2_v2.model.Mood;

public class SleepTrendsFragment extends Fragment {
    private static final String TAG = "SleepTrendsFragment";
    private BarChart sleepBarChart;
    private TextView tvAverageSleepValue;
    private TextView tvSleepQualityLabel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
    private List<Mood> moodList = new ArrayList<>();
    private GeminiApiService geminiApiService;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_sleep_trends, container, false);
        
        // Initialize views
        sleepBarChart = rootView.findViewById(R.id.sleep_bar_chart);
        tvAverageSleepValue = rootView.findViewById(R.id.tv_average_sleep_value);
        tvSleepQualityLabel = rootView.findViewById(R.id.tv_sleep_quality_label);
        
        // Initialize Gemini API service
        geminiApiService = new GeminiApiService();
        
        // Load sleep data
        loadSleepData();
        
        return rootView;
    }
    
    private void loadSleepData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Get mood data for last 7 days for sleep chart
        Calendar sevenDaysAgo = Calendar.getInstance();
        sevenDaysAgo.add(Calendar.DAY_OF_MONTH, -7);

        Firebase.getInstance().getUserMoodLogsInRange(user.getUid(), sevenDaysAgo.getTimeInMillis(), System.currentTimeMillis())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    moodList.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                        Mood mood = doc.toObject(Mood.class);
                        mood.setId(doc.getId());
                        moodList.add(mood);
                    }
                    updateSleepChart();
                    updateSleepStats();
                    generateSleepInsights();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load sleep data", e));
    }
    
    private void updateSleepChart() {
        if (sleepBarChart == null || moodList.isEmpty()) return;
        
        // Prepare data for last 7 days
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        // Get sleep data by day
        Map<String, Double> sleepByDay = new HashMap<>();
        Calendar today = Calendar.getInstance();
        
        // Initialize last 7 days with 0 sleep
        for (int i = 6; i >= 0; i--) {
            Calendar day = (Calendar) today.clone();
            day.add(Calendar.DAY_OF_MONTH, -i);
            String dayLabel = dateFormat.format(day.getTime());
            sleepByDay.put(dayLabel, 0.0);
            labels.add(dayLabel);
        }
        
        // Fill in actual sleep data
        for (Mood mood : moodList) {
            if (mood.getSleepHours() > 0) {
                Calendar moodDate = Calendar.getInstance();
                moodDate.setTime(mood.getTimestamp());
                String dayLabel = dateFormat.format(moodDate.getTime());
                
                // Update sleep for that day (take the latest entry for each day)
                Double currentSleep = sleepByDay.get(dayLabel);
                if (currentSleep == null || mood.getSleepHours() > currentSleep) {
                    sleepByDay.put(dayLabel, mood.getSleepHours());
                }
            }
        }
        
        // Create bar entries
        for (int i = 0; i < labels.size(); i++) {
            String dayLabel = labels.get(i);
            Double sleepHours = sleepByDay.get(dayLabel);
            entries.add(new BarEntry(i, sleepHours != null ? sleepHours.floatValue() : 0f));
        }
        
        // Create dataset
        BarDataSet dataSet = new BarDataSet(entries, "Sleep Hours");
        dataSet.setColor(Color.parseColor("#6366F1")); // Sleep color
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setBarBorderWidth(0f);
        
        // Customize bar appearance
        dataSet.setDrawValues(true);
        
        // Create bar data
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        
        // Configure chart
        sleepBarChart.setData(barData);
        sleepBarChart.getDescription().setEnabled(false);
        sleepBarChart.setDrawGridBackground(false);
        sleepBarChart.setDrawBorders(false);
        sleepBarChart.getLegend().setEnabled(false);
        
        // Configure X axis
        XAxis xAxis = sleepBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels) {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                }
                return "";
            }
        });
        xAxis.setTextColor(Color.parseColor("#666666"));
        xAxis.setTextSize(11f);
        
        // Configure Y axis
        YAxis yAxis = sleepBarChart.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.setGridColor(Color.parseColor("#EEEEEE"));
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(12f); // Max 12 hours for better visualization
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1fh", value);
            }
        });
        yAxis.setTextColor(Color.parseColor("#666666"));
        yAxis.setTextSize(11f);
        
        // Hide right Y axis
        sleepBarChart.getAxisRight().setEnabled(false);
        
        // Refresh chart
        sleepBarChart.invalidate();
    }
    
    private void updateSleepStats() {
        if (moodList.isEmpty()) {
            tvAverageSleepValue.setText("--h --m");
            tvSleepQualityLabel.setText("No data");
            return;
        }
        
        // Calculate average sleep
        double totalSleep = 0;
        int sleepDays = 0;
        for (Mood mood : moodList) {
            if (mood.getSleepHours() > 0) {
                totalSleep += mood.getSleepHours();
                sleepDays++;
            }
        }
        
        if (sleepDays == 0) {
            tvAverageSleepValue.setText("--h --m");
            tvSleepQualityLabel.setText("No data");
            return;
        }
        
        double averageSleep = totalSleep / sleepDays;
        int hours = (int) averageSleep;
        int minutes = (int) ((averageSleep - hours) * 60);
        
        tvAverageSleepValue.setText(String.format("%dh %dm", hours, minutes));
        
        // Determine sleep quality
        String quality;
        if (averageSleep >= 7) {
            quality = "Good quality";
        } else if (averageSleep >= 5) {
            quality = "Fair quality";
        } else {
            quality = "Poor quality";
        }
        tvSleepQualityLabel.setText(quality);
    }
    
    private void generateSleepInsights() {
        if (moodList.isEmpty()) {
            return;
        }
        
        // Prepare sleep data for AI analysis
        List<Double> sleepHoursList = new ArrayList<>();
        for (Mood mood : moodList) {
            if (mood.getSleepHours() > 0) {
                sleepHoursList.add(mood.getSleepHours());
            }
        }
        
        if (sleepHoursList.isEmpty()) {
            return;
        }
        
        // Prepare sleep data for AI analysis
        StringBuilder sleepData = new StringBuilder();
        sleepData.append("Sleep Pattern Analysis for Last 7 Days:\n\n");
        
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
                updateSleepInsightCards(result);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "AI Sleep Insights Error: " + error);
            }
        });
    }
    
    private void updateSleepInsightCards(String aiResponse) {
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
        
        if (index == 0) {
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].trim().length() > 0 && title == null) {
                    title = lines[i].trim();
                } else if (lines[i].trim().length() > 0 && title != null && desc == null) {
                    desc = lines[i].trim();
                    break;
                }
            }
        } else {
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