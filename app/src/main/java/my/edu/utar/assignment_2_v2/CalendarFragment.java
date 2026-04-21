package my.edu.utar.assignment_2_v2;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private TextView tvMonthYear, tvSelectedDate;
    private GridLayout calendarGrid;
    private Calendar calendar;
    private Calendar selectedDate;

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

        calendar = Calendar.getInstance();
        selectedDate = (Calendar) calendar.clone();

        updateCalendar();
        updateSelectedDateText();

        if (btnPrevMonth != null) {
            btnPrevMonth.setOnClickListener(v -> {
                calendar.add(Calendar.MONTH, -1);
                updateCalendar();
            });
        }

        if (btnNextMonth != null) {
            btnNextMonth.setOnClickListener(v -> {
                calendar.add(Calendar.MONTH, 1);
                updateCalendar();
            });
        }
        
        if (btnAddEntry != null) {
            btnAddEntry.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new LogMoodFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        return view;
    }

    private void updateCalendar() {
        if (calendarGrid == null) return;
        calendarGrid.removeAllViews();

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonthYear.setText(sdf.format(calendar.getTime()));

        Calendar tempCal = (Calendar) calendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1; // 0-indexed
        int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Add empty views for padding
        for (int i = 0; i < firstDayOfWeek; i++) {
            View emptyView = new View(getContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = (int) (60 * getResources().getDisplayMetrics().density);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            emptyView.setLayoutParams(params);
            calendarGrid.addView(emptyView);
        }

        // Add days
        for (int day = 1; day <= daysInMonth; day++) {
            final int dayOfMonth = day;
            LinearLayout dayLayout = (LinearLayout) LayoutInflater.from(getContext())
                    .inflate(R.layout.item_calendar_day, calendarGrid, false);
            
            TextView tvDay = dayLayout.findViewById(R.id.tv_day);
            View indicator = dayLayout.findViewById(R.id.view_indicator);
            
            tvDay.setText(String.valueOf(day));

            // Highlight selected day
            if (selectedDate != null &&
                selectedDate.get(Calendar.DAY_OF_MONTH) == day &&
                selectedDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                selectedDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
                dayLayout.setBackgroundResource(R.drawable.calendar_selection);
            } else {
                dayLayout.setBackgroundColor(Color.TRANSPARENT);
            }

            // Simulated mood indicators based on your design
            if (day == 2 || day == 4 || day == 10 || day == 17 || day == 28) indicator.setBackgroundResource(R.drawable.dot_orange);
            else if (day == 3 || day == 7 || day == 9 || day == 13 || day == 22 || day == 26) indicator.setBackgroundResource(R.drawable.dot_green);
            else if (day == 5 || day == 8 || day == 12 || day == 15 || day == 21 || day == 24 || day == 27 || day == 29 || day == 30) indicator.setBackgroundResource(R.drawable.circle_blue_bg);
            else if (day == 11 || day == 14 || day == 16 || day == 20 || day == 23 || day == 25) indicator.setBackgroundResource(R.drawable.dot_teal);
            else indicator.setVisibility(View.INVISIBLE);

            dayLayout.setOnClickListener(v -> {
                selectedDate = (Calendar) calendar.clone();
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateCalendar(); // Refresh selection UI
                updateSelectedDateText();
                
                // Format the selected date to pass to the next fragment
                SimpleDateFormat dateSdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                String formattedDate = dateSdf.format(selectedDate.getTime());
                
                // Show AddDeadlineFragment with the selected date
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