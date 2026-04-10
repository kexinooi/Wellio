package my.edu.utar.assignment_2_v2;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;
import my.edu.utar.assignment_2_v2.Mood.*;

public class LogMoodFragment extends Fragment {

    private Button saveMoodBtn;
    private EditText noteEditText;
    private String selectedMood = "";
    private View cardVeryBad, cardBad, cardOkay, cardGood, cardAmazing;
    private Chip chipStressed, chipMotivated, chipTired, chipFocused, chipAnxious, chipHappy;

    FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_log_mood, container, false);

        cardVeryBad = view.findViewById(R.id.card_very_bad);
        cardBad = view.findViewById(R.id.card_bad);
        cardOkay = view.findViewById(R.id.card_okay);
        cardGood = view.findViewById(R.id.card_good);
        cardAmazing = view.findViewById(R.id.card_amazing);

        cardVeryBad.setOnClickListener(v -> selectMood("Very Bad"));
        cardBad.setOnClickListener(v -> selectMood("Bad"));
        cardOkay.setOnClickListener(v -> selectMood("Okay"));
        cardGood.setOnClickListener(v -> selectMood("Good"));
        cardAmazing.setOnClickListener(v -> selectMood("Amazing"));

        chipStressed = view.findViewById(R.id.chip_stressed);
        chipMotivated = view.findViewById(R.id.chip_motivated);
        chipTired = view.findViewById(R.id.chip_tired);
        chipFocused = view.findViewById(R.id.chip_focused);
        chipAnxious = view.findViewById(R.id.chip_anxious);
        chipHappy = view.findViewById(R.id.chip_happy);

        saveMoodBtn = view.findViewById(R.id.btn_save_mood);
        noteEditText = view.findViewById(R.id.et_note);

        // initialize Firestore
        db = FirebaseFirestore.getInstance();

        // button click
        saveMoodBtn.setOnClickListener(v -> saveMood());

        return view;
    }

    private void saveMood() {

        String note = noteEditText.getText().toString().trim();
        List<String> feelings = getSelectedFeelings();

        if (selectedMood.isEmpty()) {
            Toast.makeText(getContext(), "Please select a mood", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert List<String> to one String for Mood.java
        String feelText = TextUtils.join(", ", feelings);

        // TEMP USER
        // String userId = "testUser1";

        // Create Mood object
        Mood moodObj = new Mood();
        moodObj.setMood(selectedMood);
        moodObj.setFeel(feelText);
        moodObj.setNote(note);
        //moodObj.put("timestamp", FieldValue.serverTimestamp());
        // moodLog.put("userId", userId);
        // moodLog.put("sleepHours", 6.5); // dummy for now
        //moodLog.put("stressLevel", "Low");
        //moodLog.put("focusLevel", "Medium");


        db.collection("mood_logs")
                .add(moodObj)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Mood saved ✅", Toast.LENGTH_SHORT).show();
                    noteEditText.setText(""); // clear input
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed ❌", Toast.LENGTH_SHORT).show();
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

        if (chipStressed.isChecked()) feelings.add("Stressed");
        if (chipMotivated.isChecked()) feelings.add("Motivated");
        if (chipTired.isChecked()) feelings.add("Tired");
        if (chipFocused.isChecked()) feelings.add("Focused");
        if (chipAnxious.isChecked()) feelings.add("Anxious");
        if (chipHappy.isChecked()) feelings.add("Happy");

        return feelings;
    }

}