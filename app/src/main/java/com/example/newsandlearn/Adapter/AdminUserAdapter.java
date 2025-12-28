package com.example.newsandlearn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.User;
import com.example.newsandlearn.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * AdminUserAdapter - Adapter for displaying users in admin panel
 */
public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private Context context;
    private List<User> users;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onUserAction(User user, String action);
    }

    public AdminUserAdapter(Context context, List<User> users, OnUserActionListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        
        holder.userName.setText(user.getName());
        holder.userEmail.setText(user.getEmail());
        holder.userRole.setText("Role: " + (user.getRole() != null ? user.getRole() : "user"));
        holder.userLevel.setText("Level " + user.getLevel() + " • " + user.getXp() + " XP");
        
        // Set role badge color
        if ("admin".equals(user.getRole())) {
            holder.userRole.setTextColor(context.getResources().getColor(R.color.primary));
        } else {
            holder.userRole.setTextColor(context.getResources().getColor(R.color.text_secondary));
        }
        
        holder.btnEditRole.setOnClickListener(v -> listener.onUserAction(user, "edit_role"));
        holder.btnDelete.setOnClickListener(v -> listener.onUserAction(user, "delete"));
        holder.card.setOnClickListener(v -> listener.onUserAction(user, "view_details"));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView userName, userEmail, userRole, userLevel;
        MaterialButton btnEditRole, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            userName = itemView.findViewById(R.id.user_name);
            userEmail = itemView.findViewById(R.id.user_email);
            userRole = itemView.findViewById(R.id.user_role);
            userLevel = itemView.findViewById(R.id.user_level);
            btnEditRole = itemView.findViewById(R.id.btn_edit_role);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
