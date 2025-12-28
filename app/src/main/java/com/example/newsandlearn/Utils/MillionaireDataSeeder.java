package com.example.newsandlearn.Utils;

import android.util.Log;

import com.example.newsandlearn.Model.MillionaireQuestion;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * MillionaireDataSeeder - Seeds sample English Millionaire questions into
 * Firestore.
 * Collection: millionaire_questions
 */
public class MillionaireDataSeeder {

    private static final String TAG = "MillionaireDataSeeder";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnCompleteListener {
        void onSuccess();

        void onFailure(Exception e);
    }

    public static void seedAll(OnCompleteListener listener) {
        List<MillionaireQuestion> questions = createSampleQuestions();
        if (questions.isEmpty()) {
            listener.onFailure(new IllegalStateException("No sample questions"));
            return;
        }

        final int[] successCount = { 0 };
        for (MillionaireQuestion q : questions) {
            db.collection("millionaire_questions")
                    .document(q.getId())
                    .set(q)
                    .addOnSuccessListener(aVoid -> {
                        successCount[0]++;
                        Log.d(TAG, "Seeded: " + q.getId());
                        if (successCount[0] == questions.size()) {
                            listener.onSuccess();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error seeding: " + q.getId(), e);
                        listener.onFailure(e);
                    });
        }
    }

    private static List<MillionaireQuestion> createSampleQuestions() {
        // Note: Keep content original (no copyrighted materials).
        // Balanced across grammar & vocabulary, increasing difficulty.
        return Arrays.asList(
                q("m1_01", 1,
                        "Choose the best meaning of 'awesome':",
                        opts("Rất tuyệt vời", "Đáng sợ", "Nhỏ bé", "Buồn ngủ"),
                        0,
                        "'Awesome' usually means 'excellent/very impressive'.",
                        "Hint: It's often a compliment.",
                        "Fun fact: 'awe' = cảm giác kinh ngạc.",
                        "easy", "vocabulary", 30),

                q("m1_02", 1,
                        "Pick the correct sentence:",
                        opts("She goes to school every day.", "She go to school every day.",
                                "She going to school every day.", "She gone to school every day."),
                        0,
                        "With 'she/he/it' in present simple, use verb + s/es.",
                        "Hint: Third-person singular gets an -s.",
                        "Tiny trick: she → goes, not go.",
                        "easy", "grammar", 30),

                q("m1_03", 2,
                        "What does 'borrow' mean?",
                        opts("Mượn", "Cho mượn", "Mua", "Bán"),
                        0,
                        "Borrow = take something from someone temporarily.",
                        "Hint: borrow FROM someone.",
                        "Opposite pair: borrow vs lend.",
                        "easy", "vocabulary", 30),

                q("m1_04", 2,
                        "Choose the correct question:",
                        opts("Where do you live?", "Where you do live?", "Where you live do?", "Where does you live?"),
                        0,
                        "Present simple questions use do/does + subject + base verb.",
                        "Hint: do + you.",
                        "If you hear 'you', think 'do'.", "easy", "grammar", 30),

                q("m1_05", 3,
                        "Select the best word: 'I feel ____ today'",
                        opts("happy", "happily", "happiness", "happierly"),
                        0,
                        "After 'feel', we commonly use an adjective: feel happy.",
                        "Hint: adjective needed.",
                        "Grammar snack: feel + adj.", "easy", "grammar", 30),

                q("m1_06", 4,
                        "What is the past tense of 'eat'?",
                        opts("ate", "eated", "eaten", "eats"),
                        0,
                        "Past simple of 'eat' is 'ate'.",
                        "Hint: irregular verb.",
                        "'eaten' is past participle (have eaten).", "medium", "grammar", 30),

                q("m1_07", 5,
                        "Meaning of 'appointment':",
                        opts("Cuộc hẹn", "Lời hứa", "Đề nghị", "Bài kiểm tra"),
                        0,
                        "An appointment is a scheduled meeting/time.",
                        "Hint: doctor appointment.",
                        "Pro tip: schedule = lên lịch.", "medium", "vocabulary", 30),

                q("m1_08", 6,
                        "Pick the correct form: 'I have ____ my homework.'",
                        opts("done", "did", "do", "doing"),
                        0,
                        "Present perfect uses have/has + past participle.",
                        "Hint: have + V3.",
                        "Did = past simple; done = V3.", "medium", "grammar", 30),

                q("m1_09", 7,
                        "Choose the best option: 'Could you ____ the window?'",
                        opts("open", "opens", "opened", "opening"),
                        0,
                        "After modal verbs (could), use base verb.",
                        "Hint: could + V1.",
                        "Modal = always base form.", "medium", "grammar", 30),

                q("m1_10", 8,
                        "Meaning of 'at least':",
                        opts("Ít nhất", "Nhiều nhất", "Ngay lập tức", "Thỉnh thoảng"),
                        0,
                        "'At least' = minimum amount.",
                        "Hint: minimum.",
                        "Example: at least 10 minutes.", "medium", "vocabulary", 30),

                q("m1_11", 9,
                        "Pick the best rewrite: 'I am tired, so I will sleep.'",
                        opts("Because I'm tired, I'll sleep.", "Because I'm tired, I slept.",
                                "Because I'm tired, I sleep yesterday.", "Because I'm tired, sleeping."),
                        0,
                        "Cause → result: because + present, future with will.",
                        "Hint: keep tense consistent.",
                        "Because + now, will + next.", "hard", "grammar", 35),

                q("m1_12", 10,
                        "Meaning of 'efficient':",
                        opts("Hiệu quả", "Đắt đỏ", "Ồn ào", "Lộn xộn"),
                        0,
                        "Efficient = doing something well without wasting time/energy.",
                        "Hint: effective but faster/less waste.",
                        "Efficient team = team làm việc gọn gàng.", "hard", "vocabulary", 35),

                q("m1_13", 11,
                        "Choose the correct sentence:",
                        opts("If I had time, I would travel.", "If I have time, I would travel.",
                                "If I had time, I will travel.", "If I would have time, I travel."),
                        0,
                        "Second conditional: If + past simple, would + base verb.",
                        "Hint: unreal now.",
                        "Had time ≠ past time; it's unreal present.", "hard", "grammar", 40),

                q("m1_14", 12,
                        "Meaning of 'reliable':",
                        opts("Đáng tin cậy", "Dễ vỡ", "Nhanh chóng", "Nông cạn"),
                        0,
                        "Reliable = can be trusted; works well consistently.",
                        "Hint: trust.",
                        "Reliable friend = bạn 'không phốt'.", "hard", "vocabulary", 40),

                q("m1_15", 15,
                        "Pick the best phrase: 'He apologized ____ being late.'",
                        opts("for", "to", "with", "at"),
                        0,
                        "We use 'apologize for + noun/gerund'.",
                        "Hint: apologize for + V-ing.",
                        "Mini meme: apologize FOR, not TO (usually).", "hard", "grammar", 45));
    }

    private static MillionaireQuestion q(
            String id,
            int tier,
            String question,
            List<String> options,
            int correctIndex,
            String explanation,
            String hint,
            String funFact,
            String difficulty,
            String category,
            int timeLimit) {
        return new MillionaireQuestion(
                id,
                tier,
                question,
                options,
                correctIndex,
                explanation,
                hint,
                funFact,
                difficulty,
                category,
                timeLimit);
    }

    private static List<String> opts(String a, String b, String c, String d) {
        return Arrays.asList(a, b, c, d);
    }
}
