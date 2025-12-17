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

import com.example.newsandlearn.Model.DailyTask;
import com.example.newsandlearn.R;

import java.util.List;

/**
 * DailyTaskAdapter - RecyclerView adapter for daily tasks
 * Displays tasks loaded from Firebase dynamically
 */
public class DailyTaskAdapter extends RecyclerView.Adapter<DailyTaskAdapter.TaskViewHolder> {

    private Context context;
    private List<DailyTask> taskList;

    public DailyTaskAdapter(Context context, List<DailyTask> taskList) {
        this.context = context;
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_daily_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        DailyTask task = taskList.get(position);
        holder.bind(task);
        
        // Add animation
        com.example.newsandlearn.Utils.AnimationHelper.itemFallDown(context, holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView taskIcon, taskTitle, taskDescription;
        TextView progressText, rewardText;
        ProgressBar taskProgress;
        ImageView completionCheck;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            taskIcon = itemView.findViewById(R.id.task_icon);
            taskTitle = itemView.findViewById(R.id.task_title);
            taskDescription = itemView.findViewById(R.id.task_description);
            taskProgress = itemView.findViewById(R.id.task_progress);
            progressText = itemView.findViewById(R.id.progress_text);
            rewardText = itemView.findViewById(R.id.reward_text);
            completionCheck = itemView.findViewById(R.id.completion_check);
        }

        public void bind(DailyTask task) {
            // Set icon from task (emoji)
            taskIcon.setText(task.getIcon() != null ? task.getIcon() : "🎯");

            // Set title and description
            taskTitle.setText(task.getTitle());
            taskDescription.setText(task.getDescription());

            // Set progress
            int progress = task.getProgress();
            taskProgress.setProgress(progress);
            progressText.setText(task.getProgressText());

            // Set reward
            rewardText.setText("+" + task.getXpReward() + " XP");

            // Show completion check if completed
            if (task.isCompleted()) {
                completionCheck.setVisibility(View.VISIBLE);
                
                // Change colors to indicate completion
                taskTitle.setTextColor(context.getColor(R.color.success));
                rewardText.setBackgroundTintList(android.content.res.ColorStateList.valueOf(context.getColor(R.color.success_light)));
                rewardText.setTextColor(context.getColor(R.color.success_dark));
            } else {
                completionCheck.setVisibility(View.GONE);
                taskTitle.setTextColor(0xFF212121); // Dark grey
                rewardText.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFF3E0)); // Light orange
                rewardText.setTextColor(0xFFFF9800); // Orange
            }

            itemView.setOnClickListener(v -> {
                 com.example.newsandlearn.Utils.AnimationHelper.scaleUp(context, itemView);
            });
        }
    }

    public void updateData(List<DailyTask> newTaskList) {
        this.taskList = newTaskList;
        notifyDataSetChanged();
    }
}
