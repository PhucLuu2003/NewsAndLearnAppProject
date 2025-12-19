package com.example.newsandlearn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.VocabularySet;
import com.example.newsandlearn.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * VocabularySetAdapter - RecyclerView adapter for vocabulary sets
 */
public class VocabularySetAdapter extends RecyclerView.Adapter<VocabularySetAdapter.SetViewHolder> {

    private Context context;
    private List<VocabularySet> setList;
    private SetListener listener;

    public interface SetListener {
        void onSetClick(VocabularySet set);
        void onDownloadClick(VocabularySet set);
        void onDeleteClick(VocabularySet set);
    }

    public VocabularySetAdapter(Context context, List<VocabularySet> setList, SetListener listener) {
        this.context = context;
        this.setList = setList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vocabulary_set, parent, false);
        return new SetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {
        VocabularySet set = setList.get(position);
        holder.bind(set);
    }

    @Override
    public int getItemCount() {
        return setList.size();
    }

    class SetViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView cardView;
        ImageView setIcon;
        TextView titleText, descriptionText, levelBadge, wordCountText;
        TextView downloadCountText, ratingText;
        ProgressBar progressBar;
        TextView progressText;
        MaterialButton actionButton;

        public SetViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = (MaterialCardView) itemView;
            setIcon = itemView.findViewById(R.id.set_icon);
            titleText = itemView.findViewById(R.id.title_text);
            descriptionText = itemView.findViewById(R.id.description_text);
            levelBadge = itemView.findViewById(R.id.level_badge);
            wordCountText = itemView.findViewById(R.id.word_count_text);
            downloadCountText = itemView.findViewById(R.id.download_count_text);
            ratingText = itemView.findViewById(R.id.rating_text);
            progressBar = itemView.findViewById(R.id.progress_bar);
            progressText = itemView.findViewById(R.id.progress_text);
            actionButton = itemView.findViewById(R.id.action_button);
        }

        public void bind(VocabularySet set) {
            // Set basic info
            titleText.setText(set.getTitle());
            descriptionText.setText(set.getDescription());
            levelBadge.setText(set.getLevel());
            wordCountText.setText(set.getWordCount() + " words");

            // Set stats
            if (set.getDownloadCount() > 0) {
                downloadCountText.setVisibility(View.VISIBLE);
                downloadCountText.setText(formatCount(set.getDownloadCount()) + " downloads");
            } else {
                downloadCountText.setVisibility(View.GONE);
            }

            if (set.getRating() > 0) {
                ratingText.setVisibility(View.VISIBLE);
                ratingText.setText("★ " + String.format("%.1f", set.getRating()));
            } else {
                ratingText.setVisibility(View.GONE);
            }

            // Set icon based on category
            setIcon.setImageResource(getCategoryIcon(set.getCategory()));

            // Set level badge color
            updateLevelBadge(set.getLevel());

            // Hide progress by default (will be shown for user's sets)
            progressBar.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);

            // Action button
            actionButton.setText("Download");
            actionButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDownloadClick(set);
                }
            });

            // Card click
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSetClick(set);
                }
            });

            // Add animation
            com.example.newsandlearn.Utils.AnimationHelper.itemFallDown(
                    context, itemView, getAdapterPosition());
        }

        private void updateLevelBadge(String level) {
            int colorRes;
            if (level == null) {
                colorRes = R.color.primary;
            } else {
                switch (level) {
                    case "A1":
                    case "A2":
                        colorRes = R.color.success;
                        break;
                    case "B1":
                    case "B2":
                        colorRes = R.color.warning;
                        break;
                    case "C1":
                    case "C2":
                        colorRes = R.color.error;
                        break;
                    default:
                        colorRes = R.color.primary;
                }
            }
            levelBadge.setBackgroundTintList(
                    context.getResources().getColorStateList(colorRes, null));
        }

        private int getCategoryIcon(String category) {
            return R.drawable.ic_book;
        }

        private String formatCount(int count) {
            if (count >= 1000000) {
                return String.format("%.1fM", count / 1000000.0);
            } else if (count >= 1000) {
                return String.format("%.1fK", count / 1000.0);
            }
            return String.valueOf(count);
        }
    }

    public void updateData(List<VocabularySet> newSetList) {
        this.setList = newSetList;
        notifyDataSetChanged();
    }
}
