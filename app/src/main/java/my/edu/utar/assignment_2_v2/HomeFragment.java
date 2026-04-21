package my.edu.utar.assignment_2_v2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.card.MaterialCardView;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Styling the individual Glance cards
        setupGlanceCard(view.findViewById(R.id.card_sleep), "Sleep", "6.5 h", "Fair", R.color.glance_sleep_bg, R.color.status_yellow);
        setupGlanceCard(view.findViewById(R.id.card_focus), "Focus", "5 h", "Good", R.color.glance_focus_bg, R.color.status_green);
        setupGlanceCard(view.findViewById(R.id.card_stress), "Stress", "3 recorded", "Low", R.color.glance_stress_bg, R.color.status_green);

        return view;
    }

    private void setupGlanceCard(View cardView, String title, String value, String status, int bgColorRes, int dotColorRes) {
        if (cardView != null) {
            MaterialCardView card = (MaterialCardView) cardView;
            card.setCardBackgroundColor(getResources().getColor(bgColorRes, null));
            
            TextView titleTv = card.findViewById(R.id.glance_title);
            TextView valueTv = card.findViewById(R.id.glance_value);
            TextView statusTv = card.findViewById(R.id.glance_status);
            View dot = card.findViewById(R.id.status_dot);

            titleTv.setText(title);
            valueTv.setText(value);
            
            if (status.isEmpty()) {
                statusTv.setVisibility(View.GONE);
            } else {
                statusTv.setText(status);
            }
            
            android.graphics.drawable.GradientDrawable background = (android.graphics.drawable.GradientDrawable) dot.getBackground();
            background.setColor(getResources().getColor(dotColorRes, null));
        }
    }
}