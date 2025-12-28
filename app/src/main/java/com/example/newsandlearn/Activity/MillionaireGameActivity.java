package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.newsandlearn.Adapter.MillionaireLadderAdapter;
import com.example.newsandlearn.Model.MillionaireQuestion;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.MillionaireDataSeeder;
import com.example.newsandlearn.Utils.ProgressHelper;
import com.example.newsandlearn.Utils.ProgressManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * English Millionaire - humorous "Ai là triệu phú"-style English learning game.
 * Uses Firestore for question bank + saving run results.
 */
public class MillionaireGameActivity extends AppCompatActivity {

    private static final int MAX_TIER = 15;

    private final int[] prizeLadder = new int[] {
            100, 200, 300, 500, 1000,
            2000, 4000, 8000, 16000, 32000,
            64000, 125000, 250000, 500000, 1000000
    };

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private MaterialButton btnSeed;
    private MaterialButton btnAnswerA, btnAnswerB, btnAnswerC, btnAnswerD;
    private MaterialButton btnFifty, btnAudience, btnPhone, btnTakeMoney;
    private MaterialButton btnBack;
    private MaterialButton btnLadder;

    private TextView tvTitle;
    private TextView tvTier;
    private TextView tvPrize;
    private TextView tvTimer;
    private TextView tvQuestion;
    private TextView tvHost;

    private LottieAnimationView confetti;

    private MillionaireLadderAdapter ladderAdapter;

    private CountDownTimer timer;
    private int timeLeft;

    private int currentTier = 1;
    private int guaranteedPrize = 0;
    private int currentPrize = 0;

    private int correctCount = 0;
    private int xpEarned = 0;

    private boolean usedFifty = false;
    private boolean usedAudience = false;
    private boolean usedPhone = false;

    private boolean answersDisabled = false;

