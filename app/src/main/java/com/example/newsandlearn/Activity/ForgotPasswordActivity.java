package com.example.newsandlearn.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.AnimationHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailInput;
    private MaterialButton resetButton, backButton;
    private LottieAnimationView progressBar;
    private CardView logoCard;
    private View emailContainer;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews();

        // Setup listeners
        setupListeners();

        // Animate entrance
        animateEntrance();
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.email_input_forgot);
        resetButton = findViewById(R.id.reset_button);
        backButton = findViewById(R.id.back_button);
        progressBar = findViewById(R.id.forgot_progress);
        logoCard = findViewById(R.id.logo_card);
        emailContainer = findViewById(R.id.email_container);
    }

    private void setupListeners() {
        // Reset button with animation
        resetButton.setOnClickListener(v -> {
            AnimationHelper.buttonPress(this, v);
            v.postDelayed(() -> {
                AnimationHelper.buttonRelease(this, v);
                resetPassword();
            }, 100);
        });

        // Back button
        backButton.setOnClickListener(v -> {
            AnimationHelper.fadeOut(this, findViewById(android.R.id.content));
            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

        // Input focus animations
        emailInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                emailContainer.animate().scaleX(1.02f).scaleY(1.02f).setDuration(200).start();
            } else {
                emailContainer.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
            }
        });
    }

    private void animateEntrance() {
        AnimationHelper.itemFallDown(this, logoCard, 0);
        AnimationHelper.itemFallDown(this, findViewById(R.id.title_forgot), 1);
        AnimationHelper.itemFallDown(this, findViewById(R.id.subtitle_forgot), 2);
        AnimationHelper.itemFallDown(this, emailContainer, 3);
        AnimationHelper.itemFallDown(this, resetButton, 4);
        AnimationHelper.itemFallDown(this, backButton, 5);
    }

    private void resetPassword() {
        String email = emailInput.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Vui lòng nhập email");
            AnimationHelper.shake(emailContainer);
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Email không hợp lệ");
            AnimationHelper.shake(emailContainer);
            return;
        }

        // Show progress
        resetButton.setEnabled(false);
        AnimationHelper.fadeIn(this, progressBar);
        AnimationHelper.rotate(this, progressBar);

        // Send password reset email
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    AnimationHelper.stopRotate(progressBar);
                    AnimationHelper.fadeOut(this, progressBar);

                    if (task.isSuccessful()) {
                        // Success animation
                        AnimationHelper.zoomInBounce(this, logoCard);

                        Toast.makeText(this,
                                "Email đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra email của bạn!",
                                Toast.LENGTH_LONG).show();

                        // Clear input
                        emailInput.setText("");

                        // Navigate back to login after delay
                        emailInput.postDelayed(() -> {
                            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            finish();
                        }, 2000);
                    } else {
                        // Error animation
                        AnimationHelper.shake(emailContainer);
                        AnimationHelper.wiggle(this, logoCard);

                        String errorMsg = task.getException() != null ? task.getException().getMessage()
                                : "Lỗi không xác định";

                        // Handle specific Firebase error messages
                        if (errorMsg.contains("no user record")) {
                            Toast.makeText(this, "Email này chưa được đăng ký", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Lỗi: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                        resetButton.setEnabled(true);
                    }
                });
    }
}
