package com.example.newsandlearn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.Article;
import com.example.newsandlearn.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    private Context context;
    private List<Article> articles;
    private OnArticleClickListener listener;

    public interface OnArticleClickListener {
        void onArticleClick(Article article);

        void onFavoriteClick(Article article);
    }

    public ArticleAdapter(Context context, OnArticleClickListener listener) {
        this.context = context;
        this.articles = new ArrayList<>();
        this.listener = listener;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
        notifyDataSetChanged();
    }

    public void addArticles(List<Article> newArticles) {
        int oldSize = articles.size();
        articles.addAll(newArticles);
        notifyItemRangeInserted(oldSize, newArticles.size());
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_newspaper_article, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = articles.get(position);
        holder.bind(article);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    class ArticleViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, sourceText, timeText, categoryText;
        ImageView articleImage, favoriteIcon;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.article_title);
            sourceText = itemView.findViewById(R.id.article_source);
            timeText = itemView.findViewById(R.id.article_time);
            categoryText = itemView.findViewById(R.id.article_category);
            articleImage = itemView.findViewById(R.id.article_image);
            favoriteIcon = itemView.findViewById(R.id.favorite_icon);
        }

        public void bind(Article article) {
            titleText.setText(article.getTitle());
            sourceText.setText(article.getSource());
            categoryText.setText(article.getCategory());

            // Format time
            if (article.getPublishedDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                timeText.setText(sdf.format(article.getPublishedDate()));
            }

            // Set favorite icon
            if (article.isFavorite()) {
                favoriteIcon.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                favoriteIcon.setImageResource(android.R.drawable.btn_star_big_off);
            }

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onArticleClick(article);
                }
            });

            favoriteIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(article);
                }
            });
        }
    }
}
