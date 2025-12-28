package com.example.newsandlearn.Fragment.Admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newsandlearn.Adapter.AdminUserAdapter;
import com.example.newsandlearn.Model.User;
import com.example.newsandlearn.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminUsersFragment - Manage users (view, edit roles, delete)
 */
public class AdminUsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private AdminUserAdapter adapter;
    private List<User> users;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_users, container, false);
        
        db = FirebaseFirestore.getInstance();
        initializeViews(view);
        loadUsers();
        
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.users_recycler_view);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        
        users = new ArrayList<>();
        adapter = new AdminUserAdapter(getContext(), users, this::onUserAction);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
        swipeRefresh.setOnRefreshListener(this::loadUsers);
        swipeRefresh.setColorSchemeResources(R.color.primary);
    }

    private void loadUsers() {
        swipeRefresh.setRefreshing(true);
        
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    users.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        // Set userId from document ID if not already set
                        if (user.getUserId() == null || user.getUserId().isEmpty()) {
                            user.setUserId(document.getId());
                        }
                        users.add(user);
                    }
                    adapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading users: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
                });
    }

    private void onUserAction(User user, String action) {
        switch (action) {
            case "edit_role":
                showRoleDialog(user);
                break;
            case "delete":
                deleteUser(user);
                break;
            case "view_details":
                showUserDetails(user);
                break;
        }
    }

    private void showRoleDialog(User user) {
        String[] roles = {"user", "admin"};
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Change Role for " + user.getName())
                .setItems(roles, (dialog, which) -> {
                    String newRole = roles[which];
                    updateUserRole(user, newRole);
                })
                .show();
    }

    private void updateUserRole(User user, String newRole) {
        if (user.getUserId() == null || user.getUserId().isEmpty()) {
            Toast.makeText(getContext(), "Error: User ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        
        db.collection("users")
                .document(user.getUserId())
                .update("role", newRole)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Role updated to " + newRole, 
                        Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error updating role: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteUser(User user) {
        if (user.getUserId() == null || user.getUserId().isEmpty()) {
            Toast.makeText(getContext(), "Error: User ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + user.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("users")
                            .document(user.getUserId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "User deleted", 
                                    Toast.LENGTH_SHORT).show();
                                loadUsers();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error deleting user: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showUserDetails(User user) {
        String details = "Name: " + user.getName() + "\n" +
                        "Email: " + user.getEmail() + "\n" +
                        "Role: " + user.getRole() + "\n" +
                        "Level: " + user.getLevel() + "\n" +
                        "XP: " + user.getXp();
        
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("User Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }
}
