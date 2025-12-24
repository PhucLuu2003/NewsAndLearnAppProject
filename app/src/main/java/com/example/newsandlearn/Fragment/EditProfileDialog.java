package com.example.newsandlearn.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.newsandlearn.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * EditProfileDialog - Dialog to edit user name and email
 */
public class EditProfileDialog extends DialogFragment {

    private String currentUsername;
    private String currentEmail;
    private TextInputEditText editUsername, editEmail;
    private MaterialButton btnSave, btnCancel;

    public static EditProfileDialog newInstance(String username, String email) {
        EditProfileDialog dialog = new EditProfileDialog();
        Bundle args = new Bundle();
        args.putString("username", username);
        args.putString("email", email);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentUsername = getArguments().getString("username", "");
            currentEmail = getArguments().getString("email", "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editUsername = view.findViewById(R.id.edit_name);
        editEmail = view.findViewById(R.id.edit_email);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);

        // Set current values
        editUsername.setText(currentUsername);
        editEmail.setText(currentEmail);

        // Setup listeners
        btnSave.setOnClickListener(v -> saveProfile());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void saveProfile() {
        String newUsername = editUsername.getText().toString().trim();
        String newEmail = editEmail.getText().toString().trim();

        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(getContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (user == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", newUsername);
        updates.put("email", newEmail);

        db.collection("users").document(user.getUid()).update(updates)
            .addOnSuccessListener(aVoid -> {
                // Update Firebase Auth display name
                user.updateProfile(
                    new UserProfileChangeRequest.Builder()
                        .setDisplayName(newUsername)
                        .build()
                ).addOnSuccessListener(task -> {
                    Toast.makeText(getContext(), "✅ Profile updated!", Toast.LENGTH_SHORT).show();
                    dismiss();
                }).addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
