package com.example.newsandlearn.Adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.newsandlearn.Model.SurvivalWord;
import com.example.newsandlearn.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 🎴 SurvivalWordAdapter - Premium flashcard adapter with SRS support
 * Beautiful flip animations, sound playback, and spaced repetition tracking
 */
public class SurvivalWordAdapter extends RecyclerView.Adapter<SurvivalWordAdapter.WordViewHolder> {

    private List<SurvivalWord> words = new ArrayList<>();
    private final Context context;
    private final OnWordActionListener listener;
    private MediaPlayer mediaPlayer;

    // 🎨 Premium Gradient colors for different SRS levels
    private static final int[][] SRS_COLORS = {
            { 0xFF667EEA, 0xFF764BA2 }, // Level 0 - New (Purple-Violet gradient)
            { 0xFF4FACFE, 0xFF00F2FE }, // Level 1 - Learning (Sky Blue-Cyan)
            { 0xFFFA709A, 0xFFFEE140 }, // Level 2 - Reviewing (Pink-Yellow sunset)
            { 0xFF11998E, 0xFF38EF7D }, // Level 3 - Mastered (Teal-Green fresh)
    };

    private static final String[] SRS_LABELS = { "New", "Learning", "Reviewing", "Mastered" };
    private static final int[] SRS_DOT_COLORS = { 0xFF764BA2, 0xFF4FACFE, 0xFFFA709A, 0xFF38EF7D };

    public interface OnWordActionListener {
        void onWordClick(SurvivalWord word, int position);

        void onKnowClick(SurvivalWord word, int position);

        void onLearnClick(SurvivalWord word, int position);

        void onPlaySound(SurvivalWord word, int position);
    }

