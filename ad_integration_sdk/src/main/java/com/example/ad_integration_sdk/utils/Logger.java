package com.example.ad_integration_sdk.utils;

import android.util.Log;

/**
 * Logger utility for the SDK
 * Provides centralized logging with SDK prefix
 */
public class Logger {
    private static final String SDK_TAG = "AdIntegrationSDK";
    private static boolean debugEnabled = true;

    /**
     * Enable or disable debug logging
     */
    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    /**
     * Check if debug logging is enabled
     */
    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * Log debug message
     */
    public static void d(String tag, String message) {
        if (debugEnabled) {
            Log.d(SDK_TAG + "_" + tag, message);
        }
    }

    /**
     * Log info message
     */
    public static void i(String tag, String message) {
        if (debugEnabled) {
            Log.i(SDK_TAG + "_" + tag, message);
        }
    }

    /**
     * Log warning message
     */
    public static void w(String tag, String message) {
        if (debugEnabled) {
            Log.w(SDK_TAG + "_" + tag, message);
        }
    }

    /**
     * Log error message
     */
    public static void e(String tag, String message) {
        Log.e(SDK_TAG + "_" + tag, message);
    }

    /**
     * Log error message with throwable
     */
    public static void e(String tag, String message, Throwable throwable) {
        Log.e(SDK_TAG + "_" + tag, message, throwable);
    }

    /**
     * Log verbose message
     */
    public static void v(String tag, String message) {
        if (debugEnabled) {
            Log.v(SDK_TAG + "_" + tag, message);
        }
    }
}
