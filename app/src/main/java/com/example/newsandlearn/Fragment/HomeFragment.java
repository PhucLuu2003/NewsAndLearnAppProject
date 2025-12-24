package com.example.newsandlearn.Fragment;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newsandlearn.Activity.AllVideosActivity;
import com.example.newsandlearn.Activity.ArticleDetailActivity;
import com.example.newsandlearn.Activity.EnhancedArticleDetailActivity;
import com.example.newsandlearn.Activity.SearchActivity;
import com.example.newsandlearn.Activity.SettingsActivity;
import com.example.newsandlearn.Adapter.HomeArticleAdapter;
import com.example.newsandlearn.Adapter.SearchSuggestionAdapter;
import com.example.newsandlearn.Adapter.VideoLessonsAdapter;
import com.example.newsandlearn.Model.Article;
import com.example.newsandlearn.Model.VideoLesson;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.AnimationHelper;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Modern HomeFragment with enhanced UI, leaderboard, and dynamic animations
 */
public class HomeFragment extends Fragment {

    // UI Components - Header
    private TextView usernameText;
    private TextView greetingText;
    private ImageView profileAvatar;
    private MaterialCardView profileCard;
    private View notificationBtn;
    private View notificationBadge;
    private View settingsBtn;
    private View voiceSearchBtn;
    
    // UI Components - Stats
    private MaterialCardView streakCard;
    private MaterialCardView scoreCard;
    private MaterialCardView rankCard;
    private TextView streakCount;
    private TextView xpText;
    private TextView userRank;
    private ProgressBar xpProgressBar;
    
    // UI Components - Content
    private CardView heroCard;
    private TextView featuredTitle, featuredReadTime, featuredSource;
    private TextView seeAllVideosBtn;
    private TextView viewAllArticlesBtn;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView videoLessonsRecycler;
    private RecyclerView articlesRecycler;
    private ProgressBar loadingIndicator;
    private ChipGroup levelChipGroup;
    
    // Search Dropdown
    private EditText searchInputHome;
    private ImageView clearSearchBtn;
    private MaterialCardView searchSuggestionsCard;
    private RecyclerView searchSuggestionsRecycler;
    private ProgressBar searchLoading;
    private TextView noResultsText;
    private TextView suggestionsHeader;
    private SearchSuggestionAdapter searchSuggestionAdapter;
    private List<Article> searchResults = new ArrayList<>();

    // Data & Adapter
    private VideoLessonsAdapter videoLessonsAdapter;
    private HomeArticleAdapter articlesAdapter;
    private List<Article> allArticles = new ArrayList<>();
    private List<Article> homeArticles = new ArrayList<>();
    private List<VideoLesson> videoLessons = new ArrayList<>();
    private Article featuredArticle;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // State
    private String currentLevel = "all";
    private boolean isLoading = false;
    private Handler animationHandler = new Handler(Looper.getMainLooper());

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews(view);
        setupVideoLessonsRecyclerView();
        setupArticlesRecyclerView();
        setupSearchDropdown();
        setupListeners();

        // Load data
        loadUserInfo();
        loadArticles();
        loadVideoLessons();
        loadHomeArticles();

        // Animate entrance
        animateEntrance();

