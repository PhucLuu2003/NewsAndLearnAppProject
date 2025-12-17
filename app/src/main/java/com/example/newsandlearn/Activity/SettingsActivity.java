package com.example.newsandlearn.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.example.newsandlearn.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * SettingsActivity - App settings and user profile
 */
public class SettingsActivity extends AppCompatActivity {

    private TextView userName, userLevel;
    private SwitchCompat notificationsSwitch, darkModeSwitch;
    private LinearLayout editProfile, changePassword, logoutButton;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeServices();
        initializeViews();
        setupListeners();
        loadUserData();
    }

    private void initializeServices() {
        auth = FirebaseAuth.getInstance();
    }

    private void initializeViews() {
        userName = findViewById(R.id.user_name);
        userLevel = findViewById(R.id.user_level);
        notificationsSwitch = findViewById(R.id.notifications_switch);
        darkModeSwitch = findViewById(R.id.dark_mode_switch);
        editProfile = findViewById(R.id.edit_profile);
        changePassword = findViewById(R.id.change_password);
        logoutButton = findViewById(R.id.logout_button);
    }

    private void setupListeners() {
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Enable/disable notifications
            Toast.makeText(this, "Notifications " + (isChecked ? "enabled" : "disabled"), 
                    Toast.LENGTH_SHORT).show();
        });

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        editProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Edit profile feature coming soon", Toast.LENGTH_SHORT).show();
        });

        changePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Change password feature coming soon", Toast.LENGTH_SHORT).show();
        });

        logoutButton.setOnClickListener(v -> logout());
    }

    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
            // TODO: Load level and XP from Firebase
            userLevel.setText("Level 5 • 1,250 XP");
        }
    }

    private void logout() {
        auth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        // TODO: Navigate to login screen
        finish();
    }
}
