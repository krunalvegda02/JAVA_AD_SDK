package com.example.ad_integration_sdk.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper class for managing SharedPreferences in the SDK
 */
public class PreferencesHelper {
    private static final String PREFS_NAME = "ad_integration_sdk_prefs";
    private static final String SDK_VERSION_KEY = "sdk_version";
    private static final String FIRST_LAUNCH_KEY = "first_launch";
    private static final String USER_CONSENT_KEY = "user_consent";
    private static final String TEST_MODE_KEY = "test_mode";

    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;

    /**
     * Initialize the preferences helper
     * @param context Application context
     */
    public static void init(Context context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            editor = preferences.edit();
        }
    }

    /**
     * Store a string value
     */
    public static void putString(String key, String value) {
        if (editor != null) {
            editor.putString(key, value);
            editor.apply();
        }
    }

    /**
     * Get a string value
     */
    public static String getString(String key, String defaultValue) {
        if (preferences != null) {
            return preferences.getString(key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * Store a boolean value
     */
    public static void putBoolean(String key, boolean value) {
        if (editor != null) {
            editor.putBoolean(key, value);
            editor.apply();
        }
    }

    /**
     * Get a boolean value
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        if (preferences != null) {
            return preferences.getBoolean(key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * Store an integer value
     */
    public static void putInt(String key, int value) {
        if (editor != null) {
            editor.putInt(key, value);
            editor.apply();
        }
    }

    /**
     * Get an integer value
     */
    public static int getInt(String key, int defaultValue) {
        if (preferences != null) {
            return preferences.getInt(key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * Store a long value
     */
    public static void putLong(String key, long value) {
        if (editor != null) {
            editor.putLong(key, value);
            editor.apply();
        }
    }

    /**
     * Get a long value
     */
    public static long getLong(String key, long defaultValue) {
        if (preferences != null) {
            return preferences.getLong(key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * Remove a key
     */
    public static void remove(String key) {
        if (editor != null) {
            editor.remove(key);
            editor.apply();
        }
    }

    /**
     * Clear all preferences
     */
    public static void clear() {
        if (editor != null) {
            editor.clear();
            editor.apply();
        }
    }

    /**
     * Check if SDK is first launch
     */
    public static boolean isFirstLaunch() {
        return getBoolean(FIRST_LAUNCH_KEY, true);
    }

    /**
     * Mark SDK as launched
     */
    public static void setFirstLaunchComplete() {
        putBoolean(FIRST_LAUNCH_KEY, false);
    }

    /**
     * Store SDK version
     */
    public static void setSdkVersion(String version) {
        putString(SDK_VERSION_KEY, version);
    }

    /**
     * Get stored SDK version
     */
    public static String getSdkVersion() {
        return getString(SDK_VERSION_KEY, "1.0.0");
    }

    /**
     * Store user consent status
     */
    public static void setUserConsent(boolean hasConsent) {
        putBoolean(USER_CONSENT_KEY, hasConsent);
    }

    /**
     * Get user consent status
     */
    public static boolean hasUserConsent() {
        return getBoolean(USER_CONSENT_KEY, false);
    }

    /**
     * Store test mode status
     */
    public static void setTestMode(boolean isTestMode) {
        putBoolean(TEST_MODE_KEY, isTestMode);
    }

    /**
     * Get test mode status
     */
    public static boolean isTestMode() {
        return getBoolean(TEST_MODE_KEY, false);
    }
}
