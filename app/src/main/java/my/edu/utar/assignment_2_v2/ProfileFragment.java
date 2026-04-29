package my.edu.utar.assignment_2_v2;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import my.edu.utar.assignment_2_v2.Utils.Firebase;
import my.edu.utar.assignment_2_v2.model.Mood;

public class ProfileFragment extends Fragment {

    private ImageView btnSettings;
    private ImageView imgProfileAvatar;
    private TextView tvProfileName;
    private TextView tvAchievementStreakTitle;
    private TextView tvAchievementStreakDesc;
    private TextView tvMindfulCount;
    private TextView tvAchievementMindfulDesc;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        btnSettings = view.findViewById(R.id.btn_settings);
        imgProfileAvatar = view.findViewById(R.id.img_profile_avatar);
        tvProfileName = view.findViewById(R.id.tv_profile_name);
        tvAchievementStreakTitle = view.findViewById(R.id.tv_achievement_streak_title);
        tvAchievementStreakDesc = view.findViewById(R.id.tv_achievement_streak_desc);
        tvMindfulCount = view.findViewById(R.id.tv_mindful_count);
        tvAchievementMindfulDesc = view.findViewById(R.id.tv_achievement_mindful_desc);

        loadProfileData();

        btnSettings.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SettingsFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void loadProfileData() {
        if (getActivity() == null) return;

        SharedPreferences prefs = getActivity().getSharedPreferences("wellio_settings", 0);

        FirebaseUser currentUser = Firebase.getInstance().getCurrentUser();
        String fullName = prefs.getString("full_name", "");
        if (TextUtils.isEmpty(fullName) && currentUser != null) {
            fullName = currentUser.getDisplayName();
        }
        if (TextUtils.isEmpty(fullName) && currentUser != null && !TextUtils.isEmpty(currentUser.getEmail())) {
            fullName = currentUser.getEmail().split("@")[0];
        }
        if (TextUtils.isEmpty(fullName)) {
            fullName = "User";
        }

        String imagePath = prefs.getString("profile_image_path", null);

        tvProfileName.setText(fullName);

        if (!TextUtils.isEmpty(imagePath)) {
            imgProfileAvatar.setImageURI(Uri.fromFile(new java.io.File(imagePath)));
        } else {
            imgProfileAvatar.setImageResource(android.R.drawable.sym_def_app_icon);
        }

        loadAchievements();
    }

    private void loadAchievements() {
        FirebaseUser currentUser = Firebase.getInstance().getCurrentUser();
        if (currentUser == null) {
            applyAchievementFallbackState();
            return;
        }

        Firebase.getInstance().getUserMoodLogs(currentUser.getUid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int streakDays = 0;
                    int noteCount = 0;
                    Set<String> loggedDays = new HashSet<>();

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                        Mood mood = doc.toObject(Mood.class);

                        Date timestamp = mood.getTimestamp();
                        if (timestamp != null) {
                            loggedDays.add(getDayKey(timestamp));
                        }

                        if (!TextUtils.isEmpty(mood.getNote())) {
                            noteCount++;
                        }
                    }

                    streakDays = calculateCurrentStreak(loggedDays);
                    bindAchievements(streakDays, noteCount);
                })
                .addOnFailureListener(e -> applyAchievementFallbackState());
    }

    private void bindAchievements(int streakDays, int noteCount) {
        if (!isAdded()) {
            return;
        }

        String streakTitle = streakDays > 0 ? streakDays + "-Day Streak" : "No Active Streak";
        String streakDesc = streakDays > 0
                ? "Logged mood for " + streakDays + (streakDays == 1 ? " day" : " days")
                : "Start logging daily to build a streak";

        tvAchievementStreakTitle.setText(streakTitle);
        tvAchievementStreakDesc.setText(streakDesc);
        tvMindfulCount.setText(String.valueOf(noteCount));
        tvAchievementMindfulDesc.setText(
                noteCount > 0
                        ? "Added " + noteCount + (noteCount == 1 ? " note" : " notes")
                        : "Add a note to unlock mindful reflections"
        );
    }

    private void applyAchievementFallbackState() {
        bindAchievements(0, 0);
    }

    private int calculateCurrentStreak(Set<String> loggedDays) {
        int streak = 0;
        Calendar cursor = Calendar.getInstance();
        resetToStartOfDay(cursor);

        while (loggedDays.contains(getDayKey(cursor.getTime()))) {
            streak++;
            cursor.add(Calendar.DAY_OF_MONTH, -1);
        }

        return streak;
    }

    private void resetToStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private String getDayKey(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        resetToStartOfDay(calendar);
        return calendar.get(Calendar.YEAR) + "-" +
                calendar.get(Calendar.MONTH) + "-" +
                calendar.get(Calendar.DAY_OF_MONTH);
    }
}
