package com.example.newsandlearn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.newsandlearn.Model.Article;
import com.example.newsandlearn.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Dynamic Article Adapter with smooth animations and modern UI
 */
public class DynamicArticleAdapter extends RecyclerView.Adapter<DynamicArticleAdapter.ArticleViewHolder> {

    private Context context;
    private List<Article> articles;
    private OnArticleClickListener listener;
    private int lastPosition = -1;

    public interface OnArticleClickListener {
        void onArticleClick(Article article);
        void onFavoriteClick(Article article);
    }

    public DynamicArticleAdapter(Context context, OnArticleClickListener listener) {
        this.context = context;
        this.articles = new ArrayList<>();
        this.listener = listener;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
        notifyDataSetChanged();
        lastPosition = -1; // Reset animation position
    }

    public void addArticles(List<Article> newArticles) {
        int oldSize = articles.size();
        articles.addAll(newArticles);
        notifyItemRangeInserted(oldSize, newArticles.size());
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_article_dynamic, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = articles.get(position);
        holder.bind(article);
        
        // Apply animation
        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    /**
     * Apply slide-in animation to items
     */
    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ArticleViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    class ArticleViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView articleCard;
        ImageView articleImage, favoriteButton;
        TextView categoryBadge, levelBadge, articleTitle;
        TextView articleSource, articleTime, readingTime, progressText;
        MaterialCardView progressBadge;
        Chip tag1, tag2;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            articleCard = itemView.findViewById(R.id.article_card);
            articleImage = itemView.findViewById(R.id.article_image);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
            categoryBadge = itemView.findViewById(R.id.article_category);
            levelBadge = itemView.findViewById(R.id.article_level);
            articleTitle = itemView.findViewById(R.id.article_title);
            articleSource = itemView.findViewById(R.id.article_source);
            articleTime = itemView.findViewById(R.id.article_time);
            readingTime = itemView.findViewById(R.id.reading_time);
            progressBadge = itemView.findViewById(R.id.progress_badge);
            progressText = itemView.findViewById(R.id.progress_text);
            tag1 = itemView.findViewById(R.id.tag1);
            tag2 = itemView.findViewById(R.id.tag2);
        }

        public void bind(Article article) {
            // Set title
            articleTitle.setText(article.getTitle());

            // Set category with color
            if (article.getCategory() != null) {
                categoryBadge.setText(article.getCategory());
                setCategoryColor(article.getCategory());
            }

            // Set level with color
            if (article.getLevel() != null) {
                levelBadge.setText(article.getLevel());
                setLevelColor(article.getLevel());
            }

            // Set source
            if (article.getSource() != null) {
                articleSource.setText(article.getSource());
            }

            // Set time ago
            if (article.getPublishedDate() != null) {
                articleTime.setText(getTimeAgo(article.getPublishedDate()));
            }

            // Set reading time
            if (article.getReadingTime() > 0) {
                readingTime.setText(article.getReadingTime() + " min read");
            }

            // Set image with Glide
            if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(article.getImageUrl())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(R.drawable.placeholder_article)
                        .error(R.drawable.placeholder_article)
                        .centerCrop()
                        .into(articleImage);
            } else {
                articleImage.setImageResource(R.drawable.placeholder_article);
            }

            // Set favorite icon
            if (article.isFavorite()) {
                favoriteButton.setImageResource(R.drawable.ic_favorite);
            } else {
                favoriteButton.setImageResource(R.drawable.ic_favorite_border);
            }

            // Show progress if article has been started
            if (article.getProgress() > 0 && article.getProgress() < 100) {
                progressBadge.setVisibility(View.VISIBLE);
                progressText.setText(article.getProgress() + "% read");
            } else {
                progressBadge.setVisibility(View.GONE);
            }

            // Set tags (optional)
            if (article.getTags() != null && !article.getTags().isEmpty()) {
                if (article.getTags().size() > 0) {
                    tag1.setText("#" + article.getTags().get(0));
                    tag1.setVisibility(View.VISIBLE);
                }
                if (article.getTags().size() > 1) {
                    tag2.setText("#" + article.getTags().get(1));
                    tag2.setVisibility(View.VISIBLE);
                }
            } else {
                tag1.setVisibility(View.GONE);
                tag2.setVisibility(View.GONE);
            }

            // Click listeners with animation
            articleCard.setOnClickListener(v -> {
                // Scale animation
                v.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .start();
                            if (listener != null) {
                                listener.onArticleClick(article);
                            }
                        })
                        .start();
            });

            favoriteButton.setOnClickListener(v -> {
                // Rotate animation
                v.animate()
                        .rotationBy(360f)
                        .setDuration(300)
                        .start();
                
                if (listener != null) {
                    listener.onFavoriteClick(article);
                }
            });
        }

        private void setCategoryColor(String category) {
            int color;
            switch (category.toLowerCase()) {
                case "technology":
                case "tech":
                    color = 0xFF2196F3; // Blue
                    break;
                case "business":
                    color = 0xFF4CAF50; // Green
                    break;
                case "science":
                    color = 0xFF9C27B0; // Purple
                    break;
                case "health":
                    color = 0xFFE91E63; // Pink
                    break;
                case "sports":
                    color = 0xFFFF5722; // Deep Orange
                    break;
                case "entertainment":
                    color = 0xFFFF9800; // Orange
                    break;
                default:
                    color = 0xFF607D8B; // Blue Grey
                    break;
            }
            ((MaterialCardView) categoryBadge.getParent()).setCardBackgroundColor(color);
        }

        private void setLevelColor(String level) {
            int color;
            switch (level.toLowerCase()) {
                case "easy":
                case "beginner":
                    color = 0xFF4CAF50; // Green
                    break;
                case "medium":
                case "intermediate":
                    color = 0xFFFF9800; // Orange
                    break;
                case "hard":
                case "advanced":
                    color = 0xFFF44336; // Red
                    break;
                default:
                    color = 0xFF2196F3; // Blue
                    break;
            }
            ((MaterialCardView) levelBadge.getParent()).setCardBackgroundColor(color);
        }

        private String getTimeAgo(Date date) {
            long timeInMillis = date.getTime();
            long now = System.currentTimeMillis();
            long diff = now - timeInMillis;

            long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            long days = TimeUnit.MILLISECONDS.toDays(diff);

            if (seconds < 60) {
                return "Just now";
            } else if (minutes < 60) {
                return minutes + "m ago";
            } else if (hours < 24) {
                return hours + "h ago";
            } else if (days < 7) {
                return days + "d ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                return sdf.format(date);
            }
        }
    }
}
