package my.edu.utar.assignment_2_v2;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import my.edu.utar.assignment_2_v2.Utils.Firebase;
import my.edu.utar.assignment_2_v2.Utils.GoogleCalendarHelper;
import my.edu.utar.assignment_2_v2.model.Deadline;

public class AddDeadlineFragment extends Fragment {

    private static final String TAG = "AddDeadlineFragment";
    private static final String DATE_FORMAT = "MMMM d, yyyy";
    private static final String TIME_FORMAT = "h:mm a";
    private static final String FULL_DATETIME_FORMAT = "MMMM d, yyyy h:mm a";
    private static final String ARG_DEADLINE_ID = "deadline_id";
    private static final String ARG_SELECTED_DATE = "selected_date";

    private String selectedDate;
    private Date selectedDueDate;
    private Calendar calendar = Calendar.getInstance();
    private String editingDeadlineId;
    private boolean isEditMode = false;

    private MaterialCardView typeAssignment, typeQuiz, typeTest, typeMidterm;
    private MaterialCardView priorityLow, priorityMedium, priorityHigh;
    private MaterialCardView selectedTypeCard;
    private MaterialCardView selectedPriorityCard;

    private TextView tvSelectedDate, tvSelectedTime;
    private EditText etTitle;
    private MaterialSwitch switchReminder;
    private RadioGroup rgReminderOptions;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
    private SimpleDateFormat timeFormatter = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());

    public static AddDeadlineFragment newInstance(String date) {
        AddDeadlineFragment fragment = new AddDeadlineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddDeadlineFragment newInstanceForEdit(String deadlineId) {
        AddDeadlineFragment fragment = new AddDeadlineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DEADLINE_ID, deadlineId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedDate = getArguments().getString(ARG_SELECTED_DATE);
            editingDeadlineId = getArguments().getString(ARG_DEADLINE_ID);
            isEditMode = editingDeadlineId != null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_deadline, container, false);

        initViews(view);
        setupListeners(view);
        parseInitialDate();

        if (isEditMode) {
            loadDeadlineForEdit();
        } else {
            // Default selections
            if (typeAssignment != null) selectType(typeAssignment);
            if (priorityLow != null) selectPriority(priorityLow);
        }

        return view;
    }

    private void initViews(View view) {
        // Back button
        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> goBack());
        }

        // Title
        etTitle = view.findViewById(R.id.et_title);

        // Date & Time
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        tvSelectedTime = view.findViewById(R.id.tv_selected_time);

        // Type cards
        typeAssignment = view.findViewById(R.id.type_assignment);
        typeQuiz = view.findViewById(R.id.type_quiz);
        typeTest = view.findViewById(R.id.type_test);
        typeMidterm = view.findViewById(R.id.type_midterm);

        // Priority cards
        priorityLow = view.findViewById(R.id.priority_low);
        priorityMedium = view.findViewById(R.id.priority_medium);
        priorityHigh = view.findViewById(R.id.priority_high);

        // Reminder
        switchReminder = view.findViewById(R.id.switch_reminder);
        rgReminderOptions = view.findViewById(R.id.rg_reminder_options);
    }

    private void setupListeners(View view) {
        // Type selection
        if (typeAssignment != null) typeAssignment.setOnClickListener(v -> selectType(typeAssignment));
        if (typeQuiz != null) typeQuiz.setOnClickListener(v -> selectType(typeQuiz));
        if (typeTest != null) typeTest.setOnClickListener(v -> selectType(typeTest));
        if (typeMidterm != null) typeMidterm.setOnClickListener(v -> selectType(typeMidterm));

        // Priority selection
        if (priorityLow != null) priorityLow.setOnClickListener(v -> selectPriority(priorityLow));
        if (priorityMedium != null) priorityMedium.setOnClickListener(v -> selectPriority(priorityMedium));
        if (priorityHigh != null) priorityHigh.setOnClickListener(v -> selectPriority(priorityHigh));

        // Date picker
        View btnDate = view.findViewById(R.id.btn_date);
        if (btnDate != null) {
            btnDate.setOnClickListener(v -> showDatePicker());
        }

        // Time picker
        View btnTime = view.findViewById(R.id.btn_time);
        if (btnTime != null) {
            btnTime.setOnClickListener(v -> showTimePicker());
        }

        // Reminder switch toggle
        if (switchReminder != null && rgReminderOptions != null) {
            rgReminderOptions.setEnabled(switchReminder.isChecked());
            for (int i = 0; i < rgReminderOptions.getChildCount(); i++) {
                rgReminderOptions.getChildAt(i).setEnabled(switchReminder.isChecked());
            }
            switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
                rgReminderOptions.setEnabled(isChecked);
                for (int i = 0; i < rgReminderOptions.getChildCount(); i++) {
                    rgReminderOptions.getChildAt(i).setEnabled(isChecked);
                }
            });
        }

        // Save button
        View btnSave = view.findViewById(R.id.btn_save_deadline);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveDeadline());
        }
    }

    private void parseInitialDate() {
        try {
            if (selectedDate != null) {
                selectedDueDate = dateFormatter.parse(selectedDate);
                calendar.setTime(selectedDueDate);
            }
            // Default time 11:59 PM
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            selectedDueDate = calendar.getTime();

            if (tvSelectedDate != null) tvSelectedDate.setText(dateFormatter.format(selectedDueDate));
            if (tvSelectedTime != null) tvSelectedTime.setText(timeFormatter.format(selectedDueDate));
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date", e);
            selectedDueDate = new Date();
        }
    }

    private void loadDeadlineForEdit() {
        if (editingDeadlineId == null) return;

        Firebase.getInstance().getUserAssignments(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                        if (editingDeadlineId.equals(doc.getId())) {
                            Deadline deadline = doc.toObject(Deadline.class);
                            deadline.setId(doc.getId());
                            populateFields(deadline);
                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load deadline for edit", e));
    }

    private void populateFields(Deadline deadline) {
        // Title
        if (etTitle != null && deadline.getTitle() != null) {
            etTitle.setText(deadline.getTitle());
        }

        // Due date
        if (deadline.getDueDate() != null) {
            selectedDueDate = deadline.getDueDate();
            calendar.setTime(selectedDueDate);
            if (tvSelectedDate != null) tvSelectedDate.setText(dateFormatter.format(selectedDueDate));
            if (tvSelectedTime != null) tvSelectedTime.setText(timeFormatter.format(selectedDueDate));
        }

        // Type
        String type = deadline.getType();
        if ("Quiz".equalsIgnoreCase(type) && typeQuiz != null) {
            selectType(typeQuiz);
        } else if ("Test".equalsIgnoreCase(type) && typeTest != null) {
            selectType(typeTest);
        } else if ("Midterm".equalsIgnoreCase(type) && typeMidterm != null) {
            selectType(typeMidterm);
        } else if (typeAssignment != null) {
            selectType(typeAssignment);
        }

        // Priority
        int priority = deadline.getPriority();
        if (priority == 3 && priorityHigh != null) {
            selectPriority(priorityHigh);
        } else if (priority == 2 && priorityMedium != null) {
            selectPriority(priorityMedium);
        } else if (priorityLow != null) {
            selectPriority(priorityLow);
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    selectedDueDate = calendar.getTime();
                    tvSelectedDate.setText(dateFormatter.format(selectedDueDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    selectedDueDate = calendar.getTime();
                    tvSelectedTime.setText(timeFormatter.format(selectedDueDate));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false);
        timePicker.show();
    }

    private void saveDeadline() {
        String title = etTitle != null ? etTitle.getText().toString().trim() : "";
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = getSelectedType();
        int priority = getSelectedPriority();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Please sign in first", Toast.LENGTH_SHORT).show();
            return;
        }

        Deadline deadline = new Deadline(user.getUid(), title, title, selectedDueDate, priority);
        deadline.setType(type);

        if (isEditMode) {
            // Update existing deadline
            deadline.setId(editingDeadlineId);
            Firebase.getInstance().updateAssignment(editingDeadlineId,deadline)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Deadline updated!", Toast.LENGTH_SHORT).show();

                        // Add to Google Calendar if reminder is enabled
                        if (switchReminder != null && switchReminder.isChecked()) {
                            addToGoogleCalendar(deadline);
                        }

                        goBack();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update deadline", e);
                        Toast.makeText(requireContext(), "Failed to update: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            // Create new deadline
            Firebase.getInstance().saveAssignment(deadline)
                    .addOnSuccessListener(documentReference -> {
                        String docId = documentReference.getId();
                        deadline.setId(docId);
                        Toast.makeText(requireContext(), "Deadline saved!", Toast.LENGTH_SHORT).show();

                        // Add to Google Calendar if reminder is enabled
                        if (switchReminder != null && switchReminder.isChecked()) {
                            addToGoogleCalendar(deadline);
                        }

                        goBack();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to save deadline", e);
                        Toast.makeText(requireContext(), "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void addToGoogleCalendar(Deadline deadline) {
        try {
            int checkedId = rgReminderOptions != null ? rgReminderOptions.getCheckedRadioButtonId() : R.id.rb_1_day;
            int reminderMinutes = GoogleCalendarHelper.getReminderMinutesFromSelection(checkedId);

            Intent calendarIntent = GoogleCalendarHelper.createCalendarEventIntent(
                    requireContext(),
                    deadline.getTitle(),
                    deadline.getType(),
                    deadline.getDueDate(),
                    reminderMinutes
            );

            if (calendarIntent != null) {
                startActivity(calendarIntent);
            } else {
                Toast.makeText(requireContext(), "No calendar app found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding to calendar", e);
        }
    }

    private String getSelectedType() {
        if (selectedTypeCard == null) return "Assignment";
        if (selectedTypeCard == typeAssignment) return "Assignment";
        if (selectedTypeCard == typeQuiz) return "Quiz";
        if (selectedTypeCard == typeTest) return "Test";
        if (selectedTypeCard == typeMidterm) return "Midterm";
        return "Assignment";
    }

    private int getSelectedPriority() {
        if (selectedPriorityCard == null) return 1;
        if (selectedPriorityCard == priorityLow) return 1;
        if (selectedPriorityCard == priorityMedium) return 2;
        if (selectedPriorityCard == priorityHigh) return 3;
        return 1;
    }

    private void selectType(MaterialCardView selected) {
        selectedTypeCard = selected;
        resetTypeCard(typeAssignment);
        resetTypeCard(typeQuiz);
        resetTypeCard(typeTest);
        resetTypeCard(typeMidterm);

        selected.setCardBackgroundColor(requireContext().getColor(R.color.deadline_type_selected_bg));
        selected.setStrokeWidth(2);
        selected.setStrokeColor(requireContext().getColor(R.color.deadline_type_selected_stroke));

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
        selectedPriorityCard = selected;
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

    private void goBack() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        } else {
            requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        }
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