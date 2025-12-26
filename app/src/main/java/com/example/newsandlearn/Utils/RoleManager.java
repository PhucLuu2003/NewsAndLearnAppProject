package com.example.newsandlearn.Utils;

import com.example.newsandlearn.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * RoleManager - Utility class to check user roles and permissions
 * Provides helper methods to verify if a user is admin or regular user
 */
public class RoleManager {

    private static final String TAG = "RoleManager";
    private static final String USERS_COLLECTION = "users";

    /**
     * Interface for role check callbacks
     */
    public interface RoleCheckCallback {
        void onResult(boolean isAdmin);

        void onError(String error);
    }

    /**
     * Check if current user is an admin
     */
    public static void isCurrentUserAdmin(RoleCheckCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection(USERS_COLLECTION)
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String role = document.getString("role");
                        boolean isAdmin = "admin".equals(role);
                        callback.onResult(isAdmin);
                    } else {
                        callback.onResult(false);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Check if a specific user is an admin
     */
    public static void isUserAdmin(String userId, RoleCheckCallback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onError("Invalid user ID");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String role = document.getString("role");
                        boolean isAdmin = "admin".equals(role);
                        callback.onResult(isAdmin);
                    } else {
                        callback.onResult(false);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Get the role of current user synchronously (use carefully, may block UI)
     * Prefer using async methods above when possible
     */
    public static String getCurrentUserRole(User user) {
        if (user == null) {
            return "user";
        }
        String role = user.getRole();
        return (role != null && !role.isEmpty()) ? role : "user";
    }

    /**
     * Check if a user object is admin
     */
    public static boolean isAdmin(User user) {
        return user != null && "admin".equals(user.getRole());
    }
}
