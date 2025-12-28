package com.example.newsandlearn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newsandlearn.Model.ListeningLesson;
import com.example.newsandlearn.R;

import java.util.List;
import java.util.Locale;

public class ListeningAdapter extends RecyclerView.Adapter<ListeningAdapter.ListeningViewHolder> {

    private Context context;
    private List<ListeningLesson> lessonList;
    private OnLessonClickListener listener;

    public interface OnLessonClickListener {
        void onLessonClick(ListeningLesson lesson);
    }

    public ListeningAdapter(Context context, List<ListeningLesson> lessonList, OnLessonClickListener listener) {
        this.context = context;
        this.lessonList = lessonList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ListeningViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_listening_card, parent, false);
        return new ListeningViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListeningViewHolder holder, int position) {
        ListeningLesson lesson = lessonList.get(position);
        holder.bind(lesson);

        com.example.newsandlearn.Utils.AnimationHelper.itemFallDown(context, holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return lessonList != null ? lessonList.size() : 0;
    }

    class ListeningViewHolder extends RecyclerView.ViewHolder {

        ImageView thumbnail;
        TextView lessonTitle, levelText, questionsText;
        LinearLayout progressLayout;
        ProgressBar progressBar;
        TextView scoreText;

        public ListeningViewHolder(@NonNull View itemView) {
            super(itemView);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            lessonTitle = itemView.findViewById(R.id.lesson_title);
            levelText = itemView.findViewById(R.id.level_text);
            questionsText = itemView.findViewById(R.id.questions_text);
            progressLayout = itemView.findViewById(R.id.progress_layout);
            progressBar = itemView.findViewById(R.id.score_progress_bar);
            scoreText = itemView.findViewById(R.id.score_text);
        }

        public void bind(ListeningLesson lesson) {
            if (lesson == null) return;

            // Set title
            if (lessonTitle != null && lesson.getTitle() != null) {
                lessonTitle.setText(lesson.getTitle());
            }

            // Set level badge
            if (levelText != null) {
                levelText.setText(lesson.getLevel() != null ? lesson.getLevel() : "A1");
            }

            // Set question count (no more duration)
            if (questionsText != null) {
                int qCount = lesson.getQuestionCount();
                questionsText.setText(String.format(Locale.getDefault(), "%d %s",
                        qCount, qCount == 1 ? "question" : "questions"));
            }

            // Handle completed vs in-progress vs not started
            if (progressLayout != null) {
                if (lesson.isCompleted() && lesson.getUserScore() > 0) {
                    // Completed lesson - show score
                    progressLayout.setVisibility(View.VISIBLE);
                    if (progressBar != null) {
                        progressBar.setProgress(lesson.getUserScore());
                    }
                    if (scoreText != null) {
                        scoreText.setText(String.format(Locale.getDefault(), "%d%%", lesson.getUserScore()));
                        try {
                            scoreText.setTextColor(context.getColor(R.color.success_green));
                        } catch (Exception e) {
                            scoreText.setTextColor(0xFF4CAF50); // Fallback green color
                        }
                    }
                } else if (lesson.getTimesListened() > 0 && !lesson.isCompleted()) {
                    // In progress - show estimated progress
                    progressLayout.setVisibility(View.VISIBLE);
                    if (progressBar != null) {
                        int estimatedProgress = Math.min(lesson.getTimesListened() * 20, 80);
                        progressBar.setProgress(estimatedProgress);
                    }
                    if (scoreText != null) {
                        scoreText.setText("In Progress");
                        try {
                            scoreText.setTextColor(context.getColor(R.color.primary));
                        } catch (Exception e) {
                            scoreText.setTextColor(0xFF6200EE); // Fallback primary color
                        }
                    }
                } else {
                    // Not started - hide progress
                    progressLayout.setVisibility(View.GONE);
                }
            }

            // Load thumbnail
            if (thumbnail != null && lesson.getThumbnailUrl() != null && !lesson.getThumbnailUrl().isEmpty()) {
                Glide.with(context)
                        .load(lesson.getThumbnailUrl())
                        .placeholder(R.color.surface_secondary)
                        .error(R.color.surface_secondary)
                        .into(thumbnail);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLessonClick(lesson);
                }
            });
        }
    }

    public void updateData(List<ListeningLesson> newLessonList) {
        this.lessonList = newLessonList;
        notifyDataSetChanged();
    }
}