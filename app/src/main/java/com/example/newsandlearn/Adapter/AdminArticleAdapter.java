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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * AdminArticleAdapter - Adapter for displaying articles in admin panel
 */
public class AdminArticleAdapter extends RecyclerView.Adapter<AdminArticleAdapter.ViewHolder> {

    private Context context;
    private List<Article> articles;
    private OnArticleActionListener listener;

    public interface OnArticleActionListener {
        void onEditArticle(Article article);
        void onDeleteArticle(Article article);
    }

    public AdminArticleAdapter(Context context, List<Article> articles, OnArticleActionListener listener) {
        this.context = context;
        this.articles = articles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_article, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article article = articles.get(position);
        
        holder.title.setText(article.getTitle());
        holder.category.setText(article.getCategory());
        holder.author.setText("By " + article.getAuthor());
        
        if (article.getPublishedDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            holder.date.setText(sdf.format(article.getPublishedDate()));
        }
        
        holder.readTime.setText(article.getReadTime() + " min read");
        
        // Load image
        if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(article.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.image);
        }
        
        holder.btnEdit.setOnClickListener(v -> listener.onEditArticle(article));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteArticle(article));
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        ImageView image;
        TextView title, category, author, date, readTime;
        MaterialButton btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            image = itemView.findViewById(R.id.article_image);
            title = itemView.findViewById(R.id.article_title);
            category = itemView.findViewById(R.id.article_category);
            author = itemView.findViewById(R.id.article_author);
            date = itemView.findViewById(R.id.article_date);
            readTime = itemView.findViewById(R.id.article_read_time);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
