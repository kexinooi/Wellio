package my.edu.utar.assignment_2_v2;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import my.edu.utar.assignment_2_v2.Utils.FirebaseAuthManager;

public class MainActivity extends AppCompatActivity implements FirebaseAuthManager.AuthCallback {

    private static final String TAG = "MainActivity";

    private FirebaseAuthManager firebaseAuthManager;
    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("wellio_settings", MODE_PRIVATE);
        String savedTheme = prefs.getString("theme", "Light");

        if (savedTheme.equals("Light")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Uncomment if you want Firebase auth flow enabled
        // mAuth = FirebaseAuth.getInstance();
        // firebaseAuthManager = new FirebaseAuthManager(this, this);
        //
        // FirebaseUser currentUser = mAuth.getCurrentUser();
        // if (currentUser == null) {
        //     signInWithGoogle();
        // } else {
        //     setupUI(savedInstanceState);
        // }

        setupUI(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        100
                );
            }
        }
    }

    private void setupUI(Bundle savedInstanceState) {
        bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.navigation_insights) {
                selectedFragment = new InsightsFragment();
            } else if (itemId == R.id.navigation_calendar) {
                selectedFragment = new CalendarFragment();
            } else if (itemId == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }

            return false;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.navigation_home);
        }
    }

    public void signInWithGoogle() {
        if (firebaseAuthManager != null) {
            firebaseAuthManager.signIn();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (firebaseAuthManager != null) {
            firebaseAuthManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSuccess(FirebaseUser user) {
        Log.d(TAG, "Sign in successful: " + user.getDisplayName());
        Toast.makeText(this, "Welcome back, " + user.getDisplayName() + "!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailure(String error) {
        Log.e(TAG, "Sign in failed: " + error);
        Toast.makeText(this, "Sign in failed: " + error, Toast.LENGTH_LONG).show();
        signInWithGoogle();
    }

    public void signOut() {
        if (firebaseAuthManager != null) {
            firebaseAuthManager.signOut();
        }
        if (mAuth != null) {
            mAuth.signOut();
        }
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
        signInWithGoogle();
    }
}