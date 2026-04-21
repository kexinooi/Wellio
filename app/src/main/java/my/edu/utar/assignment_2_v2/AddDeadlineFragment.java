package my.edu.utar.assignment_2_v2;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

        ImageView btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
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

        // Highlight selected with peach background and blue border
        selected.setCardBackgroundColor(Color.parseColor("#F8F2ED"));
        selected.setStrokeWidth(2);
        selected.setStrokeColor(Color.parseColor("#5E72E4"));
        selected.setCardElevation(0f);
    }

    private void resetTypeCard(MaterialCardView card) {
        card.setCardBackgroundColor(Color.TRANSPARENT);
        card.setStrokeWidth(0);
        card.setCardElevation(0f);
    }

    private void selectPriority(MaterialCardView selected) {
        // Reset all priority cards to neutral state
        resetPriorityCard(priorityLow, "#E6F6EF");
        resetPriorityCard(priorityMedium, "#FFF2E9");
        resetPriorityCard(priorityHigh, "#FFF2F2");

        // Highlight selected
        selected.setStrokeWidth(4);
        if (selected == priorityLow) {
            selected.setStrokeColor(Color.parseColor("#24A15E"));
        } else if (selected == priorityMedium) {
            selected.setStrokeColor(Color.parseColor("#E67E22"));
        } else if (selected == priorityHigh) {
            selected.setStrokeColor(Color.parseColor("#FF5252"));
        }
    }

    private void resetPriorityCard(MaterialCardView card, String bgColor) {
        card.setStrokeWidth(1);
        card.setStrokeColor(Color.parseColor("#D1D1D6"));
        card.setCardBackgroundColor(Color.parseColor(bgColor));
    }
}