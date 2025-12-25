package com.example.newsandlearn.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import com.airbnb.lottie.LottieAnimationView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.AnimationHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameInput, emailInput;
    private EditText passwordInput, confirmPasswordInput;
    private MaterialButton registerButton;
    private TextView loginLink;
    private ImageView backButton, passwordToggle;
    private LottieAnimationView progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CardView logoCard; // register_card wraps all
    private View usernameContainer, emailContainer, passwordContainer, confirmContainer; // Changed from CardView to View
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();
        setupListeners();
        animateEntrance();
    }

    private void initializeViews() {
        usernameInput = findViewById(R.id.username_input_register);
        emailInput = findViewById(R.id.email_input_register);
        passwordInput = findViewById(R.id.password_input_register);
        confirmPasswordInput = findViewById(R.id.confirm_password_input_register);
        registerButton = findViewById(R.id.register_button);
        loginLink = findViewById(R.id.login_link);
        backButton = findViewById(R.id.back_button);
        progressBar = findViewById(R.id.register_progress); // Updated ID
        passwordToggle = findViewById(R.id.password_toggle);
        
        logoCard = findViewById(R.id.logo_card);
        usernameContainer = findViewById(R.id.username_container); // Updated ID
        emailContainer = findViewById(R.id.email_container); // Updated ID
        passwordContainer = findViewById(R.id.password_container); // Updated ID
        confirmContainer = findViewById(R.id.confirm_password_container); // Updated ID
    }

    private void setupListeners() {
        // Register button with animation
        registerButton.setOnClickListener(v -> {
            AnimationHelper.buttonPress(this, v);
            v.postDelayed(() -> {
                AnimationHelper.buttonRelease(this, v);
                registerUser();
            }, 100);
        });

        // Login link
        loginLink.setOnClickListener(v -> {
            AnimationHelper.fadeOut(this, findViewById(android.R.id.content));
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        // Back button
        backButton.setOnClickListener(v -> {
            AnimationHelper.scaleDown(this, v);
            finish();
        });

        // Password toggle
        passwordToggle.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                confirmPasswordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordToggle.setAlpha(1.0f);
            } else {
                passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                confirmPasswordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordToggle.setAlpha(0.7f);
            }
            passwordInput.setSelection(passwordInput.getText().length());
            confirmPasswordInput.setSelection(confirmPasswordInput.getText().length());
            AnimationHelper.bounce(this, v);
        });

        // Input focus animations
        usernameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                usernameContainer.animate().scaleX(1.02f).scaleY(1.02f).setDuration(200).start();
            } else {
                usernameContainer.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
            }
        });

        emailInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                emailContainer.animate().scaleX(1.02f).scaleY(1.02f).setDuration(200).start();
            } else {
                emailContainer.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
            }
        });

        passwordInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                passwordContainer.animate().scaleX(1.02f).scaleY(1.02f).setDuration(200).start();
            } else {
                passwordContainer.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
            }
        });

        confirmPasswordInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                confirmContainer.animate().scaleX(1.02f).scaleY(1.02f).setDuration(200).start();
            } else {
                confirmContainer.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
            }
        });
    }

    private void animateEntrance() {
        AnimationHelper.itemFallDown(this, logoCard, 0);
        AnimationHelper.itemFallDown(this, findViewById(R.id.title_register), 1);
        AnimationHelper.itemFallDown(this, findViewById(R.id.subtitle_register), 2);
        AnimationHelper.itemFallDown(this, usernameContainer, 3);
        AnimationHelper.itemFallDown(this, emailContainer, 4);
        AnimationHelper.itemFallDown(this, passwordContainer, 5);
        AnimationHelper.itemFallDown(this, confirmContainer, 6);
        AnimationHelper.itemFallDown(this, registerButton, 7);
    }

    private void registerUser() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validation with animations
        if (TextUtils.isEmpty(username)) {
            usernameInput.setError("Vui lòng nhập tên người dùng");
            AnimationHelper.shake(usernameContainer);
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Vui lòng nhập email");
            AnimationHelper.shake(emailContainer);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Vui lòng nhập mật khẩu");
            AnimationHelper.shake(passwordContainer);
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Mật khẩu phải ít nhất 6 ký tự");
            AnimationHelper.shake(passwordContainer);
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Mật khẩu không khớp");
            AnimationHelper.shake(confirmContainer);
            return;
        }

        // Show progress
        registerButton.setEnabled(false);
        AnimationHelper.fadeIn(this, progressBar);
        AnimationHelper.rotate(this, progressBar);

        // Create user with Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    AnimationHelper.stopRotate(progressBar);
                    AnimationHelper.fadeOut(this, progressBar);

                    if (task.isSuccessful()) {
                        // Success animation
                        AnimationHelper.zoomInBounce(this, logoCard);
                        
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), username, email);
                        }
                    } else {
                        // Error animations
                        AnimationHelper.shake(emailContainer);
                        AnimationHelper.shake(passwordContainer);
                        AnimationHelper.wiggle(this, logoCard);
                        
                        String errorMsg = task.getException() != null ? task.getException().getMessage()
                                : "Lỗi không xác định";
                        Toast.makeText(this, "Đăng ký thất bại: " + errorMsg, Toast.LENGTH_LONG).show();
                        registerButton.setEnabled(true);
                    }
                });
    }

    private void saveUserToFirestore(String userId, String username, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("username", username);
        user.put("level", "A1");
        user.put("streak", 0);
        user.put("currentStreak", 0);  // For HomeFragment
        user.put("totalXP", 0);         // For HomeFragment leaderboard
        user.put("totalDays", 0);
        user.put("createdAt", System.currentTimeMillis());
        user.put("lastLoginDate", System.currentTimeMillis());

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show();

                    // Sign out user and navigate to LoginActivity
                    mAuth.signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.putExtra("registered_email", email);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    registerButton.setEnabled(true);
                });
    }
}
