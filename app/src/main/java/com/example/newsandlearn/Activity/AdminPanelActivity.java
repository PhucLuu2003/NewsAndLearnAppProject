package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.newsandlearn.Adapter.AdminPagerAdapter;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.RoleManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * AdminPanelActivity - Main admin panel with tabs for different management sections
 * Only accessible by users with admin role
 */
public class AdminPanelActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private AdminPagerAdapter pagerAdapter;

    private final String[] tabTitles = {
            "👥 Users",
            "📚 Lessons",
            "📖 Vocabulary",
            "📰 Articles",
            "📝 Content"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        // Check admin permission first
        checkAdminPermission();
        
        initializeViews();
        setupViewPager();
    }

    private void checkAdminPermission() {
        RoleManager.isCurrentUserAdmin(new RoleManager.RoleCheckCallback() {
            @Override
            public void onResult(boolean isAdmin) {
                if (!isAdmin) {
                    Toast.makeText(AdminPanelActivity.this, 
                        "⛔ Access Denied: Admin role required", 
                        Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AdminPanelActivity.this, 
                    "Error checking permissions: " + error, 
                    Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void initializeViews() {
        tabLayout = findViewById(R.id.admin_tab_layout);
        viewPager = findViewById(R.id.admin_view_pager);
        
        // Setup toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Panel");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupViewPager() {
        pagerAdapter = new AdminPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
