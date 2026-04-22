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

        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
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

        if (typeAssignment != null) typeAssignment.setOnClickListener(v -> selectType(typeAssignment));
        if (typeQuiz != null) typeQuiz.setOnClickListener(v -> selectType(typeQuiz));
        if (typeTest != null) typeTest.setOnClickListener(v -> selectType(typeTest));
        if (typeMidterm != null) typeMidterm.setOnClickListener(v -> selectType(typeMidterm));

        // Initialize Priority Cards
        priorityLow = view.findViewById(R.id.priority_low);
        priorityMedium = view.findViewById(R.id.priority_medium);
        priorityHigh = view.findViewById(R.id.priority_high);

        if (priorityLow != null) priorityLow.setOnClickListener(v -> selectPriority(priorityLow));
        if (priorityMedium != null) priorityMedium.setOnClickListener(v -> selectPriority(priorityMedium));
        if (priorityHigh != null) priorityHigh.setOnClickListener(v -> selectPriority(priorityHigh));

        // Set initial selection
        if (typeAssignment != null) selectType(typeAssignment);
        if (priorityLow != null) selectPriority(priorityLow);

        return view;
    }

    private void selectType(MaterialCardView selected) {
        if (typeAssignment != null) resetTypeCard(typeAssignment);
        if (typeQuiz != null) resetTypeCard(typeQuiz);
        if (typeTest != null) resetTypeCard(typeTest);
        if (typeMidterm != null) resetTypeCard(typeMidterm);

        selected.setCardBackgroundColor(Color.parseColor("#EFF2FD"));
        selected.setStrokeWidth(4);
        selected.setStrokeColor(Color.parseColor("#5C6BC0"));
    }

    private void resetTypeCard(MaterialCardView card) {
        card.setCardBackgroundColor(Color.WHITE);
        card.setStrokeWidth(0);
    }

    private void selectPriority(MaterialCardView selected) {
        if (priorityLow != null) resetPriorityCard(priorityLow);
        if (priorityMedium != null) resetPriorityCard(priorityMedium);
        if (priorityHigh != null) resetPriorityCard(priorityHigh);

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
    }

    private void resetPriorityCard(MaterialCardView card) {
        card.setStrokeWidth(0);
        card.setCardBackgroundColor(Color.WHITE);
    }
}
