package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.FirebaseDataSeeder;
import com.example.newsandlearn.Utils.GameDataSeeder;
import com.example.newsandlearn.Utils.RoleManager;
import com.example.newsandlearn.Utils.VideoUrlUpdater;
import com.google.android.material.button.MaterialButton;

/**
 * Admin Activity for data seeding and management
 */
public class AdminActivity extends AppCompatActivity {

    private MaterialButton btnSeedAll, btnSeedArticles, btnSeedVideos, btnSeedVocab, btnSeedGame, btnSeedLearnModules,
            btnUpdateVideoUrls;
    private MaterialButton btnManageFirestore;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private ScrollView scrollView;

    private FirebaseDataSeeder seeder;
    private VideoUrlUpdater videoUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        seeder = new FirebaseDataSeeder();
        videoUpdater = new VideoUrlUpdater();

        initializeViews();
        setupListeners();

        // Hard gate: only admin can access this activity.
        showProgress(true);
        RoleManager.isCurrentUserAdmin(new RoleManager.RoleCheckCallback() {
            @Override
            public void onResult(boolean isAdmin) {
                if (!isAdmin) {
                    Toast.makeText(AdminActivity.this, "Bạn không có quyền Admin", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                showProgress(false);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AdminActivity.this, "Không kiểm tra được quyền: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void initializeViews() {
        btnSeedAll = findViewById(R.id.btn_seed_all);
        btnSeedArticles = findViewById(R.id.btn_seed_articles);
        btnSeedVideos = findViewById(R.id.btn_seed_videos);
        btnSeedVocab = findViewById(R.id.btn_seed_vocab);
        btnSeedGame = findViewById(R.id.btn_seed_game);
        btnSeedLearnModules = findViewById(R.id.btn_seed_learn_modules);
        btnUpdateVideoUrls = findViewById(R.id.btn_update_video_urls);
        btnManageFirestore = findViewById(R.id.btn_manage_firestore);
        progressBar = findViewById(R.id.progress_bar);
        tvStatus = findViewById(R.id.tv_status);
        scrollView = findViewById(R.id.scroll_view);
    }

    private void setupListeners() {
        btnSeedAll.setOnClickListener(v -> seedAllData());
        btnSeedArticles.setOnClickListener(v -> seedArticles());
        btnSeedVideos.setOnClickListener(v -> seedVideos());
        btnSeedVocab.setOnClickListener(v -> seedVocabularies());
        btnSeedGame.setOnClickListener(v -> seedGameData());
        btnSeedLearnModules.setOnClickListener(v -> seedLearnModules());
        btnUpdateVideoUrls.setOnClickListener(v -> updateVideoUrls());

        btnManageFirestore.setOnClickListener(v -> {
            startActivity(new android.content.Intent(AdminActivity.this, AdminFirestoreActivity.class));
        });
    }

    private void seedAllData() {
        showProgress(true);
        appendStatus("🚀 Bắt đầu seed tất cả dữ liệu...\n");

        seeder.seedAllData(new FirebaseDataSeeder.SeedCallback() {
            @Override
            public void onSuccess(String message) {
                appendStatus("✅ " + message + "\n");
                showProgress(false);
                Toast.makeText(AdminActivity.this, "Hoàn thành!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(String message) {
                appendStatus("⏳ " + message + "\n");
            }

            @Override
            public void onFailure(String error) {
                appendStatus("❌ Lỗi: " + error + "\n");
                showProgress(false);
                Toast.makeText(AdminActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void seedArticles() {
        showProgress(true);
        appendStatus("📰 Đang seed articles...\n");

        seeder.seedArticles(new FirebaseDataSeeder.SeedCallback() {
            @Override
            public void onSuccess(String message) {
                appendStatus("✅ " + message + "\n");
                showProgress(false);
            }

            @Override
            public void onProgress(String message) {
                appendStatus("⏳ " + message + "\n");
            }

            @Override
            public void onFailure(String error) {
                appendStatus("❌ Lỗi: " + error + "\n");
                showProgress(false);
            }
        });
    }

    private void seedVideos() {
        showProgress(true);
        appendStatus("🎥 Đang seed video lessons...\n");

        seeder.seedVideoLessons(new FirebaseDataSeeder.SeedCallback() {
            @Override
            public void onSuccess(String message) {
                appendStatus("✅ " + message + "\n");
                showProgress(false);
            }

            @Override
            public void onProgress(String message) {
                appendStatus("⏳ " + message + "\n");
            }

            @Override
            public void onFailure(String error) {
                appendStatus("❌ Lỗi: " + error + "\n");
                showProgress(false);
            }
        });
    }

    private void seedVocabularies() {
        showProgress(true);
        appendStatus("📚 Đang seed vocabularies...\n");

        seeder.seedVocabularies(new FirebaseDataSeeder.SeedCallback() {
            @Override
            public void onSuccess(String message) {
                appendStatus("✅ " + message + "\n");
                showProgress(false);
            }

            @Override
            public void onProgress(String message) {
                appendStatus("⏳ " + message + "\n");
            }

            @Override
            public void onFailure(String error) {
                appendStatus("❌ Lỗi: " + error + "\n");
                showProgress(false);
            }
        });
    }

    private void seedGameData() {
        showProgress(true);
        appendStatus("🎮 Đang seed game data (questions & levels)...\n");

        GameDataSeeder.seedAllGameData(new GameDataSeeder.OnCompleteListener() {
            @Override
            public void onSuccess() {
                appendStatus("✅ Seed game data thành công!\n");
                showProgress(false);
                Toast.makeText(AdminActivity.this, "Game data đã sẵn sàng!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                appendStatus("❌ Lỗi seed game data: " + e.getMessage() + "\n");
                showProgress(false);
                Toast.makeText(AdminActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void seedLearnModules() {
        showProgress(true);
        appendStatus("📚 Đang seed Learn modules (Grammar, Listening, Speaking, Reading, Writing)...\n");

        seeder.seedAllLearnModules(new FirebaseDataSeeder.SeedCallback() {
            @Override
            public void onSuccess(String message) {
                appendStatus("✅ " + message + "\n");
                showProgress(false);
                Toast.makeText(AdminActivity.this, "Learn modules đã sẵn sàng!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(String message) {
                appendStatus("⏳ " + message + "\n");
            }

            @Override
            public void onFailure(String error) {
                appendStatus("❌ Lỗi: " + error + "\n");
                showProgress(false);
                Toast.makeText(AdminActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateVideoUrls() {
        showProgress(true);
        appendStatus("🔄 Đang cập nhật video URLs sang MP4...\n");

        videoUpdater.updateAllVideosToMp4(new VideoUrlUpdater.UpdateCallback() {
            @Override
            public void onSuccess(String message) {
                appendStatus("✅ " + message + "\n");
                showProgress(false);
                Toast.makeText(AdminActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(String message) {
                appendStatus("⏳ " + message + "\n");
            }

            @Override
            public void onFailure(String error) {
                appendStatus("❌ Lỗi: " + error + "\n");
                showProgress(false);
                Toast.makeText(AdminActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);

        // Disable all buttons while processing
        btnSeedAll.setEnabled(!show);
        btnSeedArticles.setEnabled(!show);
        btnSeedVideos.setEnabled(!show);
        btnSeedVocab.setEnabled(!show);
        btnSeedGame.setEnabled(!show);
        btnSeedLearnModules.setEnabled(!show);
        btnUpdateVideoUrls.setEnabled(!show);
        if (btnManageFirestore != null) {
            btnManageFirestore.setEnabled(!show);
        }
    }

    private void appendStatus(String message) {
        runOnUiThread(() -> {
            tvStatus.append(message);
            // Auto scroll to bottom
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        });
    }
}
