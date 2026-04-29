package my.edu.utar.assignment_2_v2;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    private ImageView ivAverageMoodIcon, ivImprovedCheck;
    private TextView tvAverageMoodStatus, tvMoodImprovementPercent, tvMoodImprovementDesc;
    private TextView tvImprovedLabel, tvTrendSummaryText, tvPeriodWeek, tvPeriodMonth;
    private TextView tvMoodInsightTitle, tvMoodInsightDesc;
    private TextView tvMoodRecommendationTitle, tvMoodRecommendationDesc;
    private int selectedTrendDays = 7;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_mood_trends, container, false);
        
        geminiApiService = new GeminiApiService(requireContext());
        moodLineChart = rootView.findViewById(R.id.mood_line_chart);
        ivAverageMoodIcon = rootView.findViewById(R.id.iv_average_mood_icon);
        ivImprovedCheck = rootView.findViewById(R.id.iv_improved_check);
        tvAverageMoodStatus = rootView.findViewById(R.id.tv_average_mood_status);
        tvMoodImprovementPercent = rootView.findViewById(R.id.tv_mood_improvement_percent);
        tvMoodImprovementDesc = rootView.findViewById(R.id.tv_mood_improvement_desc);
        tvImprovedLabel = rootView.findViewById(R.id.tv_improved_label);
        tvTrendSummaryText = rootView.findViewById(R.id.tv_trend_summary_text);
        tvPeriodWeek = rootView.findViewById(R.id.tv_period_week);
        tvPeriodMonth = rootView.findViewById(R.id.tv_period_month);
        tvMoodInsightTitle = rootView.findViewById(R.id.tv_mood_insight_title);
        tvMoodInsightDesc = rootView.findViewById(R.id.tv_mood_insight_desc);
        tvMoodRecommendationTitle = rootView.findViewById(R.id.tv_mood_recommendation_title);
        tvMoodRecommendationDesc = rootView.findViewById(R.id.tv_mood_recommendation_desc);

        setupTrendPeriodSelector();
        loadMoodData();
        return rootView;
    }

    private void loadMoodData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

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
                    updateMoodAnalytics();
                    updateMoodLineChart();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load mood data", e));
    }

    private void updateMoodAnalytics() {
        if (moodList.isEmpty()) {
            return;
        }

        Map<String, Integer> moodCounts = new HashMap<>();
        Map<String, Integer> feelingCounts = new HashMap<>();
        List<Double> sleepHoursList = new ArrayList<>();
        
        for (Mood mood : moodList) {
            String moodType = mood.getMood();
            if (moodType != null) {
                moodCounts.put(moodType, moodCounts.getOrDefault(moodType, 0) + 1);
            }
            
            String feelings = mood.getFeel();
            if (feelings != null && !feelings.isEmpty()) {
                String[] feelingArray = feelings.split(",\\s*");
                for (String feeling : feelingArray) {
                    if (!feeling.trim().isEmpty()) {
                        feelingCounts.put(feeling.trim(), feelingCounts.getOrDefault(feeling.trim(), 0) + 1);
                    }
                }
            }
            
            if (mood.getSleepHours() > 0) {
                sleepHoursList.add(mood.getSleepHours());
            }
        }

        updateWeeklyMoodSummary();
        generateMoodInsights(moodCounts, feelingCounts, sleepHoursList);
    }

    private void updateWeeklyMoodSummary() {
        if (tvAverageMoodStatus == null || tvMoodImprovementPercent == null || tvMoodImprovementDesc == null) {
            return;
        }

        Calendar weekAgo = Calendar.getInstance();
        weekAgo.add(Calendar.DAY_OF_MONTH, -7);

        Map<String, Integer> weeklyMoodCounts = new HashMap<>();
        for (Mood mood : moodList) {
            if (mood.getTimestamp() == null || mood.getMood() == null) continue;
            if (mood.getTimestamp().before(weekAgo.getTime())) continue;
            weeklyMoodCounts.put(mood.getMood(), weeklyMoodCounts.getOrDefault(mood.getMood(), 0) + 1);
        }

        String weeklyModeMood = findModeMood(weeklyMoodCounts);
        if (weeklyModeMood == null) {
            tvAverageMoodStatus.setText(R.string.mood_no_data);
            tvMoodImprovementDesc.setText(R.string.mood_no_week_data);
            tvMoodImprovementPercent.setVisibility(View.GONE);
            if (ivAverageMoodIcon != null) {
                ivAverageMoodIcon.setImageResource(R.drawable.mood_good);
            }
            return;
        }

        tvAverageMoodStatus.setText(weeklyModeMood);
        tvMoodImprovementDesc.setText(R.string.mood_week_mode_desc);
        tvMoodImprovementPercent.setVisibility(View.GONE);

        if (ivAverageMoodIcon != null) {
            ivAverageMoodIcon.setImageResource(getMoodIconRes(weeklyModeMood));
        }
    }

    private String findModeMood(Map<String, Integer> moodCounts) {
        String modeMood = null;
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : moodCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                modeMood = entry.getKey();
                maxCount = entry.getValue();
            }
        }

        return modeMood;
    }

    private int getMoodIconRes(String mood) {
        if (mood == null) return R.drawable.mood_good;
        switch (mood) {
            case "Amazing":
                return R.drawable.mood_amazing;
            case "Good":
                return R.drawable.mood_good;
            case "Okay":
                return R.drawable.mood_okay;
            case "Bad":
                return R.drawable.mood_bad;
            case "Very Bad":
                return R.drawable.mood_very_bad;
            default:
                return R.drawable.mood_good;
        }
    }

    private void setupTrendPeriodSelector() {
        if (tvPeriodWeek == null || tvPeriodMonth == null) {
            return;
        }

        tvPeriodWeek.setOnClickListener(v -> updateTrendPeriod(7));
        tvPeriodMonth.setOnClickListener(v -> updateTrendPeriod(30));
        applyTrendPeriodSelection();
    }

    private void updateTrendPeriod(int days) {
        if (selectedTrendDays == days) {
            return;
        }

        selectedTrendDays = days;
        applyTrendPeriodSelection();
        updateMoodLineChart();
    }

    private void applyTrendPeriodSelection() {
        if (tvPeriodWeek == null || tvPeriodMonth == null) {
            return;
        }

        boolean isWeekSelected = selectedTrendDays == 7;
        styleTrendTab(tvPeriodWeek, isWeekSelected);
        styleTrendTab(tvPeriodMonth, !isWeekSelected);
    }

    private void styleTrendTab(TextView tab, boolean selected) {
        if (selected) {
            tab.setBackgroundResource(R.drawable.bg_sleep_tab_selected);
            tab.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.insights_positive_bg));
            tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.insights_positive_text));
            tab.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            tab.setBackground(null);
            tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_grey_dark));
            tab.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }
    
    private void updateMoodLineChart() {
        if (moodLineChart == null || moodList.isEmpty()) return;

        List<Mood> sortedMoods = getSortedMoods();
        LinkedHashMap<String, List<Integer>> dailyMoodValues = new LinkedHashMap<>();
        Calendar cursor = Calendar.getInstance();
        resetToStartOfDay(cursor);
        cursor.add(Calendar.DAY_OF_MONTH, -(selectedTrendDays - 1));

        for (int i = 0; i < selectedTrendDays; i++) {
            dailyMoodValues.put(getDayKey(cursor.getTime()), new ArrayList<>());
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }

        for (Mood mood : sortedMoods) {
            if (mood.getTimestamp() == null) continue;

            int moodValue = getMoodValue(mood.getMood());
            if (moodValue == 0) continue;

            String dayKey = getDayKey(mood.getTimestamp());
            if (dailyMoodValues.containsKey(dayKey)) {
                dailyMoodValues.get(dayKey).add(moodValue);
            }
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, List<Integer>> dayEntry : dailyMoodValues.entrySet()) {
            List<Integer> values = dayEntry.getValue();
            if (!values.isEmpty()) {
                entries.add(new Entry(index, (float) calculateAverageInt(values)));
            }
            labels.add(formatDayLabel(dayEntry.getKey()));
            index++;
        }

        if (entries.isEmpty()) return;

        LineDataSet dataSet = new LineDataSet(entries, "Mood Trend");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(Color.parseColor("#4CAF50"));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true);
        Drawable fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.mood_sphere_gradient);
        if (fillDrawable != null) {
            dataSet.setFillDrawable(fillDrawable);
        } else {
            dataSet.setFillColor(Color.parseColor("#DFF3E4"));
        }
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet.setHighLightColor(Color.parseColor("#2E7D32"));
        dataSet.setDrawHorizontalHighlightIndicator(false);

        LineData lineData = new LineData(dataSet);

        moodLineChart.setData(lineData);
        moodLineChart.getDescription().setEnabled(false);
        moodLineChart.setDrawGridBackground(false);
        moodLineChart.setDrawBorders(false);
        moodLineChart.getLegend().setEnabled(false);
        moodLineChart.setTouchEnabled(true);
        moodLineChart.setPinchZoom(true);
        moodLineChart.setScaleEnabled(false);
        moodLineChart.setDoubleTapToZoomEnabled(false);
        moodLineChart.setExtraBottomOffset(8f);
        moodLineChart.animateX(700);

        XAxis xAxis = moodLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(Math.min(selectedTrendDays == 7 ? 7 : 6, labels.size()), true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setTextColor(Color.parseColor("#666666"));
        xAxis.setTextSize(10f);
        xAxis.setAvoidFirstLastClipping(true);

        YAxis yAxis = moodLineChart.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.setGridColor(Color.parseColor("#EEEEEE"));
        yAxis.setAxisMinimum(1f);
        yAxis.setAxisMaximum(5f);
        yAxis.setLabelCount(5, true);
        yAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                switch (Math.round(value)) {
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
        yAxis.setTextSize(10f);

        moodLineChart.getAxisRight().setEnabled(false);
        moodLineChart.invalidate();

        updateTrendSummary();
    }

    private void updateTrendSummary() {
        if (tvImprovedLabel == null || tvTrendSummaryText == null || ivImprovedCheck == null) {
            return;
        }

        Calendar today = Calendar.getInstance();
        resetToStartOfDay(today);

        Calendar currentPeriodStart = (Calendar) today.clone();
        currentPeriodStart.add(Calendar.DAY_OF_MONTH, -(selectedTrendDays - 1));

        Calendar previousPeriodStart = (Calendar) currentPeriodStart.clone();
        previousPeriodStart.add(Calendar.DAY_OF_MONTH, -selectedTrendDays);

        List<Integer> currentPeriodValues = collectMoodValuesBetween(currentPeriodStart.getTime(), new Date());
        Date previousPeriodEnd = new Date(currentPeriodStart.getTimeInMillis() - 1);
        List<Integer> previousPeriodValues = collectMoodValuesBetween(previousPeriodStart.getTime(), previousPeriodEnd);
        String periodLabel = selectedTrendDays == 7 ? "week" : "past 30 days";
        String comparisonLabel = selectedTrendDays == 7 ? "last week" : "the previous 30 days";

        if (currentPeriodValues.isEmpty()) {
            tvImprovedLabel.setText(R.string.mood_status_no_data);
            tvTrendSummaryText.setText("No mood entries recorded for the " + periodLabel + ".");
            ivImprovedCheck.setVisibility(View.VISIBLE);
            applyTrendVisuals(Color.parseColor("#6B7280"));
            return;
        }

        double currentPeriodAverage = calculateAverageInt(currentPeriodValues);
        String currentPeriodMood = getMoodLabelForScore(currentPeriodAverage);

        if (previousPeriodValues.isEmpty()) {
            tvImprovedLabel.setText("");
            tvTrendSummaryText.setText("Your mood trends around " + currentPeriodMood + " based on your " + periodLabel + " logs.");
            ivImprovedCheck.setVisibility(View.GONE);
            applyTrendVisuals(Color.parseColor("#4F46E5"));
            return;
        }

        ivImprovedCheck.setVisibility(View.VISIBLE);

        double previousPeriodAverage = calculateAverageInt(previousPeriodValues);
        double difference = currentPeriodAverage - previousPeriodAverage;
        double percentChange = previousPeriodAverage == 0 ? 0 : (difference / previousPeriodAverage) * 100.0;
        int roundedPercent = (int) Math.round(Math.abs(percentChange));

        if (Math.abs(difference) < 0.15) {
            tvImprovedLabel.setText(R.string.mood_status_stable);
            tvTrendSummaryText.setText("Your mood is steady this " + periodLabel + ", holding around " + currentPeriodMood + " compared with " + comparisonLabel + ".");
            applyTrendVisuals(Color.parseColor("#6B7280"));
        } else if (difference > 0) {
            tvImprovedLabel.setText(R.string.mood_status_improved);
            tvTrendSummaryText.setText("Your mood improved by " + roundedPercent + "% compared with " + comparisonLabel + ", trending around " + currentPeriodMood + ".");
            applyTrendVisuals(Color.parseColor("#16A34A"));
        } else {
            tvImprovedLabel.setText(R.string.mood_status_declined);
            tvTrendSummaryText.setText("Your mood dropped by " + roundedPercent + "% compared with " + comparisonLabel + ", trending around " + currentPeriodMood + ".");
            applyTrendVisuals(Color.parseColor("#DC2626"));
        }
    }

    private void applyTrendVisuals(int color) {
        tvImprovedLabel.setTextColor(color);
        ivImprovedCheck.setColorFilter(color);
        tvTrendSummaryText.setTextColor(ContextCompat.getColor(requireContext(), R.color.insights_summary_text));
    }

    private List<Mood> getSortedMoods() {
        List<Mood> sortedMoods = new ArrayList<>(moodList);
        Collections.sort(sortedMoods, (first, second) -> {
            Date firstDate = first.getTimestamp();
            Date secondDate = second.getTimestamp();
            if (firstDate == null && secondDate == null) return 0;
            if (firstDate == null) return 1;
            if (secondDate == null) return -1;
            return firstDate.compareTo(secondDate);
        });
        return sortedMoods;
    }

    private int getMoodValue(String mood) {
        if (mood == null) return 0;
        switch (mood) {
            case "Amazing":
                return 5;
            case "Good":
                return 4;
            case "Okay":
                return 3;
            case "Bad":
                return 2;
            case "Very Bad":
                return 1;
            default:
                return 0;
        }
    }

    private String getMoodLabelForScore(double score) {
        if (score >= 4.5) return "Amazing";
        if (score >= 3.5) return "Good";
        if (score >= 2.5) return "Okay";
        if (score >= 1.5) return "Bad";
        return "Very Bad";
    }

    private String getDayKey(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
    }

    private String formatDayLabel(String dayKey) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dayKey);
            return date == null ? "" : dateFormat.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    private void resetToStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private List<Integer> collectMoodValuesBetween(Date start, Date end) {
        List<Integer> values = new ArrayList<>();
        for (Mood mood : moodList) {
            if (mood.getTimestamp() == null) continue;
            if (mood.getTimestamp().before(start) || mood.getTimestamp().after(end)) continue;

            int moodValue = getMoodValue(mood.getMood());
            if (moodValue > 0) {
                values.add(moodValue);
            }
        }
        return values;
    }

    private double calculateAverageInt(List<Integer> values) {
        if (values.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Integer value : values) {
            sum += value;
        }
        return sum / values.size();
    }

    private void generateMoodInsights(Map<String, Integer> moodCounts, Map<String, Integer> feelingCounts, List<Double> sleepHoursList) {
        StringBuilder moodData = new StringBuilder();
        moodData.append("Mood Analysis for Last ").append(selectedTrendDays).append(" Days:\n\n");
        
        moodData.append("MOOD DISTRIBUTION:\n");
        for (Map.Entry<String, Integer> entry : moodCounts.entrySet()) {
            moodData.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" times\n");
        }
        
        moodData.append("\nTOP FEELINGS:\n");
        List<Map.Entry<String, Integer>> sortedFeelings = new ArrayList<>(feelingCounts.entrySet());
        Collections.sort(sortedFeelings, (a, b) -> b.getValue().compareTo(a.getValue()));
        int limit = Math.min(5, sortedFeelings.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> entry = sortedFeelings.get(i);
            moodData.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" times\n");
        }
        
        moodData.append("\nSLEEP STATISTICS:\n");
        if (!sleepHoursList.isEmpty()) {
            double avgSleep = calculateAverage(sleepHoursList);
            moodData.append("Average sleep: ").append(String.format("%.1f", avgSleep)).append(" hours\n");
        }
        
        geminiApiService.getMoodInsights(moodData.toString(), new GeminiApiService.GeminiCallback() {
            @Override
            public void onResult(String result) {
                String[] parsed = parseAIResponse(result);
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (tvMoodInsightTitle != null && parsed[0] != null) {
                            tvMoodInsightTitle.setText(parsed[0]);
                        }
                        if (tvMoodInsightDesc != null && parsed[1] != null) {
                            tvMoodInsightDesc.setText(parsed[1]);
                        }
                        if (tvMoodRecommendationTitle != null && parsed[2] != null) {
                            tvMoodRecommendationTitle.setText(parsed[2]);
                        }
                        if (tvMoodRecommendationDesc != null && parsed[3] != null) {
                            tvMoodRecommendationDesc.setText(parsed[3]);
                        }
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "AI Mood Insights Error: " + error);
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> applyHardcodedMoodInsights(moodCounts, feelingCounts, sleepHoursList));
                }
            }
        });
    }

    private void applyHardcodedMoodInsights(Map<String, Integer> moodCounts, Map<String, Integer> feelingCounts, List<Double> sleepHoursList) {
        if (tvMoodInsightTitle == null || tvMoodInsightDesc == null ||
                tvMoodRecommendationTitle == null || tvMoodRecommendationDesc == null) {
            return;
        }

        if (moodCounts.isEmpty()) {
            tvMoodInsightTitle.setText(R.string.mood_no_data);
            tvMoodInsightDesc.setText(R.string.mood_no_week_data);
            tvMoodRecommendationTitle.setText("Log mood more consistently");
            tvMoodRecommendationDesc.setText("Add a few mood entries this week so the page can surface clearer emotional patterns.");
            return;
        }

        String dominantMood = findModeMood(moodCounts);
        int dominantMoodCount = dominantMood != null ? moodCounts.getOrDefault(dominantMood, 0) : 0;
        String topFeeling = null;
        int maxFeeling = 0;
        for (Map.Entry<String, Integer> entry : feelingCounts.entrySet()) {
            if (entry.getValue() > maxFeeling) {
                maxFeeling = entry.getValue();
                topFeeling = entry.getKey();
            }
        }

        tvMoodInsightTitle.setText(dominantMood != null ? dominantMood + " is your dominant mood" : "Mood pattern detected");
        if (topFeeling != null) {
            tvMoodInsightDesc.setText("You logged " + dominantMood + " " + dominantMoodCount + " times, with \"" + topFeeling + "\" appearing most often.");
        } else {
            tvMoodInsightDesc.setText("You logged " + dominantMood + " " + dominantMoodCount + " times during the selected period.");
        }

        if (!sleepHoursList.isEmpty() && calculateAverage(sleepHoursList) < 6.5) {
            tvMoodRecommendationTitle.setText("Protect your sleep routine");
            tvMoodRecommendationDesc.setText("Your recent sleep average is on the lower side. Aim for steadier sleep to support a more stable mood.");
        } else if ("Bad".equals(dominantMood) || "Very Bad".equals(dominantMood)) {
            tvMoodRecommendationTitle.setText("Create a recovery reset");
            tvMoodRecommendationDesc.setText("Try a short walk, journaling, or a screen break after stressful periods to interrupt negative mood cycles.");
        } else if ("Okay".equals(dominantMood)) {
            tvMoodRecommendationTitle.setText("Look for your uplift moments");
            tvMoodRecommendationDesc.setText("Notice which routines or people tend to lift your mood from Okay to Good, then repeat them intentionally.");
        } else {
            tvMoodRecommendationTitle.setText("Reinforce what is working");
            tvMoodRecommendationDesc.setText("Your recent mood pattern is fairly positive. Keep the routines that helped you stay consistent.");
        }
    }
    
    private String[] parseAIResponse(String aiResponse) {
        Log.d(TAG, "AI Response: " + aiResponse);
        
        String[] lines = aiResponse.split("\n");
        String insightTitle = null, insightDesc = null;
        String recTitle = null, recDesc = null;
        
        boolean inInsight = false, inRecommendation = false;
        List<String> insightLines = new ArrayList<>();
        List<String> recLines = new ArrayList<>();
        List<String> allContentLines = new ArrayList<>();
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            Log.d(TAG, "Line " + i + ": [" + line + "] inInsight=" + inInsight + " inRecommendation=" + inRecommendation);
            
            // Detect section headers more flexibly - only match if line IS a header
            String upperLine = line.toUpperCase();
            if (upperLine.equals("INSIGHT") || upperLine.equals("INSIGHT:")) {
                inInsight = true;
                inRecommendation = false;
                Log.d(TAG, "Set inInsight=true");
                continue;
            }
            if (upperLine.equals("RECOMMENDATION") || upperLine.equals("RECOMMENDATION:") || 
                upperLine.equals("SUGGESTION") || upperLine.equals("SUGGESTION:")) {
                inRecommendation = true;
                inInsight = false;
                Log.d(TAG, "Set inRecommendation=true");
                continue;
            }
            
            // Skip empty lines but maintain section state
            if (line.isEmpty()) {
                Log.d(TAG, "Skipping empty line, maintaining state");
                continue;
            }
            
            // Skip obvious prompt/metadata lines - only skip if line starts with these keywords
            if (upperLine.startsWith("DATA:") || upperLine.startsWith("ANALYSIS") || 
                upperLine.startsWith("STATISTICS") || upperLine.startsWith("PATTERN") ||
                upperLine.startsWith("DISTRIBUTION") || upperLine.startsWith("TOTAL") ||
                upperLine.startsWith("AVERAGE") || line.startsWith("=") || line.startsWith("---")) {
                Log.d(TAG, "Skipping metadata line");
                continue;
            }
            
            // Collect lines for each section
            if (inInsight) {
                insightLines.add(line);
                Log.d(TAG, "Added to insightLines: " + line);
            } else if (inRecommendation) {
                recLines.add(line);
                Log.d(TAG, "Added to recLines: " + line);
            } else {
                // Collect all other content lines as fallback
                allContentLines.add(line);
                Log.d(TAG, "Added to allContentLines: " + line);
            }
        }
        
        Log.d(TAG, "Final - insightLines size: " + insightLines.size() + ", recLines size: " + recLines.size());
        
        // Extract insight title and description
        if (!insightLines.isEmpty()) {
            insightTitle = insightLines.get(0);
            if (insightLines.size() > 1) {
                insightDesc = insightLines.get(1);
            }
        }
        
        // Extract recommendation title and description
        if (!recLines.isEmpty()) {
            recTitle = recLines.get(0);
            if (recLines.size() > 1) {
                recDesc = recLines.get(1);
            }
        }
        
        // Fallback 1: if no sections detected, try to parse all content lines
        if (insightTitle == null && recTitle == null && !allContentLines.isEmpty()) {
            insightTitle = allContentLines.get(0);
            if (allContentLines.size() > 1) {
                insightDesc = allContentLines.get(1);
            }
            if (allContentLines.size() > 2) {
                recTitle = allContentLines.get(2);
            }
            if (allContentLines.size() > 3) {
                recDesc = allContentLines.get(3);
            }
        }
        
        // Fallback 2: if still missing, try insightLines
        if (insightTitle == null && recTitle == null && !insightLines.isEmpty()) {
            insightTitle = insightLines.get(0);
            if (insightLines.size() > 1) {
                insightDesc = insightLines.get(1);
            }
            if (insightLines.size() > 2) {
                recTitle = insightLines.get(2);
            }
            if (insightLines.size() > 3) {
                recDesc = insightLines.get(3);
            }
        }
        
        Log.d(TAG, "Parsed - InsightTitle: " + insightTitle + ", InsightDesc: " + insightDesc + ", RecTitle: " + recTitle + ", RecDesc: " + recDesc);
        
        return new String[]{
            insightTitle != null ? insightTitle : getString(R.string.mood_loading),
            insightDesc != null ? insightDesc : getString(R.string.mood_loading),
            recTitle != null ? recTitle : getString(R.string.mood_loading),
            recDesc != null ? recDesc : getString(R.string.mood_loading)
        };
    }

    private double calculateAverage(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Double value : values) {
            sum += value;
        }
        return sum / values.size();
    }
}
