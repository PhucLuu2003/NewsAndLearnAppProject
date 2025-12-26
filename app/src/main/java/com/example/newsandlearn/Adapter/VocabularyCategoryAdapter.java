package com.example.newsandlearn.Adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.VocabularyCategory;
import com.example.newsandlearn.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * 📂 VocabularyCategoryAdapter - Beautiful grid adapter for vocabulary
 * categories
 * Shows progress, emoji icons, and gradient backgrounds
 */
public class VocabularyCategoryAdapter extends RecyclerView.Adapter<VocabularyCategoryAdapter.CategoryViewHolder> {

    private List<VocabularyCategory> categories = new ArrayList<>();
    private final Context context;
    private final OnCategoryClickListener listener;

    // Predefined gradient pairs for categories
    private static final int[][] GRADIENT_COLORS = {
            { 0xFF10B981, 0xFF14B8A6 }, // Green-Teal (Greetings)
            { 0xFF3B82F6, 0xFF06B6D4 }, // Blue-Cyan (Numbers)
            { 0xFFF59E0B, 0xFFF97316 }, // Amber-Orange (Colors)
            { 0xFF8B5CF6, 0xFFA855F7 }, // Purple-Violet (Food)
            { 0xFFEC4899, 0xFFF43F5E }, // Pink-Rose (Family)
            { 0xFF6366F1, 0xFF8B5CF6 }, // Indigo-Purple (Weather)
            { 0xFF14B8A6, 0xFF06B6D4 }, // Teal-Cyan (Body)
            { 0xFFEF4444, 0xFFF97316 }, // Red-Orange (Emotions)
            { 0xFF84CC16, 0xFF22C55E }, // Lime-Green (Places)
            { 0xFF0EA5E9, 0xFF3B82F6 }, // Sky-Blue (Transport)
    };

    public interface OnCategoryClickListener {
        void onCategoryClick(VocabularyCategory category, int position);
    }

    public VocabularyCategoryAdapter(Context context, OnCategoryClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setCategories(List<VocabularyCategory> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void updateCategory(int position, VocabularyCategory category) {
        if (position >= 0 && position < categories.size()) {
            categories.set(position, category);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vocabulary_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        VocabularyCategory category = categories.get(position);
        holder.bind(category, position);

        // Staggered animation
        animateCardEntry(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    private void animateCardEntry(View view, int position) {
        view.setAlpha(0f);
        view.setTranslationY(60f);
        view.setScaleX(0.9f);
        view.setScaleY(0.9f);

        view.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(450)
                .setStartDelay(position * 70L)
                .setInterpolator(new OvershootInterpolator(0.7f))
                .start();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardCategory;
        private final View bgGradient, progressBar;
        private final TextView tvEmoji, tvName, tvNameVi, tvProgress, tvPercent;
        private final ImageView iconCompleted;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCategory = itemView.findViewById(R.id.card_category);
            bgGradient = itemView.findViewById(R.id.bg_gradient);
            tvEmoji = itemView.findViewById(R.id.tv_emoji);
            tvName = itemView.findViewById(R.id.tv_name);
            tvNameVi = itemView.findViewById(R.id.tv_name_vi);
            tvProgress = itemView.findViewById(R.id.tv_progress);
            tvPercent = itemView.findViewById(R.id.tv_percent);
            progressBar = itemView.findViewById(R.id.progress_bar);
            iconCompleted = itemView.findViewById(R.id.icon_completed);
        }

        void bind(VocabularyCategory category, int position) {
            // Set emoji
            tvEmoji.setText(category.getEmoji());

            // Set names
            tvName.setText(category.getName());
            tvNameVi.setText(category.getNameVi());

            // Set progress
            int progress = category.getProgressPercent();
            tvProgress.setText(category.getLearnedCount() + "/" + category.getWordCount());
            tvPercent.setText(progress + "%");

            // Animate progress bar width
            progressBar.post(() -> {
                int maxWidth = ((View) progressBar.getParent()).getWidth();
                int targetWidth = (int) (maxWidth * (progress / 100f));

                ObjectAnimator widthAnim = ObjectAnimator.ofInt(progressBar, "width", 0, targetWidth);
                widthAnim.setDuration(800);
                widthAnim.setStartDelay(position * 100L);
                widthAnim.addUpdateListener(animation -> {
                    int value = (int) animation.getAnimatedValue();
                    ViewGroup.LayoutParams params = progressBar.getLayoutParams();
                    params.width = value;
                    progressBar.setLayoutParams(params);
                });
                widthAnim.start();
            });

            // Show completion badge
            iconCompleted.setVisibility(progress >= 100 ? View.VISIBLE : View.GONE);

            // Set gradient background
            int[] colors = getGradientColors(category, position);
            GradientDrawable gradient = new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    colors);
            gradient.setCornerRadius(context.getResources().getDimension(R.dimen.card_corner_radius));
            bgGradient.setBackground(gradient);

            // Click listener with animation
            cardCategory.setOnClickListener(v -> {
                animateClick(v);
                if (listener != null) {
                    listener.onCategoryClick(category, position);
                }
            });
        }

        private void animateClick(View view) {
            AnimatorSet scaleDown = new AnimatorSet();
            scaleDown.playTogether(
                    ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.92f),
                    ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.92f));
            scaleDown.setDuration(100);

            AnimatorSet scaleUp = new AnimatorSet();
            scaleUp.playTogether(
                    ObjectAnimator.ofFloat(view, "scaleX", 0.92f, 1f),
                    ObjectAnimator.ofFloat(view, "scaleY", 0.92f, 1f));
            scaleUp.setDuration(150);
            scaleUp.setInterpolator(new OvershootInterpolator(2f));

            AnimatorSet fullAnim = new AnimatorSet();
            fullAnim.playSequentially(scaleDown, scaleUp);
            fullAnim.start();
        }
    }

    private int[] getGradientColors(VocabularyCategory category, int position) {
        // Try to use category's own colors first
        if (category.getGradientColors() != null && category.getGradientColors().size() >= 2) {
            String color1 = category.getGradientColors().get(0);
            String color2 = category.getGradientColors().get(1);
            try {
                return new int[] {
                        android.graphics.Color.parseColor(color1),
                        android.graphics.Color.parseColor(color2)
                };
            } catch (Exception ignored) {
            }
        }

        // Fallback to predefined colors based on position
        int colorIndex = position % GRADIENT_COLORS.length;
        return GRADIENT_COLORS[colorIndex];
    }
}
