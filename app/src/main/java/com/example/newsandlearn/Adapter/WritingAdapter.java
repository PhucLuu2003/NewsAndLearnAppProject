package com.example.newsandlearn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.WritingPrompt;
import com.example.newsandlearn.R;

import java.util.List;

/**
 * WritingAdapter - RecyclerView adapter for writing prompts
 * Displays prompts loaded from Firebase dynamically
 */
public class WritingAdapter extends RecyclerView.Adapter<WritingAdapter.WritingViewHolder> {

    private Context context;
    private List<WritingPrompt> promptList;
    private OnPromptClickListener listener;

    public interface OnPromptClickListener {
        void onPromptClick(WritingPrompt prompt);
    }

    public WritingAdapter(Context context, List<WritingPrompt> promptList, OnPromptClickListener listener) {
        this.context = context;
        this.promptList = promptList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WritingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_writing_card, parent, false);
        return new WritingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WritingViewHolder holder, int position) {
        WritingPrompt prompt = promptList.get(position);
        holder.bind(prompt);
        
        // Add animation
        com.example.newsandlearn.Utils.AnimationHelper.itemFallDown(context, holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return promptList.size();
    }

    class WritingViewHolder extends RecyclerView.ViewHolder {

        TextView promptTitle, promptText, levelText, wordRequirement, timeLimit, actionButton; // Added actionButton
        LinearLayout scoreLayout;
        TextView scoreText;

        public WritingViewHolder(@NonNull View itemView) {
            super(itemView);

            promptTitle = itemView.findViewById(R.id.prompt_title);
            promptText = itemView.findViewById(R.id.prompt_text);
            levelText = itemView.findViewById(R.id.level_text);
            wordRequirement = itemView.findViewById(R.id.word_requirement);
            timeLimit = itemView.findViewById(R.id.time_limit);
            scoreLayout = itemView.findViewById(R.id.score_layout);
            scoreText = itemView.findViewById(R.id.score_text);
            actionButton = itemView.findViewById(R.id.action_button); // Bind new view
        }

        public void bind(WritingPrompt prompt) {
            promptTitle.setText(prompt.getTitle());
            promptText.setText(prompt.getPromptText());
            levelText.setText(prompt.getLevel() != null ? prompt.getLevel() : "B1");
            wordRequirement.setText(prompt.getMinWords() + "-" + prompt.getMaxWords() + " words");
            timeLimit.setText(prompt.getTimeMinutes() + " min");

            if (prompt.isCompleted() && prompt.getOverallScore() > 0) {
                scoreLayout.setVisibility(View.VISIBLE);
                actionButton.setVisibility(View.GONE);
                scoreText.setText(prompt.getOverallScore() + "%");
            } else {
                scoreLayout.setVisibility(View.GONE);
                actionButton.setVisibility(View.VISIBLE);
            }

            itemView.setOnClickListener(v -> {
                com.example.newsandlearn.Utils.AnimationHelper.scaleUp(context, itemView);
                if (listener != null) {
                    listener.onPromptClick(prompt);
                }
            });
        }
    }

    public void updateData(List<WritingPrompt> newPromptList) {
        this.promptList = newPromptList;
        notifyDataSetChanged();
    }
}
