package my.edu.utar.assignment_2_v2;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.card.MaterialCardView;

public class AddDeadlineFragment extends Fragment {

    private String selectedDate;
    private MaterialCardView typeAssignment, typeQuiz, typeTest, typeMidterm;
    private MaterialCardView priorityLow, priorityMedium, priorityHigh;

    public static AddDeadlineFragment newInstance(String date) {
        AddDeadlineFragment fragment = new AddDeadlineFragment();
        Bundle args = new Bundle();
        args.putString("selected_date", date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedDate = getArguments().getString("selected_date");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_deadline, container, false);

        View btnBack = view.findViewById(R.id.btn_back_container);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
                    // Fallback if not in backstack
                    requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
                }
            });
        }

        TextView tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        if (tvSelectedDate != null && selectedDate != null) {
            tvSelectedDate.setText(selectedDate);
        }

        // Initialize Deadline Type Cards
        typeAssignment = view.findViewById(R.id.type_assignment);
        typeQuiz = view.findViewById(R.id.type_quiz);
        typeTest = view.findViewById(R.id.type_test);
        typeMidterm = view.findViewById(R.id.type_midterm);

        typeAssignment.setOnClickListener(v -> selectType(typeAssignment));
        typeQuiz.setOnClickListener(v -> selectType(typeQuiz));
        typeTest.setOnClickListener(v -> selectType(typeTest));
        typeMidterm.setOnClickListener(v -> selectType(typeMidterm));

        // Initialize Priority Cards
        priorityLow = view.findViewById(R.id.priority_low);
        priorityMedium = view.findViewById(R.id.priority_medium);
        priorityHigh = view.findViewById(R.id.priority_high);

        priorityLow.setOnClickListener(v -> selectPriority(priorityLow));
        priorityMedium.setOnClickListener(v -> selectPriority(priorityMedium));
        priorityHigh.setOnClickListener(v -> selectPriority(priorityHigh));

        // Set initial selection
        selectType(typeAssignment);
        selectPriority(priorityLow);

        return view;
    }

    private void selectType(MaterialCardView selected) {
        // Reset all type cards
        resetTypeCard(typeAssignment);
        resetTypeCard(typeQuiz);
        resetTypeCard(typeTest);
        resetTypeCard(typeMidterm);

        // Highlight selected with light blue background and blue border
        selected.setCardBackgroundColor(Color.parseColor("#EFF2FD"));
        selected.setStrokeWidth(4);
        selected.setStrokeColor(Color.parseColor("#5C6BC0"));
        
        // Update icons/text of selected if needed
        updateTypeCardContent(selected, true);
    }

    private void resetTypeCard(MaterialCardView card) {
        card.setCardBackgroundColor(Color.WHITE);
        card.setStrokeWidth(0);
        updateTypeCardContent(card, false);
    }
    
    private void updateTypeCardContent(MaterialCardView card, boolean isSelected) {
        int color = isSelected ? Color.parseColor("#5C6BC0") : Color.parseColor("#9E9E9E");
        int bgIconColor = isSelected ? Color.parseColor("#E8EAF6") : Color.parseColor("#F5F5F5");
        
        // Find children
        RelativeLayout layout = (RelativeLayout) card.getChildAt(0);
        MaterialCardView iconBg = (MaterialCardView) layout.getChildAt(0);
        ImageView icon = (ImageView) iconBg.getChildAt(0);
        TextView label = (TextView) layout.getChildAt(1);
        
        icon.setColorFilter(color);
        iconBg.setCardBackgroundColor(bgIconColor);
        label.setTextColor(color);
    }

    private void selectPriority(MaterialCardView selected) {
        // Reset all priority cards to neutral state
        resetPriorityCard(priorityLow, "#E8F5E9", "#4CAF50");
        resetPriorityCard(priorityMedium, "#FFFFFF", "#9E9E9E");
        resetPriorityCard(priorityHigh, "#FFFFFF", "#9E9E9E");

        // Highlight selected
        if (selected == priorityLow) {
            selected.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
            selected.setStrokeWidth(2);
            selected.setStrokeColor(Color.parseColor("#4CAF50"));
        } else if (selected == priorityMedium) {
            selected.setCardBackgroundColor(Color.parseColor("#FFF8E1"));
            selected.setStrokeWidth(2);
            selected.setStrokeColor(Color.parseColor("#FFC107"));
        } else if (selected == priorityHigh) {
            selected.setCardBackgroundColor(Color.parseColor("#FDECEA"));
            selected.setStrokeWidth(2);
            selected.setStrokeColor(Color.parseColor("#D32F2F"));
        }
        
        // Update label color
        TextView label = (TextView) selected.getChildAt(0);
        if (selected == priorityLow) label.setTextColor(Color.parseColor("#4CAF50"));
        else if (selected == priorityMedium) label.setTextColor(Color.parseColor("#FFC107"));
        else if (selected == priorityHigh) label.setTextColor(Color.parseColor("#D32F2F"));
    }

    private void resetPriorityCard(MaterialCardView card, String bgColor, String textColor) {
        card.setStrokeWidth(0);
        card.setCardBackgroundColor(Color.WHITE);
        TextView label = (TextView) card.getChildAt(0);
        label.setTextColor(Color.parseColor("#9E9E9E"));
    }
}
