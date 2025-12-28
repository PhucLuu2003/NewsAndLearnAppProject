package com.example.newsandlearn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Map;

/**
 * AdminLessonAdapter - Simple adapter to display lessons in admin panel
 */
public class AdminLessonAdapter extends RecyclerView.Adapter<AdminLessonAdapter.ViewHolder> {

    private Context context;
    private List<Map<String, Object>> lessons;

    public AdminLessonAdapter(Context context, List<Map<String, Object>> lessons) {
        this.context = context;
        this.lessons = lessons;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_lesson, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> lesson = lessons.get(position);
        
        String title = (String) lesson.get("title");
        String level = (String) lesson.get("level");
        String category = (String) lesson.get("category");
        
        holder.title.setText(title != null ? title : "Untitled Lesson");
        holder.level.setText(level != null ? level : "");
        holder.category.setText(category != null ? category : "");
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView title, level, category;

        ViewHolder(View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            title = itemView.findViewById(R.id.lesson_title);
            level = itemView.findViewById(R.id.lesson_level);
            category = itemView.findViewById(R.id.lesson_category);
        }
    }
}
