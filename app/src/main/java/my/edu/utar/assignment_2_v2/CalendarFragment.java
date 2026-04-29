package my.edu.utar.assignment_2_v2;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import my.edu.utar.assignment_2_v2.Utils.Firebase;
import my.edu.utar.assignment_2_v2.model.Deadline;
import my.edu.utar.assignment_2_v2.model.Mood;

public class CalendarFragment extends Fragment {
    private static final String TAG = "CalendarFragment";
    private TextView tvMonthYear, tvSelectedDate;
    private GridLayout calendarGrid;
    private LinearLayout linearBox;
    private LinearLayout moodBox, academicBox;
    private LinearLayout academicEntriesContainer;
    private MaterialCardView cardLog1;
    private TextView tvLog1Mood, tvLog1Tags, tvLog1Note;
    private ImageView ivMood1;
    private View noLogsView;
    private Calendar calendar;
    private Calendar selectedDate;
    private Mood selectedMoodEntry;
    private Deadline selectedAcademicEntry;
    private Map<Integer, List<Deadline>> deadlineEntries = new HashMap<>();
    private Map<Integer, List<Mood>> moodDays = new HashMap<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        tvMonthYear = view.findViewById(R.id.tv_month_year);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        calendarGrid = view.findViewById(R.id.calendar_grid);
        linearBox = view.findViewById(R.id.linearBox);
        moodBox = view.findViewById(R.id.mood_box);
        academicBox = view.findViewById(R.id.academic_box);
        academicEntriesContainer = view.findViewById(R.id.layout_academic_entries);
        cardLog1 = view.findViewById(R.id.card_log_1);
        tvLog1Mood = view.findViewById(R.id.tv_log_1_mood);
        tvLog1Tags = view.findViewById(R.id.tv_log_1_tags);
        tvLog1Note = view.findViewById(R.id.tv_log_1_note);
        ivMood1 = view.findViewById(R.id.iv_mood_1);

        View btnPrevMonth = view.findViewById(R.id.btn_prev_month);
        View btnNextMonth = view.findViewById(R.id.btn_next_month);
        View btnAddEntry = view.findViewById(R.id.btn_add_entry);

        calendar = Calendar.getInstance();
        selectedDate = (Calendar) calendar.clone();
        resetToStartOfDay(selectedDate);
        noLogsView = createNoLogsView();

        loadMonthData();
        updateCalendar();
        updateSelectedDateText();
        updateSelectedLogs();

        if (btnPrevMonth != null) {
            btnPrevMonth.setOnClickListener(v -> {
                calendar.add(Calendar.MONTH, -1);
                selectedDate = (Calendar) calendar.clone();
                selectedDate.set(Calendar.DAY_OF_MONTH, 1);
                resetToStartOfDay(selectedDate);
                loadMonthData();
                updateCalendar();
                updateSelectedDateText();
                updateSelectedLogs();
            });
        }

        if (btnNextMonth != null) {
            btnNextMonth.setOnClickListener(v -> {
                calendar.add(Calendar.MONTH, 1);
                selectedDate = (Calendar) calendar.clone();
                selectedDate.set(Calendar.DAY_OF_MONTH, 1);
                resetToStartOfDay(selectedDate);
                loadMonthData();
                updateCalendar();
                updateSelectedDateText();
                updateSelectedLogs();
            });
        }

        if (btnAddEntry != null) {
            btnAddEntry.setOnClickListener(v -> openAddDeadline());
        }

