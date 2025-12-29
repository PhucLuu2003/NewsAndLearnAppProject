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

        ImageView articleImage;
        TextView categoryBadge, levelBadge, readingTime;
        TextView articleTitle, articleExcerpt, articleSummary, wordCount, completionStatus;

        public ReadingViewHolder(@NonNull View itemView) {
            super(itemView);

            articleImage = itemView.findViewById(R.id.article_image);
            categoryBadge = itemView.findViewById(R.id.category_badge);
            levelBadge = itemView.findViewById(R.id.level_badge);
            readingTime = itemView.findViewById(R.id.reading_time);
            articleTitle = itemView.findViewById(R.id.article_title);
            articleExcerpt = itemView.findViewById(R.id.article_excerpt);
            articleSummary = itemView.findViewById(R.id.article_summary);
            wordCount = itemView.findViewById(R.id.word_count);
            completionStatus = itemView.findViewById(R.id.completion_status);
        }

        public void bind(ReadingArticle article) {
            if (articleTitle != null) {
                articleTitle.setText(article.getTitle());
            }

            if (categoryBadge != null) {
                String category = article.getCategory() != null ? article.getCategory() : "all";
                categoryBadge.setText(category.toUpperCase());
            }

            if (levelBadge != null) {
                String level = article.getLevel() != null ? article.getLevel() : "B1";
                levelBadge.setText(level);
            }

            if (articleSummary != null) {
                articleSummary.setText(article.getSummary());
            }

            if (articleExcerpt != null) {
                String body = article.getPassage() != null ? article.getPassage() : article.getContent();
                articleExcerpt.setText(buildExcerpt(body));
            }

            if (wordCount != null) {
                int wc = article.getWordCount();
                wordCount.setText(wc > 0 ? (wc + " words") : "");
            }

            if (readingTime != null) {
                int minutes = article.getEstimatedMinutes();
                if (minutes <= 0 && article.getWordCount() > 0) {
                    minutes = Math.max(1, article.getWordCount() / 200);
                }
                readingTime.setText((minutes > 0 ? minutes : 5) + " min read");
            }

            if (completionStatus != null) {
                if (article.isCompleted()) {
                    completionStatus.setVisibility(View.VISIBLE);
                    String scorePart = article.getUserScore() > 0 ? (" • " + article.getUserScore() + "%") : "";
                    completionStatus.setText("✓ Completed" + scorePart);
                } else {
                    completionStatus.setVisibility(View.GONE);
                }
            }

            if (articleImage != null) {
                String imageUrl = article.getImageUrl();
                if (imageUrl == null || imageUrl.trim().isEmpty()) {
                    articleImage.setImageResource(R.drawable.placeholder_article);
                } else {
                    Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.placeholder_article)
                            .error(R.drawable.placeholder_article)
                            .centerCrop()
                            .into(articleImage);
                }
            }

            itemView.setOnClickListener(v -> {
                com.example.newsandlearn.Utils.AnimationHelper.scaleUp(context, itemView);
                if (listener != null) {
                    listener.onArticleClick(article);
                }
            });
        }
    }

    private static String buildExcerpt(String body) {
        if (body == null)
            return "";
        String normalized = body.replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty())
            return "";
        int max = 140;
        if (normalized.length() <= max)
            return normalized;
        return normalized.substring(0, max).trim() + "…";
    }

    public void updateData(List<ReadingArticle> newArticleList) {
        this.articleList = newArticleList;
        notifyDataSetChanged();
    }
}
