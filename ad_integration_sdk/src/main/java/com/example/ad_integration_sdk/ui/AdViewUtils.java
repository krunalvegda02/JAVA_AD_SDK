package com.example.ad_integration_sdk.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

/**
 * Utility class for ad view operations
 */
public class AdViewUtils {

    /**
     * Convert dp to pixels
     */
    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.density));
    }

    /**
     * Convert pixels to dp
     */
    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / displayMetrics.density);
    }

    /**
     * Get screen width in pixels
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    /**
     * Get screen height in pixels
     */
    public static int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    /**
     * Create gradient background programmatically
     */
    public static GradientDrawable createGradientBackground(int startColor, int endColor, int cornerRadius) {
        GradientDrawable gradient = new GradientDrawable();
        gradient.setColors(new int[]{startColor, endColor});
        gradient.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        gradient.setCornerRadius(cornerRadius);
        return gradient;
    }

    /**
     * Create solid background with corner radius
     */
    public static GradientDrawable createSolidBackground(int color, int cornerRadius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(cornerRadius);
        return drawable;
    }

    /**
     * Set view margins
     */
    public static void setMargins(View view, int left, int top, int right, int bottom) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(left, top, right, bottom);
        view.setLayoutParams(layoutParams);
    }

    /**
     * Set view padding
     */
    public static void setPadding(View view, int left, int top, int right, int bottom) {
        view.setPadding(left, top, right, bottom);
    }

    /**
     * Make view visible with fade in
     */
    public static void showView(View view) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        view.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
    }

    /**
     * Hide view with fade out
     */
    public static void hideView(View view) {
        view.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }

    /**
     * Check if color is dark
     */
    public static boolean isDarkColor(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }
}
