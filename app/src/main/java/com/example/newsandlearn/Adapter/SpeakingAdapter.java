package com.example.newsandlearn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.SpeakingLesson;
import com.example.newsandlearn.R;

import java.util.List;

/**
 * SpeakingAdapter - RecyclerView adapter for speaking lessons
 * Displays data loaded from Firebase dynamically
 */
public class SpeakingAdapter extends RecyclerView.Adapter<SpeakingAdapter.SpeakingViewHolder> {

    private Context context;
    private List<SpeakingLesson> lessonList;
    private OnLessonClickListener listener;

    public interface OnLessonClickListener {
        void onLessonClick(SpeakingLesson lesson);
    }

    public SpeakingAdapter(Context context, List<SpeakingLesson> lessonList, OnLessonClickListener listener) {
        this.context = context;
        this.lessonList = lessonList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SpeakingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_speaking_card, parent, false);
        return new SpeakingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpeakingViewHolder holder, int position) {
        SpeakingLesson lesson = lessonList.get(position);
        holder.bind(lesson);
        
        // Add animation
        com.example.newsandlearn.Utils.AnimationHelper.itemFallDown(context, holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return lessonList.size();
    }

    class SpeakingViewHolder extends RecyclerView.ViewHolder {

        TextView lessonTitle, levelText, durationText, promptsText;
        LinearLayout scoreLayout;
        TextView pronunciationScoreText, fluencyScoreText, attemptsText;

        public SpeakingViewHolder(@NonNull View itemView) {
            super(itemView);

            lessonTitle = itemView.findViewById(R.id.prompt_title);
            levelText = itemView.findViewById(R.id.level_badge);
            durationText = itemView.findViewById(R.id.duration_text);
            promptsText = itemView.findViewById(R.id.prompts_text);
            scoreLayout = itemView.findViewById(R.id.score_layout);
            pronunciationScoreText = itemView.findViewById(R.id.pronunciation_score_text);
            fluencyScoreText = itemView.findViewById(R.id.fluency_score_text);
            attemptsText = itemView.findViewById(R.id.attempts_text);
        }

        public void bind(SpeakingLesson lesson) {
            lessonTitle.setText(lesson.getTitle());
            levelText.setText(lesson.getLevel() != null ? lesson.getLevel() : "B1");
            durationText.setText(lesson.getEstimatedMinutes() + " min");
            
            int pCount = lesson.getPromptCount();
            promptsText.setText(pCount + (pCount == 1 ? " prompt" : " prompts"));

            if (lesson.getAttemptCount() > 0) {
                scoreLayout.setVisibility(View.VISIBLE);
                pronunciationScoreText.setText(lesson.getPronunciationScore() + "%");
                fluencyScoreText.setText(lesson.getFluencyScore() + "%");
                attemptsText.setText(String.valueOf(lesson.getAttemptCount()));
            } else {
                scoreLayout.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                com.example.newsandlearn.Utils.AnimationHelper.scaleUp(context, itemView); // Scale animation
                if (listener != null) {
                    listener.onLessonClick(lesson);
                }
            });
        }
    }

    public void updateData(List<SpeakingLesson> newLessonList) {
        this.lessonList = newLessonList;
        notifyDataSetChanged();
    }
}
