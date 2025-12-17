package com.example.newsandlearn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.Achievement;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.AnimationHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying achievement badges in horizontal carousel
 */
public class AchievementBadgeAdapter extends RecyclerView.Adapter<AchievementBadgeAdapter.BadgeViewHolder> {

    private Context context;
    private List<Achievement> achievements;
    private OnBadgeClickListener listener;

    public interface OnBadgeClickListener {
        void onBadgeClick(Achievement achievement);
    }

    public AchievementBadgeAdapter(Context context, OnBadgeClickListener listener) {
        this.context = context;
        this.achievements = new ArrayList<>();
        this.listener = listener;
    }

    public void setAchievements(List<Achievement> achievements) {
        this.achievements = achievements;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_achievement_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);
        holder.bind(achievement);
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    class BadgeViewHolder extends RecyclerView.ViewHolder {
        CardView badgeIconCard;
        TextView badgeEmoji;
        TextView badgeName;
        TextView badgeProgress;

        public BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            badgeIconCard = itemView.findViewById(R.id.badge_icon_card);
            badgeEmoji = itemView.findViewById(R.id.badge_emoji);
            badgeName = itemView.findViewById(R.id.badge_name);
            badgeProgress = itemView.findViewById(R.id.badge_progress);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    AnimationHelper.bounce(context, v);
                    listener.onBadgeClick(achievements.get(position));
                }
            });
        }

        public void bind(Achievement achievement) {
            badgeName.setText(achievement.getName());
            badgeEmoji.setText(achievement.getIcon());

            // Show progress if not unlocked
            if (achievement.isUnlocked()) {
                badgeProgress.setVisibility(View.GONE);
                // Full opacity for unlocked
                itemView.setAlpha(1.0f);
            } else {
                badgeProgress.setVisibility(View.VISIBLE);
                badgeProgress.setText(achievement.getCurrentProgress() + "/" + achievement.getRequiredProgress());
                // Reduced opacity for locked
                itemView.setAlpha(0.5f);
            }

            // Set badge color based on type
            int backgroundColor = getBadgeColor(achievement.getType());
            badgeIconCard.setCardBackgroundColor(backgroundColor);

            // Add pulse animation for newly unlocked badges
            if (achievement.isUnlocked() && achievement.isNewlyUnlocked()) {
                AnimationHelper.pulse(context, itemView);
            }
        }

        private int getBadgeColor(String type) {
            switch (type.toLowerCase()) {
                case "gold":
                    return context.getResources().getColor(R.color.gradient_gold_start);
                case "silver":
                    return context.getResources().getColor(R.color.gradient_blue_purple_start);
                case "bronze":
                    return context.getResources().getColor(R.color.gradient_pink_orange_start);
                default:
                    return context.getResources().getColor(R.color.gradient_blue_purple_start);
            }
        }
    }
}
