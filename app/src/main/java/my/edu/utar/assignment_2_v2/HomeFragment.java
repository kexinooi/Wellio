package my.edu.utar.assignment_2_v2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import my.edu.utar.assignment_2_v2.Utils.Firebase;
import my.edu.utar.assignment_2_v2.adapter.DeadlineAdapter;
import my.edu.utar.assignment_2_v2.model.Deadline;
import my.edu.utar.assignment_2_v2.model.Mood;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private List<Deadline> upcomingDeadlines = new ArrayList<>();
    private DeadlineAdapter deadlineAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeLoadingState(view);

        // Dynamic Greeting with User Name
        TextView tvGreeting = view.findViewById(R.id.tv_greeting);
        if (tvGreeting != null) {
            String userName = getString(R.string.home_default_user);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                    userName = user.getDisplayName();
                } else if (user.getEmail() != null) {
                    userName = user.getEmail().split("@")[0];
                }
            }
            tvGreeting.setText(getString(getGreetingResId(), userName));
        }

        // "Show all" assignments click listener
        View btnShowAll = view.findViewById(R.id.btn_show_all_deadlines);
        if (btnShowAll != null) {
            btnShowAll.setOnClickListener(v -> showAllAssignmentsDialog());
        }

        loadDeadlines(view);
        loadMood(view);

        return view;
    }

    private void initializeLoadingState(View view) {
        TextView tvGreeting = view.findViewById(R.id.tv_greeting);
        TextView tvMoodStatus = view.findViewById(R.id.tv_mood_status);
        TextView tvMoodTrend = view.findViewById(R.id.tv_mood_trend);
        TextView tvMoodVsYesterday = view.findViewById(R.id.tv_mood_vs_yesterday);
        TextView tvPendingCount = view.findViewById(R.id.tv_pending_count);
        TextView tvAssignmentName1 = view.findViewById(R.id.tv_assignment_name_1);
        TextView tvAssignmentDue1 = view.findViewById(R.id.tv_assignment_due_1);
        TextView tvAssignmentName2 = view.findViewById(R.id.tv_assignment_name_2);
        TextView tvAssignmentDue2 = view.findViewById(R.id.tv_assignment_due_2);
        TextView tvDaysLeft1 = view.findViewById(R.id.tv_deadline_days_left_1);
        TextView tvDaysLeft2 = view.findViewById(R.id.tv_deadline_days_left_2);
        View assignment1 = view.findViewById(R.id.layout_assignment_1);
        View assignment2 = view.findViewById(R.id.layout_assignment_2);
        View divider1 = view.findViewById(R.id.view_assignment_divider_1);
        View dividerFooter = view.findViewById(R.id.view_assignment_divider_footer);

        if (tvGreeting != null) {
            tvGreeting.setText(getString(getGreetingResId(), getString(R.string.home_default_user)));
        }
        if (tvMoodStatus != null) tvMoodStatus.setText(R.string.home_loading);
        if (tvMoodTrend != null) tvMoodTrend.setVisibility(View.GONE);
        if (tvMoodVsYesterday != null) tvMoodVsYesterday.setVisibility(View.GONE);
        if (tvPendingCount != null) tvPendingCount.setText(R.string.home_loading);
        if (tvAssignmentName1 != null) tvAssignmentName1.setText(R.string.home_loading);
        if (tvAssignmentDue1 != null) tvAssignmentDue1.setText(R.string.home_placeholder_value);
        if (tvAssignmentName2 != null) tvAssignmentName2.setText(R.string.home_loading);
        if (tvAssignmentDue2 != null) tvAssignmentDue2.setText(R.string.home_placeholder_value);
        if (tvDaysLeft1 != null) tvDaysLeft1.setText(R.string.home_loading);
        if (tvDaysLeft2 != null) tvDaysLeft2.setText(R.string.home_loading);
        if (assignment1 != null) assignment1.setVisibility(View.GONE);
        if (assignment2 != null) assignment2.setVisibility(View.GONE);
        if (divider1 != null) divider1.setVisibility(View.GONE);
        if (dividerFooter != null) dividerFooter.setVisibility(View.GONE);

        setupGlanceCard(view.findViewById(R.id.card_sleep), getString(R.string.home_metric_sleep),
                getString(R.string.home_placeholder_value), getString(R.string.home_metric_hours),
                getString(R.string.home_loading), R.color.deadline_pending_bg, R.color.text_grey_light);
        setupGlanceCard(view.findViewById(R.id.card_focus), getString(R.string.home_metric_academic),
                getString(R.string.home_placeholder_value), getString(R.string.home_metric_due),
                getString(R.string.home_loading), R.color.deadline_pending_bg, R.color.text_grey_light);
    }

    private void loadDeadlines(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Firebase.getInstance().getUpcomingAssignments(user.getUid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    upcomingDeadlines.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                        Deadline d = doc.toObject(Deadline.class);
                        d.setId(doc.getId());
                        upcomingDeadlines.add(d);
                    }
                    updateAssignmentUI(view);
                    updateAcademicGlance(view);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load deadlines", e));
    }

    private void loadMood(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Get today's mood
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        Firebase.getInstance().getUserMoodLogsInRange(user.getUid(), today.getTimeInMillis(), tomorrow.getTimeInMillis())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Get the most recent mood for today
                        Mood todayMood = querySnapshot.getDocuments().get(0).toObject(Mood.class);
                        if (todayMood != null) {
                            updateMoodUI(view, todayMood.getMood(), todayMood.getFeel(), todayMood.getSleepHours());
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load mood", e));
    }

    private void updateSleepGlance(View view, double sleepHours) {
        String sleepText = sleepHours > 0 ? String.format(Locale.getDefault(), getString(R.string.home_sleep_format), sleepHours) : "--";
        String status = sleepHours > 0 ? (sleepHours >= 7 ? getString(R.string.home_status_good) : sleepHours >= 5 ? getString(R.string.home_status_fair) : getString(R.string.home_status_poor)) : getString(R.string.home_status_no_data);
        int badgeColor = sleepHours >= 7 ? R.color.glance_focus_badge : sleepHours >= 5 ? R.color.glance_stress_bg : R.color.deadline_red_bg;
        int dotColor = sleepHours >= 7 ? R.color.trend_green : sleepHours >= 5 ? R.color.dot_yellow : R.color.trend_red;
        setupGlanceCard(view.findViewById(R.id.card_sleep), getString(R.string.home_metric_sleep), sleepText, getString(R.string.home_metric_hours), status, badgeColor, dotColor);
    }

    private void updateAcademicGlance(View view) {
        int count = upcomingDeadlines.size();
        String status = count > 5 ? getString(R.string.home_status_busy) : count > 2 ? getString(R.string.home_status_fair) : getString(R.string.home_status_good);
        int badgeColor = count > 5 ? R.color.deadline_red_bg : count > 2 ? R.color.glance_stress_bg : R.color.glance_focus_badge;
        int dotColor = count > 5 ? R.color.trend_red : count > 2 ? R.color.dot_yellow : R.color.trend_green;
        setupGlanceCard(view.findViewById(R.id.card_focus), getString(R.string.home_metric_academic), String.valueOf(count), getString(R.string.home_metric_due), status, badgeColor, dotColor);
    }

    private void updateAssignmentUI(View view) {
        TextView tvPending = view.findViewById(R.id.tv_pending_count);
        if (tvPending != null) {
            tvPending.setText(getResources().getQuantityString(R.plurals.home_pending_count, upcomingDeadlines.size(), upcomingDeadlines.size()));
        }

        // Assignment 1
        View layout1 = view.findViewById(R.id.layout_assignment_1);
        View divider1 = view.findViewById(R.id.view_assignment_divider_1);
        if (upcomingDeadlines.size() > 0) {
            bindAssignment(view, 1, upcomingDeadlines.get(0));
            layout1.setVisibility(View.VISIBLE);
            if (divider1 != null) divider1.setVisibility(View.VISIBLE);
        } else {
            layout1.setVisibility(View.GONE);
            if (divider1 != null) divider1.setVisibility(View.GONE);
        }

        // Assignment 2
        View layout2 = view.findViewById(R.id.layout_assignment_2);
        View divider2 = view.findViewById(R.id.view_assignment_divider_footer);
        if (upcomingDeadlines.size() > 1) {
            bindAssignment(view, 2, upcomingDeadlines.get(1));
            layout2.setVisibility(View.VISIBLE);
            if (divider2 != null) divider2.setVisibility(View.VISIBLE);
        } else {
            layout2.setVisibility(View.GONE);
            if (divider2 != null) divider2.setVisibility(View.GONE);
        }
    }

    private void bindAssignment(View view, int index, Deadline deadline) {
        int nameId = index == 1 ? R.id.tv_assignment_name_1 : R.id.tv_assignment_name_2;
        int dueId = index == 1 ? R.id.tv_assignment_due_1 : R.id.tv_assignment_due_2;
        int daysLeftId = index == 1 ? R.id.tv_deadline_days_left_1 : R.id.tv_deadline_days_left_2;
        int statusCardId = index == 1 ? R.id.card_deadline_status_1 : R.id.card_deadline_status_2;
        int dotId = index == 1 ? R.id.view_status_dot_1 : R.id.view_status_dot_2;

        TextView tvName = view.findViewById(nameId);
        TextView tvDue = view.findViewById(dueId);
        TextView tvDaysLeft = view.findViewById(daysLeftId);
        MaterialCardView statusCard = view.findViewById(statusCardId);
        View dot = view.findViewById(dotId);

        if (tvName != null) tvName.setText(deadline.getTitle() != null && !deadline.getTitle().isEmpty()
                ? deadline.getTitle()
                : getString(R.string.home_assignment_name_default));

        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.home_date_format_short), Locale.getDefault());
        if (tvDue != null) tvDue.setText(getString(R.string.home_due_prefix, deadline.getDueDate() != null ? sdf.format(deadline.getDueDate()) : getString(R.string.home_assignment_due_default)));

        long daysLeft = calculateDaysLeft(deadline.getDueDate());
        if (daysLeft < 0) {
            tvDaysLeft.setText(R.string.home_overdue);
            statusCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.deadline_red_bg));
            tvDaysLeft.setTextColor(ContextCompat.getColor(requireContext(), R.color.deadline_red_text));
            dot.setBackgroundResource(R.drawable.circle_red);
        } else if (daysLeft == 0) {
            tvDaysLeft.setText(R.string.home_today);
            statusCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.deadline_orange_bg));
            tvDaysLeft.setTextColor(ContextCompat.getColor(requireContext(), R.color.deadline_orange_text));
            dot.setBackgroundResource(R.drawable.circle_orange);
        } else {
            tvDaysLeft.setText(getResources().getQuantityString(R.plurals.home_days_left, (int) daysLeft, (int) daysLeft));
            if (daysLeft <= 2) {
                statusCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.deadline_red_bg));
                tvDaysLeft.setTextColor(ContextCompat.getColor(requireContext(), R.color.deadline_red_text));
                dot.setBackgroundResource(R.drawable.circle_red);
            } else if (daysLeft <= 5) {
                statusCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.deadline_orange_bg));
                tvDaysLeft.setTextColor(ContextCompat.getColor(requireContext(), R.color.deadline_orange_text));
                dot.setBackgroundResource(R.drawable.circle_orange);
            } else {
                statusCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.deadline_priority_low_bg));
                tvDaysLeft.setTextColor(ContextCompat.getColor(requireContext(), R.color.deadline_priority_low_text));
                dot.setBackgroundResource(R.drawable.dot_green);
            }
        }
    }

    private long calculateDaysLeft(Date dueDate) {
        if (dueDate == null) return 999;
        Date now = new Date();
        long diff = dueDate.getTime() - now.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    private void showAllAssignmentsDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.dialog_all_assignments, null);

        RecyclerView rv = bottomSheetView.findViewById(R.id.rv_assignments);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(requireContext()));
            deadlineAdapter = new DeadlineAdapter(upcomingDeadlines, new DeadlineAdapter.OnDeadlineClickListener() {
                @Override
                public void onDeadlineClick(Deadline deadline) {
                    // Optional: open detail view
                }

                @Override
                public void onDeadlineEdit(Deadline deadline) {
                    bottomSheetDialog.dismiss();
                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, AddDeadlineFragment.newInstanceForEdit(deadline.getId()))
                                .addToBackStack(null)
                                .commit();
                    }
                }

                @Override
                public void onDeadlineDelete(Deadline deadline) {
                    deleteDeadline(deadline.getId(), () -> {
                        loadDeadlines(getView()); // Refresh after deletion
                        if (deadlineAdapter != null) {
                            deadlineAdapter.updateList(upcomingDeadlines);
                        }
                    });
                }
            });
            rv.setAdapter(deadlineAdapter);
        }

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.setOnShowListener(dialog -> {
            View sheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (sheet != null) {
                sheet.setBackgroundResource(R.drawable.bg_bottom_sheet_dashboard);
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(sheet);
                behavior.setSkipCollapsed(true);
            }
        });
        bottomSheetDialog.show();
    }

    private void deleteDeadline(String deadlineId, Runnable onSuccess) {
        Firebase.getInstance().deleteAssignment(deadlineId)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), R.string.home_deadline_deleted, Toast.LENGTH_SHORT).show();
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete deadline", e);
                    Toast.makeText(requireContext(), getString(R.string.home_failed_delete_deadline, e.getMessage()), Toast.LENGTH_LONG).show();
                });
    }

    private void updateMoodUI(View view, String mood, String feelings, double sleepHours) {
        TextView tvMood = view.findViewById(R.id.tv_mood_status);
        TextView tvMoodVsYesterday = view.findViewById(R.id.tv_mood_vs_yesterday);
        TextView tvMoodTrend = view.findViewById(R.id.tv_mood_trend);
        ImageView ivMoodIcon = view.findViewById(R.id.iv_mood_icon);

        if (tvMood != null) tvMood.setText(mood);
        
        // Update sleep hours display
        updateSleepGlance(view, sleepHours);
        
        // Set mood icon and color based on mood
        int iconRes = R.drawable.mood_amazing;
        int colorRes = R.color.status_green;
        
        switch (mood != null ? mood.toLowerCase() : "") {
            case "very bad":
                iconRes = R.drawable.mood_very_bad;
                colorRes = R.color.deadline_red_text;
                break;
            case "bad":
                iconRes = R.drawable.mood_bad;
                colorRes = R.color.deadline_orange_text;
                break;
            case "okay":
                iconRes = R.drawable.mood_okay;
                colorRes = R.color.deadline_priority_low_text;
                break;
            case "good":
                iconRes = R.drawable.mood_good;
                colorRes = R.color.primary_blue;
                break;
            case "amazing":
                iconRes = R.drawable.mood_amazing;
                colorRes = R.color.calendar_button_text;
                break;
        }
        
        if (tvMoodTrend != null) tvMoodTrend.setVisibility(View.GONE);
        if (tvMoodVsYesterday != null) tvMoodVsYesterday.setVisibility(View.GONE);
        if (ivMoodIcon != null) ivMoodIcon.setImageResource(iconRes);
    }
    private int getGreetingResId() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) {
            return R.string.home_greeting_morning_format;
        } else if (hour >= 12 && hour < 17) {
            return R.string.home_greeting_afternoon_format;
        } else if (hour >= 17 && hour < 21) {
            return R.string.home_greeting_evening_format;
        } else {
            return R.string.home_greeting_night_format;
        }
    }

    private void setupGlanceCard(View cardView, String title, String value, String unit, String status, int badgeColorRes, int dotColorRes) {
        if (cardView != null) {
            MaterialCardView card = (MaterialCardView) cardView;
            int cardBackgroundRes = card.getId() == R.id.card_sleep
                    ? R.color.glance_sleep_bg
                    : R.color.glance_focus_bg;
            card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), cardBackgroundRes));

            TextView titleTv = card.findViewById(R.id.glance_title);
            TextView valueNumTv = card.findViewById(R.id.glance_value_num);
            TextView valueUnitTv = card.findViewById(R.id.glance_value_unit);
            TextView statusTv = card.findViewById(R.id.glance_status);
            View dot = card.findViewById(R.id.status_dot);
            MaterialCardView statusBadge = card.findViewById(R.id.status_badge);

            if (titleTv != null) titleTv.setText(title);
            if (valueNumTv != null) valueNumTv.setText(value);
            if (valueUnitTv != null) valueUnitTv.setText(unit);
            if (statusTv != null) statusTv.setText(status);

            if (statusBadge != null) statusBadge.setCardBackgroundColor(ContextCompat.getColor(requireContext(), badgeColorRes));

            if (dot != null) {
                android.graphics.drawable.GradientDrawable background = (android.graphics.drawable.GradientDrawable) dot.getBackground();
                if (background != null) {
                    background.setColor(ContextCompat.getColor(requireContext(), dotColorRes));
                }
            }
        }
    }
}
