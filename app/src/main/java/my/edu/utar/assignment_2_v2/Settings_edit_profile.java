package my.edu.utar.assignment_2_v2;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class Settings_edit_profile extends AppCompatActivity {

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_edit_profile);

        EditText etFullName = findViewById(R.id.etFullName);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPhone = findViewById(R.id.etPhone);
        Button btnSaveProfile = findViewById(R.id.btnSaveProfile);
        ImageView btnBack = findViewById(R.id.btn_back2);
        ImageView imgProfile = findViewById(R.id.imgProfile);
        TextView tvChangePhoto = findViewById(R.id.tvChangePhoto);

        SharedPreferences prefs = getSharedPreferences("wellio_settings", MODE_PRIVATE);

        etFullName.setText(prefs.getString("full_name", ""));
        etEmail.setText(prefs.getString("email", ""));
        etPhone.setText(prefs.getString("phone", ""));

        String savedImageUri = prefs.getString("profile_image_uri", null);
        if (savedImageUri != null) {
            imgProfile.setImageURI(Uri.parse(savedImageUri));
        }

        pickMedia = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        imgProfile.setImageURI(uri);
                        prefs.edit().putString("profile_image_uri", uri.toString()).apply();
                    } else {
                        Toast.makeText(Settings_edit_profile.this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        tvChangePhoto.setOnClickListener(v -> {
            pickMedia.launch(
                    new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build()
            );
        });

        imgProfile.setOnClickListener(v -> {
            pickMedia.launch(
                    new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build()
            );
        });

        btnSaveProfile.setOnClickListener(v -> {
            prefs.edit()
                    .putString("full_name", etFullName.getText().toString().trim())
                    .putString("email", etEmail.getText().toString().trim())
                    .putString("phone", etPhone.getText().toString().trim())
                    .apply();

            Toast.makeText(Settings_edit_profile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnBack.setOnClickListener(v -> {
            finish();
        });
    }
}