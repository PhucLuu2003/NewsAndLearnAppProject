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

import com.example.newsandlearn.Model.Vocabulary;
import com.example.newsandlearn.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * VocabularyAdapter - RecyclerView adapter for vocabulary list
 */
public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder> {

    private Context context;
    private List<Vocabulary> vocabularyList;
    private VocabularyListener listener;

    public interface VocabularyListener {
        void onVocabularyClick(Vocabulary vocabulary);
        void onSpeakerClick(Vocabulary vocabulary);
        void onFavoriteClick(Vocabulary vocabulary);
    }

    public VocabularyAdapter(Context context, List<Vocabulary> vocabularyList, VocabularyListener listener) {
        this.context = context;
        this.vocabularyList = vocabularyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VocabularyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vocabulary_card, parent, false);
        return new VocabularyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabularyViewHolder holder, int position) {
        Vocabulary vocabulary = vocabularyList.get(position);
        holder.bind(vocabulary);
        
        // Add animation
        com.example.newsandlearn.Utils.AnimationHelper.itemFallDown(context, holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return vocabularyList.size();
    }

    class VocabularyViewHolder extends RecyclerView.ViewHolder {
        
        TextView wordText, translationText, masteryBadge, phoneticText; // Added phoneticText
        TextView nextReviewText, masteryPercentageText; // Added masteryPercentageText, removed reviewCountText
        ProgressBar masteryProgress;
        ImageView speakerButton, favoriteButton;

        public VocabularyViewHolder(@NonNull View itemView) {
            super(itemView);
            
            wordText = itemView.findViewById(R.id.word_text);
            translationText = itemView.findViewById(R.id.translation_text);
            phoneticText = itemView.findViewById(R.id.phonetic_text); // Bind new view
            masteryBadge = itemView.findViewById(R.id.mastery_badge);
            // reviewCountText removed from layout
            nextReviewText = itemView.findViewById(R.id.next_review_text);
            masteryProgress = itemView.findViewById(R.id.mastery_progress);
            masteryPercentageText = itemView.findViewById(R.id.mastery_percentage_text); // Bind new view
            speakerButton = itemView.findViewById(R.id.speaker_button);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
        }

        public void bind(Vocabulary vocabulary) {
            // Set word and translation
            wordText.setText(vocabulary.getWord());
            translationText.setText(vocabulary.getTranslation());
            
            // Set pronunciation
            if (vocabulary.getPronunciation() != null && !vocabulary.getPronunciation().isEmpty()) {
                phoneticText.setText(vocabulary.getPronunciation());
                phoneticText.setVisibility(View.VISIBLE);
            } else {
                phoneticText.setVisibility(View.GONE);
            }

            // Set mastery badge
            masteryBadge.setText(vocabulary.getMasteryLevel());
            updateMasteryBadgeBackground(vocabulary.getMastery());

            // Set progress
            int progress = (vocabulary.getMastery() * 100) / 5; // 5 is max mastery
            masteryProgress.setProgress(progress);
            
            // Set percentage text
            masteryPercentageText.setText(progress + "%");

            // Set review info
            nextReviewText.setText("Next review: " + getNextReviewText(vocabulary));

            // Set favorite icon
            if (vocabulary.isFavorite()) {
                favoriteButton.setImageResource(R.drawable.ic_favorite);
                favoriteButton.setColorFilter(context.getColor(R.color.error)); // Consider using a better color from resources or hardcoded red
            } else {
                favoriteButton.setImageResource(R.drawable.ic_favorite_border);
                favoriteButton.setColorFilter(0xFF757575); // Grey color
            }

            // Click listeners - existing logic looks fine
            itemView.setOnClickListener(v -> {
                com.example.newsandlearn.Utils.AnimationHelper.scaleUp(context, itemView); // Add click animation
                if (listener != null) {
                    listener.onVocabularyClick(vocabulary);
                }
            });

            speakerButton.setOnClickListener(v -> {
                com.example.newsandlearn.Utils.AnimationHelper.buttonPress(context, v);
                if (listener != null) {
                    listener.onSpeakerClick(vocabulary);
                }
            });

            favoriteButton.setOnClickListener(v -> {
                com.example.newsandlearn.Utils.AnimationHelper.buttonPress(context, v);
                if (listener != null) {
                    listener.onFavoriteClick(vocabulary);
                }
            });
        }

        private void updateMasteryBadgeBackground(int mastery) {
            int backgroundRes;
            switch (mastery) {
                case 0:
                    // New
                    backgroundRes = R.drawable.badge_background_blue; // Use generic for now or specific
                    break;
                case 1:
                case 2:
                    // Learning
                    backgroundRes = R.drawable.badge_background_blue;
                    break;
                case 3:
                case 4:
                    // Known
                    backgroundRes = R.drawable.badge_background_blue;
                    break;
                case 5:
                    // Mastered
                    backgroundRes = R.drawable.badge_background_blue;
                    break;
                default:
                    backgroundRes = R.drawable.badge_background_blue;
            }
            // For now using the single blue badge I created. 
            // In future can create badge_background_green, badge_background_orange etc.
            masteryBadge.setBackgroundResource(backgroundRes);
        }

        private String getNextReviewText(Vocabulary vocabulary) {
            if (vocabulary.getNextReview() == null) {
                return "now";
            }

            Date now = new Date();
            Date nextReview = vocabulary.getNextReview();

            if (nextReview.before(now)) {
                return "now";
            }

            long diffMillis = nextReview.getTime() - now.getTime();
            long days = TimeUnit.MILLISECONDS.toDays(diffMillis);
            
            if (days > 0) {
                return days + (days == 1 ? " day" : " days");
            } else {
                long hours = TimeUnit.MILLISECONDS.toHours(diffMillis);
                if (hours > 0) return hours + " hrs";
                return "soon";
            }
        }
    }

    public void updateData(List<Vocabulary> newVocabularyList) {
        this.vocabularyList = newVocabularyList;
        notifyDataSetChanged();
    }
}
