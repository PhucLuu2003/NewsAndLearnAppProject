package com.example.newsandlearn.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Adapter for multiple choice options with pill-shaped buttons
 */
public class MultipleChoiceAdapter extends RecyclerView.Adapter<MultipleChoiceAdapter.OptionViewHolder> {

    private List<String> options;
    private String correctAnswer;
    private OnOptionSelectedListener listener;
    private int selectedPosition = -1;
    private boolean isAnswered = false;

    private static final String[] OPTION_LETTERS = { "A", "B", "C", "D", "E", "F" };

    public interface OnOptionSelectedListener {
        void onOptionSelected(String selectedOption, boolean isCorrect);
    }

    public MultipleChoiceAdapter(List<String> options, String correctAnswer,
            OnOptionSelectedListener listener) {
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_multiple_choice_option, parent, false);
        return new OptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OptionViewHolder holder, int position) {
        String option = options.get(position);
        String letter = position < OPTION_LETTERS.length ? OPTION_LETTERS[position] : String.valueOf(position + 1);

        holder.bind(option, letter, position);
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    class OptionViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView optionCard;
        TextView optionLetter;
        TextView optionText;
        ImageView statusIcon;

        OptionViewHolder(@NonNull View itemView) {
            super(itemView);
            optionCard = itemView.findViewById(R.id.option_card);
            optionLetter = itemView.findViewById(R.id.option_letter);
            optionText = itemView.findViewById(R.id.option_text);
            statusIcon = itemView.findViewById(R.id.status_icon);
        }

        void bind(String option, String letter, int position) {
            optionLetter.setText(letter);
            optionText.setText(option);

            if (!isAnswered) {
                // Enable selection
                optionCard.setOnClickListener(v -> {
                    if (!isAnswered) {
                        // Press effect animation
                        v.animate()
                                .scaleX(0.95f)
                                .scaleY(0.95f)
                                .setDuration(100)
                                .withEndAction(() -> {
                                    v.animate()
                                            .scaleX(1f)
                                            .scaleY(1f)
                                            .setDuration(100)
                                            .start();
                                })
                                .start();

                        selectOption(position, option);
                    }
                });

                // Reset to default state
                optionCard.setCardBackgroundColor(
                        itemView.getContext().getColor(R.color.dark_card));
                optionCard.setStrokeColor(
                        itemView.getContext().getColor(R.color.blue));
                statusIcon.setVisibility(View.GONE);
            } else {
                // Show answer state
                boolean isCorrect = option.equals(correctAnswer);
                boolean isSelected = position == selectedPosition;

                if (isCorrect) {
                    // Highlight correct answer in green
                    optionCard.setCardBackgroundColor(
                            itemView.getContext().getColor(R.color.green_translucent));
                    optionCard.setStrokeColor(
                            itemView.getContext().getColor(R.color.green));
                    statusIcon.setImageResource(R.drawable.ic_check);
                    statusIcon.setVisibility(View.VISIBLE);
                } else if (isSelected) {
                    // Highlight selected wrong answer in red
                    optionCard.setCardBackgroundColor(
                            itemView.getContext().getColor(R.color.red_translucent));
                    optionCard.setStrokeColor(
                            itemView.getContext().getColor(R.color.red));
                    statusIcon.setImageResource(R.drawable.ic_close);
                    statusIcon.setVisibility(View.VISIBLE);

                    // Shake animation for wrong answer
                    Animation shake = AnimationUtils.loadAnimation(
                            itemView.getContext(), R.anim.shake);
                    optionCard.startAnimation(shake);
                }

                optionCard.setOnClickListener(null);
            }
        }
    }

    private void selectOption(int position, String option) {
        selectedPosition = position;
        isAnswered = true;

        boolean isCorrect = option.equals(correctAnswer);

        // Update all items to show answer state
        notifyDataSetChanged();

        // Notify listener
        if (listener != null) {
            listener.onOptionSelected(option, isCorrect);
        }
    }
}
