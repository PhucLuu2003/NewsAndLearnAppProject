package com.example.newsandlearn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.ReadingArticle;
import com.example.newsandlearn.R;

import java.util.List;

/**
 * ReadingAdapter - RecyclerView adapter for reading articles
 * Displays articles loaded from Firebase dynamically
 */
public class ReadingAdapter extends RecyclerView.Adapter<ReadingAdapter.ReadingViewHolder> {

    private Context context;
    private List<ReadingArticle> articleList;
    private OnArticleClickListener listener;

    public interface OnArticleClickListener {
        void onArticleClick(ReadingArticle article);
    }

    public ReadingAdapter(Context context, List<ReadingArticle> articleList, OnArticleClickListener listener) {
        this.context = context;
        this.articleList = articleList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReadingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reading_card, parent, false);
        return new ReadingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReadingViewHolder holder, int position) {
        ReadingArticle article = articleList.get(position);
        holder.bind(article);
        
        // Add animation
        com.example.newsandlearn.Utils.AnimationHelper.itemFallDown(context, holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    class ReadingViewHolder extends RecyclerView.ViewHolder {

        ImageView articleImage, bookmarkButton; // Added bookmarkButton
        TextView articleTitle, articleSummary, levelText, wordCount, readTime;

        public ReadingViewHolder(@NonNull View itemView) {
            super(itemView);

            articleImage = itemView.findViewById(R.id.article_image);
            articleTitle = itemView.findViewById(R.id.article_title);
            articleSummary = itemView.findViewById(R.id.article_summary);
            levelText = itemView.findViewById(R.id.level_text);
            wordCount = itemView.findViewById(R.id.word_count);
            readTime = itemView.findViewById(R.id.read_time);
            bookmarkButton = itemView.findViewById(R.id.bookmark_button); // Bind new view
        }

        public void bind(ReadingArticle article) {
            if (articleTitle != null) {
                articleTitle.setText(article.getTitle());
            }
            
            if (articleSummary != null) {
                articleSummary.setText(article.getSummary());
            }
            
            // Format level text with category if available
            if (levelText != null) {
                String levelStr = article.getLevel() != null ? article.getLevel() : "B1";
                if (article.getCategory() != null && !article.getCategory().isEmpty()) {
                    levelStr += " • " + article.getCategory();
                }
                levelText.setText(levelStr);
            }
            
            if (wordCount != null) {
                wordCount.setText(article.getWordCount() + " words");
            }
            
            if (readTime != null) {
                readTime.setText(article.getEstimatedMinutes() + " min read");
            }

            // TODO: Load image from Firebase Storage URL
            // if (articleImage != null) {
            //     Glide.with(context).load(article.getImageUrl()).into(articleImage);
            // }

            itemView.setOnClickListener(v -> {
                com.example.newsandlearn.Utils.AnimationHelper.scaleUp(context, itemView);
                if (listener != null) {
                    listener.onArticleClick(article);
                }
            });
            
            if (bookmarkButton != null) {
                bookmarkButton.setOnClickListener(v -> {
                    com.example.newsandlearn.Utils.AnimationHelper.buttonPress(context, v);
                    android.widget.Toast.makeText(context, "Bookmark coming soon!", android.widget.Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    public void updateData(List<ReadingArticle> newArticleList) {
        this.articleList = newArticleList;
        notifyDataSetChanged();
    }
}
