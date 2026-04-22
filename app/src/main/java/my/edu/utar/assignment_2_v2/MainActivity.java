package my.edu.utar.assignment_2_v2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import my.edu.utar.assignment_2_v2.Utils.FirebaseAuthManager;

public class MainActivity extends AppCompatActivity implements FirebaseAuthManager.AuthCallback{
    private static final String TAG = "MainActivity";

    private FirebaseAuthManager FirebaseAuthManager;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable Edge-to-Edge to remove the black navigation bar area
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseAuthManager = new FirebaseAuthManager(this, this);

        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not signed in, redirect to sign in
            signInWithGoogle();
        } else {
            // User is already signed in, setup UI
            setupUI();
        }
    }
    private void setupUI() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

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
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
        // Set the default fragment to HomeFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }
    public void signInWithGoogle() {
        FirebaseAuthManager.signIn();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FirebaseAuthManager.onActivityResult(requestCode, resultCode, data);
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
        // Retry sign in
        signInWithGoogle();
    }

    public void signOut() {
        FirebaseAuthManager.signOut();
        mAuth.signOut();
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
        signInWithGoogle();
    }
}