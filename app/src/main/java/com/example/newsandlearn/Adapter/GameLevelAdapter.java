package com.example.newsandlearn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.GameLevel;
import com.example.newsandlearn.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying game levels
 */
public class GameLevelAdapter extends RecyclerView.Adapter<GameLevelAdapter.LevelViewHolder> {

    private Context context;
    private List<GameLevel> levels;
    private OnLevelClickListener listener;

    public interface OnLevelClickListener {
        void onLevelClick(GameLevel level);
    }

    public GameLevelAdapter(Context context, OnLevelClickListener listener) {
        this.context = context;
        this.levels = new ArrayList<>();
        this.listener = listener;
    }

    public void setLevels(List<GameLevel> levels) {
        this.levels = levels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_game_level, parent, false);
        return new LevelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        GameLevel level = levels.get(position);
        holder.bind(level);
    }

    @Override
    public int getItemCount() {
        return levels.size();
    }

    class LevelViewHolder extends RecyclerView.ViewHolder {
        TextView levelNumber, levelName, levelDescription;
        TextView star1, star2, star3;
        ImageView statusIcon;
        View lockedOverlay;

        public LevelViewHolder(@NonNull View itemView) {
            super(itemView);
            levelNumber = itemView.findViewById(R.id.level_number);
            levelName = itemView.findViewById(R.id.level_name);
            levelDescription = itemView.findViewById(R.id.level_description);
            star1 = itemView.findViewById(R.id.star1);
            star2 = itemView.findViewById(R.id.star2);
            star3 = itemView.findViewById(R.id.star3);
            statusIcon = itemView.findViewById(R.id.status_icon);
            lockedOverlay = itemView.findViewById(R.id.locked_overlay);
        }

        public void bind(GameLevel level) {
            levelNumber.setText(String.valueOf(level.getLevelNumber()));
            levelName.setText(level.getName());
            levelDescription.setText(level.getDescription());

            // Update stars
            updateStars(level.getStarsEarned());

            // Update lock status
            if (level.isUnlocked()) {
                lockedOverlay.setVisibility(View.GONE);
                statusIcon.setImageResource(level.isCompleted() ? 
                    android.R.drawable.checkbox_on_background : 
                    android.R.drawable.ic_media_play);
                itemView.setAlpha(1.0f);
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onLevelClick(level);
                    }
                });
            } else {
                lockedOverlay.setVisibility(View.VISIBLE);
                statusIcon.setImageResource(android.R.drawable.ic_lock_lock);
                itemView.setAlpha(0.6f);
                itemView.setOnClickListener(null);
            }
        }

        private void updateStars(int stars) {
            star1.setAlpha(stars >= 1 ? 1.0f : 0.3f);
            star2.setAlpha(stars >= 2 ? 1.0f : 0.3f);
            star3.setAlpha(stars >= 3 ? 1.0f : 0.3f);
        }
    }
}
