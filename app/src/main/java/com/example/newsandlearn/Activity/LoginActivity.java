package com.example.newsandlearn.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.newsandlearn.R;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView gotoregisterTv,forgotPasswordBtn, registerBtn;
    private ImageButton googleBtn;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();

        // 1. Forgot Password Click
        forgotPasswordBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
        // 2. Register Click
        registerBtn.setOnClickListener(v -> {
             Intent intent = new Intent(LoginActivity.this, AuthOnboardingActivity.class);
             startActivity(intent);
        });

        // 3. Login Button Click
        loginButton.setOnClickListener(v -> handleLogin());
    }

    private void initViews() {
        emailInput = findViewById(R.id.email_input_login);
        passwordInput = findViewById(R.id.password_input_login);
        loginButton = findViewById(R.id.login_button);
        forgotPasswordBtn = findViewById(R.id.forgot_password_text);
        registerBtn = findViewById(R.id.register_text);
        googleBtn = findViewById(R.id.google_login_button);

        // Note: You might want to add a ProgressBar to your XML
        // for better UX during network calls.
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // --- Logic Check 1: Null/Empty Check ---
        if (TextUtils.isEmpty(email)) {
            showErrorDialog("Lỗi nhập liệu", "Vui lòng nhập địa chỉ email.");
            emailInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showErrorDialog("Lỗi nhập liệu", "Định dạng email không hợp lệ.");
            emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            showErrorDialog("Lỗi nhập liệu", "Vui lòng nhập mật khẩu.");
            passwordInput.requestFocus();
            return;
        }

        // Show loading state (Optional: Disable button to prevent double clicks)
        loginButton.setEnabled(false);

        // --- Logic Check 2: Firebase Authentication ---
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Auth success, now check Firestore
                        checkFirestoreUser();
                    } else {
                        loginButton.setEnabled(true);
                        handleAuthErrors(task.getException());
                    }
                });
    }

    private void checkFirestoreUser() {
        String uid = mAuth.getCurrentUser().getUid();

        // --- Logic Check 3: Check if account exists in Firestore ---
        db.collection("Users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                         Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                         startActivity(intent);
                         finish();
                    } else {
                        mAuth.signOut();
                        showErrorDialog("Lỗi dữ liệu", "Tài khoản không tồn tại trong hệ thống dữ liệu.");
                        loginButton.setEnabled(true);
                    }
                })
                .addOnFailureListener(e -> {
                    loginButton.setEnabled(true);
                    showErrorDialog("Lỗi kết nối", "Không thể truy cập máy chủ: " + e.getMessage());
                });
    }

    private void handleAuthErrors(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            showErrorDialog("Tài khoản không tồn tại", "Email này chưa được đăng ký.");
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            showErrorDialog("Sai thông tin", "Mật khẩu không chính xác.");
        } else {
            showErrorDialog("Lỗi hệ thống", "Đã xảy ra lỗi: " + exception.getMessage());
        }
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}