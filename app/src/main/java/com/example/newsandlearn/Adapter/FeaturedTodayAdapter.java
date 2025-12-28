package com.example.newsandlearn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newsandlearn.Model.Article;
import com.example.newsandlearn.R;

import java.util.ArrayList;
import java.util.List;

public class FeaturedTodayAdapter extends RecyclerView.Adapter<FeaturedTodayAdapter.FeaturedViewHolder> {

    public interface OnFeaturedClickListener {
        void onClick(Article article);
    }

    private final Context context;
    private final OnFeaturedClickListener listener;
    private List<Article> items = new ArrayList<>();

    public FeaturedTodayAdapter(Context context, OnFeaturedClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submitList(List<Article> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeaturedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_featured_today, parent, false);
        return new FeaturedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedViewHolder holder, int position) {
        Article article = items.get(position);

        holder.title.setText(article.getTitle());
        holder.source.setText(article.getSource());
        holder.readTime.setText("⏱ " + article.getReadingTime() + " phút đọc");

        String imageUrl = article.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_article)
                    .error(R.drawable.placeholder_article)
                    .centerCrop()
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.placeholder_article);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(article);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class FeaturedViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView source;
        TextView readTime;

        FeaturedViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.featured_image);
            title = itemView.findViewById(R.id.featured_title);
            source = itemView.findViewById(R.id.featured_source);
            readTime = itemView.findViewById(R.id.featured_read_time);
        }
    }
}
