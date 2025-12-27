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

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private MaterialButton loginButton;
    private TextView registerLink, forgotPasswordLink;
    private LottieAnimationView progressBar;
    private FirebaseAuth mAuth;
    private CardView logoCard; // login_card contains all inputs now
    private View emailContainer, passwordContainer; // Changed from CardView to View/ViewGroup
    private CardView googleLoginBtn, facebookLoginBtn;
    private ImageView passwordToggle;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
        emailInput = findViewById(R.id.email_input_login);
        passwordInput = findViewById(R.id.password_input_login);
        loginButton = findViewById(R.id.login_button);
        registerLink = findViewById(R.id.register_link);
        forgotPasswordLink = findViewById(R.id.forgot_password_link);
        progressBar = findViewById(R.id.login_progress); // Changed ID to match XML
        passwordToggle = findViewById(R.id.password_toggle);
        
        logoCard = findViewById(R.id.logo_card);
        emailContainer = findViewById(R.id.email_container); // Updated ID
        passwordContainer = findViewById(R.id.password_container); // Updated ID
        // login_card is the main container, we might want to animate it too, but individual inputs are better for focus
        googleLoginBtn = findViewById(R.id.google_login_btn);
        facebookLoginBtn = findViewById(R.id.facebook_login_btn);
    }

    private void setupListeners() {
        // Login button with animation
        loginButton.setOnClickListener(v -> {
            AnimationHelper.buttonPress(this, v);
            v.postDelayed(() -> {
                AnimationHelper.buttonRelease(this, v);
                loginUser();
            }, 100);
        });

        // Register link
        registerLink.setOnClickListener(v -> {
            AnimationHelper.fadeOut(this, findViewById(android.R.id.content));
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Forgot password
        forgotPasswordLink.setOnClickListener(v -> {
            AnimationHelper.pulse(this, v);
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // Password toggle
        passwordToggle.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordToggle.setAlpha(1.0f);
            } else {
                passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordToggle.setAlpha(0.7f);
            }
            passwordInput.setSelection(passwordInput.getText().length());
            AnimationHelper.bounce(this, v);
        });

        // Social login buttons
        googleLoginBtn.setOnClickListener(v -> {
            AnimationHelper.scaleUp(this, v);
            Toast.makeText(this, "Google login coming soon", Toast.LENGTH_SHORT).show();
        });

        facebookLoginBtn.setOnClickListener(v -> {
            AnimationHelper.scaleUp(this, v);
            Toast.makeText(this, "Facebook login coming soon", Toast.LENGTH_SHORT).show();
        });

        // Input focus animations
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
    }

    private void animateEntrance() {
        // Staggered entrance animations
        AnimationHelper.itemFallDown(this, logoCard, 0);
        AnimationHelper.itemFallDown(this, findViewById(R.id.title_login), 1);
        AnimationHelper.itemFallDown(this, findViewById(R.id.subtitle_login), 2);
        AnimationHelper.itemFallDown(this, emailContainer, 3);
        AnimationHelper.itemFallDown(this, passwordContainer, 4);
        AnimationHelper.itemFallDown(this, loginButton, 5);
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validation with animations
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

        // Show progress
        loginButton.setEnabled(false);
        AnimationHelper.fadeIn(this, progressBar);
        AnimationHelper.rotate(this, progressBar);

        // Sign in with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    AnimationHelper.stopRotate(progressBar);
                    AnimationHelper.fadeOut(this, progressBar);

                    if (task.isSuccessful()) {
                        // Success animation
                        AnimationHelper.zoomInBounce(this, logoCard);
                        
                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                        // Navigate to MainActivity with animation
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    } else {
                        // Error animation
                        AnimationHelper.shake(emailContainer);
                        AnimationHelper.shake(passwordContainer);
                        AnimationHelper.wiggle(this, logoCard);
                        
                        String errorMsg = task.getException() != null ? task.getException().getMessage()
                                : "Lỗi không xác định";
                        Toast.makeText(this, "Đăng nhập thất bại: " + errorMsg, Toast.LENGTH_LONG).show();
                        loginButton.setEnabled(true);
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }
}
