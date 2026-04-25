package my.edu.utar.assignment_2_v2.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class GoogleCalendarHelper {
    private static final String TAG = "GoogleCalendarHelper";

    /**
     * Inserts a deadline event into the device's default Google Calendar using an Intent.
     * This opens the Calendar app with pre-filled details for the user to confirm.
     *
     * @param context   The activity context
     * @param title     Event title
     * @param type      Deadline type (Assignment, Quiz, Test, Midterm)
     * @param dueDate   Due date and time
     * @param reminderMinutes Minutes before event to remind (default 1440 = 1 day)
     * @return Intent that can be started with startActivity()
     */
    public static Intent createCalendarEventIntent(Context context, String title, String type, Date dueDate, int reminderMinutes) {
        Calendar beginTime = Calendar.getInstance();
        beginTime.setTime(dueDate);

        Calendar endTime = Calendar.getInstance();
        endTime.setTime(dueDate);
        endTime.add(Calendar.HOUR_OF_DAY, 1);

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, "[Wellio] " + type + ": " + title)
                .putExtra(CalendarContract.Events.DESCRIPTION, "Deadline for " + type + " - " + title)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.ALL_DAY, false)
                .putExtra(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID())
                .putExtra(CalendarContract.Events.HAS_ALARM, 1);

        // Add reminder
        intent.putExtra(CalendarContract.Reminders.MINUTES, reminderMinutes);

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            return intent;
        } else {
            Log.w(TAG, "No calendar app found to handle intent");
            return null;
        }
    }

    /**
     * Directly insert a calendar event via ContentResolver (requires WRITE_CALENDAR permission).
     * Returns the event ID if successful, or null on failure.
     */
    public static Long insertEventDirectly(Context context, String title, String type, Date dueDate, int reminderMinutes) {
        try {
            Calendar beginTime = Calendar.getInstance();
            beginTime.setTime(dueDate);

            Calendar endTime = Calendar.getInstance();
            endTime.setTime(dueDate);
            endTime.add(Calendar.HOUR_OF_DAY, 1);

            ContentValues eventValues = new ContentValues();
            eventValues.put(CalendarContract.Events.CALENDAR_ID, 1); // Primary calendar
            eventValues.put(CalendarContract.Events.TITLE, "[Wellio] " + type + ": " + title);
            eventValues.put(CalendarContract.Events.DESCRIPTION, "Deadline for " + type + " - " + title);
            eventValues.put(CalendarContract.Events.DTSTART, beginTime.getTimeInMillis());
            eventValues.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
            eventValues.put(CalendarContract.Events.ALL_DAY, 0);
            eventValues.put(CalendarContract.Events.HAS_ALARM, 1);
            eventValues.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

            Uri eventUri = context.getContentResolver().insert(CalendarContract.Events.CONTENT_URI, eventValues);
            if (eventUri == null) {
                Log.e(TAG, "Failed to insert calendar event");
                return null;
            }

            long eventId = Long.parseLong(eventUri.getLastPathSegment());

            // Add reminder
            ContentValues reminderValues = new ContentValues();
            reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId);
            reminderValues.put(CalendarContract.Reminders.MINUTES, reminderMinutes);
            reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            context.getContentResolver().insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);

            Log.d(TAG, "Calendar event inserted with ID: " + eventId);
            return eventId;
        } catch (Exception e) {
            Log.e(TAG, "Error inserting calendar event", e);
            return null;
        }
    }

    public static int getReminderMinutesFromSelection(int radioButtonId) {
        if (radioButtonId == my.edu.utar.assignment_2_v2.R.id.rb_3_days) {
            return 3 * 24 * 60; // 3 days
        } else if (radioButtonId == my.edu.utar.assignment_2_v2.R.id.rb_1_week) {
            return 7 * 24 * 60; // 1 week
        } else {
            return 24 * 60; // 1 day (default)
        }
    }
}
