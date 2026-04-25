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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class Settings_edit_profile extends AppCompatActivity {

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private ImageView imgProfile;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_edit_profile);

        EditText etFullName = findViewById(R.id.etFullName);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPhone = findViewById(R.id.etPhone);
        Button btnSaveProfile = findViewById(R.id.btnSaveProfile);
        ImageView btnBack = findViewById(R.id.btn_back2);
        imgProfile = findViewById(R.id.imgProfile);
        TextView tvChangePhoto = findViewById(R.id.tvChangePhoto);

        prefs = getSharedPreferences("wellio_settings", MODE_PRIVATE);

        etFullName.setText(prefs.getString("full_name", ""));
        etEmail.setText(prefs.getString("email", ""));
        etPhone.setText(prefs.getString("phone", ""));

        String savedImagePath = prefs.getString("profile_image_path", null);
        if (savedImagePath != null) {
            File imageFile = new File(savedImagePath);
            if (imageFile.exists()) {
                imgProfile.setImageURI(Uri.fromFile(imageFile));
            }
        }

        pickMedia = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        String savedPath = saveImageToInternalStorage(uri);

                        if (savedPath != null) {
                            prefs.edit()
                                    .putString("profile_image_path", savedPath)
                                    .apply();

                            imgProfile.setImageURI(Uri.fromFile(new File(savedPath)));
                        } else {
                            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        tvChangePhoto.setOnClickListener(v -> openPhotoPicker());
        imgProfile.setOnClickListener(v -> openPhotoPicker());

        btnSaveProfile.setOnClickListener(v -> {
            prefs.edit()
                    .putString("full_name", etFullName.getText().toString().trim())
                    .putString("email", etEmail.getText().toString().trim())
                    .putString("phone", etPhone.getText().toString().trim())
                    .apply();

            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void openPhotoPicker() {
        pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        );
    }

    private String saveImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);

            if (inputStream == null) {
                return null;
            }

            File file = new File(getFilesDir(), "profile_image.jpg");
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return file.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}