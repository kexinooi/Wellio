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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import my.edu.utar.assignment_2_v2.Utils.Firebase;
import my.edu.utar.assignment_2_v2.model.Deadline;
import my.edu.utar.assignment_2_v2.model.Mood;

public class CalendarFragment extends Fragment {

    private static final String TAG = "CalendarFragment";
    private TextView tvMonthYear, tvSelectedDate;
    private GridLayout calendarGrid;
    private Calendar calendar;
    private Calendar selectedDate;
    private Map<Integer, String> deadlineDays = new HashMap<>();
    private Map<Integer, Mood> moodDays = new HashMap<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        tvMonthYear = view.findViewById(R.id.tv_month_year);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        calendarGrid = view.findViewById(R.id.calendar_grid);

        View btnPrevMonth = view.findViewById(R.id.btn_prev_month);
        View btnNextMonth = view.findViewById(R.id.btn_next_month);
        View btnAddEntry = view.findViewById(R.id.btn_add_entry);
        View btnLogMoodBottom = view.findViewById(R.id.btn_log_mood_bottom);

        calendar = Calendar.getInstance();
        selectedDate = (Calendar) calendar.clone();

        loadMonthData();
        updateCalendar();
        updateSelectedDateText();

        if (btnPrevMonth != null) {
            btnPrevMonth.setOnClickListener(v -> {
                calendar.add(Calendar.MONTH, -1);
                loadMonthData();
                updateCalendar();
            });
        }

        if (btnNextMonth != null) {
            btnNextMonth.setOnClickListener(v -> {
                calendar.add(Calendar.MONTH, 1);
                loadMonthData();
                updateCalendar();
            });
        }

        if (btnAddEntry != null) {
            btnAddEntry.setOnClickListener(v -> openAddDeadline());
        }

        if (btnLogMoodBottom != null) {
            btnLogMoodBottom.setOnClickListener(v -> openLogMood());
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
        SimpleDateFormat dateSdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String formattedDate = dateSdf.format(selectedDate.getTime());
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LogMoodFragment())
                .addToBackStack(null)
                .commit();
    }

    private void loadMonthData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

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
                    deadlineDays.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Deadline deadline = doc.toObject(Deadline.class);
                        if (deadline.getDueDate() != null) {
                            Calendar d = Calendar.getInstance();
                            d.setTime(deadline.getDueDate());
                            if (d.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                                    d.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) {
                                int day = d.get(Calendar.DAY_OF_MONTH);
                                String type = deadline.getType() != null ? deadline.getType().toLowerCase() : "assignment";
                                deadlineDays.put(day, type);
                            }
                        }
                    }
                    updateCalendar();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load deadlines", e));

        // Load moods
        Firebase.getInstance().getUserMoodLogsInRange(user.getUid(), start.getTimeInMillis(), end.getTimeInMillis())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    moodDays.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Mood mood = doc.toObject(Mood.class);
                        if (mood.getTimestamp() != null) {
                            Calendar d = Calendar.getInstance();
                            d.setTime(mood.getTimestamp());
                            if (d.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                                    d.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) {
                                int day = d.get(Calendar.DAY_OF_MONTH);
                                moodDays.put(day, mood);
                            }
                        }
                    }
                    updateCalendar();
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
            View indicator = dayLayout.findViewById(R.id.view_indicator);

            tvDay.setText(String.valueOf(day));

            if (selectedDate != null &&
                    selectedDate.get(Calendar.DAY_OF_MONTH) == day &&
                    selectedDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                    selectedDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
                dayLayout.setBackgroundResource(R.drawable.calendar_selection);
            } else {
                dayLayout.setBackgroundColor(Color.TRANSPARENT);
            }

            // Show mood indicators (priority over deadlines)
            Mood mood = moodDays.get(day);
            if (mood != null) {
                indicator.setVisibility(View.VISIBLE);
                // Set color based on mood
                switch (mood.getMood() != null ? mood.getMood().toLowerCase() : "") {
                    case "very bad":
                        indicator.setBackgroundResource(R.drawable.circle_red);
                        break;
                    case "bad":
                        indicator.setBackgroundResource(R.drawable.dot_orange);
                        break;
                    case "okay":
                        indicator.setBackgroundResource(R.drawable.dot_orange);
                        break;
                    case "good":
                        indicator.setBackgroundResource(R.drawable.circle_blue_bg);
                        break;
                    case "amazing":
                        indicator.setBackgroundResource(R.drawable.dot_green);
                        break;
                    default:
                        indicator.setBackgroundResource(R.drawable.dot_teal);
                        break;
                }
            } else {
                // Show deadline indicators if no mood
                String type = deadlineDays.get(day);
                if (type != null) {
                    indicator.setVisibility(View.VISIBLE);
                    switch (type) {
                        case "quiz":
                            indicator.setBackgroundResource(R.drawable.dot_orange);
                            break;
                        case "test":
                            indicator.setBackgroundResource(R.drawable.circle_blue_bg);
                            break;
                        case "midterm":
                            indicator.setBackgroundResource(R.drawable.dot_teal);
                            break;
                        default:
                            indicator.setBackgroundResource(R.drawable.dot_green);
                            break;
                    }
                } else {
                    indicator.setVisibility(View.INVISIBLE);
                }
            }

            dayLayout.setOnClickListener(v -> {
                selectedDate = (Calendar) calendar.clone();
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateCalendar();
                updateSelectedDateText();

                SimpleDateFormat dateSdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                String formattedDate = dateSdf.format(selectedDate.getTime());

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, AddDeadlineFragment.newInstance(formattedDate))
                        .addToBackStack(null)
                        .commit();
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = (int) (60 * getResources().getDisplayMetrics().density);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            dayLayout.setLayoutParams(params);

            calendarGrid.addView(dayLayout);
        }
    }

    private void updateSelectedDateText() {
        if (tvSelectedDate == null || selectedDate == null) return;
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        tvSelectedDate.setText(sdf.format(selectedDate.getTime()));
    }
}