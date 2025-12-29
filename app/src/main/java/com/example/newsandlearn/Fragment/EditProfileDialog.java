package com.example.newsandlearn.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.newsandlearn.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileDialog extends DialogFragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "EditProfileDialog";

    private String currentUsername;
    private String currentEmail;

    private TextInputEditText editUsername, editEmail;
    private CircleImageView profileImageView;
    private MaterialButton btnSave, btnCancel, btnChangePhoto;
    private Uri imageUri;

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
        profileImageView = view.findViewById(R.id.profile_image_edit);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnChangePhoto = view.findViewById(R.id.btn_change_photo);

        editUsername.setText(currentUsername);
        editEmail.setText(currentEmail);

        loadAvatarImage();

        btnChangePhoto.setOnClickListener(v -> openImageChooser());
        btnSave.setOnClickListener(v -> saveProfile());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void loadAvatarImage() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(profileImageView);
        } else {
            Glide.with(this).load(R.drawable.ic_profile_placeholder).into(profileImageView);
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(profileImageView);
        }
    }

    private void saveProfile() {
        String newUsername = editUsername.getText().toString().trim();
        if (newUsername.isEmpty()) {
            Toast.makeText(getContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            uploadImageToFirebase();
        } else {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String existingPhotoUrl = user != null && user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
            updateUserInfo(existingPhotoUrl);
        }
    }

    private void uploadImageToFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Uploading...");

        String fileName = "profile_pictures/" + user.getUid() + "/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference fileReference = FirebaseStorage.getInstance().getReference(fileName);

        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        updateUserInfo(downloadUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Save");
                });
    }

    private void updateUserInfo(String photoUrl) {
        String newUsername = editUsername.getText().toString().trim();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        UserProfileChangeRequest.Builder profileUpdatesBuilder = new UserProfileChangeRequest.Builder()
                .setDisplayName(newUsername);

        if (photoUrl != null) {
            profileUpdatesBuilder.setPhotoUri(Uri.parse(photoUrl));
        }

        user.updateProfile(profileUpdatesBuilder.build()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                updateFirestoreUser(user.getUid(), newUsername, photoUrl);
            } else {
                Toast.makeText(getContext(), "Failed to update profile.", Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
                btnSave.setText("Save");
            }
        });
    }

    private void updateFirestoreUser(String userId, String username, String photoUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", editEmail.getText().toString().trim());

        if (photoUrl != null) {
            userData.put("avatarUrl", photoUrl);
        }
        userData.put("lastUpdated", System.currentTimeMillis());

        db.collection("users").document(userId)
                .set(userData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Auth updated, but failed to save to database.", Toast.LENGTH_LONG).show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Save");
                    dismiss();
                });
    }
}
