package my.edu.utar.assignment_2_v2;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;
import my.edu.utar.assignment_2_v2.model.*;
import my.edu.utar.assignment_2_v2.Utils.Firebase;

public class LogMoodFragment extends Fragment {

    private Button saveMoodBtn;
    private EditText noteEditText;
    private EditText sleepHoursEditText;
    private String selectedMood = "";
    private View cardVeryBad, cardBad, cardOkay, cardGood, cardAmazing;
    private Chip chipStressed, chipMotivated, chipTired, chipFocused, chipAnxious, chipHappy;

    FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_log_mood, container, false);

        // Back button logic
        ImageView btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        cardVeryBad = view.findViewById(R.id.card_very_bad);
        cardBad = view.findViewById(R.id.card_bad);
        cardOkay = view.findViewById(R.id.card_okay);
        cardGood = view.findViewById(R.id.card_good);
        cardAmazing = view.findViewById(R.id.card_amazing);

        if (cardVeryBad != null) cardVeryBad.setOnClickListener(v -> selectMood("Very Bad"));
        if (cardBad != null) cardBad.setOnClickListener(v -> selectMood("Bad"));
        if (cardOkay != null) cardOkay.setOnClickListener(v -> selectMood("Okay"));
        if (cardGood != null) cardGood.setOnClickListener(v -> selectMood("Good"));
        if (cardAmazing != null) cardAmazing.setOnClickListener(v -> selectMood("Amazing"));

        chipStressed = view.findViewById(R.id.chip_stressed);
        chipMotivated = view.findViewById(R.id.chip_motivated);
        chipTired = view.findViewById(R.id.chip_tired);
        chipFocused = view.findViewById(R.id.chip_focused);
        chipAnxious = view.findViewById(R.id.chip_anxious);
        chipHappy = view.findViewById(R.id.chip_happy);

        saveMoodBtn = view.findViewById(R.id.btn_save_mood);
        noteEditText = view.findViewById(R.id.et_note);
        sleepHoursEditText = view.findViewById(R.id.et_sleep_hours);

        // initialize Firestore
        db = FirebaseFirestore.getInstance();

        // button click
        if (saveMoodBtn != null) {
            saveMoodBtn.setOnClickListener(v -> saveMood());
        }

        return view;
    }

    private void saveMood() {

        String note = noteEditText.getText().toString().trim();
        List<String> feelings = getSelectedFeelings();

        if (selectedMood.isEmpty()) {
            Toast.makeText(getContext(), "Please select a mood", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user
        String userId = Firebase.getInstance().getCurrentUser() != null ? 
                Firebase.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get sleep hours
        double sleepHours = 0.0;
        String sleepHoursText = sleepHoursEditText.getText().toString().trim();
        if (!sleepHoursText.isEmpty()) {
            try {
                sleepHours = Double.parseDouble(sleepHoursText);
                if (sleepHours < 0 || sleepHours > 24) {
                    Toast.makeText(getContext(), "Please enter valid sleep hours (0-24)", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter valid sleep hours", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Convert List<String> to one String for Mood.java
        String feelText = TextUtils.join(", ", feelings);

        // Create Mood object with userId and sleep hours
        Mood moodObj = new Mood(userId, feelText, selectedMood, note, sleepHours);

        // Use Firebase utility to save mood
        Firebase.getInstance().saveMoodLog(moodObj)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Mood saved ✅", Toast.LENGTH_SHORT).show();
                    noteEditText.setText(""); // clear input
                    sleepHoursEditText.setText(""); // clear sleep hours
                    resetSelection(); // reset mood selection
                    selectedMood = ""; // clear selected mood
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save mood: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    private void selectMood(String mood) {
        selectedMood = mood;
        Toast.makeText(getContext(), "Selected: " + mood, Toast.LENGTH_SHORT).show();
        resetSelection();

        switch (mood) {
            case "Very Bad":
                highlightCard(cardVeryBad);
                break;
            case "Bad":
                highlightCard(cardBad);
                break;
            case "Okay":
                highlightCard(cardOkay);
                break;
            case "Good":
                highlightCard(cardGood);
                break;
            case "Amazing":
                highlightCard(cardAmazing);
                break;
        }
    }
    private void highlightCard(View card) {
        if (card instanceof com.google.android.material.card.MaterialCardView) {
            com.google.android.material.card.MaterialCardView mCard =
                    (com.google.android.material.card.MaterialCardView) card;

            mCard.setStrokeWidth(4);
            mCard.setStrokeColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    private void resetSelection() {
        resetCard(cardVeryBad);
        resetCard(cardBad);
        resetCard(cardOkay);
        resetCard(cardGood);
        resetCard(cardAmazing);
    }

    private void resetCard(View card) {
        if (card instanceof com.google.android.material.card.MaterialCardView) {
            com.google.android.material.card.MaterialCardView mCard =
                    (com.google.android.material.card.MaterialCardView) card;

            mCard.setStrokeWidth(0);
        }
    }

    private List<String> getSelectedFeelings() {
        List<String> feelings = new ArrayList<>();

        if (chipStressed != null && chipStressed.isChecked()) feelings.add("Stressed");
        if (chipMotivated != null && chipMotivated.isChecked()) feelings.add("Motivated");
        if (chipTired != null && chipTired.isChecked()) feelings.add("Tired");
        if (chipFocused != null && chipFocused.isChecked()) feelings.add("Focused");
        if (chipAnxious != null && chipAnxious.isChecked()) feelings.add("Anxious");
        if (chipHappy != null && chipHappy.isChecked()) feelings.add("Happy");

        return feelings;
    }

}