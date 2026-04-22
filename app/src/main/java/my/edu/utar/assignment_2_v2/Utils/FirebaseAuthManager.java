package my.edu.utar.assignment_2_v2.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import my.edu.utar.assignment_2_v2.R;
import my.edu.utar.assignment_2_v2.model.User;

public class FirebaseAuthManager {
    private static final String TAG = "GoogleSignInHelper";
    private static final int RC_SIGN_IN = 9001;

    private final Activity activity;
    private final FirebaseAuth mAuth;
    private final GoogleSignInClient mGoogleSignInClient;
    private final AuthCallback callback;

    public FirebaseAuthManager(Activity activity, AuthCallback callback) {
        this.activity = activity;
        this.callback = callback;
        this.mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                callback.onFailure("Google sign in failed: " + e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(activity,task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "signInWithCredential:success");
                FirebaseUser user = mAuth.getCurrentUser();
                saveUserToFirestore(user, acct);
                callback.onSuccess(user);
            } else {
                Log.w(TAG, "signInWithCredential:failure", task.getException());
                callback.onFailure("Firebase Auth Failed: " + task.getException().getMessage());
            }
        });
    }

    private void saveUserToFirestore(FirebaseUser firebaseUser, GoogleSignInAccount googleAccount) {
        if (firebaseUser != null) {
            User user = new User(
                    firebaseUser.getUid(),
                    firebaseUser.getDisplayName(),
                    firebaseUser.getEmail(),
                    firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null
            );

            Firebase.getInstance().saveUser(user)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User data saved successfully"))
                    .addOnFailureListener(e -> Log.w(TAG, "Failed to save user data", e));
        }
    }

    public void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // New method to allow changing users
    public void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut();
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }
}