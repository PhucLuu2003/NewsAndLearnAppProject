package com.example.newsandlearn.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.GameSession;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.PronunciationScoreCalculator;

import java.util.List;

/**
 * Adapter to display word results in the game result screen
 */
public class WordResultAdapter extends RecyclerView.Adapter<WordResultAdapter.ViewHolder> {

    private List<GameSession.HitResult> hitResults;

    public WordResultAdapter(List<GameSession.HitResult> hitResults) {
        this.hitResults = hitResults;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_word_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameSession.HitResult result = hitResults.get(position);
        
        // Set word
        holder.wordText.setText(result.getTargetWord());
        
        // Set phonetic (if available - we'll need to pass this from the song)
        // For now, hide it
        holder.phoneticText.setVisibility(View.GONE);
        
        // Set spoken word
        if (result.getSpokenWord() != null && !result.getSpokenWord().isEmpty()) {
            holder.spokenText.setText("You said: " + result.getSpokenWord());
            holder.spokenText.setVisibility(View.VISIBLE);
        } else {
            holder.spokenText.setVisibility(View.GONE);
        }
        
        // Set rating
        String rating = result.getRating();
        holder.ratingBadge.setText(rating);
        
        // Set colors based on rating
        int color = PronunciationScoreCalculator.getColorForRating(rating);
        holder.ratingBadge.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(color));
        
        // Set status icon
        if ("MISS".equals(rating)) {
            holder.statusIcon.setText("✗");
            holder.statusIcon.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336")));
        } else if ("PERFECT".equals(rating)) {
            holder.statusIcon.setText("⭐");
            holder.statusIcon.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(Color.parseColor("#FFD700")));
        } else if ("GREAT".equals(rating)) {
            holder.statusIcon.setText("✓");
            holder.statusIcon.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        } else {
            holder.statusIcon.setText("○");
            holder.statusIcon.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(Color.parseColor("#2196F3")));
        }
    }

    @Override
    public int getItemCount() {
        return hitResults.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView statusIcon;
        TextView wordText;
        TextView phoneticText;
        TextView spokenText;
        TextView ratingBadge;

        ViewHolder(View itemView) {
            super(itemView);
            statusIcon = itemView.findViewById(R.id.status_icon);
            wordText = itemView.findViewById(R.id.word_text);
            phoneticText = itemView.findViewById(R.id.phonetic_text);
            spokenText = itemView.findViewById(R.id.spoken_text);
            ratingBadge = itemView.findViewById(R.id.rating_badge);
        }
    }
}
