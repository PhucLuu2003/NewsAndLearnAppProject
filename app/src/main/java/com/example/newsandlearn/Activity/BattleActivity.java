package com.example.newsandlearn.Activity;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newsandlearn.Model.GameCharacter;
import com.example.newsandlearn.Model.GameLevel;
import com.example.newsandlearn.Model.GameQuestion;
import com.example.newsandlearn.Model.Monster;
import com.example.newsandlearn.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * BattleActivity - Main battle screen
 */
public class BattleActivity extends AppCompatActivity {

    // UI Components
    private ImageView monsterImage;
    private TextView monsterName, monsterHpText, battleLog;
    private ProgressBar monsterHpBar;
    private TextView questionText, timerText;
    private TextView playerHpText;
    private ProgressBar playerHpBar;
    private MaterialButton optionA, optionB, optionC, optionD;

    // Game Data
    private GameLevel currentLevel;
    private GameCharacter player;
    private Monster monster;
    private List<GameQuestion> questions;
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private CountDownTimer questionTimer;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get data from intent
        currentLevel = (GameLevel) getIntent().getSerializableExtra("level");
        player = (GameCharacter) getIntent().getSerializableExtra("character");

        if (currentLevel == null || player == null) {
            Toast.makeText(this, "Error loading battle data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initializeViews();

        // Create monster
        createMonster();

        // Load questions
        loadQuestions();
    }

    private void initializeViews() {
        monsterImage = findViewById(R.id.monster_image);
        monsterName = findViewById(R.id.monster_name);
        monsterHpText = findViewById(R.id.monster_hp_text);
        monsterHpBar = findViewById(R.id.monster_hp_bar);
        battleLog = findViewById(R.id.battle_log);
        questionText = findViewById(R.id.question_text);
        timerText = findViewById(R.id.timer_text);
        playerHpText = findViewById(R.id.player_hp_text);
        playerHpBar = findViewById(R.id.player_hp_bar);
        optionA = findViewById(R.id.option_a);
        optionB = findViewById(R.id.option_b);
        optionC = findViewById(R.id.option_c);
        optionD = findViewById(R.id.option_d);

        // Setup option click listeners
        optionA.setOnClickListener(v -> checkAnswer(0));
        optionB.setOnClickListener(v -> checkAnswer(1));
        optionC.setOnClickListener(v -> checkAnswer(2));
        optionD.setOnClickListener(v -> checkAnswer(3));

        // Disable buttons initially until questions are loaded
        enableButtons(false);
    }

    private void createMonster() {
        // Create monster based on level
        String monsterType = currentLevel.getLevelNumber() % 5 == 0 ? "boss" : "normal";
        monster = new Monster(
                "monster_" + currentLevel.getId(),
                currentLevel.getTheme() + " Monster",
                currentLevel.getLevelNumber(),
                monsterType);

        updateMonsterUI();
        updatePlayerUI();
    }

    private void loadQuestions() {
        if (currentLevel.getQuestionIds() == null || currentLevel.getQuestionIds().isEmpty()) {
            // Load random questions if no specific questions assigned
            loadRandomQuestions();
            return;
        }

        questions = new ArrayList<>();
        final int expected = currentLevel.getQuestionIds().size();
        final int[] completed = { 0 };

        // Show a helpful message while loading.
        battleLog.setText("Loading questions...");
        enableButtons(false);

        for (String questionId : currentLevel.getQuestionIds()) {
            db.collection("game_questions").document(questionId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            GameQuestion question = document.toObject(GameQuestion.class);
                            if (question != null) {
                                questions.add(question);
                            }
                        }
                        completed[0]++;
                        maybeStartQuestions(expected, completed[0]);
                    })
                    .addOnFailureListener(e -> {
                        completed[0]++;
                        maybeStartQuestions(expected, completed[0]);
                    });
        }
    }

    private void maybeStartQuestions(int expected, int completed) {
        if (completed < expected) {
            return;
        }

        if (questions != null && !questions.isEmpty()) {
            Collections.shuffle(questions);
            showNextQuestion();
        } else {
            // Fallback to random questions if assigned IDs were invalid/missing.
            loadRandomQuestions();
        }
    }

    private void loadRandomQuestions() {
        db.collection("game_questions")
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    questions = new ArrayList<>();
                    queryDocumentSnapshots.forEach(doc -> {
                        GameQuestion q = doc.toObject(GameQuestion.class);
                        questions.add(q);
                    });

                    if (!questions.isEmpty()) {
                        Collections.shuffle(questions);
                        showNextQuestion();
                    } else {
                        Toast.makeText(this, "No questions found!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading questions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    battleLog.setText("Failed to load questions.");
                    enableButtons(false);
                });
    }

    private void showNextQuestion() {
        // Check if questions are loaded
        if (questions == null || questions.isEmpty()) {
            battleLog.setText("Loading questions...");
            enableButtons(false);
            return;
        }

        if (currentQuestionIndex >= questions.size()) {
            // All questions answered
            endBattle(true);
            return;
        }

        if (!monster.isAlive()) {
            endBattle(true);
            return;
        }

        if (!player.isAlive()) {
            endBattle(false);
            return;
        }

        GameQuestion question = questions.get(currentQuestionIndex);

        // Update UI
        questionText.setText(question.getQuestion());
        List<String> options = question.getOptions();
        if (options != null && options.size() >= 4) {
            optionA.setText("A. " + options.get(0));
            optionB.setText("B. " + options.get(1));
            optionC.setText("C. " + options.get(2));
            optionD.setText("D. " + options.get(3));
        }

        // Reset button states and enable buttons
        resetButtonStates();
        enableButtons(true);

        // Make sure buttons are clickable
        optionA.setClickable(true);
        optionB.setClickable(true);
        optionC.setClickable(true);
        optionD.setClickable(true);

        // Start timer
        startQuestionTimer(question.getTimeLimit());

        battleLog.setText("Answer the question to attack!");
    }

    private void checkAnswer(int selectedIndex) {
        // Check if questions are loaded
        if (questions == null || questions.isEmpty()) {
            Toast.makeText(this, "Questions not loaded yet. Please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if currentQuestionIndex is valid
        if (currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) {
            Toast.makeText(this, "Invalid question index", Toast.LENGTH_SHORT).show();
            return;
        }

        if (questionTimer != null) {
            questionTimer.cancel();
        }

        enableButtons(false);

        GameQuestion question = questions.get(currentQuestionIndex);
        boolean isCorrect = question.isCorrect(selectedIndex);

        if (isCorrect) {
            correctAnswers++;
            highlightCorrectAnswer(selectedIndex);
            battleLog.setText("✅ Correct! You attack the monster!");

            // Player attacks monster
            playerAttack();

        } else {
            highlightWrongAnswer(selectedIndex, question.getCorrectAnswerIndex());
            battleLog.setText("❌ Wrong! Monster attacks you!");

            // Monster attacks player
            monsterAttack();
        }

        // Next question after delay
        battleLog.postDelayed(() -> {
            currentQuestionIndex++;
            showNextQuestion();
        }, 2000);
    }

    private void playerAttack() {
        int damage = player.getAttack();
        monster.takeDamage(damage);
        updateMonsterUI();

        // Animate monster
        animateHit(monsterImage);

        Toast.makeText(this, "💥 Dealt " + damage + " damage!", Toast.LENGTH_SHORT).show();

        // Gain EXP
        player.gainExp(10);
        updatePlayerUI();
    }

    private void monsterAttack() {
        int damage = monster.getAttack();
        player.takeDamage(damage);
        updatePlayerUI();

        Toast.makeText(this, "💔 Took " + damage + " damage!", Toast.LENGTH_SHORT).show();
    }

    private void startQuestionTimer(int seconds) {
        questionTimer = new CountDownTimer(seconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                timerText.setText(secondsLeft + "s");

                if (secondsLeft <= 5) {
                    timerText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            }

            @Override
            public void onFinish() {
                timerText.setText("0s");
                checkAnswer(-1); // Time's up = wrong answer
            }
        };
        questionTimer.start();
        timerText.setTextColor(getResources().getColor(R.color.primary));
    }

    private void updateMonsterUI() {
        monsterName.setText(monster.getName() + " Lv." + monster.getLevel());
        monsterHpText.setText(monster.getCurrentHp() + "/" + monster.getMaxHp());
        monsterHpBar.setMax(monster.getMaxHp());
        monsterHpBar.setProgress(monster.getCurrentHp());
    }

    private void updatePlayerUI() {
        playerHpText.setText(player.getCurrentHp() + "/" + player.getMaxHp());
        playerHpBar.setMax(player.getMaxHp());
        playerHpBar.setProgress(player.getCurrentHp());
    }

    private void highlightCorrectAnswer(int index) {
        MaterialButton correctButton = getButtonByIndex(index);
        if (correctButton != null) {
            correctButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        }
    }

    private void highlightWrongAnswer(int wrongIndex, int correctIndex) {
        MaterialButton wrongButton = getButtonByIndex(wrongIndex);
        MaterialButton correctButton = getButtonByIndex(correctIndex);

        if (wrongButton != null) {
            wrongButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        }
        if (correctButton != null) {
            correctButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        }
    }

    private void resetButtonStates() {
        optionA.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        optionB.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        optionC.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        optionD.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void enableButtons(boolean enabled) {
        optionA.setEnabled(enabled);
        optionB.setEnabled(enabled);
        optionC.setEnabled(enabled);
        optionD.setEnabled(enabled);
    }

    private MaterialButton getButtonByIndex(int index) {
        switch (index) {
            case 0:
                return optionA;
            case 1:
                return optionB;
            case 2:
                return optionC;
            case 3:
                return optionD;
            default:
                return null;
        }
    }

    private void animateHit(View view) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        shake.setDuration(500);
        shake.start();
    }

    private void endBattle(boolean victory) {
        if (questionTimer != null) {
            questionTimer.cancel();
        }

        int stars = calculateStars();

        String title = victory ? "🎉 Victory!" : "💀 Defeated!";
        String message = victory ? "You defeated the monster!\n\n" +
                "Correct Answers: " + correctAnswers + "/" + questions.size() + "\n" +
                "Stars Earned: " + getStarString(stars) + "\n" +
                "EXP Gained: " + monster.getExpReward() : "You were defeated!\nTry again to improve!";

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Continue", (dialog, which) -> {
                    if (victory) {
                        saveProgress(stars);
                    }
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private int calculateStars() {
        float accuracy = (float) correctAnswers / questions.size();
        if (accuracy >= 0.9)
            return 3;
        if (accuracy >= 0.7)
            return 2;
        if (accuracy >= 0.5)
            return 1;
        return 0;
    }

    private String getStarString(int stars) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stars; i++) {
            sb.append("⭐");
        }
        return sb.toString();
    }

    private void saveProgress(int stars) {
        if (auth.getCurrentUser() == null)
            return;

        String userId = auth.getCurrentUser().getUid();

        // Update level completion
        currentLevel.complete(stars);
        
        // Update player's highest completed level to unlock next level
        player.completeLevel(currentLevel.getLevelNumber());
        
        // Save level progress
        db.collection("users").document(userId)
                .collection("game_levels")
                .document(currentLevel.getId())
                .set(currentLevel);

        // Save character progress (including highestCompletedLevel)
        db.collection("users").document(userId)
                .collection("game_character")
                .document("main")
                .set(player);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (questionTimer != null) {
            questionTimer.cancel();
        }
    }
}
