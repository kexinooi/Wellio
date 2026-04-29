package my.edu.utar.assignment_2_v2;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private TextView tvSleepInsightTitle, tvSleepInsightDesc;
    private TextView tvSleepRecommendationTitle, tvSleepRecommendationDesc;
    private TextView tvPeriodWeek, tvPeriodMonth;
    private TextView tvSleepTrendSummaryText;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
    private List<Mood> moodList = new ArrayList<>();
    private GeminiApiService geminiApiService;
    private int selectedTrendDays = 7;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sleep_trends, container, false);
        
        sleepBarChart = rootView.findViewById(R.id.sleep_bar_chart);
        tvAverageSleepValue = rootView.findViewById(R.id.tv_average_sleep_value);
        tvSleepQualityLabel = rootView.findViewById(R.id.tv_sleep_quality_label);
        tvSleepInsightTitle = rootView.findViewById(R.id.tv_sleep_insight_title);
        tvSleepInsightDesc = rootView.findViewById(R.id.tv_sleep_insight_desc);
        tvSleepRecommendationTitle = rootView.findViewById(R.id.tv_sleep_recommendation_title);
        tvSleepRecommendationDesc = rootView.findViewById(R.id.tv_sleep_recommendation_desc);
        tvPeriodWeek = rootView.findViewById(R.id.tv_period_week);
        tvPeriodMonth = rootView.findViewById(R.id.tv_period_month);
        tvSleepTrendSummaryText = rootView.findViewById(R.id.tv_sleep_trend_summary_text);
        
        geminiApiService = new GeminiApiService(requireContext());
        
        setupTrendPeriodSelector();
        loadSleepData();
        
        return rootView;
    }

    private void setupTrendPeriodSelector() {
        if (tvPeriodWeek == null || tvPeriodMonth == null) return;
        tvPeriodWeek.setOnClickListener(v -> updateTrendPeriod(7));
        tvPeriodMonth.setOnClickListener(v -> updateTrendPeriod(30));
        applyTrendPeriodSelection();
    }

    private void updateTrendPeriod(int days) {
        if (selectedTrendDays == days) return;
        selectedTrendDays = days;
        applyTrendPeriodSelection();
        loadSleepData();
    }

    private void applyTrendPeriodSelection() {
        if (tvPeriodWeek == null || tvPeriodMonth == null) return;
        boolean isWeekSelected = selectedTrendDays == 7;
        styleTrendTab(tvPeriodWeek, isWeekSelected);
        styleTrendTab(tvPeriodMonth, !isWeekSelected);
    }

    private void styleTrendTab(TextView tab, boolean selected) {
        if (selected) {
            tab.setBackgroundResource(R.drawable.bg_sleep_tab_selected);
            tab.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.sleep_tab_selected_bg));
            tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.sleep_tab_selected_text));
            tab.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            tab.setBackground(null);
            tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_grey_dark));
            tab.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }
    
    private void loadSleepData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Calendar daysAgo = Calendar.getInstance();
        daysAgo.add(Calendar.DAY_OF_MONTH, -selectedTrendDays);

        Firebase.getInstance().getUserMoodLogsInRange(user.getUid(), daysAgo.getTimeInMillis(), System.currentTimeMillis())
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
                    updateSleepTrendSummary();
                    generateAISleepInsights();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load sleep data", e));
    }
    
    private void updateSleepChart() {
        if (sleepBarChart == null) return;
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        Map<String, Double> sleepByDay = new HashMap<>();
        Calendar today = Calendar.getInstance();
        
        for (int i = selectedTrendDays - 1; i >= 0; i--) {
            Calendar day = (Calendar) today.clone();
            day.add(Calendar.DAY_OF_MONTH, -i);
            String dayLabel = dateFormat.format(day.getTime());
            sleepByDay.put(dayLabel, 0.0);
            labels.add(dayLabel);
        }
        
        for (Mood mood : moodList) {
            if (mood.getSleepHours() > 0 && mood.getTimestamp() != null) {
                Calendar moodDate = Calendar.getInstance();
                moodDate.setTime(mood.getTimestamp());
                String dayLabel = dateFormat.format(moodDate.getTime());
                if (sleepByDay.containsKey(dayLabel)) {
                    Double currentSleep = sleepByDay.get(dayLabel);
                    if (currentSleep == null || mood.getSleepHours() > currentSleep) {
                        sleepByDay.put(dayLabel, mood.getSleepHours());
                    }
                }
            }
        }
        
        float maxSleepValue = 0f;
        for (int i = 0; i < labels.size(); i++) {
            Double sleepHours = sleepByDay.get(labels.get(i));
            float value = sleepHours != null ? sleepHours.floatValue() : 0f;
            maxSleepValue = Math.max(maxSleepValue, value);
            entries.add(new BarEntry(i, value));
        }
        
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(Color.parseColor("#6366F1"));
        dataSet.setDrawValues(selectedTrendDays <= 7);
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        
        sleepBarChart.setData(barData);
        sleepBarChart.getDescription().setEnabled(false);
        sleepBarChart.getLegend().setEnabled(false);
        
        XAxis xAxis = sleepBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(Math.min(labels.size(), 6));
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setTextColor(Color.parseColor("#666666"));
        xAxis.setTextSize(10f);
        
        YAxis yAxis = sleepBarChart.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(Math.max(8f, (float) Math.ceil(maxSleepValue + 1f)));
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.0fh", value);
            }
        });
        yAxis.setTextColor(Color.parseColor("#666666"));
        yAxis.setTextSize(10f);
        
        sleepBarChart.getAxisRight().setEnabled(false);
        sleepBarChart.invalidate();
    }
    
    private void updateSleepStats() {
        if (moodList.isEmpty()) {
            tvAverageSleepValue.setText(R.string.sleep_no_data_value);
            tvSleepQualityLabel.setText(R.string.sleep_no_data_label);
            return;
        }
        
        double totalSleep = 0;
        int sleepDays = 0;
        for (Mood mood : moodList) {
            if (mood.getSleepHours() > 0) {
                totalSleep += mood.getSleepHours();
                sleepDays++;
            }
        }
        
        if (sleepDays == 0) {
            tvAverageSleepValue.setText(R.string.sleep_no_data_value);
            tvSleepQualityLabel.setText(R.string.sleep_no_data_label);
            return;
        }
        
        double averageSleep = totalSleep / sleepDays;
        int hours = (int) averageSleep;
        int minutes = (int) ((averageSleep - hours) * 60);
        tvAverageSleepValue.setText(String.format(Locale.getDefault(), "%dh %dm", hours, minutes));
        
        String quality = averageSleep >= 7 ? getString(R.string.sleep_quality_good) : averageSleep >= 5 ? getString(R.string.sleep_quality_fair) : getString(R.string.sleep_quality_poor);
        tvSleepQualityLabel.setText(quality);
    }

    private void updateSleepTrendSummary() {
        if (tvSleepTrendSummaryText == null) return;
        Calendar today = Calendar.getInstance();
        resetToStartOfDay(today);
        Calendar currentPeriodStart = (Calendar) today.clone();
        currentPeriodStart.add(Calendar.DAY_OF_MONTH, -(selectedTrendDays - 1));
        Calendar previousPeriodStart = (Calendar) currentPeriodStart.clone();
        previousPeriodStart.add(Calendar.DAY_OF_MONTH, -selectedTrendDays);

        List<Double> currentPeriodSleep = collectSleepValuesBetween(currentPeriodStart.getTime(), new Date());
        Date previousPeriodEnd = new Date(currentPeriodStart.getTimeInMillis() - 1);
        List<Double> previousPeriodSleep = collectSleepValuesBetween(previousPeriodStart.getTime(), previousPeriodEnd);

        String periodLabel = selectedTrendDays == 7 ? "week" : "past 30 days";
        String comparisonLabel = selectedTrendDays == 7 ? "last week" : "the previous 30 days";

        if (currentPeriodSleep.isEmpty()) {
            tvSleepTrendSummaryText.setText(String.format("No sleep entries recorded for the %s.", periodLabel));
            return;
        }

        double currentAverage = calculateAverage(currentPeriodSleep);
        String averageLabel = formatSleepDuration(currentAverage);

        if (previousPeriodSleep.isEmpty()) {
            tvSleepTrendSummaryText.setText(String.format("Your sleep trends around %s, averaging %s based on your %s logs.", getSleepQualityLabel(currentAverage).toLowerCase(Locale.getDefault()), averageLabel, periodLabel));
            return;
        }

        double previousAverage = calculateAverage(previousPeriodSleep);
        double difference = currentAverage - previousAverage;
        int roundedPercent = (int) Math.round(Math.abs(previousAverage == 0 ? 0 : (difference / previousAverage) * 100.0));

        if (Math.abs(difference) < 0.15) {
            tvSleepTrendSummaryText.setText(String.format("Your sleep is steady this %s, holding around %s compared with %s.", periodLabel, averageLabel, comparisonLabel));
        } else if (difference > 0) {
            tvSleepTrendSummaryText.setText(String.format("Your sleep improved by %d%% compared with %s, averaging %s.", roundedPercent, comparisonLabel, averageLabel));
        } else {
            tvSleepTrendSummaryText.setText(String.format("Your sleep dropped by %d%% compared with %s, averaging %s.", roundedPercent, comparisonLabel, averageLabel));
        }
    }
    
    private void generateAISleepInsights() {
        List<Double> sleepHoursList = new ArrayList<>();
        for (Mood mood : moodList) {
            if (mood.getSleepHours() > 0) sleepHoursList.add(mood.getSleepHours());
        }
        
        if (sleepHoursList.isEmpty()) return;

        StringBuilder sleepData = new StringBuilder();
        sleepData.append("Sleep Pattern Analysis for Last ").append(selectedTrendDays).append(" Days:\n\n");
        double avgSleep = calculateAverage(sleepHoursList);
        sleepData.append("Average Sleep: ").append(String.format(Locale.getDefault(), "%.1f", avgSleep)).append(" hours\n");
        sleepData.append("Consistency (StdDev): ").append(String.format(Locale.getDefault(), "%.1f", Math.sqrt(calculateVariance(sleepHoursList, avgSleep)))).append("\n");
        
        geminiApiService.getSleepInsights(sleepData.toString(), new GeminiApiService.GeminiCallback() {
            @Override
            public void onResult(String result) {
                String[] parsed = parseAIResponse(result);
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (tvSleepInsightTitle != null && parsed[0] != null) tvSleepInsightTitle.setText(parsed[0]);
                        if (tvSleepInsightDesc != null && parsed[1] != null) tvSleepInsightDesc.setText(parsed[1]);
                        if (tvSleepRecommendationTitle != null && parsed[2] != null) tvSleepRecommendationTitle.setText(parsed[2]);
                        if (tvSleepRecommendationDesc != null && parsed[3] != null) tvSleepRecommendationDesc.setText(parsed[3]);
                    });
                }
            }
            @Override public void onError(String error) { 
                Log.e(TAG, "AI Sleep Insights Error: " + error); 
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> applyHardcodedSleepInsights(sleepHoursList));
                }
            }
        });
    }

    private void applyHardcodedSleepInsights(List<Double> sleepHoursList) {
        if (tvSleepInsightTitle == null || tvSleepInsightDesc == null ||
                tvSleepRecommendationTitle == null || tvSleepRecommendationDesc == null) {
            return;
        }

        double avgSleep = calculateAverage(sleepHoursList);
        double minSleep = 24;
        double maxSleep = 0;
        for (Double h : sleepHoursList) {
            if (h < minSleep) minSleep = h;
            if (h > maxSleep) maxSleep = h;
        }
        double stdDev = Math.sqrt(calculateVariance(sleepHoursList, avgSleep));

        if (avgSleep >= 7) {
            tvSleepInsightTitle.setText("Your sleep is in a healthy range");
            tvSleepInsightDesc.setText("You are averaging " + formatSleepDuration(avgSleep) + " with a range of " + formatSleepDuration(minSleep) + " to " + formatSleepDuration(maxSleep) + ".");
        } else if (avgSleep >= 5) {
            tvSleepInsightTitle.setText("Your sleep is a bit short");
            tvSleepInsightDesc.setText("You are averaging " + formatSleepDuration(avgSleep) + ", which may explain lower energy on some days.");
        } else {
            tvSleepInsightTitle.setText("Your recent sleep is running low");
            tvSleepInsightDesc.setText("You are averaging only " + formatSleepDuration(avgSleep) + ", which can affect mood, focus, and recovery.");
        }

        if (stdDev > 1.5) {
            tvSleepRecommendationTitle.setText("Stabilize your sleep timing");
            tvSleepRecommendationDesc.setText("Your sleep pattern looks irregular. Try keeping your bedtime and wake time closer each day.");
        } else if (avgSleep < 6.5) {
            tvSleepRecommendationTitle.setText("Extend sleep by 30 to 60 minutes");
            tvSleepRecommendationDesc.setText("A slightly earlier bedtime can help move your average into a more restorative range.");
        } else {
            tvSleepRecommendationTitle.setText("Keep your routine consistent");
            tvSleepRecommendationDesc.setText("Your sleep pattern looks fairly steady. Maintain the same evening routine to preserve the momentum.");
        }
    }
    
    private String[] parseAIResponse(String aiResponse) {
        String[] lines = aiResponse.split("\n");
        String insightTitle = null, insightDesc = null, recTitle = null, recDesc = null;
        boolean inInsight = false, inRecommendation = false;
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.toUpperCase().contains("INSIGHT")) { inInsight = true; inRecommendation = false; continue; }
            if (trimmed.toUpperCase().contains("RECOMMENDATION")) { inRecommendation = true; inInsight = false; continue; }
            if (trimmed.contains(":") || trimmed.contains("-") || trimmed.contains("*")) continue;
            if (inInsight) { if (insightTitle == null) insightTitle = trimmed; else if (insightDesc == null) insightDesc = trimmed; }
            else if (inRecommendation) { if (recTitle == null) recTitle = trimmed; else if (recDesc == null) recDesc = trimmed; }
        }
        return new String[]{ insightTitle != null ? insightTitle : getString(R.string.sleep_insight_default_title), insightDesc != null ? insightDesc : getString(R.string.sleep_insight_default_desc), recTitle != null ? recTitle : getString(R.string.sleep_recommendation_default_title), recDesc != null ? recDesc : getString(R.string.sleep_recommendation_default_desc) };
    }

    private void resetToStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0);
    }

    private List<Double> collectSleepValuesBetween(Date start, Date end) {
        List<Double> values = new ArrayList<>();
        for (Mood mood : moodList) {
            if (mood.getTimestamp() != null && !mood.getTimestamp().before(start) && !mood.getTimestamp().after(end) && mood.getSleepHours() > 0) values.add(mood.getSleepHours());
        }
        return values;
    }

    private String formatSleepDuration(double hoursValue) {
        int hours = (int) hoursValue;
        int minutes = (int) Math.round((hoursValue - hours) * 60);
        if (minutes == 60) { hours += 1; minutes = 0; }
        return hours + "h " + minutes + "m";
    }

    private String getSleepQualityLabel(double averageSleep) {
        return averageSleep >= 7 ? getString(R.string.sleep_quality_good) : averageSleep >= 5 ? getString(R.string.sleep_quality_fair) : getString(R.string.sleep_quality_poor);
    }
    
    private double calculateAverage(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Double value : values) sum += value;
        return sum / values.size();
    }
    
    private double calculateVariance(List<Double> values, double mean) {
        if (values.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Double value : values) sum += Math.pow(value - mean, 2);
        return sum / values.size();
    }
}