        return view;
    }

    private void initializeViews(View view) {
        // Refresh & Loading
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        loadingIndicator = view.findViewById(R.id.loading_indicator);

        // Header
        profileAvatar = view.findViewById(R.id.profile_avatar);
        profileCard = view.findViewById(R.id.profile_card);
        usernameText = view.findViewById(R.id.username);
        greetingText = view.findViewById(R.id.greeting_text);
        notificationBtn = view.findViewById(R.id.notification_btn);
        notificationBadge = view.findViewById(R.id.notification_badge);
        settingsBtn = view.findViewById(R.id.settings_btn);

        // Voice Search (part of search dropdown now)
        voiceSearchBtn = view.findViewById(R.id.voice_search_btn);

        // Stats Cards
        streakCard = view.findViewById(R.id.streak_card);
        scoreCard = view.findViewById(R.id.score_card);
        rankCard = view.findViewById(R.id.rank_card);
        streakCount = view.findViewById(R.id.streak_count);
        xpText = view.findViewById(R.id.user_level);
        userRank = view.findViewById(R.id.user_rank);
        xpProgressBar = view.findViewById(R.id.xp_progress_bar);

        // Hero Section
        heroCard = view.findViewById(R.id.hero_card);
        featuredTitle = view.findViewById(R.id.featured_title);
        featuredReadTime = view.findViewById(R.id.featured_read_time);
        featuredSource = view.findViewById(R.id.featured_source);

        // Filters
        levelChipGroup = view.findViewById(R.id.level_chip_group);
        
        // Search Dropdown
        searchInputHome = view.findViewById(R.id.search_input_home);
        clearSearchBtn = view.findViewById(R.id.clear_search_btn);
        searchSuggestionsCard = view.findViewById(R.id.search_suggestions_card);
        searchSuggestionsRecycler = view.findViewById(R.id.search_suggestions_recycler);
        searchLoading = view.findViewById(R.id.search_loading);
        noResultsText = view.findViewById(R.id.no_results_text);
        suggestionsHeader = view.findViewById(R.id.suggestions_header);

        // Section
        seeAllVideosBtn = view.findViewById(R.id.see_all_videos_btn);
        viewAllArticlesBtn = view.findViewById(R.id.view_all_articles_btn);

        // RecyclerView
        videoLessonsRecycler = view.findViewById(R.id.video_lessons_recycler);
        articlesRecycler = view.findViewById(R.id.articles_recycler);
        
        // Set initial greeting
        updateGreeting();
    }
    
    private void updateGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        String greeting;
        if (hour < 12) {
            greeting = "Good Morning 👋";
        } else if (hour < 18) {
            greeting = "Good Afternoon ☀️";
        } else {
            greeting = "Good Evening 🌙";
        }
        
        if (greetingText != null) {
            greetingText.setText(greeting);
        }
    }
    
    private void updateGreetingWithLevel(int level, String levelTitle) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        String timeGreeting;
        String emoji;
        if (hour < 12) {
            timeGreeting = "Morning";
            emoji = "👋";
        } else if (hour < 18) {
            timeGreeting = "Afternoon";
            emoji = "☀️";
        } else {
            timeGreeting = "Evening";
            emoji = "🌙";
        }
        
        if (greetingText != null) {
            // Compact format: "Morning 👋 • Lv.5"
            greetingText.setText(timeGreeting + " " + emoji + " • Lv." + level);
        }
    }
    
    private void setupVideoLessonsRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        videoLessonsRecycler.setLayoutManager(layoutManager);

        videoLessonsAdapter = new VideoLessonsAdapter(getContext(), videoLessons);
        videoLessonsRecycler.setAdapter(videoLessonsAdapter);
    }
    
    private void setupArticlesRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        articlesRecycler.setLayoutManager(layoutManager);
        articlesRecycler.setNestedScrollingEnabled(false);

        articlesAdapter = new HomeArticleAdapter(homeArticles, getContext());
        articlesRecycler.setAdapter(articlesAdapter);
    }
    
    private void setupSearchDropdown() {
        // Setup RecyclerView
        searchSuggestionsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        searchSuggestionAdapter = new SearchSuggestionAdapter(article -> {
            // Hide dropdown
            searchSuggestionsCard.setVisibility(View.GONE);
            searchInputHome.clearFocus();
            searchInputHome.setText("");
            
            // Open article detail
            Intent intent = new Intent(getActivity(), EnhancedArticleDetailActivity.class);
            intent.putExtra("article_id", article.getId());
            startActivity(intent);
        });
        searchSuggestionsRecycler.setAdapter(searchSuggestionAdapter);
        
        // Text change listener
        searchInputHome.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                
                // Show/hide clear button
                clearSearchBtn.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                
                if (query.isEmpty()) {
                    searchSuggestionsCard.setVisibility(View.GONE);
                } else {
                    performSearchSuggestions(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Clear button
        clearSearchBtn.setOnClickListener(v -> {
            searchInputHome.setText("");
            searchSuggestionsCard.setVisibility(View.GONE);
        });
        
        // Focus listener
        searchInputHome.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && searchInputHome.getText().toString().isEmpty()) {
                searchSuggestionsCard.setVisibility(View.GONE);
            }
        });
    }

    private void performSearchSuggestions(String query) {
        searchLoading.setVisibility(View.VISIBLE);
        noResultsText.setVisibility(View.GONE);
        searchSuggestionsCard.setVisibility(View.VISIBLE);
        
        // Filter articles
        searchResults.clear();
        String lowerQuery = query.toLowerCase();
        
        for (Article article : allArticles) {
            boolean matches = 
                (article.getTitle() != null && article.getTitle().toLowerCase().contains(lowerQuery)) ||
                (article.getCategory() != null && article.getCategory().toLowerCase().contains(lowerQuery)) ||
                (article.getSource() != null && article.getSource().toLowerCase().contains(lowerQuery)) ||
                (article.getLevel() != null && article.getLevel().toLowerCase().contains(lowerQuery));
            
            if (matches) {
                searchResults.add(article);
                if (searchResults.size() >= 5) break; // Limit to 5 suggestions
            }
        }
        
        searchLoading.setVisibility(View.GONE);
        
        if (searchResults.isEmpty()) {
            noResultsText.setVisibility(View.VISIBLE);
            suggestionsHeader.setVisibility(View.GONE);
            searchSuggestionsRecycler.setVisibility(View.GONE);
        } else {
            noResultsText.setVisibility(View.GONE);
            suggestionsHeader.setVisibility(View.VISIBLE);
            searchSuggestionsRecycler.setVisibility(View.VISIBLE);
            searchSuggestionAdapter.setSuggestions(searchResults);
        }
    }


    private void setupListeners() {
        // Pull to Refresh
        swipeRefresh.setColorSchemeColors(
                getResources().getColor(R.color.purple_500));
        swipeRefresh.setOnRefreshListener(() -> {
            loadArticles();
            loadUserInfo();
            loadVideoLessons();
        });

        // Search Bar - Now using dropdown (see setupSearchDropdown())
        // OLD CODE: Commented out because search now uses inline dropdown
        /*
        searchBarHome.setOnClickListener(v -> {
            animateSearchClick(v);
            v.postDelayed(() -> {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }, 200);
        });
        */


        // Voice Search
        if (voiceSearchBtn != null) {
            voiceSearchBtn.setOnClickListener(v -> {
                animatePulse(v);
                Toast.makeText(getContext(), "🎤 Voice search coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

        // Header Buttons
        notificationBtn.setOnClickListener(v -> {
            animatePulse(v);
            Toast.makeText(getContext(), "📬 No new notifications", Toast.LENGTH_SHORT).show();
        });
        
        settingsBtn.setOnClickListener(v -> {
            animatePulse(v);
            v.postDelayed(() -> {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }, 150);
        });

        // Profile Card
        if (profileCard != null) {
            profileCard.setOnClickListener(v -> {
                animatePulse(v);
                Toast.makeText(getContext(), "👤 Profile coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

        // Stats Cards with Animations
        if (streakCard != null) {
            streakCard.setOnClickListener(v -> {
                animateStatsCard(v);
                Toast.makeText(getContext(), "🔥 Keep your streak alive!", Toast.LENGTH_SHORT).show();
            });
        }

        if (scoreCard != null) {
            scoreCard.setOnClickListener(v -> {
                animateStatsCard(v);
                Toast.makeText(getContext(), "⚡ Earn more XP by completing lessons!", Toast.LENGTH_SHORT).show();
            });
        }

        if (rankCard != null) {
            rankCard.setOnClickListener(v -> {
                animateStatsCard(v);
                v.postDelayed(() -> {
                    // Navigate to LeaderboardActivity
                    Intent intent = new Intent(getActivity(), com.example.newsandlearn.Activity.LeaderboardActivity.class);
                    startActivity(intent);
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }, 150);
            });
        }

        // Hero Card - Navigate to Articles tab
        heroCard.setOnClickListener(v -> {
            animateCardPress(v);
            v.postDelayed(() -> {
                // Navigate to Articles tab (index 1 in bottom navigation)
                if (getActivity() instanceof com.example.newsandlearn.Activity.MainActivity) {
                    ((com.example.newsandlearn.Activity.MainActivity) getActivity()).navigateToTab(1);
                }
            }, 150);
        });

        // See All Videos
        seeAllVideosBtn.setOnClickListener(v -> {
            animatePulse(v);
            v.postDelayed(() -> {
                Intent intent = new Intent(getActivity(), com.example.newsandlearn.Activity.AllVideosActivity.class);
                startActivity(intent);
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }, 150);
        });
        
        // View All Articles Button
        if (viewAllArticlesBtn != null) {
            viewAllArticlesBtn.setOnClickListener(v -> {
                animatePulse(v);
                // Navigate to Articles tab
                if (getActivity() != null) {
                    // You can navigate to ArticleFragment or show all articles
                    Toast.makeText(getContext(), "📰 Navigating to all articles...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Filter Chips
        if (levelChipGroup != null) {
            levelChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (!checkedIds.isEmpty()) {
                    int checkedId = checkedIds.get(0);
                    View fragmentView = getView();
                    if (fragmentView != null) {
                        Chip chip = fragmentView.findViewById(checkedId);
                        if (chip != null) {
                            animatePulse(chip);
                            // Filter logic can be added here if needed
                        }
                    }
                }
            });
        }
    }

    private void loadUserInfo() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String username = document.getString("username");
                            if (username != null) {
                                usernameText.setText(username);
                            }

                            // Load streak with animation
                            Long streak = document.getLong("currentStreak");
                            if (streak != null && streakCount != null) {
                                animateNumberCount(streakCount, 0, streak.intValue());
                            }



                            // Load XP with level progress
                            Long xp = document.getLong("totalXP");
                            if (xp != null && xpText != null) {
                                int totalXP = xp.intValue();
                                int currentProgress = com.example.newsandlearn.Utils.LevelSystem.getCurrentLevelProgress(totalXP);
                                int xpNeeded = com.example.newsandlearn.Utils.LevelSystem.getXPNeededForNextLevel(totalXP);
                                
                                // Display: "150/500" (current progress / XP needed for next level)
                                xpText.setText(currentProgress + "/" + xpNeeded);
                                
                                // Update progress bar
                                if (xpProgressBar != null && xpNeeded > 0) {
                                    int progressPercentage = (int) ((currentProgress * 100.0) / xpNeeded);
                                    xpProgressBar.setProgress(progressPercentage);
                                }
                                
                                // Update greeting with level
                                int level = com.example.newsandlearn.Utils.LevelSystem.getLevelFromXP(totalXP);
                                String levelTitle = com.example.newsandlearn.Utils.LevelSystem.getLevelTitle(level);
                                updateGreetingWithLevel(level, levelTitle);
                            }

                            // Calculate and display rank
                            calculateUserRank(userId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        usernameText.setText("User");
                    });
        }
    }

    private void calculateUserRank(String userId) {
        db.collection("users")
                .orderBy("totalXP", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int rank = 1;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getId().equals(userId)) {
                            final int finalRank = rank;
                            if (userRank != null) {
                                userRank.setText("#" + finalRank);
                                animatePulse(rankCard);
                            }
                            break;
                        }
                        rank++;
                    }
                })
;
    }

    private void loadArticles() {
        if (isLoading)
            return;

        showLoading(true);
        isLoading = true;

        db.collection("articles")
                .orderBy("publishedDate", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allArticles.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Article article = documentToArticle(document);
                        allArticles.add(article);
                    }

                    // Set featured article (first one)
                    if (!allArticles.isEmpty()) {
                        featuredArticle = allArticles.get(0);
                        updateHeroSection(featuredArticle);
                    } else {
                        Toast.makeText(getContext(),
                                "Chưa có bài viết. Vui lòng seed dữ liệu.",
                                Toast.LENGTH_LONG).show();
                    }

                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                    isLoading = false;
                })
                .addOnFailureListener(e -> {
                    allArticles.clear();
                    Toast.makeText(getContext(),
                            "Lỗi tải bài viết: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                    isLoading = false;
                });
    }

    private void loadVideoLessons() {
        db.collection("video_lessons")
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    videoLessons.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        VideoLesson lesson = document.toObject(VideoLesson.class);
                        if (lesson != null) {
                            videoLessons.add(lesson);
                        }
                    }
                    if (videoLessonsAdapter != null) {
                        videoLessonsAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading videos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private Article documentToArticle(QueryDocumentSnapshot document) {
        String id = document.getId();
        String title = document.getString("title");
        String content = document.getString("content");
        String imageUrl = document.getString("imageUrl");
        String category = document.getString("category");
        String level = document.getString("level");
        String source = document.getString("source");
        Date publishedDate = document.getDate("publishedDate");
        Long views = document.getLong("views");
        Long readingTime = document.getLong("readingTime");
        Boolean isFavorite = document.getBoolean("isFavorite");

        return new Article(
                id != null ? id : "",
                title != null ? title : "",
                content != null ? content : "",
                imageUrl != null ? imageUrl : "",
                category != null ? category : "",
                level != null ? level : "easy",
                source != null ? source : "",
                publishedDate != null ? publishedDate : new Date(),
                views != null ? views.intValue() : 0,
                readingTime != null ? readingTime.intValue() : 5,
                isFavorite != null ? isFavorite : false);
    }

    private void updateHeroSection(Article article) {
        if (article != null) {
            featuredTitle.setText(article.getTitle());
            featuredReadTime.setText("⏱ " + article.getReadingTime() + " phút đọc");
            featuredSource.setText(article.getSource());
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            loadingIndicator.setVisibility(View.VISIBLE);
        } else {
            loadingIndicator.setVisibility(View.GONE);
        }
    }

    private void openArticleDetail(Article article) {
        Intent intent = new Intent(getActivity(), ArticleDetailActivity.class);
        intent.putExtra("article_id", article.getId());
        startActivity(intent);
    }


    // Animation Methods
    private void animateEntrance() {
        if (getView() == null) return;

        // Animate stats cards
        animationHandler.postDelayed(() -> {
            if (streakCard != null) animateCardEntrance(streakCard, 0);
        }, 100);
        
        animationHandler.postDelayed(() -> {
            if (scoreCard != null) animateCardEntrance(scoreCard, 100);
        }, 200);
        
        animationHandler.postDelayed(() -> {
            if (rankCard != null) animateCardEntrance(rankCard, 200);
        }, 300);

        // Animate hero card
        if (heroCard != null) {
            heroCard.setAlpha(0f);
            heroCard.setTranslationY(50f);
            heroCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setStartDelay(400)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private void animateCardEntrance(View view, long delay) {
        view.setAlpha(0f);
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setStartDelay(delay)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void animatePulse(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    private void animateStatsCard(View view) {
        view.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(150)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

    private void animateCardPress(View view) {
        view.animate()
                .scaleX(0.97f)
                .scaleY(0.97f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    private void animateSearchClick(View view) {
        view.animate()
                .scaleX(0.98f)
                .scaleY(0.98f)
                .alpha(0.9f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    private void animateNumberCount(TextView textView, int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            textView.setText(String.valueOf(animation.getAnimatedValue()));
        });
        animator.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to fragment
        if (videoLessonsAdapter != null && !videoLessons.isEmpty()) {
            videoLessonsAdapter.notifyDataSetChanged();
        }
        updateGreeting();
    }
    
    private void loadHomeArticles() {
        db.collection("articles")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(4)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    homeArticles.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        Article article = doc.toObject(Article.class);
                        if (article != null) {
                            article.setId(doc.getId());
                            homeArticles.add(article);
                        }
                    }
                    
                    // Update adapter and show/hide RecyclerView
                    if (articlesAdapter != null) {
                        articlesAdapter.updateArticles(homeArticles);
                        if (articlesRecycler != null) {
                            articlesRecycler.setVisibility(homeArticles.isEmpty() ? View.GONE : View.VISIBLE);
                        }
                    }
                    
                    // Debug log
                    if (homeArticles.isEmpty()) {
                        Toast.makeText(getContext(), "No articles found. Please seed data.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading articles: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    android.util.Log.e("HomeFragment", "Error loading articles", e);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
        }
    }
}