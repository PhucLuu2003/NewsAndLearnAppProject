package com.example.newsandlearn.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.R;

import java.util.ArrayList;
import java.util.List;

public class AdminStringAdapter extends RecyclerView.Adapter<AdminStringAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onClick(String value);
    }

    private final List<String> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public AdminStringAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<String> values) {
        items.clear();
        if (values != null) {
            items.addAll(values);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String value = items.get(position);
        holder.title.setText(value);
        holder.subtitle.setVisibility(View.GONE);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(value);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView subtitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.row_title);
            subtitle = itemView.findViewById(R.id.row_subtitle);
        }
    }
}
