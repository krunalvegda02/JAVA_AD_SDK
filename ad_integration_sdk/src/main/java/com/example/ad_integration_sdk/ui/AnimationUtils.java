package com.example.ad_integration_sdk.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;

public class AnimationUtils {

    /**
     * Fade in animation
     */
    public static void fadeIn(View view) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        fadeIn.setDuration(300);
        fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeIn.start();
    }

    /**
     * Fade out animation - FIXED VERSION
     */
    public static void fadeOut(View view) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        fadeOut.setDuration(300);
        fadeOut.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
        fadeOut.start();
    }

    /**
     * Fade out animation with callback - NEW METHOD
     */
    public static void fadeOut(View view, Runnable onComplete) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        fadeOut.setDuration(300);
        fadeOut.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        fadeOut.start();
    }

    /**
     * Pulse animation for click feedback
     */
    public static void pulseAnimation(View view) {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.1f, 1f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.1f, 1f);

        ObjectAnimator pulse = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY);
        pulse.setDuration(150);
        pulse.setInterpolator(new OvershootInterpolator());
        pulse.start();
    }

    /**
     * Button press animation
     */
    public static void animateButtonPress(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                view.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .setInterpolator(new BounceInterpolator())
                        .start();
                break;
        }
    }

    /**
     * Bounce entrance animation
     */
    public static void bounceIn(View view) {
        view.setScaleX(0.3f);
        view.setScaleY(0.3f);
        view.setVisibility(View.VISIBLE);

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 0.3f, 1f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 0.3f, 1f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);

        ObjectAnimator bounceIn = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY, alpha);
        bounceIn.setDuration(500);
        bounceIn.setInterpolator(new BounceInterpolator());
        bounceIn.start();
    }

    /**
     * Slide up animation
     */
    public static void slideUp(View view) {
        view.setTranslationY(view.getHeight());
        view.setVisibility(View.VISIBLE);

        view.animate()
                .translationY(0)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    /**
     * Slide down animation
     */
    public static void slideDown(View view, Runnable onComplete) {
        view.animate()
                .translationY(view.getHeight())
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    view.setVisibility(View.GONE);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                })
                .start();
    }
}
