package com.example.ad_integration_sdk.utils;

import android.content.Context;

public class AdSize {
    private final int width;
    private final int height;
    private final String name;

    private AdSize(int width, int height, String name) {
        this.width = width;
        this.height = height;
        this.name = name;
    }

    // Standard IAB Banner Sizes (Google AdMob Compatible)
    public static final AdSize BANNER = new AdSize(30, 50, "BANNER");
    public static final AdSize LARGE_BANNER = new AdSize(320, 100, "LARGE_BANNER");
    public static final AdSize MEDIUM_RECTANGLE = new AdSize(300, 250, "MEDIUM_RECTANGLE");
    public static final AdSize FULL_BANNER = new AdSize(468, 60, "FULL_BANNER");
    public static final AdSize LEADERBOARD = new AdSize(728, 90, "LEADERBOARD");
    public static final AdSize SMART_BANNER = new AdSize(-1, -2, "SMART_BANNER"); // Full width, adaptive height

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getName() { return name; }

    public int getWidthInPixels(Context context) {
        if (width == -1) {
            return context.getResources().getDisplayMetrics().widthPixels;
        }
        return (int) (width * context.getResources().getDisplayMetrics().density);
    }

    public int getHeightInPixels(Context context) {
        if (height == -2) {
            return (int) (50 * context.getResources().getDisplayMetrics().density);
        }
        return (int) (height * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public String toString() {
        return name + " (" + width + "x" + height + ")";
    }
}
