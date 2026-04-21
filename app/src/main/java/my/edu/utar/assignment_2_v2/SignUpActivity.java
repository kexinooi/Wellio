package my.edu.utar.assignment_2_v2;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.bottom_prompt_container).setOnClickListener(v -> {
            finish(); // Go back to Login
        });

        findViewById(R.id.btn_signup).setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
            finishAffinity();
        });
    }
}