    public SurvivalWordAdapter(Context context, OnWordActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setWords(List<SurvivalWord> words) {
        this.words = words != null ? words : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void updateWord(int position, SurvivalWord word) {
        if (position >= 0 && position < words.size()) {
            words.set(position, word);
            notifyItemChanged(position);
        }
    }

    public void removeWord(int position) {
        if (position >= 0 && position < words.size()) {
            words.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_survival_word_card, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        SurvivalWord word = words.get(position);
        holder.bind(word, position);

        // Entry animation
        animateCardEntry(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    private void animateCardEntry(View view, int position) {
        view.setAlpha(0f);
        view.setTranslationX(100f);

        view.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(400)
                .setStartDelay(position * 80L)
                .setInterpolator(new OvershootInterpolator(0.5f))
                .start();
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    class WordViewHolder extends RecyclerView.ViewHolder {
        private final FrameLayout cardContainer;
        private final MaterialCardView cardWord;
        private final View containerTop, dotLevel;
        private final ImageView imgWord;
        private final FloatingActionButton fabSound;
        private final TextView tvEmoji, tvSrsLevel, tvCategoryTag, tvWord, tvPronunciation, tvMeaningVi, tvExample,
                tvReviewInfo;
        private final LinearLayout containerExample, containerReviewInfo;
        private final MaterialButton btnKnow, btnLearn;

        WordViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.card_container);
            cardWord = itemView.findViewById(R.id.card_word);
            containerTop = itemView.findViewById(R.id.container_top);
            dotLevel = itemView.findViewById(R.id.dot_level);
            imgWord = itemView.findViewById(R.id.img_word);
            fabSound = itemView.findViewById(R.id.fab_sound);
            tvEmoji = itemView.findViewById(R.id.tv_emoji);
            tvSrsLevel = itemView.findViewById(R.id.tv_srs_level);
            tvCategoryTag = itemView.findViewById(R.id.tv_category_tag);
            tvWord = itemView.findViewById(R.id.tv_word);
            tvPronunciation = itemView.findViewById(R.id.tv_pronunciation);
            tvMeaningVi = itemView.findViewById(R.id.tv_meaning_vi);
            tvExample = itemView.findViewById(R.id.tv_example);
            tvReviewInfo = itemView.findViewById(R.id.tv_review_info);
            containerExample = itemView.findViewById(R.id.container_example);
            containerReviewInfo = itemView.findViewById(R.id.container_review_info);
            btnKnow = itemView.findViewById(R.id.btn_know);
            btnLearn = itemView.findViewById(R.id.btn_learn);
        }

        void bind(SurvivalWord word, int position) {
            // Set word content
            tvWord.setText(word.getWord());
            tvPronunciation.setText("/" + word.getPronunciation() + "/");
            tvMeaningVi.setText(word.getMeaningVi());
            tvCategoryTag.setText(word.getCategory());

            // Set emoji or image
            if (word.getImageUrl() != null && !word.getImageUrl().isEmpty()) {
                imgWord.setVisibility(View.VISIBLE);
                tvEmoji.setVisibility(View.GONE);
                Glide.with(context)
                        .load(word.getImageUrl())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imgWord);
            } else {
                imgWord.setVisibility(View.GONE);
                tvEmoji.setVisibility(View.VISIBLE);
                tvEmoji.setText(word.getEmoji() != null ? word.getEmoji() : "📚");
            }

            // Set example sentence if available
            if (word.getExampleSentence() != null && !word.getExampleSentence().isEmpty()) {
                containerExample.setVisibility(View.VISIBLE);
                tvExample.setText(word.getExampleSentence());
            } else {
                containerExample.setVisibility(View.GONE);
            }

            // Set SRS level
            int srsLevel = Math.min(word.getSrsLevel(), 3);
            tvSrsLevel.setText(SRS_LABELS[srsLevel]);
            tvSrsLevel.setTextColor(SRS_DOT_COLORS[srsLevel]);
            dotLevel.getBackground().setTint(SRS_DOT_COLORS[srsLevel]);

            // Set gradient background based on SRS level
            int[] colors = SRS_COLORS[srsLevel];
            GradientDrawable gradient = new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    colors);
            gradient.setCornerRadii(new float[] { 64, 64, 64, 64, 0, 0, 0, 0 });
            containerTop.setBackground(gradient);

            // Set review info
            if (word.getNextReviewTime() > 0) {
                containerReviewInfo.setVisibility(View.VISIBLE);
                String reviewText = getReviewTimeText(word.getNextReviewTime());
                tvReviewInfo.setText("📅 " + reviewText);
            } else {
                containerReviewInfo.setVisibility(View.GONE);
            }

            // Click listeners
            cardWord.setOnClickListener(v -> {
                animateCardClick(v);
                if (listener != null) {
                    listener.onWordClick(word, position);
                }
            });

            fabSound.setOnClickListener(v -> {
                animateSoundButton(v);
                if (listener != null) {
                    listener.onPlaySound(word, position);
                }
                playSound(word.getAudioUrl());
            });

            btnKnow.setOnClickListener(v -> {
                animateButton(v);
                if (listener != null) {
                    listener.onKnowClick(word, position);
                }
            });

            btnLearn.setOnClickListener(v -> {
                animateButton(v);
                if (listener != null) {
                    listener.onLearnClick(word, position);
                }
            });
        }

        private void animateCardClick(View view) {
            AnimatorSet scaleAnim = new AnimatorSet();

            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1f);

            scaleAnim.playTogether(scaleX, scaleY);
            scaleAnim.setDuration(200);
            scaleAnim.setInterpolator(new OvershootInterpolator(2f));
            scaleAnim.start();
        }

        private void animateSoundButton(View view) {
            // Pulse animation
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.3f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.3f, 1f);

            AnimatorSet pulseAnim = new AnimatorSet();
            pulseAnim.playTogether(scaleX, scaleY);
            pulseAnim.setDuration(300);
            pulseAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            pulseAnim.start();
        }

        private void animateButton(View view) {
            view.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction(() -> view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .setInterpolator(new OvershootInterpolator(2f))
                            .start())
                    .start();
        }
    }

    private String getReviewTimeText(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = timestamp - now;

        if (diff <= 0) {
            return "Review now";
        } else if (diff < 60 * 60 * 1000) { // < 1 hour
            int minutes = (int) (diff / (60 * 1000));
            return "In " + minutes + " min";
        } else if (diff < 24 * 60 * 60 * 1000) { // < 1 day
            int hours = (int) (diff / (60 * 60 * 1000));
            return "In " + hours + " hours";
        } else if (diff < 7 * 24 * 60 * 60 * 1000) { // < 1 week
            int days = (int) (diff / (24 * 60 * 60 * 1000));
            return "In " + days + " days";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }

    private void playSound(String audioUrl) {
        if (audioUrl == null || audioUrl.isEmpty())
            return;

        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
