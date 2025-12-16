package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; // Added for the back link
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newsandlearn.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmailInput;
    private Button btnResetPassword;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmailInput = findViewById(R.id.etEmailInput);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePasswordReset();
            }
        });

        // Manual go back option
        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Closes this activity and returns to Login
            }
        });
    }

    private void handlePasswordReset() {
        String email = etEmailInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        String message = "Reset link sent to " + email;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // Delay finish slightly or call it immediately to return to Login
        finish();
    }
}