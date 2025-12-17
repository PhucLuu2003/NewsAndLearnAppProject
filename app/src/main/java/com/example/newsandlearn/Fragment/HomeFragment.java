package com.example.newsandlearn.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.example.newsandlearn.Activity.ArticleDetailActivity;
import com.example.newsandlearn.Activity.SearchActivity;
import com.example.newsandlearn.Adapter.ArticleAdapter;
import com.example.newsandlearn.Adapter.VideoLessonsAdapter;
import com.example.newsandlearn.Model.Article;
import com.example.newsandlearn.Model.VideoLesson;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.AnimationHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Modern HomeFragment with Firebase integration
 */
public class HomeFragment extends Fragment {

    // UI Components
    private TextView usernameText;
    private ImageView profileAvatar;
    private View notificationBtn;
    private View settingsBtn;
    private LinearLayout searchBarHome;
    private ChipGroup levelChipGroup;
    private Chip chipAll, chipEasy, chipMedium, chipHard;
    private CardView heroCard;
    private TextView featuredTitle, featuredReadTime, featuredSource;
    private TextView seeAllBtn;
    private TextView seeAllVideosBtn;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView articlesRecyclerView;
    private RecyclerView videoLessonsRecycler;
    private ProgressBar loadingIndicator;
    private TextView emptyState;

    // Gamification UI
    private LinearLayout statsCard;
    private TextView streakCount;
    private TextView xpText;

    // Data & Adapter
    private ArticleAdapter adapter;
    private VideoLessonsAdapter videoLessonsAdapter;
    private List<Article> allArticles = new ArrayList<>();
    private List<VideoLesson> videoLessons = new ArrayList<>();
    private Article featuredArticle;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // State
    private String currentLevel = "all";
    private boolean isLoading = false;

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
        setupRecyclerView();
        setupVideoLessonsRecyclerView();
        setupListeners();

        // Load data
        loadUserInfo();
        loadArticles();
        loadVideoLessons();
        
        // Animate entrance
        animateEntrance();

