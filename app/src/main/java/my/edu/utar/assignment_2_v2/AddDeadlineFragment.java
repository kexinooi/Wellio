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

        // Deadline Type Views
        typeAssignment = view.findViewById(R.id.type_assignment);
        typeQuiz = view.findViewById(R.id.type_quiz);
        typeTest = view.findViewById(R.id.type_test);
        typeMidterm = view.findViewById(R.id.type_midterm);

        if (typeAssignment != null) typeAssignment.setOnClickListener(v -> selectType(typeAssignment));
        if (typeQuiz != null) typeQuiz.setOnClickListener(v -> selectType(typeQuiz));
        if (typeTest != null) typeTest.setOnClickListener(v -> selectType(typeTest));
        if (typeMidterm != null) typeMidterm.setOnClickListener(v -> selectType(typeMidterm));

        // Priority Cards
        priorityLow = view.findViewById(R.id.priority_low);
        priorityMedium = view.findViewById(R.id.priority_medium);
        priorityHigh = view.findViewById(R.id.priority_high);

        if (priorityLow != null) priorityLow.setOnClickListener(v -> selectPriority(priorityLow));
        if (priorityMedium != null) priorityMedium.setOnClickListener(v -> selectPriority(priorityMedium));
        if (priorityHigh != null) priorityHigh.setOnClickListener(v -> selectPriority(priorityHigh));

        // Default selected
        if (typeAssignment != null) selectType(typeAssignment);
        if (priorityLow != null) selectPriority(priorityLow);

        return view;
    }

    private void selectType(MaterialCardView selected) {

        resetTypeCard(typeAssignment);
        resetTypeCard(typeQuiz);
        resetTypeCard(typeTest);
        resetTypeCard(typeMidterm);

        // Background + border
        selected.setCardBackgroundColor(requireContext().getColor(R.color.deadline_type_selected_bg));
        selected.setStrokeWidth(2);
        selected.setStrokeColor(requireContext().getColor(R.color.deadline_type_selected_stroke));

        // 👉 Brighten text + icon
        TextView label = selected.findViewById(getLabelId(selected.getId()));
        MaterialCardView iconBg = selected.findViewById(getIconBgId(selected.getId()));

        if (label != null) {
            label.setTextColor(requireContext().getColor(R.color.deadline_type_selected_tint));
        }

        if (iconBg != null) {
            iconBg.setCardBackgroundColor(requireContext().getColor(R.color.deadline_type_selected_icon_bg));
        }
    }
    private void resetTypeCard(MaterialCardView card) {
        if (card == null) return;

        card.setCardBackgroundColor(requireContext().getColor(R.color.deadline_type_unselected_bg));
        card.setStrokeWidth(0);

        // 👉 reset text + icon
        TextView label = card.findViewById(getLabelId(card.getId()));
        MaterialCardView iconBg = card.findViewById(getIconBgId(card.getId()));

        if (label != null) {
            label.setTextColor(requireContext().getColor(R.color.deadline_type_unselected_tint));
        }

        if (iconBg != null) {
            iconBg.setCardBackgroundColor(requireContext().getColor(R.color.deadline_type_unselected_icon_bg));
        }
    }

    private void selectPriority(MaterialCardView selected) {
        if (priorityLow != null) resetPriorityCard(priorityLow);
        if (priorityMedium != null) resetPriorityCard(priorityMedium);
        if (priorityHigh != null) resetPriorityCard(priorityHigh);

        if (selected == priorityLow) {
            selected.setCardBackgroundColor(requireContext().getColor(R.color.deadline_priority_low_bg));
            selected.setStrokeWidth(2);
            selected.setStrokeColor(requireContext().getColor(R.color.deadline_priority_low_text));
        } else if (selected == priorityMedium) {
            selected.setCardBackgroundColor(requireContext().getColor(R.color.deadline_orange_bg));
            selected.setStrokeWidth(2);
            selected.setStrokeColor(requireContext().getColor(R.color.deadline_orange_text));
        } else if (selected == priorityHigh) {
            selected.setCardBackgroundColor(requireContext().getColor(R.color.deadline_red_bg));
            selected.setStrokeWidth(2);
            selected.setStrokeColor(requireContext().getColor(R.color.deadline_red_text));
        }
    }

    private void resetPriorityCard(MaterialCardView card) {
        if (card == null) return;
        card.setStrokeWidth(1);
        card.setStrokeColor(requireContext().getColor(R.color.deadline_form_stroke));
        card.setCardBackgroundColor(requireContext().getColor(R.color.deadline_form_surface));
    }

    private int getLabelId(int cardId) {
        if (cardId == R.id.type_assignment) return R.id.tv_type_assignment_label;
        if (cardId == R.id.type_quiz) return R.id.tv_type_quiz_label;
        if (cardId == R.id.type_test) return R.id.tv_type_test_label;
        if (cardId == R.id.type_midterm) return R.id.tv_type_midterm_label;
        return -1;
    }

    private int getIconBgId(int cardId) {
        if (cardId == R.id.type_assignment) return R.id.iv_type_assign_bg;
        if (cardId == R.id.type_quiz) return R.id.iv_type_quiz_bg;
        if (cardId == R.id.type_test) return R.id.iv_type_test_bg;
        if (cardId == R.id.type_midterm) return R.id.iv_type_midterm_bg;
        return -1;
    }
}