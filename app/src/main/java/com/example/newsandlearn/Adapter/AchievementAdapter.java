package com.example.newsandlearn.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.Achievement;
import com.example.newsandlearn.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

    private List<Achievement> achievements;
    private OnAchievementClickListener listener;

    public interface OnAchievementClickListener {
        void onAchievementClick(Achievement achievement);
    }

    public AchievementAdapter() {
        this.achievements = new ArrayList<>();
    }

    public void setAchievements(List<Achievement> achievements) {
        this.achievements = achievements;
        notifyDataSetChanged();
    }

    public void setOnAchievementClickListener(OnAchievementClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);
        holder.bind(achievement, listener);
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        private final TextView achievementIcon;
        private final TextView achievementName;
        private final TextView achievementDescription;
        private final LinearProgressIndicator achievementProgress;
        private final TextView achievementProgressText;
        private final ImageView lockOverlay;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            achievementIcon = itemView.findViewById(R.id.achievementIcon);
            achievementName = itemView.findViewById(R.id.achievementName);
            achievementDescription = itemView.findViewById(R.id.achievementDescription);
            achievementProgress = itemView.findViewById(R.id.achievementProgress);
            achievementProgressText = itemView.findViewById(R.id.achievementProgressText);
            lockOverlay = itemView.findViewById(R.id.lockOverlay);
        }

        public void bind(Achievement achievement, OnAchievementClickListener listener) {
            // Set icon (emoji or icon name)
            String icon = achievement.getIcon();
            if (icon != null && !icon.isEmpty()) {
                achievementIcon.setText(icon);
            } else {
                achievementIcon.setText(getIconEmoji(achievement.getCategory()));
            }

            // Set name and description
            achievementName.setText(achievement.getTitle());
            achievementDescription.setText(achievement.getDescription());

            // Handle locked/unlocked state
            if (achievement.isUnlocked()) {
                // Achievement is unlocked
                lockOverlay.setVisibility(View.GONE);
                achievementProgress.setVisibility(View.GONE);
                achievementProgressText.setVisibility(View.GONE);
                achievementIcon.setAlpha(1.0f);
                achievementName.setAlpha(1.0f);
                achievementDescription.setAlpha(1.0f);
            } else {
                // Achievement is locked, show progress
                lockOverlay.setVisibility(View.VISIBLE);
                achievementIcon.setAlpha(0.5f);
                achievementName.setAlpha(0.7f);
                achievementDescription.setAlpha(0.7f);

                // Show progress if available
                int target = achievement.getConditionTarget();
                int current = achievement.getConditionCurrent();
                if (target > 0) {
                    achievementProgress.setVisibility(View.VISIBLE);
                    achievementProgressText.setVisibility(View.VISIBLE);

                    int progress = (current * 100) / target;
                    achievementProgress.setProgress(Math.min(100, progress));

                    achievementProgressText.setText(current + " / " + target);
                } else {
                    achievementProgress.setVisibility(View.GONE);
                    achievementProgressText.setVisibility(View.GONE);
                }
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAchievementClick(achievement);
                }
            });
        }

        private String getIconEmoji(Achievement.AchievementCategory category) {
            if (category == null)
                return "🏆";
            switch (category) {
                case STREAK:
                    return "🔥";
                case VOCABULARY:
                    return "📚";
                case READING:
                    return "🎓";
                case GRAMMAR:
                    return "📝";
                default:
                    return "🏆";
            }
        }
    }
}
