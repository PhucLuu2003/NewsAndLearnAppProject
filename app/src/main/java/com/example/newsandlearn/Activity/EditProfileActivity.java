package com.example.newsandlearn.Activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.AnimationHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView profileImage, backButton;
    private TextView changePhotoText;
    private EditText nameInput, emailInput;
    private MaterialButton saveButton;
    private LottieAnimationView progressBar;
    private CardView profileImageCard;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private Uri selectedImageUri;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize views
        initializeViews();

        // Setup image picker
        setupImagePicker();

        // Setup listeners
        setupListeners();

        // Load current user data
        loadUserData();

        // Animate entrance
        animateEntrance();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        profileImage = findViewById(R.id.profile_image);
        profileImageCard = findViewById(R.id.profile_image_card);
        changePhotoText = findViewById(R.id.change_photo_text);
        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        saveButton = findViewById(R.id.save_button);
        progressBar = findViewById(R.id.edit_profile_progress);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // Display selected image
                            Glide.with(this)
                                    .load(selectedImageUri)
                                    .circleCrop()
                                    .into(profileImage);
                            AnimationHelper.zoomInBounce(this, profileImageCard);
                        }
                    }
                });
    }

    private void setupListeners() {
        // Back button
        backButton.setOnClickListener(v -> {
            AnimationHelper.buttonPress(this, v);
            finish();
        });

        // Change photo
        View.OnClickListener changePhotoListener = v -> {
            AnimationHelper.pulse(this, profileImageCard);
            openImagePicker();
        };
        profileImageCard.setOnClickListener(changePhotoListener);
        changePhotoText.setOnClickListener(changePhotoListener);

        // Save button
        saveButton.setOnClickListener(v -> {
            AnimationHelper.buttonPress(this, v);
            v.postDelayed(() -> {
                AnimationHelper.buttonRelease(this, v);
                saveProfile();
            }, 100);
        });
    }

    private void animateEntrance() {
        AnimationHelper.itemFallDown(this, profileImageCard, 0);
        AnimationHelper.itemFallDown(this, findViewById(R.id.name_container), 1);
        AnimationHelper.itemFallDown(this, findViewById(R.id.email_container), 2);
        AnimationHelper.itemFallDown(this, saveButton, 3);
    }

    private void loadUserData() {
        if (currentUser != null) {
            // Load name
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                nameInput.setText(displayName);
            }

            // Load email
            String email = currentUser.getEmail();
            if (email != null && !email.isEmpty()) {
                emailInput.setText(email);
                emailInput.setEnabled(false); // Email cannot be changed easily
            }

            // Load profile image
            Uri photoUrl = currentUser.getPhotoUrl();
            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile)
                        .into(profileImage);
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void saveProfile() {
        String newName = nameInput.getText().toString().trim();

        // Validate name
        if (TextUtils.isEmpty(newName)) {
            nameInput.setError("Vui lòng nhập tên");
            AnimationHelper.shake(findViewById(R.id.name_container));
            return;
        }

        // Show progress
        saveButton.setEnabled(false);
        AnimationHelper.fadeIn(this, progressBar);
        AnimationHelper.rotate(this, progressBar);

        // Update profile with name and selected image URI (local)
        updateUserProfile(newName, selectedImageUri);
    }

    private void updateUserProfile(String newName, Uri photoUri) {
        if (currentUser == null)
            return;

        // Build profile update request
        UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName);

        if (photoUri != null) {
            profileUpdates.setPhotoUri(photoUri);
        }

        // Update Firebase Auth profile
        currentUser.updateProfile(profileUpdates.build())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Also update Firestore user document
                        updateFirestoreUserData(newName, photoUri);
                    } else {
                        AnimationHelper.stopRotate(progressBar);
                        AnimationHelper.fadeOut(this, progressBar);
                        saveButton.setEnabled(true);
                        Toast.makeText(this, "Lỗi cập nhật profile: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateFirestoreUserData(String newName, Uri photoUri) {
        if (currentUser == null)
            return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("displayName", newName);
        // Không lưu photoUrl vào Firestore, chỉ lưu local
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection("users")
                .document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    AnimationHelper.stopRotate(progressBar);
                    AnimationHelper.fadeOut(this, progressBar);

                    // Success animation
                    AnimationHelper.zoomInBounce(this, profileImageCard);
                    Toast.makeText(this, "Cập nhật profile thành công!", Toast.LENGTH_SHORT).show();

                    // Return to previous screen after delay
                    saveButton.postDelayed(() -> finish(), 1000);
                })
                .addOnFailureListener(e -> {
                    // Even if Firestore update fails, Auth update succeeded
                    AnimationHelper.stopRotate(progressBar);
                    AnimationHelper.fadeOut(this, progressBar);
                    Toast.makeText(this, "Profile đã cập nhật (cảnh báo: lỗi Firestore)",
                            Toast.LENGTH_SHORT).show();
                    saveButton.postDelayed(() -> finish(), 1000);
                });
    }
}
