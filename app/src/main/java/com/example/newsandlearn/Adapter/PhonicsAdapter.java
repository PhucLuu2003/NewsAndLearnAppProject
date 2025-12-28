package com.example.newsandlearn.Adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.PhonicsLesson;
import com.example.newsandlearn.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * 🎴 PhonicsAdapter - Premium adapter with beautiful animations
 * Displays phonics lessons in a grid with sound playback
 */
public class PhonicsAdapter extends RecyclerView.Adapter<PhonicsAdapter.PhonicsViewHolder> {

    private List<PhonicsLesson> lessons = new ArrayList<>();
    private final Context context;
    private final OnLessonClickListener listener;
    private MediaPlayer mediaPlayer;
    private int lastPlayedPosition = -1;

    // Gradient colors for different categories
    private static final int[] ALPHABET_COLORS = { 0xFF6366F1, 0xFF8B5CF6 }; // Purple
    private static final int[] VOWEL_COLORS = { 0xFFEF4444, 0xFFF97316 }; // Red-Orange
    private static final int[] CONSONANT_COLORS = { 0xFF3B82F6, 0xFF06B6D4 }; // Blue-Cyan
    private static final int[] STRESS_COLORS = { 0xFF10B981, 0xFF14B8A6 }; // Green-Teal

    public interface OnLessonClickListener {
        void onLessonClick(PhonicsLesson lesson, int position);

        void onPlaySound(PhonicsLesson lesson, int position);
    }

    public PhonicsAdapter(Context context, OnLessonClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setLessons(List<PhonicsLesson> lessons) {
        this.lessons = lessons != null ? lessons : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addLesson(PhonicsLesson lesson) {
        lessons.add(lesson);
        notifyItemInserted(lessons.size() - 1);
    }

    @NonNull
    @Override
    public PhonicsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_phonics_card, parent, false);
        return new PhonicsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhonicsViewHolder holder, int position) {
        PhonicsLesson lesson = lessons.get(position);
        holder.bind(lesson, position);

        // Animate card entry
        animateCardEntry(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    private void animateCardEntry(View view, int position) {
        view.setAlpha(0f);
        view.setTranslationY(50f);
        view.setScaleX(0.95f);
        view.setScaleY(0.95f);

        view.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setStartDelay(position * 50L) // Staggered animation
                .setInterpolator(new OvershootInterpolator(0.8f))
                .start();
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    class PhonicsViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardLesson;
        private final View containerTop;
        private final TextView tvLetter, tvIpa, tvTitle, tvExamples, tvXp, tvLevel;
        private final ImageView iconCompleted, btnPlaySound;
        private final View dotLevel;
        private final LinearLayout containerLevel;

        PhonicsViewHolder(@NonNull View itemView) {
            super(itemView);
            cardLesson = itemView.findViewById(R.id.card_lesson);
            containerTop = itemView.findViewById(R.id.container_top);
            tvLetter = itemView.findViewById(R.id.tv_letter);
            tvIpa = itemView.findViewById(R.id.tv_ipa);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvExamples = itemView.findViewById(R.id.tv_examples);
            tvXp = itemView.findViewById(R.id.tv_xp);
            tvLevel = itemView.findViewById(R.id.tv_level);
            iconCompleted = itemView.findViewById(R.id.icon_completed);
            btnPlaySound = itemView.findViewById(R.id.btn_play_sound);
            dotLevel = itemView.findViewById(R.id.dot_level);
            containerLevel = itemView.findViewById(R.id.container_level);
        }

        void bind(PhonicsLesson lesson, int position) {
            // Set letter/symbol
            tvLetter.setText(lesson.getTitle().substring(0, 1).toUpperCase());

            // Set IPA symbol
            tvIpa.setText("/" + lesson.getSymbol() + "/");

            // Set title
            tvTitle.setText(lesson.getTitle());

            // Set example words with emojis
            if (lesson.getExampleWords() != null && !lesson.getExampleWords().isEmpty()) {
                StringBuilder examples = new StringBuilder();
                for (int i = 0; i < Math.min(2, lesson.getExampleWords().size()); i++) {
                    if (i > 0)
                        examples.append(", ");
                    examples.append(lesson.getExampleWords().get(i));
                }
                tvExamples.setText(examples.toString());
            }

            // Set XP
            tvXp.setText("+" + lesson.getXpReward() + " XP");

            // Set level with color coding
            String level = lesson.getLevel();
            tvLevel.setText(level);
            int levelColor;
            switch (level.toLowerCase()) {
                case "easy":
                    levelColor = 0xFF10B981; // Green
                    break;
                case "medium":
                    levelColor = 0xFFF59E0B; // Amber
                    break;
                case "hard":
                    levelColor = 0xFFEF4444; // Red
                    break;
                default:
                    levelColor = 0xFF6B7280; // Gray
            }
            tvLevel.setTextColor(levelColor);
            dotLevel.getBackground().setTint(levelColor);

            // Set completion status
            iconCompleted.setVisibility(lesson.isCompleted() ? View.VISIBLE : View.GONE);

            // Set background gradient based on category
            int[] colors = getColorsForCategory(lesson.getCategory());
            // Apply gradient programmatically or use different drawables

            // Click listeners
            cardLesson.setOnClickListener(v -> {
                animateCardClick(v);
                if (listener != null) {
                    listener.onLessonClick(lesson, position);
                }
            });

            btnPlaySound.setOnClickListener(v -> {
                animateSoundButton(v);
                if (listener != null) {
                    listener.onPlaySound(lesson, position);
                }
                playSound(lesson.getAudioUrl(), position);
            });
        }

        private void animateCardClick(View view) {
            AnimatorSet scaleDown = new AnimatorSet();
            scaleDown.playTogether(
                    ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f),
                    ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f));
            scaleDown.setDuration(100);

            AnimatorSet scaleUp = new AnimatorSet();
            scaleUp.playTogether(
                    ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f),
                    ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f));
            scaleUp.setDuration(100);
            scaleUp.setInterpolator(new OvershootInterpolator(2f));

            AnimatorSet fullAnim = new AnimatorSet();
            fullAnim.playSequentially(scaleDown, scaleUp);
            fullAnim.start();
        }

        private void animateSoundButton(View view) {
            ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(view, "rotation", 0f, 20f, -20f, 10f, -10f, 0f);
            rotateAnim.setDuration(400);
            rotateAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            rotateAnim.start();
        }
    }

    private int[] getColorsForCategory(String category) {
        if (category == null)
            return ALPHABET_COLORS;
        switch (category.toLowerCase()) {
            case "vowel":
            case "vowels":
                return VOWEL_COLORS;
            case "consonant":
            case "consonants":
                return CONSONANT_COLORS;
            case "stress":
            case "word stress":
                return STRESS_COLORS;
            default:
                return ALPHABET_COLORS;
        }
    }

    private void playSound(String audioUrl, int position) {
        if (audioUrl == null || audioUrl.isEmpty())
            return;

        try {
            // Stop previous playback
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                mediaPlayer = null;
            });

            lastPlayedPosition = position;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
