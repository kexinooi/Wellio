package com.example.groupassignment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock; // NEW: Required for the timer
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;

public class MainActivity extends AppCompatActivity {

    private final String WEB_CLIENT_ID = "100216324548-fv7picu46kim0pvpr4duhd588920sopa.apps.googleusercontent.com";
    private FirebaseAuthManager authManager;
    private GeminiApiService geminiApiService;
    private TextView tvResult;
    private EditText etMoodInput;
    private Button btnCopy;

    // NEW: A tracker to stop the "Double-Fire" bug
    private long lastClickTime = 0;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();
                authManager.handleSignInResult(data, new FirebaseAuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        tvResult.setText("Welcome to Wellio!\nLogged in as: " + user.getEmail());
                    }
                    @Override
                    public void onFailure(String errorMessage) {
                        tvResult.setText("Login Failed: " + errorMessage);
                    }
                });
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance());

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnTestAI = findViewById(R.id.btnTestAI);
        btnCopy = findViewById(R.id.btnCopy);
        etMoodInput = findViewById(R.id.etMoodInput);
        tvResult = findViewById(R.id.tvResult);

        authManager = new FirebaseAuthManager(this, WEB_CLIENT_ID);
        geminiApiService = new GeminiApiService();

        btnLogin.setOnClickListener(v -> {
            tvResult.setText("Opening Google Sign-In...");
            signInLauncher.launch(authManager.getSignInIntent());
        });

        btnLogout.setOnClickListener(v -> {
            authManager.signOut();
            tvResult.setText("You have been signed out. You can now log in with a different account.");
            btnCopy.setVisibility(View.GONE);
            Toast.makeText(this, "Signed Out Successfully", Toast.LENGTH_SHORT).show();
        });

        btnCopy.setOnClickListener(v -> {
            String aiAdvice = tvResult.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Wellio Advice", aiAdvice);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(MainActivity.this, "Advice copied to clipboard!", Toast.LENGTH_SHORT).show();
        });

        // FIXED: Simplified the listener so it only triggers ONCE
        etMoodInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                btnTestAI.performClick();
                return true;
            }
            return false;
        });

        btnTestAI.setOnClickListener(v -> {
            // --- THE BULLETPROOF TIMER ---
            // If the button was clicked less than 3 seconds ago, completely ignore it.
            if (SystemClock.elapsedRealtime() - lastClickTime < 3000) {
                return;
            }
            lastClickTime = SystemClock.elapsedRealtime(); // Reset the timer

            String userMood = etMoodInput.getText().toString().trim();

            if (userMood.isEmpty()) {
                tvResult.setText("Please type your current feelings or academic workload into the text box first!");
                return;
            }

            btnTestAI.setEnabled(false);
            btnTestAI.setText("Waiting for AI...");
            btnCopy.setVisibility(View.GONE);

            tvResult.setText("Wellio AI is analyzing your workload...");

            geminiApiService.getWellbeingRecommendations(userMood, new GeminiApiService.GeminiCallback() {
                @Override
                public void onResult(String result) {
                    runOnUiThread(() -> {
                        tvResult.setText(result);
                        btnTestAI.setEnabled(true);
                        btnTestAI.setText("Get Wellio AI Advice");
                        btnCopy.setVisibility(View.VISIBLE);
                    });
                }
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        tvResult.setText("Error: " + error);
                        btnTestAI.setEnabled(true);
                        btnTestAI.setText("Get Wellio AI Advice");
                    });
                }
            });
        });
    }
}