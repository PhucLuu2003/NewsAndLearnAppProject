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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * SettingsActivity - App settings and user profile
 */
public class SettingsActivity extends AppCompatActivity {

    private TextView userName, userLevel;
    private SwitchCompat notificationsSwitch, darkModeSwitch;
    private LinearLayout editProfile, changePassword, logoutButton;
    private Button seedDataButton, reseedVideosButton, adminPanelButton, seedLearnModulesButton, addAudioButton;

    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

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
        seedDataButton = findViewById(R.id.seed_data_button);
        reseedVideosButton = findViewById(R.id.reseed_videos_button);
        adminPanelButton = findViewById(R.id.admin_panel_button);
        seedLearnModulesButton = findViewById(R.id.seed_learn_modules_button);
        addAudioButton = findViewById(R.id.add_audio_button);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Seeding Data");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
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

        // Seed data button
        seedDataButton.setOnClickListener(v -> seedData());
        
        // Reseed videos button
        reseedVideosButton.setOnClickListener(v -> reseedVideoLessons());
        
        // Admin Panel button
        adminPanelButton.setOnClickListener(v -> openAdminPanel());
        
        // Seed Learn Modules button
        seedLearnModulesButton.setOnClickListener(v -> seedLearnModules());
        
        // Add Audio to Speaking Lessons button
        addAudioButton.setOnClickListener(v -> addAudioToSpeakingLessons());
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
     * Seed sample data to Firebase
     */
    private void seedData() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        FirebaseDataSeeder seeder = new FirebaseDataSeeder();
        seeder.seedAllData(new FirebaseDataSeeder.SeedCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(SettingsActivity.this,
                            "✅ " + message,
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onProgress(String message) {
                runOnUiThread(() -> {
                    progressDialog.setMessage(message);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(SettingsActivity.this,
                            "❌ Error: " + error,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    /**
     * Clear and reseed video lessons with direct MP4 URLs
     * This fixes the YouTube black screen issue
     */
    private void reseedVideoLessons() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setTitle("Fixing Video Lessons");
        progressDialog.setMessage("Clearing old videos...");
        progressDialog.show();

        FirebaseDataSeeder seeder = new FirebaseDataSeeder();
        seeder.seedVideoLessons(new FirebaseDataSeeder.SeedCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(SettingsActivity.this,
                            "✅ Video lessons fixed! " + message,
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onProgress(String message) {
                runOnUiThread(() -> {
                    progressDialog.setMessage(message);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(SettingsActivity.this,
                            "❌ Error: " + error,
                            Toast.LENGTH_LONG).show();
                });
            }
        }, true); // true = clear first
    }
    
    /**
     * Open Admin Panel Activity
     */
    private void openAdminPanel() {
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
    }
    
    /**
     * Seed Learn Modules data (Grammar, Listening, Speaking, Reading, Writing)
     */
    private void seedLearnModules() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setTitle("Seeding Learn Modules");
        progressDialog.setMessage("Creating lessons...");
        progressDialog.show();

        FirebaseDataSeeder seeder = new FirebaseDataSeeder();
        seeder.seedAllLearnModules(new FirebaseDataSeeder.SeedCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(SettingsActivity.this,
                            "✅ " + message,
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onProgress(String message) {
                runOnUiThread(() -> {
                    progressDialog.setMessage(message);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(SettingsActivity.this,
                            "❌ Error: " + error,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    /**
     * Add sample audio URLs to all speaking lessons
     */
    private void addAudioToSpeakingLessons() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setTitle("Adding Audio");
        progressDialog.setMessage("Adding sample audio to speaking lessons...");
        progressDialog.show();

        com.example.newsandlearn.Utils.SpeakingAudioUpdater.addAudioToSpeakingLessons(
            new com.example.newsandlearn.Utils.SpeakingAudioUpdater.UpdateCallback() {
                @Override
                public void onSuccess(int updatedCount) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this,
                                "✅ Added audio to " + updatedCount + " speaking lessons!",
                                Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this,
                                "❌ Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
                }
            });
    }
}
