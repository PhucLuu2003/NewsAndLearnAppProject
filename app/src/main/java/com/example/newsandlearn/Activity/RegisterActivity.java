package com.example.newsandlearn.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newsandlearn.R;
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
    private ImageView backArrow;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        usernameInput = findViewById(R.id.username_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        registerButton = findViewById(R.id.confirm_button);
        loginLink = findViewById(R.id.login_link);
        backArrow = findViewById(R.id.back_arrow);
        progressBar = findViewById(R.id.progress_bar);

        // Set click listeners
        registerButton.setOnClickListener(v -> registerUser());

        loginLink.setOnClickListener(v -> {
            finish(); // Go back to login
        });

        backArrow.setOnClickListener(v -> {
            finish();
        });
    }

    private void registerUser() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(username)) {
            usernameInput.setError("Vui lòng nhập tên người dùng");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Vui lòng nhập email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Vui lòng nhập mật khẩu");
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Mật khẩu phải ít nhất 6 ký tự");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Mật khẩu không khớp");
            return;
        }

        // Show progress
        registerButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // Create user with Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        // Registration success
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save user data to Firestore
                            saveUserToFirestore(user.getUid(), username, email);
                        }
                    } else {
                        // Registration failed
                        String errorMsg = task.getException() != null ? task.getException().getMessage()
                                : "Lỗi không xác định";
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + errorMsg, Toast.LENGTH_LONG)
                                .show();
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
        user.put("totalDays", 0);
        user.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_SHORT)
                            .show();

                    // Sign out user and navigate to LoginActivity
                    mAuth.signOut();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("registered_email", email);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                    registerButton.setEnabled(true);
                });
    }
}
