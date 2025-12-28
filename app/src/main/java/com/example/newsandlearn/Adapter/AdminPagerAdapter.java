package com.example.newsandlearn.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.newsandlearn.Fragment.Admin.AdminArticlesFragment;
import com.example.newsandlearn.Fragment.Admin.AdminContentFragment;
import com.example.newsandlearn.Fragment.Admin.AdminLessonsFragment;
import com.example.newsandlearn.Fragment.Admin.AdminUsersFragment;
import com.example.newsandlearn.Fragment.Admin.AdminVocabularyFragment;

/**
 * AdminPagerAdapter - Adapter for admin panel tabs
 */
public class AdminPagerAdapter extends FragmentStateAdapter {

    private static final int NUM_TABS = 5;

    public AdminPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new AdminUsersFragment();
            case 1:
                return new AdminLessonsFragment();
            case 2:
                return new AdminVocabularyFragment();
            case 3:
                return new AdminArticlesFragment();
            case 4:
                return new AdminContentFragment();
            default:
                return new AdminUsersFragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}
