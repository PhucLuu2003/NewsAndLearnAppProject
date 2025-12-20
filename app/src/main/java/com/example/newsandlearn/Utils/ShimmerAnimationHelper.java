package com.example.newsandlearn.Utils;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * ShimmerAnimationHelper - Tạo shimmer effect cho skeleton loading
 */
public class ShimmerAnimationHelper {
    
    private ValueAnimator shimmerAnimator;
    
    public void startShimmer(View... views) {
        if (views == null || views.length == 0) return;
        
        shimmerAnimator = ValueAnimator.ofFloat(0f, 1f);
        shimmerAnimator.setDuration(1500);
        shimmerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shimmerAnimator.setRepeatMode(ValueAnimator.REVERSE);
        shimmerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        shimmerAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            float alpha = 0.3f + (value * 0.4f); // Alpha từ 0.3 đến 0.7
            
            for (View view : views) {
                if (view != null) {
                    view.setAlpha(alpha);
                }
            }
        });
        
        shimmerAnimator.start();
    }
    
    public void stopShimmer() {
        if (shimmerAnimator != null && shimmerAnimator.isRunning()) {
            shimmerAnimator.cancel();
            shimmerAnimator = null;
        }
    }
    
    /**
     * Tạo shimmer effect cho một view duy nhất
     */
    public static void applyShimmer(View view) {
        if (view == null) return;
        
        ValueAnimator animator = ValueAnimator.ofFloat(0.3f, 1f);
        animator.setDuration(1500);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        animator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            view.setAlpha(alpha);
        });
        
        animator.start();
        view.setTag(animator); // Store animator in tag
    }
    
    /**
     * Dừng shimmer effect
     */
    public static void stopShimmer(View view) {
        if (view == null) return;
        
        Object tag = view.getTag();
        if (tag instanceof ValueAnimator) {
            ValueAnimator animator = (ValueAnimator) tag;
            animator.cancel();
            view.setTag(null);
        }
        
        view.setAlpha(1f);
    }
}
