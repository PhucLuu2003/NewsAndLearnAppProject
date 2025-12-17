package com.example.newsandlearn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.GrammarLesson;
import com.example.newsandlearn.R;

import java.util.List;

/**
 * GrammarAdapter - RecyclerView adapter for grammar lessons
 * Displays data loaded from Firebase dynamically
 */
public class GrammarAdapter extends RecyclerView.Adapter<GrammarAdapter.GrammarViewHolder> {

    private Context context;
    private List<GrammarLesson> lessonList;
    private OnLessonClickListener listener;

    public interface OnLessonClickListener {
        void onLessonClick(GrammarLesson lesson);
    }

    public GrammarAdapter(Context context, List<GrammarLesson> lessonList, OnLessonClickListener listener) {
        this.context = context;
        this.lessonList = lessonList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GrammarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_grammar_card, parent, false);
        return new GrammarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GrammarViewHolder holder, int position) {
        GrammarLesson lesson = lessonList.get(position);
        holder.bind(lesson);
        
        // Add animation
        com.example.newsandlearn.Utils.AnimationHelper.itemFallDown(context, holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return lessonList.size();
    }

    class GrammarViewHolder extends RecyclerView.ViewHolder {

        TextView lessonTitle, lessonDescription, levelBadge;
        TextView categoryText, exerciseCount, statusText; // Added statusText
        ImageView completionIcon;

        public GrammarViewHolder(@NonNull View itemView) {
            super(itemView);

            lessonTitle = itemView.findViewById(R.id.lesson_title);
            lessonDescription = itemView.findViewById(R.id.lesson_description);
            levelBadge = itemView.findViewById(R.id.level_badge);
            categoryText = itemView.findViewById(R.id.category_text);
            exerciseCount = itemView.findViewById(R.id.exercise_count);
            completionIcon = itemView.findViewById(R.id.completion_icon);
            statusText = itemView.findViewById(R.id.status_text); // Bind new view
        }

        public void bind(GrammarLesson lesson) {
            // Set title and description
            lessonTitle.setText(lesson.getTitle());
            lessonDescription.setText(lesson.getDescription());

            // Set level badge
            levelBadge.setText(lesson.getLevel() != null ? lesson.getLevel() : "B1");
            updateLevelBadgeColor(lesson.getLevel());

            // Set category
            String category = lesson.getCategory();
            if (category != null) {
                category = category.toUpperCase(); // Uppercase for badge style
            } else {
                category = "GRAMMAR";
            }
            categoryText.setText(category);

            // Set exercise count
            int count = lesson.getExerciseCount();
            exerciseCount.setText(count + (count == 1 ? " exercise" : " exercises"));

            // Show completion icon and status text
            if (lesson.isCompleted()) {
                completionIcon.setVisibility(View.VISIBLE);
                statusText.setText("Completed");
                statusText.setTextColor(context.getColor(R.color.success)); // Green
            } else {
                completionIcon.setVisibility(View.GONE);
                statusText.setText("Start");
                statusText.setTextColor(0xFF1976D2); // Blue
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                com.example.newsandlearn.Utils.AnimationHelper.scaleUp(context, itemView);
                if (listener != null) {
                    listener.onLessonClick(lesson);
                }
            });
        }

        private void updateLevelBadgeColor(String level) {
            int backgroundRes;
            if (level == null) {
                backgroundRes = R.drawable.badge_learning;
            } else if (level.startsWith("A")) {
                backgroundRes = R.drawable.badge_new;
            } else if (level.startsWith("B")) {
                backgroundRes = R.drawable.badge_learning;
            } else if (level.startsWith("C")) {
                backgroundRes = R.drawable.badge_known;
            } else {
                backgroundRes = R.drawable.badge_learning;
            }
            levelBadge.setBackgroundResource(backgroundRes);
        }
    }

    public void updateData(List<GrammarLesson> newLessonList) {
        this.lessonList = newLessonList;
        notifyDataSetChanged();
    }
}
