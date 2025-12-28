package com.example.newsandlearn.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newsandlearn.Model.ReadingArticle;
import com.example.newsandlearn.R;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ReadingAdapter - RecyclerView adapter for reading articles
 * Compatible with existing code structure
 */
public class ReadingAdapter extends RecyclerView.Adapter<ReadingAdapter.ReadingViewHolder> {

    private final List<ReadingArticle> articles;
    private final OnArticleClickListener listener;
    private Context context;

    public interface OnArticleClickListener {
        void onArticleClick(ReadingArticle article);
    }

    public ReadingAdapter(Context context, List<ReadingArticle> articles, OnArticleClickListener listener) {
        this.context = context;
        this.articles = articles;
        this.listener = listener;
    }

    public void updateData(List<ReadingArticle> newData) {
        articles.clear();
        articles.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReadingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reading_card, parent, false);
        context = parent.getContext();
        return new ReadingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReadingViewHolder holder, int position) {
        ReadingArticle article = articles.get(position);
        holder.bind(article, position);
    }

    @Override
    public int getItemCount() {
        Log.d("ADAPTER_DEBUG", "itemCount=" + articles.size());
        return articles.size();
    }

    class ReadingViewHolder extends RecyclerView.ViewHolder {
        // Try to find all possible view IDs (some may not exist in your layout)
        CardView cardRoot;
        ImageView articleImage, bookmarkIcon, bookmarkButton;
        TextView articleTitle, articleSummary, articleExcerpt;
        TextView categoryBadge, levelBadge, levelText;
        TextView readingTime, readTime, wordCount, questionsCount;
        TextView userScore, lastReadInfo, progressText;
        LinearLayout completionBadge, progressSection;
        ProgressBar readingProgress;

        public ReadingViewHolder(@NonNull View itemView) {
            super(itemView);

            // Find views - safely handle if they don't exist
            cardRoot = itemView.findViewById(R.id.card_root);
            articleImage = itemView.findViewById(R.id.article_image);
            bookmarkIcon = itemView.findViewById(R.id.bookmark_icon);
            bookmarkButton = itemView.findViewById(R.id.bookmark_button);

            articleTitle = itemView.findViewById(R.id.article_title);
            articleSummary = itemView.findViewById(R.id.tv_article_summary);
            articleExcerpt = itemView.findViewById(R.id.article_excerpt);

            categoryBadge = itemView.findViewById(R.id.category_badge);
            levelBadge = itemView.findViewById(R.id.level_badge);
            levelText = itemView.findViewById(R.id.level_text);

            readingTime = itemView.findViewById(R.id.reading_time);
            readTime = itemView.findViewById(R.id.read_time);
            wordCount = itemView.findViewById(R.id.word_count);
            questionsCount = itemView.findViewById(R.id.questions_count);

            completionBadge = itemView.findViewById(R.id.completion_badge);
            userScore = itemView.findViewById(R.id.user_score);
            progressSection = itemView.findViewById(R.id.progress_section);
            readingProgress = itemView.findViewById(R.id.reading_progress);
            progressText = itemView.findViewById(R.id.progress_text);
            lastReadInfo = itemView.findViewById(R.id.last_read_info);
        }

        public void bind(ReadingArticle article, int position) {
            // Set title
            if (articleTitle != null) {
                articleTitle.setText(article.getTitle());
            }

            // Set excerpt/summary - check which TextView exists
            String excerptText = getExcerptText(article);
            if (articleExcerpt != null) {
                articleExcerpt.setText(excerptText);
            } else if (articleSummary != null) {
                articleSummary.setText(excerptText);
            }

            // Load image if view exists
            if (articleImage != null) {
                if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
                    try {
                        Glide.with(context)
                                .load(article.getImageUrl())
                                .placeholder(R.drawable.placeholder_article)
                                .error(R.drawable.placeholder_article)
                                .centerCrop()
                                .into(articleImage);
                    } catch (Exception e) {
                        articleImage.setImageResource(R.drawable.placeholder_article);
                    }
                } else {
                    articleImage.setImageResource(R.drawable.placeholder_article);
                }
            }

            // Set category badge
            if (categoryBadge != null && article.getCategory() != null) {
                categoryBadge.setText(article.getCategory().toUpperCase());
                try {
                    categoryBadge.setBackgroundTintList(
                            context.getResources().getColorStateList(getCategoryColor(article.getCategory()))
                    );
                } catch (Exception e) {
                    // Color not found, use default
                }
            }

            // Set level badge or level text
            if (article.getLevel() != null) {
                if (levelBadge != null) {
                    levelBadge.setText(article.getLevel().toUpperCase());
                    try {
                        levelBadge.setBackgroundTintList(
                                context.getResources().getColorStateList(getLevelColor(article.getLevel()))
                        );
                    } catch (Exception e) {
                        // Color not found, use default
                    }
                } else if (levelText != null) {
                    String levelStr = article.getLevel();
                    if (article.getCategory() != null) {
                        levelStr += " • " + article.getCategory();
                    }
                    levelText.setText(levelStr);
                }
            }

            // Set reading time
            int minutes = article.getEstimatedMinutes();
            if (minutes == 0 && article.getWordCount() > 0) {
                minutes = Math.max(1, article.getWordCount() / 200);
            }
            if (minutes == 0) minutes = 5;

            if (readingTime != null) {
                readingTime.setText(minutes + " min");
            } else if (readTime != null) {
                readTime.setText(minutes + " min read");
            }

            // Set word count
            if (wordCount != null) {
                if (article.getWordCount() > 0) {
                    wordCount.setText(article.getWordCount() + " words");
                } else {
                    wordCount.setText("~" + (minutes * 200) + " words");
                }
            }

            // Set questions count
            if (questionsCount != null && article.getQuestions() != null) {
                questionsCount.setText(article.getQuestions().size() + " Q");
            }

            // Handle completion status
            if (article.isCompleted()) {
                if (completionBadge != null) {
                    completionBadge.setVisibility(View.VISIBLE);
                }
                if (userScore != null) {
                    userScore.setText(article.getUserScore() + "%");
                }
                if (progressSection != null) {
                    progressSection.setVisibility(View.GONE);
                }
                if (lastReadInfo != null && article.getLastReadAt() != null) {
                    lastReadInfo.setVisibility(View.VISIBLE);
                    lastReadInfo.setText("Completed " + getTimeAgo(article.getLastReadAt()));
                }
            } else if (article.getReadCount() > 0) {
                // In progress
                if (completionBadge != null) {
                    completionBadge.setVisibility(View.GONE);
                }
                if (progressSection != null) {
                    progressSection.setVisibility(View.VISIBLE);
                }
                if (readingProgress != null) {
                    int progress = Math.min(50, article.getReadCount() * 20);
                    readingProgress.setProgress(progress);
                }
                if (progressText != null) {
                    int progress = Math.min(50, article.getReadCount() * 20);
                    progressText.setText(progress + "% Complete");
                }
                if (lastReadInfo != null && article.getLastReadAt() != null) {
                    lastReadInfo.setVisibility(View.VISIBLE);
                    lastReadInfo.setText("Started " + getTimeAgo(article.getLastReadAt()));
                }
            } else {
                // Not started
                if (completionBadge != null) {
                    completionBadge.setVisibility(View.GONE);
                }
                if (progressSection != null) {
                    progressSection.setVisibility(View.GONE);
                }
                if (lastReadInfo != null) {
                    lastReadInfo.setVisibility(View.GONE);
                }
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onArticleClick(article);
                }
            });

            // Bookmark button
            if (bookmarkButton != null) {
                bookmarkButton.setOnClickListener(v -> {
                    android.widget.Toast.makeText(context, "Bookmark coming soon!", android.widget.Toast.LENGTH_SHORT).show();
                });
            }
        }

        private String getExcerptText(ReadingArticle article) {
            if (article.getSummary() != null && !article.getSummary().isEmpty()) {
                return article.getSummary();
            } else if (article.getPassage() != null && !article.getPassage().isEmpty()) {
                String passage = article.getPassage();
                return passage.length() > 120 ? passage.substring(0, 120) + "..." : passage;
            } else if (article.getContent() != null && !article.getContent().isEmpty()) {
                String content = article.getContent();
                return content.length() > 120 ? content.substring(0, 120) + "..." : content;
            } else {
                return "Read this article to improve your English skills";
            }
        }

        private int getCategoryColor(String category) {
            switch (category.toLowerCase()) {
                case "news": return R.color.category_news;
                case "story": return R.color.category_story;
                case "science": return R.color.category_science;
                case "culture": return R.color.category_culture;
                case "technology": return R.color.category_technology;
                default: return R.color.primary;
            }
        }

        private int getLevelColor(String level) {
            switch (level.toUpperCase()) {
                case "A1": return R.color.level_a1;
                case "A2": return R.color.level_a2;
                case "B1": return R.color.level_b1;
                case "B2": return R.color.level_b2;
                case "C1": return R.color.level_c1;
                case "C2": return R.color.level_c2;
                default: return R.color.primary;
            }
        }

        private String getTimeAgo(Date date) {
            long timeMillis = date.getTime();
            long now = System.currentTimeMillis();
            long diff = now - timeMillis;

            long days = TimeUnit.MILLISECONDS.toDays(diff);
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);

            if (days > 0) {
                return days == 1 ? "yesterday" : days + " days ago";
            } else if (hours > 0) {
                return hours == 1 ? "1 hour ago" : hours + " hours ago";
            } else if (minutes > 0) {
                return minutes == 1 ? "1 minute ago" : minutes + " minutes ago";
            } else {
                return "just now";
            }
        }
    }
}
