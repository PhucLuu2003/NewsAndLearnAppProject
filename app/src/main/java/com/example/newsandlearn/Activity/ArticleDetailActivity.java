package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newsandlearn.Model.Article;
import com.example.newsandlearn.R;

public class ArticleDetailActivity extends AppCompatActivity {

    private ImageView articleImage;
    private TextView headerText, articleTitle, articleContent, articleSource, articleDate;
    private ImageButton backButton, favoriteButton, shareButton;
    private Article article;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        // Initialize views
        articleImage = findViewById(R.id.article_image);
        headerText = findViewById(R.id.header_text);
        articleTitle = findViewById(R.id.article_title);
        articleContent = findViewById(R.id.article_content);
        articleSource = findViewById(R.id.article_source);
        articleDate = findViewById(R.id.article_date);
        backButton = findViewById(R.id.back_button);
        favoriteButton = findViewById(R.id.favorite_button);
        shareButton = findViewById(R.id.share_button);

        // Get article from intent
        if (getIntent().hasExtra("article")) {
            article = (Article) getIntent().getSerializableExtra("article");
            displayArticle();
        }

        // Setup listeners
        backButton.setOnClickListener(v -> finish());

        favoriteButton.setOnClickListener(v -> {
            // TODO: Toggle favorite
        });

        shareButton.setOnClickListener(v -> {
            // TODO: Share article
        });
    }

    private void displayArticle() {
        if (article != null) {
            articleTitle.setText(article.getTitle());
            articleContent.setText(article.getContent());
            articleSource.setText(article.getSource());
            headerText.setText(article.getLevel().toUpperCase() + " ENGLISH");

            // TODO: Load image with Glide or Picasso
            // Glide.with(this).load(article.getImageUrl()).into(articleImage);
        }
    }
}
