package my.edu.utar.assignment_2_v2;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private List<Deadline> upcomingDeadlines = new ArrayList<>();
    private DeadlineAdapter deadlineAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Dynamic Greeting with User Name
        TextView tvGreeting = view.findViewById(R.id.tv_greeting);
        if (tvGreeting != null) {
            String userName = "user";
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                    userName = user.getDisplayName();
                } else if (user.getEmail() != null) {
                    userName = user.getEmail().split("@")[0];
                }
            }
            tvGreeting.setText(getGreeting() + ", " + userName + "! ☀️");
        }

        // Setup Mood Overview
        updateMoodUI(view, "Good", "Meh", "vs yesterday", R.drawable.mood_amazing, R.color.status_green);

        // Glance cards
        setupGlanceCard(view.findViewById(R.id.card_sleep), "SLEEP", "6.5", "h", "Fair", R.color.glance_sleep_badge, R.color.glance_sleep_dot);
        setupGlanceCard(view.findViewById(R.id.card_focus), "ACADEMIC", "0", "due", "Good", R.color.glance_focus_badge, R.color.glance_focus_dot);
        setupGlanceCard(view.findViewById(R.id.card_stress), "MOOD", "Good", "", "Low", R.color.glance_stress_badge, R.color.glance_stress_dot);

        // "Show all" assignments click listener
        View btnShowAll = view.findViewById(R.id.btn_show_all_deadlines);
        if (btnShowAll != null) {
            btnShowAll.setOnClickListener(v -> showAllAssignmentsDialog());
        }

        loadDeadlines(view);

        return view;
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

    private void updateAcademicGlance(View view) {
        int count = upcomingDeadlines.size();
        String status = count > 5 ? "Busy" : count > 2 ? "Fair" : "Good";
        int badgeColor = count > 5 ? R.color.glance_stress_badge : count > 2 ? R.color.glance_focus_badge : R.color.glance_sleep_badge;
        int dotColor = count > 5 ? R.color.glance_stress_dot : count > 2 ? R.color.glance_focus_dot : R.color.glance_sleep_dot;
        setupGlanceCard(view.findViewById(R.id.card_focus), "ACADEMIC", String.valueOf(count), "due", status, badgeColor, dotColor);
    }

    private void updateAssignmentUI(View view) {
        TextView tvPending = view.findViewById(R.id.tv_pending_count);
        if (tvPending != null) {
            tvPending.setText(upcomingDeadlines.size() + " pending");
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

        if (tvName != null) tvName.setText(deadline.getTitle());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        if (tvDue != null) tvDue.setText("Due " + (deadline.getDueDate() != null ? sdf.format(deadline.getDueDate()) : "N/A"));

        long daysLeft = calculateDaysLeft(deadline.getDueDate());
        if (daysLeft < 0) {
            tvDaysLeft.setText("Overdue");
            statusCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.deadline_red_bg));
            tvDaysLeft.setTextColor(ContextCompat.getColor(requireContext(), R.color.deadline_red_text));
            dot.setBackgroundResource(R.drawable.circle_red);
        } else if (daysLeft == 0) {
            tvDaysLeft.setText("Today");
            statusCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.deadline_orange_bg));
            tvDaysLeft.setTextColor(ContextCompat.getColor(requireContext(), R.color.deadline_orange_text));
            dot.setBackgroundResource(R.drawable.circle_orange);
        } else {
            tvDaysLeft.setText(daysLeft + " day" + (daysLeft > 1 ? "s" : "") + " left");
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
            deadlineAdapter = new DeadlineAdapter(upcomingDeadlines, deadline -> {
                // Optional: open detail/edit
            });
            rv.setAdapter(deadlineAdapter);
        }

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void updateMoodUI(View view, String status, String trend, String comparison, int iconRes, int trendColorRes) {
        TextView tvFeelingLabel = view.findViewById(R.id.tv_feeling_label);
        TextView tvMoodStatus = view.findViewById(R.id.tv_mood_status);
        TextView tvMoodTrend = view.findViewById(R.id.tv_mood_trend);
        TextView tvMoodVsYesterday = view.findViewById(R.id.tv_mood_vs_yesterday);
        ImageView ivMoodIcon = view.findViewById(R.id.iv_mood_icon);

        if (tvFeelingLabel != null) tvFeelingLabel.setText("Feeling");
        if (tvMoodStatus != null) tvMoodStatus.setText(status + ",");
        if (tvMoodTrend != null) {
            if (trend == null || trend.isEmpty()) {
                tvMoodTrend.setVisibility(View.GONE);
            } else {
                tvMoodTrend.setVisibility(View.VISIBLE);
                tvMoodTrend.setText(trend);
                tvMoodTrend.setTextColor(ContextCompat.getColor(requireContext(), trendColorRes));
            }
        }
        if (tvMoodVsYesterday != null) tvMoodVsYesterday.setText(" " + comparison);
        if (ivMoodIcon != null) ivMoodIcon.setImageResource(iconRes);
    }

    private String getGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return "Good Morning";
        } else if (hour >= 12 && hour < 17) {
            return "Good Afternoon";
        } else if (hour >= 17 && hour < 21) {
            return "Good Evening";
        } else {
            return "Good Night";
        }
    }

    private void setupGlanceCard(View cardView, String title, String value, String unit, String status, int badgeColorRes, int dotColorRes) {
        if (cardView != null) {
            MaterialCardView card = (MaterialCardView) cardView;
            card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));

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
