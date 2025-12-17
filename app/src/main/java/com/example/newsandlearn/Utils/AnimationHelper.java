package com.example.newsandlearn.Utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Enhanced Animation Helper cho UI mượt mà và hiện đại
 */
public class AnimationHelper {

    /**
     * Fade in animation cho view
     */
    public static void fadeIn(View view, long duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Fade out animation cho view
     */
    public static void fadeOut(View view, long duration) {
        view.animate()
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }

    /**
     * Slide in from bottom animation
     */
    public static void slideInFromBottom(View view, long duration) {
        view.setTranslationY(view.getHeight());
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Scale up animation (button press effect)
     */
    public static void scaleUp(Context context, View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .setInterpolator(new OvershootInterpolator())
                            .start();
                })
                .start();
    }

    /**
     * Button press animation
     */
    public static void buttonPress(Context context, View view) {
        view.animate()
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Button release animation
     */
    public static void buttonRelease(Context context, View view) {
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(150)
                .setInterpolator(new OvershootInterpolator(2f))
                .start();
    }

    /**
     * Bounce animation
     */
    public static void bounce(Context context, View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", 0, -30, 0);
        animator.setDuration(500);
        animator.setInterpolator(new BounceInterpolator());
        animator.start();
    }

    /**
     * Pulse animation (heartbeat effect)
     */
    public static void pulse(Context context, View view) {
        AnimatorSet set = new AnimatorSet();
        
        ObjectAnimator scaleX1 = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f);
        ObjectAnimator scaleY1 = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f);
        ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(view, "scaleX", 1.1f, 1f);
        ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(view, "scaleY", 1.1f, 1f);
        
        scaleX1.setDuration(200);
        scaleY1.setDuration(200);
        scaleX2.setDuration(200);
        scaleY2.setDuration(200);
        
        set.play(scaleX1).with(scaleY1);
        set.play(scaleX2).with(scaleY2).after(scaleX1);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();
    }

    /**
     * Heartbeat animation for favorite
     */
    public static void heartbeat(Context context, View view) {
        AnimatorSet set = new AnimatorSet();
        
        ObjectAnimator scaleX1 = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.3f);
        ObjectAnimator scaleY1 = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.3f);
        ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(view, "scaleX", 1.3f, 1f);
        ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(view, "scaleY", 1.3f, 1f);
        
        scaleX1.setDuration(150);
        scaleY1.setDuration(150);
        scaleX2.setDuration(150);
        scaleY2.setDuration(150);
        
        set.play(scaleX1).with(scaleY1);
        set.play(scaleX2).with(scaleY2).after(scaleX1);
        set.setInterpolator(new OvershootInterpolator());
        set.start();
    }

    /**
     * Item fall down animation (staggered entrance)
     */
    public static void itemFallDown(Context context, View view, int position) {
        view.setAlpha(0f);
        view.setTranslationY(-100f);
        view.setVisibility(View.VISIBLE);
        
        view.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(500)
                .setStartDelay(position * 80L)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Shake animation for error
     */
    public static void shake(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 
                0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        animator.setDuration(500);
        animator.start();
    }

    /**
     * Rotate animation
     */
    public static void rotate(View view, float fromDegree, float toDegree, long duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", fromDegree, toDegree);
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    /**
     * Reveal animation (circular reveal)
     */
    public static void revealView(View view) {
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(400);
        set.setInterpolator(new OvershootInterpolator());
        set.start();
    }

    /**
     * Hide view with animation
     */
    public static void hideView(View view) {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(300);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
        set.start();
    }

    /**
     * Flip animation
     */
    public static void flipView(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotationY", 0f, 360f);
        animator.setDuration(600);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    /**
     * Shimmer loading effect
     */
    public static void shimmer(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.3f, 1f, 0.3f);
        animator.setDuration(1500);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    /**
     * Slide in from right
     */
    public static void slideInFromRight(View view, long duration) {
        view.setTranslationX(view.getWidth());
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .translationX(0)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Slide out to left
     */
    public static void slideOutToLeft(View view, long duration) {
        view.animate()
                .translationX(-view.getWidth())
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }

    /**
     * Pop in animation (for dialogs/modals)
     */
    public static void popIn(View view) {
        view.setScaleX(0.3f);
        view.setScaleY(0.3f);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.3f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.3f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(400);
        set.setInterpolator(new OvershootInterpolator(1.5f));
        set.start();
    }

    /**
     * Count up animation for numbers
     */
    public static void countUp(android.widget.TextView textView, int from, int to, long duration) {
        ValueAnimator animator = ValueAnimator.ofInt(from, to);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            textView.setText(String.valueOf(animation.getAnimatedValue()));
        });
        animator.start();
    }

    /**
     * Scale down animation
     */
    public static void scaleDown(Context context, View view) {
        view.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(150)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

    /**
     * Fade in with duration parameter
     */
    public static void fadeIn(Context context, View view) {
        fadeIn(view, 300);
    }

    /**
     * Fade out with duration parameter
     */
    public static void fadeOut(Context context, View view) {
        fadeOut(view, 300);
    }

    /**
     * Rotate animation with context
     */
    public static void rotate(Context context, View view) {
        rotate(view, 0f, 360f, 1000);
    }

    /**
     * Stop rotate animation
     */
    public static void stopRotate(View view) {
        view.animate().cancel();
        view.setRotation(0f);
    }

    /**
     * Zoom in bounce animation
     */
    public static void zoomInBounce(Context context, View view) {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f, 1f);
        
        set.playTogether(scaleX, scaleY);
        set.setDuration(600);
        set.setInterpolator(new BounceInterpolator());
        set.start();
    }

    /**
     * Wiggle animation (like shake but rotation-based)
     */
    public static void wiggle(Context context, View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", 
                0, -5, 5, -5, 5, -3, 3, 0);
        animator.setDuration(500);
        animator.start();
    }
}