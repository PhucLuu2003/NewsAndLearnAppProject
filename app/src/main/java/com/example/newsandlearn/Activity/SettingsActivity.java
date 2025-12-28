package com.example.newsandlearn.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.FirebaseDataSeeder;
import com.example.newsandlearn.Utils.RoleManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * SettingsActivity - App settings and user profile
 */
public class SettingsActivity extends AppCompatActivity {

    private TextView userName, userLevel, devToolsHeader;
    private SwitchCompat notificationsSwitch, darkModeSwitch;
    private LinearLayout editProfile, changePassword, logoutButton;
    private Button adminPanelButton, seedDataButton, reseedVideosButton, seedLearnModulesButton, addAudioButton;

    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    private boolean isAdminUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeServices();
        initializeViews();
        setupListeners();
        loadUserData();

        // Hide Admin-only tools by default; enable only after role check.
        applyAdminVisibility(false);
        RoleManager.isCurrentUserAdmin(new RoleManager.RoleCheckCallback() {
            @Override
            public void onResult(boolean isAdmin) {
                isAdminUser = isAdmin;
                applyAdminVisibility(isAdmin);
            }

            @Override
            public void onError(String error) {
                isAdminUser = false;
                applyAdminVisibility(false);
            }
        });
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
        
        // Developer Tools (Admin only)
        devToolsHeader = findViewById(R.id.tv_developer_tools_header);
        seedDataButton = findViewById(R.id.seed_data_button);
        reseedVideosButton = findViewById(R.id.reseed_videos_button);
        adminPanelButton = findViewById(R.id.admin_panel_button);
        seedLearnModulesButton = findViewById(R.id.seed_learn_modules_button);
        addAudioButton = findViewById(R.id.add_audio_button);
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

        // Admin Panel button - Only admin feature
        adminPanelButton.setOnClickListener(v -> requireAdminThen(this::openAdminPanel));
    }

    private void requireAdminThen(Runnable action) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isAdminUser) {
            action.run();
            return;
        }

        // Fallback: re-check role (covers cases where local flag is stale)
        RoleManager.isCurrentUserAdmin(new RoleManager.RoleCheckCallback() {
            @Override
            public void onResult(boolean isAdmin) {
                isAdminUser = isAdmin;
                applyAdminVisibility(isAdmin);
                if (isAdmin) {
                    action.run();
                } else {
                    Toast.makeText(SettingsActivity.this, "Bạn không có quyền Admin", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                isAdminUser = false;
                applyAdminVisibility(false);
                Toast.makeText(SettingsActivity.this, "Không kiểm tra được quyền: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyAdminVisibility(boolean isAdmin) {
        runOnUiThread(() -> {
            int visibility = isAdmin ? android.view.View.VISIBLE : android.view.View.GONE;
            
            // Hide/show all developer tools
            if (devToolsHeader != null) devToolsHeader.setVisibility(visibility);
            if (seedDataButton != null) seedDataButton.setVisibility(visibility);
            if (reseedVideosButton != null) reseedVideosButton.setVisibility(visibility);
            if (adminPanelButton != null) adminPanelButton.setVisibility(visibility);
            if (seedLearnModulesButton != null) seedLearnModulesButton.setVisibility(visibility);
            if (addAudioButton != null) addAudioButton.setVisibility(visibility);
        });
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

    /**
     * Open Admin Panel Activity (New tabbed interface)
     * All data management and seeding functions are now in the Admin Panel
     */
    private void openAdminPanel() {
        Intent intent = new Intent(this, AdminPanelActivity.class);
        startActivity(intent);
    }
}
