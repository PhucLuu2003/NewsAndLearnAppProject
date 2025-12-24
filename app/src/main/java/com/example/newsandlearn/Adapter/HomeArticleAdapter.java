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
import com.example.newsandlearn.Activity.ArticleDetailActivity;
import com.example.newsandlearn.Model.Article;
import com.example.newsandlearn.R;

import java.util.List;

public class HomeArticleAdapter extends RecyclerView.Adapter<HomeArticleAdapter.ArticleViewHolder> {

    private List<Article> articles;
    private Context context;

    public HomeArticleAdapter(List<Article> articles, Context context) {
        this.articles = articles;
        this.context = context;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article_compact, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = articles.get(position);

        holder.title.setText(article.getTitle());
        holder.source.setText(article.getSource());
        holder.readTime.setText(article.getReadTime() + " min");
        holder.level.setText(article.getLevel());

        // Load image with Glide
        if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(article.getImageUrl())
                    .placeholder(R.drawable.ic_article_placeholder)
                    .error(R.drawable.ic_article_placeholder)
                    .centerCrop()
                    .into(holder.thumbnail);
        }

        // Bookmark icon
        if (article.isBookmarked()) {
            holder.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_filled);
        } else {
            holder.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_border);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ArticleDetailActivity.class);
            intent.putExtra("article_id", article.getId());
            intent.putExtra("article_title", article.getTitle());
            intent.putExtra("article_url", article.getUrl());
            context.startActivity(intent);
        });

        // Bookmark click listener
        holder.bookmarkIcon.setOnClickListener(v -> {
            article.setBookmarked(!article.isBookmarked());
            notifyItemChanged(position);
            // TODO: Save bookmark state to Firestore
        });
    }

    @Override
    public int getItemCount() {
        return articles != null ? Math.min(articles.size(), 4) : 0; // Show max 4 articles
    }

    public void updateArticles(List<Article> newArticles) {
        this.articles = newArticles;
        notifyDataSetChanged();
    }

    static class ArticleViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title;
        TextView source;
        TextView readTime;
        TextView level;
        ImageView bookmarkIcon;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.article_thumbnail);
            title = itemView.findViewById(R.id.article_title);
            source = itemView.findViewById(R.id.article_source);
            readTime = itemView.findViewById(R.id.article_read_time);
            level = itemView.findViewById(R.id.article_level);
            bookmarkIcon = itemView.findViewById(R.id.bookmark_icon);
        }
    }
}
