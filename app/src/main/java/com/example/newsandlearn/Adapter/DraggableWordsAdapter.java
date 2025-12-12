package com.example.newsandlearn.Adapter;

import android.content.ClipData;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Adapter for draggable word chips in fill-in-the-blank questions
 */
public class DraggableWordsAdapter extends RecyclerView.Adapter<DraggableWordsAdapter.WordViewHolder> {

    private List<String> words;
    private boolean enabled = true;

    public DraggableWordsAdapter(List<String> words) {
        this.words = words;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_draggable_word, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        String word = words.get(position);
        holder.bind(word, enabled);
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView wordCard;
        TextView wordText;

        WordViewHolder(@NonNull View itemView) {
            super(itemView);
            wordCard = itemView.findViewById(R.id.word_card);
            wordText = itemView.findViewById(R.id.word_text);
        }

        void bind(String word, boolean enabled) {
            wordText.setText(word);

            if (enabled) {
                // Set up drag listener
                wordCard.setOnLongClickListener(v -> {
                    // Start drag operation
                    ClipData data = ClipData.newPlainText("word", word);
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                    v.startDragAndDrop(data, shadowBuilder, v, 0);

                    // Animate elevation during drag
                    v.animate()
                            .scaleX(1.1f)
                            .scaleY(1.1f)
                            .alpha(0.5f)
                            .setDuration(200)
                            .start();

                    return true;
                });

                // Reset animation when drag ends
                wordCard.setOnDragListener((v, event) -> {
                    if (event.getAction() == android.view.DragEvent.ACTION_DRAG_ENDED) {
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .alpha(1f)
                                .setDuration(200)
                                .start();
                    }
                    return true;
                });

                wordCard.setAlpha(1f);
            } else {
                wordCard.setOnLongClickListener(null);
                wordCard.setAlpha(0.5f);
            }
        }
    }
}
