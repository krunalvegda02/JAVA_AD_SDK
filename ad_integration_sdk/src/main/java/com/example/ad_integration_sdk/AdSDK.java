//package com.example.ad_integration_sdk;
//
//import android.content.Context;
//import android.util.Log;
//import com.example.ad_integration_sdk.network.NetworkClient;
//import com.example.ad_integration_sdk.utils.Logger;
//import com.example.ad_integration_sdk.utils.PreferencesHelper;
//
//public class AdSDK {
//    private static final String TAG = "AdSDK";
//    private static final String SDK_VERSION = "1.0.0";
//
//    private static AdSDK instance;
//    private static boolean isInitialized = false;
//    private static boolean isTestMode = false;
//
//    private Context context;
//    private String publisherId;
//    private NetworkClient networkClient;
//
//    private AdSDK() {}
//
//    /**
//     * Initialize the AdSDK with publisher ID
//     * @param context Application context
//     * @param publisherId Your publisher ID from the platform
//     */
//    public static synchronized void initialize(Context context, String publisherId) {
//        if (isInitialized) {
//            Logger.d(TAG, "SDK already initialized");
//            return;
//        }
//
//        if (context == null || publisherId == null || publisherId.trim().isEmpty()) {
//            throw new IllegalArgumentException("Context and publisherId cannot be null or empty");
//        }
//
//        try {
//            instance = new AdSDK();
//            instance.context = context.getApplicationContext();
//            instance.publisherId = publisherId;
//
//            // Initialize components
//            PreferencesHelper.init(context);
////          instance.networkClient = new NetworkClient(publisherId, isTestMode);
//            instance.networkClient = new NetworkClient(publisherId, isTestMode);
//
//            isInitialized = true;
//            Logger.i(TAG, "AdSDK v" + SDK_VERSION + " initialized successfully for publisher: " + publisherId);
//
//        } catch (Exception e) {
//            Logger.e(TAG, "Failed to initialize AdSDK", e);
//            throw new RuntimeException("AdSDK initialization failed", e);
//        }
//    }
//
//    /**
//     * Get AdSDK instance
//     */
//    public static AdSDK getInstance() {
//        if (!isInitialized) {
//            throw new IllegalStateException("AdSDK must be initialized before use. Call AdSDK.initialize() first.");
//        }
//
//        return instance;
//    }
//
//    /**
//     * Enable test mode for development
//     */
//    public static void setTestMode(boolean testMode) {
//        isTestMode = testMode;
//        Logger.d(TAG, "Test mode " + (testMode ? "enabled" : "disabled"));
//    }
//
//    /**
//     * Check if SDK is initialized
//     */
//    public static boolean isInitialized() {
//        return isInitialized;
//    }
//
//    /**
//     * Get SDK version
//     */
//    public static String getVersion() {
//        return SDK_VERSION;
//    }
//
//    // Getters
//    public Context getContext() { return context; }
//    public String getPublisherId() { return publisherId; }
//    public NetworkClient getNetworkClient() { return networkClient; }
//    public boolean isTestMode() { return isTestMode; }
//}



package com.example.ad_integration_sdk;

import android.content.Context;
import com.example.ad_integration_sdk.network.NetworkClient;
import com.example.ad_integration_sdk.utils.Logger;
import com.example.ad_integration_sdk.utils.PreferencesHelper;

public class AdSDK {
    private static final String TAG = "AdSDK";
    private static final String SDK_VERSION = "1.0.0";

    private static AdSDK instance;
    private static boolean isInitialized = false;
    private static boolean isTestMode = false;

    private Context context;
    private String publisherId;
    private NetworkClient networkClient;

    private AdSDK() {}

    /** Initialize the SDK. Call once in your Application or Activity. */
    public static synchronized void initialize(Context context, String publisherId) {
        if (isInitialized) {
            Logger.d(TAG, "SDK already initialized");
            return;
        }
        if (context == null || publisherId == null || publisherId.trim().isEmpty()) {
            throw new IllegalArgumentException("Context and publisherId cannot be null or empty");
        }
        instance = new AdSDK();
        instance.context = context.getApplicationContext();
        instance.publisherId = publisherId;

        // Initialize helpers
        PreferencesHelper.init(context);

        // Pass testMode flag into NetworkClient
        instance.networkClient = new NetworkClient(publisherId, isTestMode);

        isInitialized = true;
        Logger.i(TAG, "AdSDK v" + SDK_VERSION + " initialized (testMode=" + isTestMode + ")");
    }

    /** Enable or disable test mode before initialization. */
    public static void setTestMode(boolean testMode) {
        isTestMode = testMode;
        Logger.d(TAG, "Test mode " + (testMode ? "enabled" : "disabled"));
    }

    public static AdSDK getInstance() {
        if (!isInitialized) {
            throw new IllegalStateException("AdSDK must be initialized first.");
        }
        return instance;
    }

    public static boolean isInitialized() { return isInitialized; }
    public static String getVersion() { return SDK_VERSION; }
    public Context getContext() { return context; }
    public String getPublisherId() { return publisherId; }
    public NetworkClient getNetworkClient() { return networkClient; }
    public boolean isTestMode() { return isTestMode; }
}








