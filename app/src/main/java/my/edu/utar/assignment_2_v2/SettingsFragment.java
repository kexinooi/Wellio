package my.edu.utar.assignment_2_v2;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Build;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private ImageView btnBack;
    private RelativeLayout btnEditProfile, btnAccountSecurity, btnThemeSelector, btnHelpCenter, btnAboutApp, layoutMoodReminder;
    private MaterialSwitch switchMoodReminders;
    private CheckBox cbThemeLight;
    private TextView tvCurrentTheme, tvProfileEmail, tvReminderTime;

    private SharedPreferences prefs;

    private static final String PREFS_NAME = "wellio_settings";
    private static final String KEY_THEME = "theme";
    private static final String KEY_MOOD_REMINDERS = "mood_reminders";
    private static final String KEY_REMINDER_TIME = "reminder_time";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        prefs = requireActivity().getSharedPreferences(PREFS_NAME, 0);

        btnBack = view.findViewById(R.id.btn_back);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnAccountSecurity = view.findViewById(R.id.btn_account_security);
        btnThemeSelector = view.findViewById(R.id.btn_theme_selector);
        btnHelpCenter = view.findViewById(R.id.btn_help_center);
        btnAboutApp = view.findViewById(R.id.btn_about_app);
        layoutMoodReminder = view.findViewById(R.id.layout_mood_reminder);

        switchMoodReminders = view.findViewById(R.id.switch_mood_reminders);
        cbThemeLight = view.findViewById(R.id.cb_theme_light);

        tvCurrentTheme = view.findViewById(R.id.tv_current_theme);
        tvProfileEmail = view.findViewById(R.id.tv_profile_email);
        tvReminderTime = view.findViewById(R.id.tv_reminder_time);

        loadSavedSettings();

        btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), Settings_edit_profile.class);
            startActivity(intent);
        });

        btnAccountSecurity.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), Settings_account_security.class);
            startActivity(intent);
        });

        layoutMoodReminder.setOnClickListener(v -> {
            if (switchMoodReminders.isChecked()) {
                showTimePicker();
            } else {
                Toast.makeText(requireContext(), "Enable mood reminders first", Toast.LENGTH_SHORT).show();
            }
        });

        switchMoodReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_MOOD_REMINDERS, isChecked).apply();

            String savedReminderTime = prefs.getString(KEY_REMINDER_TIME, "8:00 PM");

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(savedReminderTime));

                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);

                if (isChecked) {
                    scheduleMoodReminder(hour, minute);
                    Toast.makeText(requireContext(), "Mood reminders enabled", Toast.LENGTH_SHORT).show();
                } else {
                    cancelMoodReminder();
                    Toast.makeText(requireContext(), "Mood reminders disabled", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btnThemeSelector.setOnClickListener(v -> showThemeDialog());

        btnHelpCenter.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), Settings_help_center.class);
            startActivity(intent);
        });

        btnAboutApp.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), Settings_about_app.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSavedSettings();
    }

    private void loadSavedSettings() {
        boolean remindersEnabled = prefs.getBoolean(KEY_MOOD_REMINDERS, true);
        String savedTheme = prefs.getString(KEY_THEME, "Light");
        String savedEmail = prefs.getString("email", "kexin@example.com");
        String savedReminderTime = prefs.getString(KEY_REMINDER_TIME, "8:00 PM");

        switchMoodReminders.setChecked(remindersEnabled);
        tvCurrentTheme.setText(savedTheme);
        tvProfileEmail.setText("Email: " + savedEmail);
        tvReminderTime.setText("Daily at " + savedReminderTime);

        cbThemeLight.setChecked(savedTheme.equals("Light"));
    }

    private void showThemeDialog() {
        String[] themes = {"Light", "Dark"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Theme")
                .setItems(themes, (dialog, which) -> {
                    String selectedTheme = themes[which];

                    prefs.edit().putString(KEY_THEME, selectedTheme).apply();
                    tvCurrentTheme.setText(selectedTheme);
                    cbThemeLight.setChecked(selectedTheme.equals("Light"));

                    if (selectedTheme.equals("Light")) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }
                })
                .show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    Calendar selectedTime = Calendar.getInstance();
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);

                    SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
                    String formattedTime = sdf.format(selectedTime.getTime());

                    prefs.edit().putString(KEY_REMINDER_TIME, formattedTime).apply();
                    tvReminderTime.setText("Daily at " + formattedTime);

                    Toast.makeText(requireContext(),
                            "Reminder time set to " + formattedTime,
                            Toast.LENGTH_SHORT).show();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );

        timePickerDialog.show();
    }

    private void scheduleMoodReminder(int hourOfDay, int minute) {
        Context context = requireContext();

        Intent intent = new Intent(context, Settings_reminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }

    private void cancelMoodReminder() {
        Context context = requireContext();

        Intent intent = new Intent(context, Settings_reminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}