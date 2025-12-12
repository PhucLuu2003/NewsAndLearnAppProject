package com.example.newsandlearn.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newsandlearn.Activity.VideoLessonActivity;
import com.example.newsandlearn.Model.VideoLesson;
import com.example.newsandlearn.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying video lessons in a horizontal RecyclerView
 */
public class VideoLessonsAdapter extends RecyclerView.Adapter<VideoLessonsAdapter.VideoLessonViewHolder> {

    private Context context;
    private List<VideoLesson> videoLessons;
    private FirebaseFirestore db;

    public VideoLessonsAdapter(Context context, List<VideoLesson> videoLessons) {
        this.context = context;
        this.videoLessons = videoLessons != null ? videoLessons : new ArrayList<>();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public VideoLessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_video_lesson, parent, false);
        return new VideoLessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoLessonViewHolder holder, int position) {
        VideoLesson lesson = videoLessons.get(position);
        holder.bind(lesson);
    }

    @Override
    public int getItemCount() {
        return videoLessons.size();
    }

    public void updateData(List<VideoLesson> newLessons) {
        this.videoLessons = newLessons != null ? newLessons : new ArrayList<>();
        notifyDataSetChanged();
    }

    class VideoLessonViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView videoLessonCard;
        ImageView videoThumbnail;
        TextView videoTitle, videoDescription, levelBadge, viewsCount, durationBadge;

        VideoLessonViewHolder(@NonNull View itemView) {
            super(itemView);
            videoLessonCard = itemView.findViewById(R.id.video_lesson_card);
            videoThumbnail = itemView.findViewById(R.id.video_thumbnail);
            videoTitle = itemView.findViewById(R.id.video_title);
            videoDescription = itemView.findViewById(R.id.video_description);
            levelBadge = itemView.findViewById(R.id.level_badge);
            viewsCount = itemView.findViewById(R.id.views_count);
            durationBadge = itemView.findViewById(R.id.duration_badge);
        }

        void bind(VideoLesson lesson) {
            // Set text data
            videoTitle.setText(lesson.getTitle() != null ? lesson.getTitle() : "Untitled");
            videoDescription.setText(lesson.getDescription() != null ? lesson.getDescription() : "");

            // Handle null level
            String level = lesson.getLevel() != null ? lesson.getLevel() : "A1";
            levelBadge.setText(level);
            durationBadge.setText(lesson.getFormattedDuration());

            // Format views count
            String views = formatViews(lesson.getViews());
            viewsCount.setText(views);

            // Load thumbnail image using Glide
            Glide.with(context)
                    .load(lesson.getThumbnailUrl())
                    .placeholder(R.drawable.video_placeholder) // Custom placeholder
                    .error(R.drawable.video_placeholder) // Custom error drawable
                    .centerCrop()
                    .into(videoThumbnail);

            // Set level badge color based on level
            setLevelBadgeColor(level);

            // Click listener
            videoLessonCard.setOnClickListener(v -> {
                // Update view count
                updateViewCount(lesson);

                // Launch VideoLessonActivity
                Intent intent = new Intent(context, VideoLessonActivity.class);
                intent.putExtra("lesson_id", lesson.getId());
                context.startActivity(intent);
            });
        }

        private void setLevelBadgeColor(String level) {
            // Handle null or empty level
            if (level == null || level.isEmpty()) {
                level = "A1";
            }

            int color;
            switch (level.toUpperCase()) {
                case "A1":
                    color = context.getColor(R.color.green);
                    break;
                case "A2":
                    color = context.getColor(R.color.blue_light);
                    break;
                case "B1":
                    color = context.getColor(R.color.blue);
                    break;
                case "B2":
                    color = 0xFFFFA726; // Orange
                    break;
                case "C1":
                case "C2":
                    color = context.getColor(R.color.red);
                    break;
                default:
                    color = context.getColor(R.color.green);
            }
            levelBadge.setTextColor(color);
        }

        private String formatViews(int views) {
            if (views < 1000) {
                return String.valueOf(views);
            } else if (views < 1000000) {
                return String.format("%.1fK", views / 1000.0);
            } else {
                return String.format("%.1fM", views / 1000000.0);
            }
        }

        private void updateViewCount(VideoLesson lesson) {
            db.collection("video_lessons")
                    .document(lesson.getId())
                    .update("views", lesson.getViews() + 1)
                    .addOnSuccessListener(aVoid -> {
                        lesson.setViews(lesson.getViews() + 1);
                    });
        }
    }
}