    private final Set<Integer> eliminatedOptions = new HashSet<>();
    private MillionaireQuestion currentQuestion;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_millionaire_game);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        bindViews();
        setupLadderAdapter();
        setupListeners();

        resetGame();
        loadQuestionForTier(currentTier);
    }

    private void bindViews() {
        btnSeed = findViewById(R.id.btn_seed_millionaire);
        btnAnswerA = findViewById(R.id.btn_answer_a);
        btnAnswerB = findViewById(R.id.btn_answer_b);
        btnAnswerC = findViewById(R.id.btn_answer_c);
        btnAnswerD = findViewById(R.id.btn_answer_d);

        btnFifty = findViewById(R.id.btn_lifeline_fifty);
        btnAudience = findViewById(R.id.btn_lifeline_audience);
        btnPhone = findViewById(R.id.btn_lifeline_phone);
        btnTakeMoney = findViewById(R.id.btn_take_money);

        btnBack = findViewById(R.id.btn_back);
        btnLadder = findViewById(R.id.btn_ladder);

        tvTitle = findViewById(R.id.tv_game_title);
        tvTier = findViewById(R.id.tv_tier);
        tvPrize = findViewById(R.id.tv_prize);
        tvTimer = findViewById(R.id.tv_timer);
        tvQuestion = findViewById(R.id.tv_question);
        tvHost = findViewById(R.id.tv_host);

        confetti = findViewById(R.id.confetti_animation);
    }

    private void setupLadderAdapter() {
        ladderAdapter = new MillionaireLadderAdapter(this);
        List<MillionaireLadderAdapter.TierItem> tiers = new ArrayList<>();
        // Display from 15 -> 1 like the real show
        for (int tier = MAX_TIER; tier >= 1; tier--) {
            tiers.add(new MillionaireLadderAdapter.TierItem(tier, prizeLadder[tier - 1]));
        }
        ladderAdapter.setItems(tiers);
        ladderAdapter.setCurrentTier(currentTier);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        if (btnLadder != null) {
            btnLadder.setOnClickListener(v -> showLadderBottomSheet());
        }

        btnSeed.setOnClickListener(v -> seedQuestions());

        btnAnswerA.setOnClickListener(v -> onAnswerSelected(0));
        btnAnswerB.setOnClickListener(v -> onAnswerSelected(1));
        btnAnswerC.setOnClickListener(v -> onAnswerSelected(2));
        btnAnswerD.setOnClickListener(v -> onAnswerSelected(3));

        btnFifty.setOnClickListener(v -> useFiftyFifty());
        btnAudience.setOnClickListener(v -> useAskAudience());
        btnPhone.setOnClickListener(v -> usePhoneAFriend());
        btnTakeMoney.setOnClickListener(v -> confirmTakeMoney());
    }

    private void resetGame() {
        currentTier = 1;
        guaranteedPrize = 0;
        currentPrize = 0;
        correctCount = 0;
        xpEarned = 0;

        usedFifty = false;
        usedAudience = false;
        usedPhone = false;

        eliminatedOptions.clear();
        currentQuestion = null;

        // Reset lifelines UI
        btnFifty.setEnabled(true);
        btnFifty.setText("50:50");
        btnAudience.setEnabled(true);
        btnAudience.setText("Audience");
        btnPhone.setEnabled(true);
        btnPhone.setText("Phone");

        updateTopBar();
        setHostLine(
                "Xin chào! Hôm nay ta chơi 'English Millionaire' — sai thì buồn, đúng thì... cũng buồn vì hết câu! 😄");
    }

    private void updateTopBar() {
        tvTitle.setText("🧠 English Millionaire");
        tvTier.setText(String.format(Locale.getDefault(), "Question %d/%d", currentTier, MAX_TIER));
        tvPrize.setText(String.format(Locale.getDefault(), "Prize: $%,d • Safe: $%,d", currentPrize, guaranteedPrize));

        if (ladderAdapter != null) {
            ladderAdapter.setCurrentTier(currentTier);
        }
    }

    private void showLadderBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_millionaire_ladder, null);

        RecyclerView rv = view.findViewById(R.id.rv_ladder_sheet);
        MaterialButton btnClose = view.findViewById(R.id.btn_ladder_close);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(ladderAdapter);
        if (ladderAdapter != null)
            ladderAdapter.setCurrentTier(currentTier);

        // Auto-scroll to current tier (list is 15..1)
        int pos = MAX_TIER - currentTier;
        rv.post(() -> rv.scrollToPosition(Math.max(0, Math.min(pos, MAX_TIER - 1))));

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(view);
        dialog.show();
    }

    private void setHostLine(String line) {
        tvHost.setText(line);
    }

    private void seedQuestions() {
        btnSeed.setEnabled(false);
        btnSeed.setText("⏳ Seeding...");

        MillionaireDataSeeder.seedAll(new MillionaireDataSeeder.OnCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MillionaireGameActivity.this, "✅ Seeded English Millionaire questions!",
                        Toast.LENGTH_SHORT).show();
                btnSeed.setEnabled(true);
                btnSeed.setText("✅ Seeded!");
                loadQuestionForTier(currentTier);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MillionaireGameActivity.this, "❌ Seed failed: " + e.getMessage(), Toast.LENGTH_LONG)
                        .show();
                btnSeed.setEnabled(true);
                btnSeed.setText("🎲 Seed Questions");
            }
        });
    }

    private void loadQuestionForTier(int tier) {
        cancelTimer();
        disableAnswers(true);
        showAnswerButtonsEnabled(false);

        eliminatedOptions.clear();

        updateTopBar();
        tvQuestion.setText("Loading question...");
        setHostLine(pickHostIntroLine(tier));

        db.collection("millionaire_questions")
                .whereEqualTo("tier", tier)
                .get()
                .addOnSuccessListener(qs -> {
                    if (qs.isEmpty()) {
                        tvQuestion.setText("No questions found for this tier. Tap 'Seed Questions'.");
                        showAnswerButtonsEnabled(false);
                        disableAnswers(true);
                        return;
                    }

                    int pick = new Random().nextInt(qs.size());
                    currentQuestion = qs.getDocuments().get(pick).toObject(MillionaireQuestion.class);
                    if (currentQuestion == null) {
                        tvQuestion.setText("Question parse error. Try seeding again.");
                        return;
                    }

                    renderQuestion(currentQuestion);
                    startTimer(currentQuestion.getTimeLimit() > 0 ? currentQuestion.getTimeLimit() : 30);
                })
                .addOnFailureListener(e -> {
                    tvQuestion.setText("Failed to load questions: " + e.getMessage());
                    Toast.makeText(this, "Error loading question", Toast.LENGTH_SHORT).show();
                });
    }

    private void renderQuestion(MillionaireQuestion q) {
        tvQuestion.setText(q.getQuestion() != null ? q.getQuestion() : "");

        List<String> opts = q.getOptions();
        if (opts == null || opts.size() < 4) {
            opts = new ArrayList<>();
            opts.add("(missing A)");
            opts.add("(missing B)");
            opts.add("(missing C)");
            opts.add("(missing D)");
        }

        String a = "A. " + opts.get(0);
        String b = "B. " + opts.get(1);
        String c = "C. " + opts.get(2);
        String d = "D. " + opts.get(3);

        // store base labels so 50:50 doesn't keep appending "(removed)"
        btnAnswerA.setTag(a);
        btnAnswerB.setTag(b);
        btnAnswerC.setTag(c);
        btnAnswerD.setTag(d);

        btnAnswerA.setText(a);
        btnAnswerB.setText(b);
        btnAnswerC.setText(c);
        btnAnswerD.setText(d);

        showAnswerButtonsEnabled(true);
        disableAnswers(false);
    }

    private void startTimer(int seconds) {
        timeLeft = seconds;
        tvTimer.setText(String.format(Locale.getDefault(), "⏱ %ds", timeLeft));

        timer = new CountDownTimer(seconds * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = (int) (millisUntilFinished / 1000L);
                tvTimer.setText(String.format(Locale.getDefault(), "⏱ %ds", timeLeft));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("⏱ 0s");
                if (currentQuestion != null) {
                    setHostLine("Hết giờ! Đồng hồ chạy nhanh hơn deadline 😅");
                    handleWrongAnswer(-1, true);
                }
            }
        };
        timer.start();
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void onAnswerSelected(int index) {
        if (currentQuestion == null)
            return;
        if (eliminatedOptions.contains(index))
            return;

        cancelTimer();
        disableAnswers(true);

        boolean correct = currentQuestion.isCorrect(index);
        if (correct) {
            handleCorrectAnswer(index);
        } else {
            handleWrongAnswer(index, false);
        }
    }

    private void handleCorrectAnswer(int selectedIndex) {
        correctCount++;

        // Prize update
        currentPrize = prizeLadder[currentTier - 1];
        if (currentTier == 5)
            guaranteedPrize = 1000;
        if (currentTier == 10)
            guaranteedPrize = 32000;

        // XP (small but meaningful)
        int tierXp = Math.min(25 + currentTier * 5, 120);
        xpEarned += tierXp;

        updateTopBar();

        if (currentTier == 5 || currentTier == 10 || currentTier == 15) {
            playConfetti();
        }

        String explanation = safe(currentQuestion.getExplanation(), "Nice! Đáp án đúng.");

        setHostLine(pickHostCorrectLine(currentTier));

        if (currentTier >= MAX_TIER) {
            showResultBottomSheet(
                    true,
                    String.format(Locale.getDefault(), "You reached $%,d", currentPrize),
                    explanation,
                    tierXp,
                    "🏆 Finish",
                    "Take money",
                    () -> endGame(true),
                    () -> endGame(true));
        } else {
            showResultBottomSheet(
                    true,
                    String.format(Locale.getDefault(), "You reached $%,d", currentPrize),
                    explanation,
                    tierXp,
                    "Next →",
                    "Take money",
                    () -> {
                        currentTier++;
                        loadQuestionForTier(currentTier);
                    },
                    () -> endGame(true));
        }
    }

    private void handleWrongAnswer(int selectedIndex, boolean timeout) {
        int finalPrize = guaranteedPrize;
        currentPrize = finalPrize;
        updateTopBar();

        String correctAnswer = (currentQuestion != null) ? currentQuestion.getCorrectAnswer() : "";
        String explanation = (currentQuestion != null) ? safe(currentQuestion.getExplanation(), "") : "";

        String title = "❌ Wrong";
        String msg;
        if (timeout) {
            msg = "Time's up!\n\nCorrect answer: " + correctAnswer;
        } else {
            msg = "Correct answer: " + correctAnswer;
        }
        if (!explanation.trim().isEmpty())
            msg += "\n\n" + explanation;

        setHostLine(pickHostWrongLine(currentTier));

        String subtitle = timeout ? "⏱ Time's up" : "❌ Wrong";
        String bsMsg = subtitle + "\n\n" + msg;

        showResultBottomSheet(
                false,
                String.format(Locale.getDefault(), "You leave with $%,d", currentPrize),
                bsMsg,
                0,
                "Play again",
                "Exit",
                () -> {
                    saveRun(false);
                    resetGame();
                    loadQuestionForTier(currentTier);
                },
                () -> {
                    saveRun(false);
                    finish();
                });
    }

    private void confirmTakeMoney() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Take money?")
                .setMessage(String.format(Locale.getDefault(), "You will leave with $%,d. Continue?", currentPrize))
                .setPositiveButton("Take it", (d, w) -> endGame(true))
                .setNegativeButton("Keep playing", null)
                .show();
    }

    private void endGame(boolean voluntary) {
        saveRun(voluntary);

        String title = "🎉 Game Over";
        String msg = String.format(Locale.getDefault(),
                "You finished at question %d/%d\nPrize: $%,d\nCorrect: %d\nXP: +%d",
                currentTier, MAX_TIER, currentPrize, correctCount, xpEarned);

        showResultBottomSheet(
                true,
                title,
                msg,
                0,
                "Play again",
                "Exit",
                () -> {
                    resetGame();
                    loadQuestionForTier(currentTier);
                },
                this::finish);
    }

    private void saveRun(boolean voluntaryExitOrWin) {
        if (auth.getCurrentUser() == null)
            return;

        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> run = new HashMap<>();
        run.put("timestamp", FieldValue.serverTimestamp());
        run.put("reachedTier", currentTier);
        run.put("prize", currentPrize);
        run.put("guaranteedPrize", guaranteedPrize);
        run.put("correctCount", correctCount);
        run.put("xpEarned", xpEarned);
        run.put("usedFifty", usedFifty);
        run.put("usedAudience", usedAudience);
        run.put("usedPhone", usedPhone);
        run.put("voluntaryExitOrWin", voluntaryExitOrWin);

        db.collection("users").document(uid)
                .collection("millionaire_runs")
                .add(run);

        // Keep leaderboard compatible: update users.totalXP
        if (xpEarned > 0) {
            db.collection("users").document(uid)
                    .set(new HashMap<String, Object>() {
                        {
                            put("totalXP", FieldValue.increment(xpEarned));
                            put("lastGamePlayedAt", FieldValue.serverTimestamp());
                        }
                    }, com.google.firebase.firestore.SetOptions.merge());

            // Also update detailed progress (gamification system)
            ProgressManager.getInstance().addXP(xpEarned, null);
            ProgressHelper.incrementDailyGoal();
        }
    }

    private void useFiftyFifty() {
        if (usedFifty || currentQuestion == null)
            return;
        usedFifty = true;
        btnFifty.setEnabled(false);
        btnFifty.setText("50:50 ✓");

        int correct = currentQuestion.getCorrectAnswerIndex();
        List<Integer> wrong = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (i != correct)
                wrong.add(i);
        }

        // Remove two wrong options
        Random r = new Random();
        int first = wrong.remove(r.nextInt(wrong.size()));
        int second = wrong.remove(r.nextInt(wrong.size()));
        eliminatedOptions.add(first);
        eliminatedOptions.add(second);
        applyEliminationsToUI();

        setHostLine("50:50! Cắt bớt drama, giữ lại hy vọng 😄");
    }

    private void applyEliminationsToUI() {
        setOptionEliminated(btnAnswerA, eliminatedOptions.contains(0));
        setOptionEliminated(btnAnswerB, eliminatedOptions.contains(1));
        setOptionEliminated(btnAnswerC, eliminatedOptions.contains(2));
        setOptionEliminated(btnAnswerD, eliminatedOptions.contains(3));
    }

    private void setOptionEliminated(MaterialButton btn, boolean eliminated) {
        btn.setEnabled(!answersDisabled && !eliminated);
        btn.setAlpha(eliminated ? 0.35f : 1.0f);

        Object base = btn.getTag();
        String baseLabel = (base instanceof String) ? (String) base : String.valueOf(btn.getText());
        btn.setText(eliminated ? (baseLabel + "  (removed)") : baseLabel);
    }

    private void showResultBottomSheet(
            boolean positiveTone,
            String subtitle,
            String body,
            int xpDelta,
            String primaryText,
            String secondaryText,
            Runnable onPrimary,
            Runnable onSecondary) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_millionaire_result, null);

        TextView tvTitle = view.findViewById(R.id.tv_bs_title);
        TextView tvSubtitle = view.findViewById(R.id.tv_bs_subtitle);
        TextView tvExplanation = view.findViewById(R.id.tv_bs_explanation);
        TextView tvFunFact = view.findViewById(R.id.tv_bs_funfact);
        TextView tvXp = view.findViewById(R.id.tv_bs_xp);
        MaterialButton btnPrimary = view.findViewById(R.id.btn_bs_primary);
        MaterialButton btnSecondary = view.findViewById(R.id.btn_bs_secondary);

        tvTitle.setText(positiveTone ? "✅ Nice!" : "💥 Oops!");
        tvSubtitle.setText(subtitle);

        // The body already contains explanation + fun fact in some cases.
        // Keep it simple: put everything in explanation block, and only show fun fact
        // when it's clearly present in the question.
        tvExplanation.setText(body);

        String funFact = (currentQuestion != null) ? safe(currentQuestion.getFunFact(), "") : "";
        if (!funFact.trim().isEmpty() && positiveTone) {
            tvFunFact.setVisibility(View.VISIBLE);
            tvFunFact.setText("✨ " + funFact);
        } else {
            tvFunFact.setVisibility(View.GONE);
        }

        if (xpDelta > 0) {
            tvXp.setVisibility(View.VISIBLE);
            tvXp.setText(String.format(Locale.getDefault(), "+%d XP", xpDelta));
        } else {
            tvXp.setVisibility(View.GONE);
        }

        btnPrimary.setText(primaryText);
        btnSecondary.setText(secondaryText);

        dialog.setCancelable(false);
        dialog.setContentView(view);

        btnPrimary.setOnClickListener(v -> {
            dialog.dismiss();
            if (onPrimary != null)
                onPrimary.run();
        });

        btnSecondary.setOnClickListener(v -> {
            dialog.dismiss();
            if (onSecondary != null)
                onSecondary.run();
        });

        dialog.show();
    }

    private void useAskAudience() {
        if (usedAudience || currentQuestion == null)
            return;
        usedAudience = true;
        btnAudience.setEnabled(false);
        btnAudience.setText("Audience ✓");

        int correct = currentQuestion.getCorrectAnswerIndex();

        // Bias depends on tier (harder tier => less confident audience)
        int baseCorrect = clamp(70 - currentTier * 2, 30, 70);
        int correctPct = baseCorrect + new Random().nextInt(15); // add some randomness

        int remaining = 100 - correctPct;
        int a = 0, b = 0, c = 0, d = 0;
        int[] vals = new int[] { 0, 0, 0, 0 };
        vals[correct] = correctPct;

        List<Integer> wrongs = new ArrayList<>();
        for (int i = 0; i < 4; i++)
            if (i != correct && !eliminatedOptions.contains(i))
                wrongs.add(i);
        if (wrongs.isEmpty()) {
            wrongs.add((correct + 1) % 4);
        }

        // Distribute remaining
        Random r = new Random();
        for (int i = 0; i < wrongs.size(); i++) {
            int give = (i == wrongs.size() - 1) ? remaining : r.nextInt(remaining + 1);
            remaining -= give;
            vals[wrongs.get(i)] += give;
        }

        String msg = String.format(Locale.getDefault(),
                "Audience votes:\nA: %d%%\nB: %d%%\nC: %d%%\nD: %d%%",
                vals[0], vals[1], vals[2], vals[3]);

        new MaterialAlertDialogBuilder(this)
                .setTitle("👥 Ask the Audience")
                .setMessage(msg)
                .setPositiveButton("Got it", null)
                .show();

        setHostLine("Khán giả đã vote. Nhưng nhớ: số đông đôi khi… cũng sai 😅");
    }

    private void usePhoneAFriend() {
        if (usedPhone || currentQuestion == null)
            return;
        usedPhone = true;
        btnPhone.setEnabled(false);
        btnPhone.setText("Phone ✓");

        String hint = safe(currentQuestion.getHint(), "Your friend says: 'Think simple. Don't panic!' ");
        new MaterialAlertDialogBuilder(this)
                .setTitle("📞 Phone a Friend")
                .setMessage(hint)
                .setPositiveButton("Thanks!", null)
                .show();

        setHostLine("Bạn thân đã tư vấn. Không chắc đúng, nhưng chắc… thân 😄");
    }

    private void disableAnswers(boolean disabled) {
        answersDisabled = disabled;
        btnAnswerA.setEnabled(!disabled);
        btnAnswerB.setEnabled(!disabled);
        btnAnswerC.setEnabled(!disabled);
        btnAnswerD.setEnabled(!disabled);

        // re-apply eliminations
        applyEliminationsToUI();
    }

    private void showAnswerButtonsEnabled(boolean show) {
        int vis = show ? View.VISIBLE : View.INVISIBLE;
        btnAnswerA.setVisibility(vis);
        btnAnswerB.setVisibility(vis);
        btnAnswerC.setVisibility(vis);
        btnAnswerD.setVisibility(vis);
    }

    private void playConfetti() {
        if (confetti == null)
            return;
        confetti.setVisibility(View.VISIBLE);
        confetti.playAnimation();
        confetti.postDelayed(() -> {
            confetti.cancelAnimation();
            confetti.setVisibility(View.GONE);
        }, 1800);
    }

    private String pickHostIntroLine(int tier) {
        if (tier <= 3)
            return "Warm-up thôi. Cứ bình tĩnh như đang lướt TikTok!";
        if (tier <= 6)
            return "Giờ bắt đầu có mùi căng thẳng rồi đó… nhưng vẫn cute!";
        if (tier <= 10)
            return "Đến mốc an toàn rồi. Đừng tự làm khó cuộc đời nhé 😄";
        if (tier <= 14)
            return "Đây là vùng nguy hiểm. Sai 1 cái là về bờ thật luôn!";
        return "Câu cuối! Hít thở sâu. Bạn là nhân vật chính mà!";
    }

    private String pickHostCorrectLine(int tier) {
        if (tier <= 3)
            return "Chuẩn! Trí tuệ toả sáng nhẹ.";
        if (tier <= 6)
            return "Đúng! Não hoạt động 100% công suất.";
        if (tier <= 10)
            return "Đúng! Bạn đang chơi hệ 'đỉnh'.";
        if (tier <= 14)
            return "Đúng! Tôi nổi da gà luôn.";
        return "ĐÚNG! Bạn vừa phá đảo 'English Millionaire'!";
    }

    private String pickHostWrongLine(int tier) {
        if (tier <= 3)
            return "Sai nhẹ thôi. Lần sau ăn chắc mặc bền nhé!";
        if (tier <= 6)
            return "Sai rồi… Nhưng không sao, ta học tiếp!";
        if (tier <= 10)
            return "Ôi không… Cú twist này đau.";
        return "Sai ở vùng này thì… thôi ta làm lại từ đầu cho đẹp!";
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static String safe(String s, String fallback) {
        if (s == null)
            return fallback;
        if (s.trim().isEmpty())
            return fallback;
        return s;
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelTimer();
    }
}
