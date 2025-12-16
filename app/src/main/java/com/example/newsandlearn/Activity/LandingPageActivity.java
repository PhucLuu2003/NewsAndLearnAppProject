package com.example.newsandlearn.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newsandlearn.R;

public class LandingPageActivity extends AppCompatActivity {

    private Button btnStart;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnStart = findViewById(R.id.start_button);
        btnLogin = findViewById(R.id.login_button);
    }

    private void setupClickListeners() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LandingPageActivity.this, AuthOnboardingActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LandingPageActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}