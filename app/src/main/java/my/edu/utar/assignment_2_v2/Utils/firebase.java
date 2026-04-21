package my.edu.utar.assignment_2_v2.Utils;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import my.edu.utar.assignment_2_v2.model.Assignment;
import my.edu.utar.assignment_2_v2.model.Mood;
import my.edu.utar.assignment_2_v2.model.User;

public class firebase {
    private static firebase instance;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    // Collection references
    private final CollectionReference usersCollection;
    private final CollectionReference moodLogsCollection;
    private final CollectionReference assignmentsCollection;

    private firebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        usersCollection = db.collection("users");
        moodLogsCollection = db.collection("moodLogs");
        assignmentsCollection = db.collection("assignments");
    }

    public static synchronized firebase getInstance() {
        if (instance == null) {
            instance = new firebase();
        }
        return instance;
    }

    // Authentication methods
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public void signOut() {
        mAuth.signOut();
    }

    // User operations
    public Task<Void> saveUser(User user) {
        return usersCollection.document(user.getUid()).set(user);
    }

    public Task<DocumentReference> saveMoodLog(Mood moodLog) {
        return moodLogsCollection.add(moodLog);
    }

    public Task<Void> updateMoodLog(String moodLogId, Mood moodLog) {
        return moodLogsCollection.document(moodLogId).set(moodLog);
    }

    public Task<Void> deleteMoodLog(String moodLogId) {
        return moodLogsCollection.document(moodLogId).delete();
    }

    public Query getUserMoodLogs(String userId) {
        return moodLogsCollection.whereEqualTo("userId", userId).orderBy("timestamp", Query.Direction.DESCENDING);
    }

    public Query getUserMoodLogsInRange(String userId, long startDate, long endDate) {
        return moodLogsCollection
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("timestamp", new java.util.Date(startDate))
                .whereLessThanOrEqualTo("timestamp", new java.util.Date(endDate))
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }

    // Assignment operations
    public Task<DocumentReference> saveAssignment(Assignment assignment) {
        return assignmentsCollection.add(assignment);
    }

    public Task<Void> updateAssignment(String assignmentId, Assignment assignment) {
        return assignmentsCollection.document(assignmentId).set(assignment);
    }

    public Task<Void> deleteAssignment(String assignmentId) {
        return assignmentsCollection.document(assignmentId).delete();
    }

    public Query getUserAssignments(String userId) {
        return assignmentsCollection.whereEqualTo("userId", userId).orderBy("dueDate", Query.Direction.ASCENDING);
    }

    public Query getUpcomingAssignments(String userId) {
        java.util.Date now = new java.util.Date();
        return assignmentsCollection
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("dueDate", now)
                .orderBy("dueDate", Query.Direction.ASCENDING);
    }

    // Analytics methods
    public QuerySnapshot getMoodAnalytics(String userId, int days) {
        long endDate = System.currentTimeMillis();
        long startDate = endDate - (days * 24 * 60 * 60 * 1000L);

        try {
            return getUserMoodLogsInRange(userId, startDate, endDate).get().getResult();
        } catch (Exception e) {
            return null;
        }
    }
}

