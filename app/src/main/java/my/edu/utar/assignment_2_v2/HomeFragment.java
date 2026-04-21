package my.edu.utar.assignment_2_v2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Calendar;
import my.edu.utar.assignment_2_v2.R;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Dynamic Greeting with User Name
        TextView tvGreeting = view.findViewById(R.id.tv_greeting);
        if (tvGreeting != null) {
            String userName = "user"; // Default name
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                    userName = user.getDisplayName();
                } else if (user.getEmail() != null) {
                    // Fallback to part of email if display name is missing
                    userName = user.getEmail().split("@")[0];
                }
            }
            tvGreeting.setText(getGreeting() + ", " + userName + "! ☀️");
        }

        // Setup Mood Overview (Example data: "Feeling Good, Meh vs yesterday")
        updateMoodUI(view, "Good", "Meh", "vs yesterday", R.drawable.mood_amazing, R.color.status_green);

        // Styling the individual Glance cards to match the design 100%
        // Sleep Card: Status "Fair"
        setupGlanceCard(view.findViewById(R.id.card_sleep), "SLEEP", "6.5", "h", "Fair", R.color.glance_sleep_badge, R.color.glance_sleep_dot);
        
        // Academic Card: Value "2 due", Status "Good"
        setupGlanceCard(view.findViewById(R.id.card_focus), "ACADEMIC", "2", "due", "Good", R.color.glance_focus_badge, R.color.glance_focus_dot);
        
        // Mood Card: Value "Good", Status "Low"
        setupGlanceCard(view.findViewById(R.id.card_stress), "MOOD", "Good", "", "Low", R.color.glance_stress_badge, R.color.glance_stress_dot);

        // "Show all" assignments click listener
        View btnShowAll = view.findViewById(R.id.btn_show_all_deadlines);
        if (btnShowAll != null) {
            btnShowAll.setOnClickListener(v -> showAllAssignmentsDialog());
        }

        return view;
    }

    private void showAllAssignmentsDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.dialog_all_assignments, null);
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
            // The outer card is white now as per design
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
            
            // Set badge background color
            if (statusBadge != null) statusBadge.setCardBackgroundColor(ContextCompat.getColor(requireContext(), badgeColorRes));
            
            // Set dot color
            if (dot != null) {
                android.graphics.drawable.GradientDrawable background = (android.graphics.drawable.GradientDrawable) dot.getBackground();
                if (background != null) {
                    background.setColor(ContextCompat.getColor(requireContext(), dotColorRes));
                }
            }
        }
    }
}