        if (cardLog1 != null) {
            cardLog1.setOnClickListener(v -> openSelectedMoodForEdit());
        }
        return view;
    }

    private void openAddDeadline() {
        SimpleDateFormat dateSdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String formattedDate = dateSdf.format(selectedDate.getTime());
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, AddDeadlineFragment.newInstance(formattedDate))
                .addToBackStack(null)
                .commit();
    }

    private void openLogMood() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, LogMoodFragment.newInstance(selectedDate.getTimeInMillis()))
                .addToBackStack(null)
                .commit();
    }

    private void openSelectedMoodForEdit() {
        if (selectedMoodEntry == null || selectedMoodEntry.getId() == null) return;
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, LogMoodFragment.newInstanceForEdit(selectedMoodEntry.getId()))
                .addToBackStack(null)
                .commit();
    }

    private void openSelectedAcademicForEdit() {
        if (selectedAcademicEntry == null || selectedAcademicEntry.getId() == null) return;
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, AddDeadlineFragment.newInstanceForEdit(selectedAcademicEntry.getId()))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMonthData();
        updateCalendar();
        updateSelectedDateText();
        updateSelectedLogs();
    }

    private void loadMonthData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        deadlineEntries.clear();
        moodDays.clear();

        Calendar start = (Calendar) calendar.clone();
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);

        Calendar end = (Calendar) calendar.clone();
        end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);

        // Load deadlines
        Firebase.getInstance().getUserAssignments(user.getUid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    deadlineEntries.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Deadline deadline = doc.toObject(Deadline.class);
                        deadline.setId(doc.getId());
                        if (deadline.getDueDate() != null) {
                            Calendar d = Calendar.getInstance();
                            d.setTime(deadline.getDueDate());
                            if (d.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                                    d.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) {
                                int day = d.get(Calendar.DAY_OF_MONTH);
                                if (!deadlineEntries.containsKey(day)) {
                                    deadlineEntries.put(day, new ArrayList<>());
                                }
                                deadlineEntries.get(day).add(deadline);
                            }
                        }
                    }
                    updateCalendar();
                    updateSelectedLogs();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load deadlines", e));

        // Load moods
        Firebase.getInstance().getUserMoodLogsInRange(user.getUid(), start.getTimeInMillis(), end.getTimeInMillis())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    moodDays.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Mood mood = doc.toObject(Mood.class);
                        mood.setId(doc.getId());
                        if (mood.getTimestamp() != null) {
                            Calendar d = Calendar.getInstance();
                            d.setTime(mood.getTimestamp());
                            if (d.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                                    d.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) {
                                int day = d.get(Calendar.DAY_OF_MONTH);
                                if (!moodDays.containsKey(day)) {
                                    moodDays.put(day, new ArrayList<>());
                                }
                                moodDays.get(day).add(mood);
                            }
                        }
                    }
                    updateCalendar();
                    updateSelectedLogs();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load moods", e));
    }

    private void updateCalendar() {
        if (calendarGrid == null) return;
        calendarGrid.removeAllViews();

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonthYear.setText(sdf.format(calendar.getTime()));

        Calendar tempCal = (Calendar) calendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < firstDayOfWeek; i++) {
            View emptyView = new View(getContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = (int) (60 * getResources().getDisplayMetrics().density);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            emptyView.setLayoutParams(params);
            calendarGrid.addView(emptyView);
        }

        for (int day = 1; day <= daysInMonth; day++) {
            final int dayOfMonth = day;
            LinearLayout dayLayout = (LinearLayout) LayoutInflater.from(getContext())
                    .inflate(R.layout.item_calendar_day, calendarGrid, false);

            TextView tvDay = dayLayout.findViewById(R.id.tv_day);
            View moodIndicator = dayLayout.findViewById(R.id.view_indicator_mood);
            View academicIndicator = dayLayout.findViewById(R.id.view_indicator_academic);

            tvDay.setText(String.valueOf(day));

            if (selectedDate != null &&
                    selectedDate.get(Calendar.DAY_OF_MONTH) == day &&
                    selectedDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                    selectedDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
                dayLayout.setBackgroundResource(R.drawable.calendar_selection);
            } else {
                dayLayout.setBackgroundColor(Color.TRANSPARENT);
            }

            // Show mood indicators on the left
            List<Mood> moods = moodDays.get(day);
            if (moods != null && !moods.isEmpty()) {
                moodIndicator.setVisibility(View.VISIBLE);
                switch (moods.get(0).getMood() != null ? moods.get(0).getMood().toLowerCase() : "") {
                    case "very bad":
                        moodIndicator.setBackgroundResource(R.drawable.circle_purple);
                        break;
                    case "bad":
                        moodIndicator.setBackgroundResource(R.drawable.circle_orange);
                        break;
                    case "okay":
                        moodIndicator.setBackgroundResource(R.drawable.dot_yellow);
                        break;
                    case "good":
                        moodIndicator.setBackgroundResource(R.drawable.dot_green);
                        break;
                    case "amazing":
                        moodIndicator.setBackgroundResource(R.drawable.dot_teal);
                        break;
                    default:
                        moodIndicator.setBackgroundResource(R.drawable.dot_teal);
                        break;
                }
            } else {
                moodIndicator.setVisibility(View.INVISIBLE);
            }

            // Show academic indicators on the right (hollow dots)
            List<Deadline> deadlines = deadlineEntries.get(day);
            if (deadlines != null && !deadlines.isEmpty()) {
                academicIndicator.setVisibility(View.VISIBLE);
                String type = deadlines.get(0).getType() != null ? deadlines.get(0).getType().toLowerCase() : "assignment";
                switch (type) {
                    case "quiz":
                        academicIndicator.setBackgroundResource(R.drawable.hollow_dot_orange);
                        break;
                    case "test":
                        academicIndicator.setBackgroundResource(R.drawable.hollow_dot_blue);
                        break;
                    case "midterm":
                        academicIndicator.setBackgroundResource(R.drawable.hollow_dot_teal);
                        break;
                    default:
                        academicIndicator.setBackgroundResource(R.drawable.hollow_dot_green);
                        break;
                }
            } else {
                academicIndicator.setVisibility(View.INVISIBLE);
            }

            dayLayout.setOnClickListener(v -> {
                selectedDate = (Calendar) calendar.clone();
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                resetToStartOfDay(selectedDate);
                updateCalendar();
                updateSelectedDateText();
                updateSelectedLogs();
                showDateActionDialog();
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = (int) (60 * getResources().getDisplayMetrics().density);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            dayLayout.setLayoutParams(params);

            calendarGrid.addView(dayLayout);
        }
    }

    private void updateSelectedLogs() {
        if (linearBox == null || selectedDate == null) return;
        List<Mood> moods = moodDays.get(selectedDate.get(Calendar.DAY_OF_MONTH));
        List<Deadline> deadlines = deadlineEntries.get(selectedDate.get(Calendar.DAY_OF_MONTH));

        boolean hasMoods = moods != null && !moods.isEmpty();
        boolean hasDeadlines = deadlines != null && !deadlines.isEmpty();
        selectedMoodEntry = null;
        selectedAcademicEntry = null;
        if (!hasMoods && !hasDeadlines) {
            showNoLogsState();
            return;
        }

        if (noLogsView.getParent() == linearBox) {
            linearBox.removeView(noLogsView);
        }
        if (moodBox != null) moodBox.setVisibility(hasMoods ? View.VISIBLE : View.GONE);
        if (academicBox != null) academicBox.setVisibility(hasDeadlines ? View.VISIBLE : View.GONE);
        if (hasMoods) {
            moods.sort((a, b) -> compareDatesDesc(a.getTimestamp(), b.getTimestamp()));
            selectedMoodEntry = moods.get(0);
            bindMoodEntry(cardLog1, ivMood1, tvLog1Mood, tvLog1Tags, tvLog1Note, selectedMoodEntry);
        }

        if (hasDeadlines) {
            deadlines.sort((a, b) -> compareDatesAsc(a.getDueDate(), b.getDueDate()));
            selectedAcademicEntry = deadlines.get(0);
            renderAcademicEntries(deadlines);
        } else if (academicEntriesContainer != null) {
            academicEntriesContainer.removeAllViews();
        }
    }

    private void showNoLogsState() {
        if (linearBox == null) return;
        selectedMoodEntry = null;
        selectedAcademicEntry = null;
        if (moodBox != null) moodBox.setVisibility(View.GONE);
        if (academicBox != null) academicBox.setVisibility(View.GONE);
        if (noLogsView.getParent() == null) {
            linearBox.addView(noLogsView, 1);
        }
    }

    private View createNoLogsView() {
        TextView emptyView = new TextView(requireContext());
        emptyView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        emptyView.setPadding(0, (int) (12 * getResources().getDisplayMetrics().density), 0, 0);
        emptyView.setText(R.string.calendar_no_logs);
        emptyView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_grey_dark));
        emptyView.setTextSize(14);
        emptyView.setVisibility(View.GONE);
        return emptyView;
    }

    private void bindMoodEntry(MaterialCardView card, ImageView iconView, TextView titleView, TextView tagsView, TextView noteView, Mood mood) {
        if (card != null) card.setVisibility(View.VISIBLE);
        titleView.setText(mood.getMood() != null ? mood.getMood() : getString(R.string.mood_no_data));
        tagsView.setText((mood.getFeel() != null && !mood.getFeel().isEmpty())
                ? mood.getFeel()
                : getString(R.string.mood_no_data));
        noteView.setText((mood.getNote() != null && !mood.getNote().isEmpty())
                ? mood.getNote()
                : getString(R.string.mood_no_data));
        iconView.setImageResource(getMoodIconRes(mood.getMood()));
    }

    private void renderAcademicEntries(List<Deadline> deadlines) {
        if (academicEntriesContainer == null) return;

        academicEntriesContainer.removeAllViews();

        for (Deadline deadline : deadlines) {
            View academicItem = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_calendar_academic_log, academicEntriesContainer, false);

            MaterialCardView card = (MaterialCardView) academicItem;
            ImageView iconView = academicItem.findViewById(R.id.iv_academic_icon);
            TextView titleView = academicItem.findViewById(R.id.tv_academic_title);
            TextView typeView = academicItem.findViewById(R.id.tv_academic_type);
            TextView subtitleView = academicItem.findViewById(R.id.tv_academic_subtitle);

            titleView.setText(deadline.getTitle() != null && !deadline.getTitle().isEmpty()
                    ? deadline.getTitle()
                    : getString(R.string.calendar_no_academic_data));

            String academicType = deadline.getType() != null && !deadline.getType().isEmpty()
                    ? deadline.getType()
                    : getString(R.string.calendar_no_academic_data);
            typeView.setText(getString(R.string.calendar_academic_label, academicType));
            subtitleView.setText(getAcademicSubtitle(deadline));
            iconView.setImageResource(getAcademicIconRes(deadline.getType()));

            card.setOnClickListener(v -> {
                selectedAcademicEntry = deadline;
                openSelectedAcademicForEdit();
            });

            academicEntriesContainer.addView(academicItem);
        }
    }

    private int compareDatesDesc(Date first, Date second) {
        if (first == null && second == null) return 0;
        if (first == null) return 1;
        if (second == null) return -1;
        return second.compareTo(first);
    }

    private int compareDatesAsc(Date first, Date second) {
        if (first == null && second == null) return 0;
        if (first == null) return 1;
        if (second == null) return -1;
        return first.compareTo(second);
    }

    private int getMoodIconRes(String mood) {
        if (mood == null) return R.drawable.mood_good;
        switch (mood.toLowerCase(Locale.getDefault())) {
            case "very bad":
                return R.drawable.mood_very_bad;
            case "bad":
                return R.drawable.mood_bad;
            case "okay":
                return R.drawable.mood_okay;
            case "good":
                return R.drawable.mood_good;
            case "amazing":
                return R.drawable.mood_amazing;
            default:
                return R.drawable.mood_good;
        }
    }

    private void showDateActionDialog() {
        if (getContext() == null || selectedDate == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.calendar_action_title)
                .setMessage(getString(R.string.calendar_action_message, formatSelectedDate()))
                .setPositiveButton(R.string.calendar_action_log_mood, (dialog, which) -> openLogMood())
                .setNegativeButton(R.string.calendar_action_log_academic, (dialog, which) -> openAddDeadline())
                .setNeutralButton(android.R.string.cancel, null)
                .show();
    }

    private String formatSelectedDate() {
        if (selectedDate == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        return sdf.format(selectedDate.getTime());
    }

    private int getAcademicIconRes(String type) {
        if (type == null) return R.drawable.ic_type_assignment;
        switch (type.toLowerCase(Locale.getDefault())) {
            case "quiz":
                return R.drawable.ic_type_quiz;
            case "test":
                return R.drawable.ic_type_test;
            case "midterm":
                return R.drawable.ic_type_midterm;
            default:
                return R.drawable.ic_type_assignment;
        }
    }

    private String getAcademicSubtitle(Deadline deadline) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String dueTime = deadline.getDueDate() != null
                ? timeFormat.format(deadline.getDueDate())
                : getString(R.string.calendar_no_academic_data);
        return getString(R.string.calendar_academic_subtitle, dueTime, getPriorityLabel(deadline.getPriority()));
    }

    private String getPriorityLabel(int priority) {
        switch (priority) {
            case 3:
                return getString(R.string.calendar_priority_high);
            case 2:
                return getString(R.string.calendar_priority_medium);
            default:
                return getString(R.string.calendar_priority_low);
        }
    }

    private void resetToStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void updateSelectedDateText() {
        if (tvSelectedDate == null || selectedDate == null) return;
        tvSelectedDate.setText(formatSelectedDate());
    }
}
