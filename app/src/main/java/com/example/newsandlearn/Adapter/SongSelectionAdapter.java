package com.example.newsandlearn.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.PronunciationSong;
import com.example.newsandlearn.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.List;

/**
 * Adapter cho danh sách bài hát
 */
public class SongSelectionAdapter extends RecyclerView.Adapter<SongSelectionAdapter.ViewHolder> {

    private List<PronunciationSong> songs;
    private OnSongClickListener listener;

    public interface OnSongClickListener {
        void onSongClick(PronunciationSong song);
    }

    public SongSelectionAdapter(List<PronunciationSong> songs, OnSongClickListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PronunciationSong song = songs.get(position);
        holder.bind(song, listener);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView titleText, categoryText, durationText, highScoreText;
        Chip difficultyChip, statusChip;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            titleText = itemView.findViewById(R.id.song_title);
            categoryText = itemView.findViewById(R.id.song_category);
            durationText = itemView.findViewById(R.id.song_duration);
            highScoreText = itemView.findViewById(R.id.high_score);
            difficultyChip = itemView.findViewById(R.id.difficulty_chip);
            statusChip = itemView.findViewById(R.id.status_chip);
        }

        void bind(PronunciationSong song, OnSongClickListener listener) {
            titleText.setText("🎵 " + song.getTitle());
            categoryText.setText(song.getCategory());
            
            int minutes = song.getDurationSeconds() / 60;
            int seconds = song.getDurationSeconds() % 60;
            durationText.setText(String.format("⏱️ %d:%02d | 🎯 %d words", 
                minutes, seconds, song.getTotalWords()));
            
            if (song.getHighScore() > 0) {
                highScoreText.setText("🏆 High Score: " + song.getHighScore());
                highScoreText.setVisibility(View.VISIBLE);
            } else {
                highScoreText.setVisibility(View.GONE);
            }

            // Difficulty stars
            String stars = "";
            for (int i = 0; i < song.getDifficulty(); i++) {
                stars += "⭐";
            }
            difficultyChip.setText(stars);

            // Status
            if (song.isUnlocked()) {
                statusChip.setText("▶ Play");
                statusChip.setChipBackgroundColorResource(android.R.color.holo_green_dark);
                cardView.setAlpha(1.0f);
            } else {
                statusChip.setText("🔒 Locked");
                statusChip.setChipBackgroundColorResource(android.R.color.darker_gray);
                cardView.setAlpha(0.6f);
            }

            cardView.setOnClickListener(v -> listener.onSongClick(song));
        }
    }
}
