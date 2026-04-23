package my.edu.utar.assignment_2_v2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class Settings_account_security extends AppCompatActivity {

    private ImageView btnBack;
    private RelativeLayout btnChangePassword, btnPrivacyPolicy, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_account_security);

        btnBack = findViewById(R.id.btn_back_security);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnPrivacyPolicy = findViewById(R.id.btn_privacy_policy);
        btnLogout = findViewById(R.id.btn_logout);

        btnBack.setOnClickListener(v -> finish());

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(Settings_account_security.this, Settings_changePassword.class);
            startActivity(intent);
        });

        btnPrivacyPolicy.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Privacy Policy")
                    .setMessage("Your mood entries and profile information are stored only for app functionality. In future versions, this page can be linked to a full privacy policy.")
                    .setPositiveButton("OK", null)
                    .show();
        });

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        SharedPreferences prefs = getSharedPreferences("wellio_settings", MODE_PRIVATE);
                        prefs.edit().clear().apply();

                        Intent intent = new Intent(Settings_account_security.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}