package com.example.newsandlearn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newsandlearn.Model.ListeningLesson;
import com.example.newsandlearn.R;

import java.util.List;

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
        return lessonList.size();
    }

    class ListeningViewHolder extends RecyclerView.ViewHolder {

        ImageView thumbnail;
        TextView lessonTitle, durationText, levelText, questionsText;
        LinearLayout progressLayout;
        TextView scoreText;

        public ListeningViewHolder(@NonNull View itemView) {
            super(itemView);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            lessonTitle = itemView.findViewById(R.id.lesson_title);
            durationText = itemView.findViewById(R.id.duration_text);
            levelText = itemView.findViewById(R.id.level_text);
            questionsText = itemView.findViewById(R.id.questions_text);
            progressLayout = itemView.findViewById(R.id.progress_layout);
            scoreText = itemView.findViewById(R.id.score_text);
        }

        public void bind(ListeningLesson lesson) {
            lessonTitle.setText(lesson.getTitle());
            durationText.setText(lesson.getFormattedDuration());
            levelText.setText(lesson.getLevel() != null ? lesson.getLevel() : "A1");

            int qCount = lesson.getQuestionCount();
            questionsText.setText(qCount + (qCount == 1 ? " question" : " questions"));

            if (lesson.isCompleted() && lesson.getUserScore() > 0) {
                progressLayout.setVisibility(View.VISIBLE);
                scoreText.setText(lesson.getUserScore() + "%");
            } else {
                progressLayout.setVisibility(View.GONE);
            }

            if (lesson.getThumbnailUrl() != null && !lesson.getThumbnailUrl().isEmpty()) {
                Glide.with(context).load(lesson.getThumbnailUrl()).into(thumbnail);
            }

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
