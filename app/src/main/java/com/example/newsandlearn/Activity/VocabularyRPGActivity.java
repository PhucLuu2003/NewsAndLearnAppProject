package com.example.newsandlearn.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newsandlearn.Adapter.GameLevelAdapter;
import com.example.newsandlearn.Model.GameCharacter;
import com.example.newsandlearn.Model.GameLevel;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.GameDataSeeder;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * VocabularyRPGActivity - Full screen RPG game
 */
public class VocabularyRPGActivity extends AppCompatActivity implements GameLevelAdapter.OnLevelClickListener {

    private SwipeRefreshLayout swipeRefresh;
    private MaterialButton btnSeedData;
    private CircleImageView characterAvatar;
    private TextView characterName, characterLevel;
    private TextView hpText, expText;
    private ProgressBar hpBar, expBar;
    private TextView attackValue, defenseValue;
    private RecyclerView levelsRecycler;
    
    private GameLevelAdapter adapter;
    private List<GameLevel> levels;
    private GameCharacter playerCharacter;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_rpg);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews();
        setupRecyclerView();
        
        // Load data
        loadPlayerCharacter();
        loadLevels();
    }

    private void initializeViews() {
        swipeRefresh = findViewById(R.id.swipe_refresh);
        btnSeedData = findViewById(R.id.btn_seed_data);
        characterAvatar = findViewById(R.id.character_avatar);
        characterName = findViewById(R.id.character_name);
        characterLevel = findViewById(R.id.character_level);
        hpText = findViewById(R.id.hp_text);
        expText = findViewById(R.id.exp_text);
        hpBar = findViewById(R.id.hp_bar);
        expBar = findViewById(R.id.exp_bar);
        attackValue = findViewById(R.id.attack_value);
        defenseValue = findViewById(R.id.defense_value);
        levelsRecycler = findViewById(R.id.levels_recycler);

        swipeRefresh.setOnRefreshListener(() -> {
            loadPlayerCharacter();
            loadLevels();
        });
        swipeRefresh.setColorSchemeResources(R.color.primary);

        // Seed Data Button
        btnSeedData.setOnClickListener(v -> seedGameData());
        
        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        levels = new ArrayList<>();
        adapter = new GameLevelAdapter(this, this);
        levelsRecycler.setLayoutManager(new LinearLayoutManager(this));
        levelsRecycler.setAdapter(adapter);
    }

    private void seedGameData() {
        btnSeedData.setEnabled(false);
        btnSeedData.setText("⏳ Seeding...");
        
        GameDataSeeder.seedAllGameData(new GameDataSeeder.OnCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(VocabularyRPGActivity.this, "✅ Game data seeded successfully!", Toast.LENGTH_SHORT).show();
                btnSeedData.setText("✅ Data Seeded!");
                btnSeedData.setEnabled(true);
                
                // Reload data
                loadLevels();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(VocabularyRPGActivity.this, "❌ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                btnSeedData.setText("🎲 Seed Game Data");
                btnSeedData.setEnabled(true);
            }
        });
    }

    private void loadPlayerCharacter() {
        if (auth.getCurrentUser() == null) {
            createDefaultCharacter();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("game_character")
                .document("main")
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        playerCharacter = document.toObject(GameCharacter.class);
                        updateCharacterUI();
                    } else {
                        createDefaultCharacter();
                    }
                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading character", Toast.LENGTH_SHORT).show();
                    createDefaultCharacter();
                    swipeRefresh.setRefreshing(false);
                });
    }

    private void createDefaultCharacter() {
        playerCharacter = new GameCharacter("main", "Hero", "warrior");
        characterName.setText(playerCharacter.getName());
        updateCharacterUI();
        saveCharacter();
    }

    private void updateCharacterUI() {
        if (playerCharacter == null) return;

        characterName.setText(playerCharacter.getName());
        characterLevel.setText("Level " + playerCharacter.getLevel());
        
        // HP
        hpText.setText("❤️ HP: " + playerCharacter.getCurrentHp() + "/" + playerCharacter.getMaxHp());
        hpBar.setMax(playerCharacter.getMaxHp());
        hpBar.setProgress(playerCharacter.getCurrentHp());
        
        // EXP
        expText.setText("⚡ EXP: " + playerCharacter.getCurrentExp() + "/" + playerCharacter.getMaxExp());
        expBar.setMax(playerCharacter.getMaxExp());
        expBar.setProgress(playerCharacter.getCurrentExp());
        
        // Stats
        attackValue.setText(String.valueOf(playerCharacter.getAttack()));
        defenseValue.setText(String.valueOf(playerCharacter.getDefense()));
    }

    private void loadLevels() {
        db.collection("game_levels")
                .orderBy("levelNumber")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    levels.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        GameLevel level = document.toObject(GameLevel.class);
                        
                        // Check if level should be unlocked based on player level
                        if (playerCharacter != null && 
                            playerCharacter.getLevel() >= level.getRequiredLevel()) {
                            level.setUnlocked(true);
                        }
                        
                        levels.add(level);
                    }
                    
                    adapter.setLevels(levels);
                    swipeRefresh.setRefreshing(false);
                    
                    if (levels.isEmpty()) {
                        Toast.makeText(this, "No levels found. Please seed data.", 
                            Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading levels: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
                });
    }

    @Override
    public void onLevelClick(GameLevel level) {
        if (!level.isUnlocked()) {
            Toast.makeText(this, "Level locked! Reach level " + 
                level.getRequiredLevel() + " to unlock.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start battle
        Intent intent = new Intent(this, BattleActivity.class);
        intent.putExtra("level", level);
        intent.putExtra("character", playerCharacter);
        startActivity(intent);
    }

    private void saveCharacter() {
        if (auth.getCurrentUser() == null || playerCharacter == null) return;

        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("game_character")
                .document("main")
                .set(playerCharacter)
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error saving character", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh character stats when returning from battle
        loadPlayerCharacter();
    }
}