        return view;
    }

    private void initializeViews(View view) {
        // Refresh & Loading
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        emptyState = view.findViewById(R.id.empty_state);

        // Header
        profileAvatar = view.findViewById(R.id.profile_avatar);
        usernameText = view.findViewById(R.id.username);
        notificationBtn = view.findViewById(R.id.notification_btn);
        settingsBtn = view.findViewById(R.id.settings_btn);

        // Search
        searchBarHome = view.findViewById(R.id.search_bar_home);

        // Level Chips
        levelChipGroup = view.findViewById(R.id.level_chip_group);
        chipAll = view.findViewById(R.id.chip_all);
        chipEasy = view.findViewById(R.id.chip_easy);
        chipMedium = view.findViewById(R.id.chip_medium);
        chipHard = view.findViewById(R.id.chip_hard);

        // Hero Section
        heroCard = view.findViewById(R.id.hero_card);
        featuredTitle = view.findViewById(R.id.featured_title);
        featuredReadTime = view.findViewById(R.id.featured_read_time);
        featuredSource = view.findViewById(R.id.featured_source);

        // Section
        seeAllBtn = view.findViewById(R.id.see_all_btn);
        seeAllVideosBtn = view.findViewById(R.id.see_all_videos_btn);

        // RecyclerView
        articlesRecyclerView = view.findViewById(R.id.articles_recycler_view);
        videoLessonsRecycler = view.findViewById(R.id.video_lessons_recycler);

        // Gamification
        statsCard = view.findViewById(R.id.stats_card);
        streakCount = view.findViewById(R.id.streak_count);
        xpText = view.findViewById(R.id.user_level);
    }

    private void setupRecyclerView() {
        articlesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ArticleAdapter(getContext(), new ArticleAdapter.OnArticleClickListener() {
            @Override
            public void onArticleClick(Article article) {
                openArticleDetail(article);
            }

            @Override
            public void onFavoriteClick(Article article) {
                toggleFavorite(article);
            }
        });
        articlesRecyclerView.setAdapter(adapter);
    }

    private void setupVideoLessonsRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        videoLessonsRecycler.setLayoutManager(layoutManager);

        videoLessonsAdapter = new VideoLessonsAdapter(getContext(), videoLessons);
        videoLessonsRecycler.setAdapter(videoLessonsAdapter);
    }

    private void setupListeners() {
        // Pull to Refresh
        swipeRefresh.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_light));
        swipeRefresh.setOnRefreshListener(() -> {
            loadArticles();
            loadUserInfo();
            loadVideoLessons();
        });

        // Search Bar
        searchBarHome.setOnClickListener(v -> {
            AnimationHelper.scaleUp(requireContext(), v);
            v.postDelayed(() -> {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }, 150);
        });

        // Header Buttons
        notificationBtn.setOnClickListener(v -> Toast.makeText(getContext(), "Thông báo", Toast.LENGTH_SHORT).show());
        settingsBtn.setOnClickListener(v -> Toast.makeText(getContext(), "Cài đặt", Toast.LENGTH_SHORT).show());

        // Level Chips
        levelChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_all) {
                currentLevel = "all";
            } else if (checkedId == R.id.chip_easy) {
                currentLevel = "easy";
            } else if (checkedId == R.id.chip_medium) {
                currentLevel = "medium";
            } else if (checkedId == R.id.chip_hard) {
                currentLevel = "hard";
            }
            filterArticlesByLevel();
        });

        // Hero Card
        heroCard.setOnClickListener(v -> {
            AnimationHelper.buttonPress(requireContext(), v);
            v.postDelayed(() -> {
                AnimationHelper.buttonRelease(requireContext(), v);
                if (featuredArticle != null) {
                    openArticleDetail(featuredArticle);
                }
            }, 100);
        });

        // See All
        seeAllBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SearchActivity.class);
            startActivity(intent);
        });

        // See All Videos
        seeAllVideosBtn.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Tất cả Video Lessons", Toast.LENGTH_SHORT).show();
        });

        // Stats card click for details
        if (statsCard != null) {
            statsCard.setOnClickListener(v -> {
                AnimationHelper.pulse(requireContext(), v);
                Toast.makeText(getContext(), "Tap to view detailed stats", Toast.LENGTH_SHORT).show();
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
                            
                            // Load streak
                            Long streak = document.getLong("currentStreak");
                            if (streak != null && streakCount != null) {
                                streakCount.setText(String.valueOf(streak));
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        usernameText.setText("User");
                    });
        }
    }

    private void loadArticles() {
        if (isLoading)
            return;

        showLoading(true);
        isLoading = true;

        // Load from Firebase Firestore
        db.collection("articles")
                .orderBy("publishedDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allArticles.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Article article = documentToArticle(document);
                        allArticles.add(article);
                    }

                    // If no articles from Firebase, create sample data
                    if (allArticles.isEmpty()) {
                        allArticles = createSampleArticles();
                    }

                    // Set featured article (first one)
                    if (!allArticles.isEmpty()) {
                        featuredArticle = allArticles.get(0);
                        updateHeroSection(featuredArticle);
                    }

                    filterArticlesByLevel();
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                    isLoading = false;
                })
                .addOnFailureListener(e -> {
                    // Fallback to sample data
                    allArticles = createSampleArticles();
                    if (!allArticles.isEmpty()) {
                        featuredArticle = allArticles.get(0);
                        updateHeroSection(featuredArticle);
                    }
                    filterArticlesByLevel();
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                    isLoading = false;
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

    private void filterArticlesByLevel() {
        List<Article> filteredArticles = new ArrayList<>();

        if (currentLevel.equals("all")) {
            filteredArticles.addAll(allArticles);
        } else {
            for (Article article : allArticles) {
                if (article.getLevel().equalsIgnoreCase(currentLevel)) {
                    filteredArticles.add(article);
                }
            }
        }

        adapter.setArticles(filteredArticles);

        // Show empty state if no articles
        if (filteredArticles.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            articlesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            articlesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void updateHeroSection(Article article) {
        if (article != null) {
            featuredTitle.setText(article.getTitle());
            featuredReadTime.setText(article.getReadingTime() + " phút đọc");
            featuredSource.setText(article.getSource());
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            loadingIndicator.setVisibility(View.VISIBLE);
            articlesRecyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        } else {
            loadingIndicator.setVisibility(View.GONE);
        }
    }

    private void openArticleDetail(Article article) {
        Intent intent = new Intent(getActivity(), ArticleDetailActivity.class);
        intent.putExtra("article", article);
        startActivity(intent);
    }

    private void toggleFavorite(Article article) {
        article.setFavorite(!article.isFavorite());
        adapter.notifyDataSetChanged();

        // Update in Firebase
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .collection("favorites").document(article.getId())
                    .set(article)
                    .addOnSuccessListener(aVoid -> {
                        String message = article.isFavorite() ? "Đã thêm vào yêu thích" : "Đã xóa khỏi yêu thích";
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    });
        }
    }
    
    private void animateEntrance() {
        // Staggered entrance animations for main sections
        if (getView() == null) return;
        
        if (heroCard != null) AnimationHelper.itemFallDown(requireContext(), heroCard, 3);
        if (videoLessonsRecycler != null) AnimationHelper.itemFallDown(requireContext(), videoLessonsRecycler, 5);
        if (articlesRecyclerView != null) AnimationHelper.itemFallDown(requireContext(), articlesRecyclerView, 6);
    }

    private List<Article> createSampleArticles() {
        List<Article> articles = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            articles.add(new Article(
                    "1",
                    "10 Phương pháp học tiếng Anh hiệu quả nhất 2024",
                    "Khám phá những phương pháp học tiếng Anh được chứng minh mang lại hiệu quả cao nhất...",
                    "",
                    "Học tập",
                    "easy",
                    "BBC Learning English",
                    sdf.parse("2024-01-15"),
                    1250,
                    5,
                    false));

            articles.add(new Article(
                    "2",
                    "Cách cải thiện kỹ năng nghe tiếng Anh",
                    "Những bài tập và phương pháp giúp bạn nâng cao khả năng nghe hiểu tiếng Anh...",
                    "",
                    "Kỹ năng",
                    "medium",
                    "English Central",
                    sdf.parse("2024-01-14"),
                    890,
                    7,
                    false));

            articles.add(new Article(
                    "3",
                    "Ngữ pháp tiếng Anh nâng cao: Câu điều kiện",
                    "Hướng dẫn chi tiết về các loại câu điều kiện trong tiếng Anh...",
                    "",
                    "Ngữ pháp",
                    "hard",
                    "Cambridge English",
                    sdf.parse("2024-01-13"),
                    650,
                    10,
                    false));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return articles;
    }

    private void loadVideoLessons() {
        if (getContext() == null)
            return;

        db.collection("video_lessons")
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (getContext() == null)
                        return;

                    videoLessons.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            VideoLesson lesson = document.toObject(VideoLesson.class);
                            if (lesson != null) {
                                if (lesson.getId() == null || lesson.getId().isEmpty()) {
                                    lesson.setId(document.getId());
                                }
                                videoLessons.add(lesson);
                            }
                        } catch (Exception e) {
                            android.util.Log.e("HomeFragment", "Error parsing lesson: " + e.getMessage(), e);
                        }
                    }

                    if (videoLessonsAdapter != null && !videoLessons.isEmpty()) {
                        videoLessonsAdapter.updateData(videoLessons);
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() == null)
                        return;
                    android.util.Log.e("HomeFragment", "Error loading video lessons", e);
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to fragment
        if (adapter != null && !allArticles.isEmpty()) {
            adapter.notifyDataSetChanged();
        }
        if (videoLessonsAdapter != null && !videoLessons.isEmpty()) {
            videoLessonsAdapter.notifyDataSetChanged();
        }
    }
}