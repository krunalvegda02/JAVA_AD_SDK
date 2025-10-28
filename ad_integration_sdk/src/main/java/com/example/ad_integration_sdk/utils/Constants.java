package com.example.ad_integration_sdk.utils;

/**
 * Constants used throughout the SDK
 */
public class Constants {

    // Network Configuration
    public static final int NETWORK_TIMEOUT_MS = 10000;
    public static final int MAX_RETRY_COUNT = 3;
    public static final long RETRY_DELAY_MS = 1000;

    // Ad Event Types
    public static final String EVENT_IMPRESSION = "impression";
    public static final String EVENT_CLICK = "click";
    public static final String EVENT_SHOW = "show";
    public static final String EVENT_CLOSE = "close";
    public static final String EVENT_REWARD = "reward";
    public static final String EVENT_VIDEO_START = "video_start";
    public static final String EVENT_VIDEO_COMPLETE = "video_complete";

    // Ad Types
    public static final String AD_TYPE_BANNER = "banner";
    public static final String AD_TYPE_INTERSTITIAL = "interstitial";
    public static final String AD_TYPE_REWARDED = "rewarded";

    // Cache Configuration
    public static final int CACHE_MAX_SIZE_MB = 50;
    public static final long CACHE_MAX_AGE_MS = 24 * 60 * 60 * 1000; // 24 hours

    // Animation Durations
    public static final int ANIMATION_DURATION_SHORT = 150;
    public static final int ANIMATION_DURATION_MEDIUM = 300;
    public static final int ANIMATION_DURATION_LONG = 500;

    // Video Configuration
    public static final int VIDEO_TIMEOUT_MS = 30000;
    public static final int MIN_VIDEO_DURATION_MS = 5000;

    // Interstitial Configuration
    public static final int INTERSTITIAL_CLOSE_BUTTON_DELAY_MS = 5000;

    // Preferences Keys
    public static final String PREF_FIRST_LAUNCH = "first_launch";
    public static final String PREF_USER_CONSENT = "user_consent";
    public static final String PREF_LAST_AD_SHOWN = "last_ad_shown";
    public static final String PREF_SDK_VERSION = "sdk_version";

    // Error Messages
    public static final String ERROR_SDK_NOT_INITIALIZED = "SDK not initialized";
    public static final String ERROR_INVALID_REQUEST = "Invalid ad request";
    public static final String ERROR_NETWORK_ERROR = "Network error";
    public static final String ERROR_NO_ADS = "No ads available";
    public static final String ERROR_AD_EXPIRED = "Ad expired";
    public static final String ERROR_INVALID_PLACEMENT = "Invalid placement ID";
}






