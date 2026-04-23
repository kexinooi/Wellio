package my.edu.utar.assignment_2_v2;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Settings_about_app extends AppCompatActivity {

    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_about_app);

        btnBack = findViewById(R.id.btn_back_about);

        btnBack.setOnClickListener(v -> finish());
    }
}