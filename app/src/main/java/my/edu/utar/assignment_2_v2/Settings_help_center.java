package my.edu.utar.assignment_2_v2;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Settings_help_center extends AppCompatActivity {

    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_help_center);

        btnBack = findViewById(R.id.btn_back_help);

        LinearLayout faqLogMood = findViewById(R.id.faq_log_mood);
        LinearLayout faqEditProfile = findViewById(R.id.faq_edit_profile);
        LinearLayout faqReminder = findViewById(R.id.faq_reminder);
        LinearLayout faqPassword = findViewById(R.id.faq_password);
        LinearLayout faqContact = findViewById(R.id.faq_contact);

        TextView answer1 = findViewById(R.id.tv_answer_1);
        TextView answer2 = findViewById(R.id.tv_answer_2);
        TextView answer3 = findViewById(R.id.tv_answer_3);
        TextView answer4 = findViewById(R.id.tv_answer_4);
        TextView answer5 = findViewById(R.id.tv_answer_5);

        ImageView arrow1 = findViewById(R.id.iv_arrow_1);
        ImageView arrow2 = findViewById(R.id.iv_arrow_2);
        ImageView arrow3 = findViewById(R.id.iv_arrow_3);
        ImageView arrow4 = findViewById(R.id.iv_arrow_4);
        ImageView arrow5 = findViewById(R.id.iv_arrow_5);

        btnBack.setOnClickListener(v -> finish());

        faqLogMood.setOnClickListener(v -> toggleAnswer(answer1, arrow1));
        faqEditProfile.setOnClickListener(v -> toggleAnswer(answer2, arrow2));
        faqReminder.setOnClickListener(v -> toggleAnswer(answer3, arrow3));
        faqPassword.setOnClickListener(v -> toggleAnswer(answer4, arrow4));
        faqContact.setOnClickListener(v -> toggleAnswer(answer5, arrow5));
    }

    private void toggleAnswer(TextView answerView, ImageView arrowView) {
        if (answerView.getVisibility() == View.GONE) {
            answerView.setVisibility(View.VISIBLE);
            arrowView.setRotation(90f);
        } else {
            answerView.setVisibility(View.GONE);
            arrowView.setRotation(0f);
        }
    }
}