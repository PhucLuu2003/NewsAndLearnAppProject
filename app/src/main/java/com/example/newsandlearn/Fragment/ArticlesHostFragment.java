package com.example.newsandlearn.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.newsandlearn.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ArticlesHostFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private android.widget.TextView headerTitle;
    private android.widget.TextView headerSubtitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_articles_host, container, false);

        tabLayout = view.findViewById(R.id.articles_tab_layout);
        viewPager = view.findViewById(R.id.articles_view_pager);
        headerTitle = view.findViewById(R.id.articles_header_title);
        headerSubtitle = view.findViewById(R.id.articles_header_subtitle);

        viewPager.setAdapter(new ArticlesPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(R.string.articles_tab_discover);
            } else {
                tab.setText(R.string.articles_tab_practice);
            }
        }).attach();

        // Update header when tab changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateHeader(position);
            }
        });

        return view;
    }

    private void updateHeader(int position) {
        if (position == 0) {
            headerTitle.setText("📰 Discover Articles");
            headerSubtitle.setText("Read, learn, and improve your English");
        } else {
            headerTitle.setText("📝 Practice Words");
            headerSubtitle.setText("Review vocabulary from your readings");
        }
    }

    private static class ArticlesPagerAdapter extends FragmentStateAdapter {

        ArticlesPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new ArticleFragment();
            }
            return new ArticlePracticeFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
