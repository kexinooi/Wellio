package my.edu.utar.assignment_2_v2;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import my.edu.utar.assignment_2_v2.Utils.Firebase;
import my.edu.utar.assignment_2_v2.Utils.GeminiApiService;
import my.edu.utar.assignment_2_v2.model.Deadline;

public class AcademicTrendsFragment extends Fragment {

    private static final String TAG = "AcademicTrendsFragment";
    private List<Deadline> allDeadlines = new ArrayList<>();
    private LinearLayout chipsContainer;
    private LinearLayout insightsContainer;
    private ImageView timelineImageView;
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
    private SimpleDateFormat dayFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
    private GeminiApiService geminiApiService;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_academic_trends, container, false);

        chipsContainer = rootView.findViewById(R.id.chips_container);
        insightsContainer = rootView.findViewById(R.id.insights_container);
        timelineImageView = rootView.findViewById(R.id.img_timeline);
        
        geminiApiService = new GeminiApiService();
        
        loadDeadlines();
        return rootView;
    }
    
    private void loadDeadlines() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Firebase.getInstance().getUserAssignments(user.getUid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allDeadlines.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Deadline d = doc.toObject(Deadline.class);
                        d.setId(doc.getId());
                        allDeadlines.add(d);
                    }
                    Collections.sort(allDeadlines, (a, b) -> {
                        if (a.getDueDate() == null) return 1;
                        if (b.getDueDate() == null) return -1;
                        return a.getDueDate().compareTo(b.getDueDate());
                    });
                    populateChips();
                    generateInsights();
                    createTimelineVisualization();
                    generateAIInsights();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load deadlines", e));
    }

    private void populateChips() {
        if (chipsContainer == null) return;
        chipsContainer.removeAllViews();

        int count = 0;
        for (Deadline deadline : allDeadlines) {
            if (count >= 6) break;
            if (deadline.getDueDate() == null) continue;

            long daysLeft = calculateDaysLeft(deadline.getDueDate());
            if (daysLeft < -1) continue;

            MaterialCardView chip = createChip(deadline);
            chipsContainer.addView(chip);
            count++;
        }

        if (count == 0) {
            TextView noData = new TextView(requireContext());
            noData.setText("No upcoming deadlines");
            noData.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_grey_dark));
            noData.setTextSize(14);
            chipsContainer.addView(noData);
        }
    }

    private MaterialCardView createChip(Deadline deadline) {
        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int) (100 * getResources().getDisplayMetrics().density),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 8, 8, 8);
        card.setLayoutParams(params);
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.academic_chip_surface));
        card.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.academic_chip_stroke));
        card.setStrokeWidth(1);
        card.setCardElevation(4);
        card.setRadius(12 * getResources().getDisplayMetrics().density);

        LinearLayout inner = new LinearLayout(requireContext());
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(16, 16, 16, 16);
        inner.setGravity(android.view.Gravity.CENTER);

        String type = deadline.getType() != null ? deadline.getType().toUpperCase() : "ASSIGN";
        int typeColor = getTypeColor(deadline.getType());

        TextView tvType = new TextView(requireContext());
        tvType.setText(type);
        tvType.setTextSize(10);
        tvType.setTextColor(typeColor);
        tvType.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText(deadline.getTitle());
        tvTitle.setTextSize(12);
        tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.academic_chip_title));
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setGravity(android.view.Gravity.CENTER);
        tvTitle.setMaxLines(2);
        tvTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = 8;
        tvTitle.setLayoutParams(titleParams);

        TextView tvDate = new TextView(requireContext());
        tvDate.setText(deadline.getDueDate() != null ? dayFormat.format(deadline.getDueDate()) : "N/A");
        tvDate.setTextSize(10);
        tvDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.academic_chip_date));
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dateParams.topMargin = 8;
        tvDate.setLayoutParams(dateParams);

        inner.addView(tvType);
        inner.addView(tvTitle);
        inner.addView(tvDate);
        card.addView(inner);

        return card;
    }

    private int getTypeColor(String type) {
        if (type == null) return ContextCompat.getColor(requireContext(), R.color.academic_assign_text);
        switch (type.toLowerCase()) {
            case "quiz": return ContextCompat.getColor(requireContext(), R.color.academic_quiz_text);
            case "test": return ContextCompat.getColor(requireContext(), R.color.academic_midterm_text);
            case "midterm": return ContextCompat.getColor(requireContext(), R.color.academic_final_text);
            default: return ContextCompat.getColor(requireContext(), R.color.academic_assign_text);
        }
    }

    private void generateInsights() {
        if (insightsContainer == null) return;

        List<String> titles = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();

        Date now = new Date();
        List<Deadline> next5Days = new ArrayList<>();
        for (Deadline d : allDeadlines) {
            if (d.getDueDate() == null) continue;
            long days = calculateDaysLeft(d.getDueDate());
            if (days >= 0 && days <= 5) next5Days.add(d);
        }
        if (!next5Days.isEmpty()) {
            titles.add(next5Days.size() + " deadline" + (next5Days.size() > 1 ? "s" : "") + " in the next 5 days");
            StringBuilder desc = new StringBuilder();
            for (int i = 0; i < Math.min(3, next5Days.size()); i++) {
                if (i > 0) desc.append(", ");
                desc.append(next5Days.get(i).getTitle());
            }
            if (next5Days.size() > 3) desc.append(" and more");
            desc.append(" due soon — stay on track");
            descriptions.add(desc.toString());
        } else {
            titles.add("No deadlines in the next 5 days");
            descriptions.add("You're in the clear! Use this time to get ahead on future work.");
        }

        Map<String, Integer> monthCount = new HashMap<>();
        for (Deadline d : allDeadlines) {
            if (d.getDueDate() == null) continue;
            String month = monthFormat.format(d.getDueDate());
            monthCount.put(month, monthCount.getOrDefault(month, 0) + 1);
        }
        String busiestMonth = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : monthCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                busiestMonth = entry.getKey();
            }
        }
        if (busiestMonth != null && maxCount > 0) {
            titles.add(busiestMonth + " is your busiest month");
            descriptions.add(maxCount + " deadline" + (maxCount > 1 ? "s" : "") + " scheduled in " + busiestMonth + " — plan your study time wisely");
        } else {
            titles.add("No deadlines scheduled yet");
            descriptions.add("Add your first deadline to start tracking your academic workload.");
        }

        Deadline nearestMidterm = null;
        Deadline nearestDeadline = null;
        for (Deadline d : allDeadlines) {
            if (d.getDueDate() == null) continue;
            if (d.getDueDate().before(now)) continue;
            if (nearestDeadline == null || d.getDueDate().before(nearestDeadline.getDueDate())) {
                nearestDeadline = d;
            }
            if ("midterm".equalsIgnoreCase(d.getType())) {
                if (nearestMidterm == null || d.getDueDate().before(nearestMidterm.getDueDate())) {
                    nearestMidterm = d;
                }
            }
        }
        if (nearestMidterm != null) {
            long days = calculateDaysLeft(nearestMidterm.getDueDate());
            titles.add(nearestMidterm.getTitle() + " is " + days + " day" + (days != 1 ? "s" : "") + " away");
            descriptions.add("Start reviewing past quizzes and assignments to prepare for this midterm.");
        } else if (nearestDeadline != null) {
            long days = calculateDaysLeft(nearestDeadline.getDueDate());
            titles.add(nearestDeadline.getTitle() + " is " + days + " day" + (days != 1 ? "s" : "") + " away");
            descriptions.add("Your nearest deadline is approaching — make sure to allocate enough time.");
        } else {
            titles.add("No upcoming deadlines");
            descriptions.add("You're all caught up! Great job staying on top of your work.");
        }

        int insightIndex = 0;
        for (int i = 0; i < insightsContainer.getChildCount(); i++) {
            View child = insightsContainer.getChildAt(i);
            if (child instanceof MaterialCardView) {
                if (insightIndex < titles.size()) {
                    updateInsightCard((MaterialCardView) child, titles.get(insightIndex), descriptions.get(insightIndex));
                    child.setVisibility(View.VISIBLE);
                    insightIndex++;
                } else {
                    child.setVisibility(View.GONE);
                }
            }
        }
    }

    private void updateInsightCard(MaterialCardView card, String title, String description) {
        List<TextView> textViews = findTextViews(card);
        if (textViews.size() >= 2) {
            textViews.get(0).setText(title);
            textViews.get(1).setText(description);
        }
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

    private void createTimelineVisualization() {
        if (timelineImageView == null) return;

        timelineImageView.post(() -> {
            if (timelineImageView.getWidth() <= 0 || timelineImageView.getHeight() <= 0) return;
            
            int width = timelineImageView.getWidth();
            int height = timelineImageView.getHeight();

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            Paint bgPaint = new Paint();
            bgPaint.setColor(ContextCompat.getColor(requireContext(), R.color.academic_chip_surface));
            canvas.drawRect(0, 0, width, height, bgPaint);

            Paint linePaint = new Paint();
            linePaint.setColor(ContextCompat.getColor(requireContext(), R.color.text_grey_light));
            linePaint.setStrokeWidth(4f);
            canvas.drawLine(50, height / 2, width - 50, height / 2, linePaint);

            List<Deadline> upcoming = new ArrayList<>();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 14);
            Date fourteenDaysLater = cal.getTime();
            
            for (Deadline d : allDeadlines) {
                if (d.getDueDate() != null && !d.getDueDate().before(new Date()) && !d.getDueDate().after(fourteenDaysLater)) {
                    upcoming.add(d);
                }
            }

            if (upcoming.isEmpty()) {
                Paint textPaint = new Paint();
                textPaint.setColor(ContextCompat.getColor(requireContext(), R.color.text_grey_dark));
                textPaint.setTextSize(32f);
                textPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("No upcoming deadlines", width / 2, height / 2, textPaint);
            } else {
                for (int i = 0; i < upcoming.size(); i++) {
                    Deadline deadline = upcoming.get(i);
                    long daysFromStart = calculateDaysBetween(new Date(), deadline.getDueDate());
                    if (daysFromStart < 0) daysFromStart = 0;
                    if (daysFromStart > 14) daysFromStart = 14;
                    
                    float x = 50 + (daysFromStart / 14f) * (width - 100);
                    float y = height / 2;

                    Paint circlePaint = new Paint();
                    circlePaint.setColor(getTypeColor(deadline.getType()));
                    circlePaint.setAntiAlias(true);
                    canvas.drawCircle(x, y, 12f, circlePaint);

                    Paint datePaint = new Paint();
                    datePaint.setColor(ContextCompat.getColor(requireContext(), R.color.text_grey_dark));
                    datePaint.setTextSize(18f);
                    datePaint.setTextAlign(Paint.Align.CENTER);
                    String dateLabel = dayFormat.format(deadline.getDueDate());
                    canvas.drawText(dateLabel, x, y - 20, datePaint);

                    Paint titlePaint = new Paint();
                    titlePaint.setColor(ContextCompat.getColor(requireContext(), R.color.text_black));
                    titlePaint.setTextSize(16f);
                    titlePaint.setTextAlign(Paint.Align.CENTER);
                    String title = deadline.getTitle();
                    if (title.length() > 8) title = title.substring(0, 8) + "...";
                    canvas.drawText(title, x, y + 35, titlePaint);
                }
            }

            timelineImageView.setImageBitmap(bitmap);
        });
    }

    private long calculateDaysBetween(Date start, Date end) {
        long diff = end.getTime() - start.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    private long calculateDaysLeft(Date dueDate) {
        if (dueDate == null) return 999;
        Date now = new Date();
        long diff = dueDate.getTime() - now.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    private void generateAIInsights() {
        StringBuilder academicData = new StringBuilder();
        academicData.append("Academic Workload Analysis:\n\n");
        
        academicData.append("DEADLINE OVERVIEW:\n");
        academicData.append("Total deadlines: ").append(allDeadlines.size()).append("\n");
        
        Date now = new Date();
        List<Deadline> upcoming = new ArrayList<>();
        List<Deadline> overdue = new ArrayList<>();
        
        for (Deadline d : allDeadlines) {
            if (d.getDueDate() == null) continue;
            if (d.getDueDate().before(now)) {
                overdue.add(d);
            } else {
                upcoming.add(d);
            }
        }
        
        academicData.append("Upcoming deadlines: ").append(upcoming.size()).append("\n");
        academicData.append("Overdue deadlines: ").append(overdue.size()).append("\n\n");
        
        Map<String, Integer> typeCount = new HashMap<>();
        for (Deadline d : allDeadlines) {
            String type = d.getType() != null ? d.getType() : "assignment";
            typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
        }
        
        academicData.append("DEADLINE TYPES:\n");
        for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
            academicData.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        academicData.append("\nNEXT 7 DAYS WORKLOAD:\n");
        Calendar sevenDaysLater = Calendar.getInstance();
        sevenDaysLater.add(Calendar.DAY_OF_MONTH, 7);
        int nextWeekDeadlines = 0;
        
        for (Deadline d : upcoming) {
            if (d.getDueDate() != null && !d.getDueDate().after(sevenDaysLater.getTime())) {
                nextWeekDeadlines++;
            }
        }
        academicData.append("Deadlines in next 7 days: ").append(nextWeekDeadlines).append("\n");
        
        academicData.append("\nSTRESS INDICATORS:\n");
        if (overdue.size() > 0) {
            academicData.append("- ").append(overdue.size()).append(" overdue deadlines (HIGH STRESS)\n");
        }
        if (nextWeekDeadlines >= 3) {
            academicData.append("- ").append(nextWeekDeadlines).append(" deadlines next week (HIGH WORKLOAD)\n");
        } else if (nextWeekDeadlines >= 2) {
            academicData.append("- ").append(nextWeekDeadlines).append(" deadlines next week (MODERATE WORKLOAD)\n");
        }
        
        geminiApiService.getAcademicInsights(academicData.toString(), new GeminiApiService.GeminiCallback() {
            @Override
            public void onResult(String result) {
                updateAcademicInsightCards(result);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "AI Academic Insights Error: " + error);
            }
        });
    }
    
    private void updateAcademicInsightCards(String aiResponse) {
        if (insightsContainer == null) return;
        
        // Find the first 3 MaterialCardViews (insight cards) and update them
        int insightCardIndex = 0;
        for (int i = 0; i < insightsContainer.getChildCount(); i++) {
            View child = insightsContainer.getChildAt(i);
            if (child instanceof MaterialCardView && insightCardIndex < 3) {
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
    }
    
    private String[] parseInsights(String aiResponse, int index) {
        String[] lines = aiResponse.split("\n");
        String title = null;
        String desc = null;
        
        int found = 0;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().length() > 0) {
                found++;
                if (found > index * 2 && title == null) {
                    title = lines[i].trim();
                } else if (found > index * 2 && title != null && desc == null) {
                    desc = lines[i].trim();
                    break;
                }
            }
        }
        
        return new String[]{title != null ? title : "Loading...", desc != null ? desc : "Loading..."};
    }
}
