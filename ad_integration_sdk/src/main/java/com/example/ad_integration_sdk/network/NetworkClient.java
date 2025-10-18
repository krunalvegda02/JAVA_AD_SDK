//package com.example.ad_integration_sdk.network;
//
//import android.os.Handler;
//import android.os.Looper;
//import com.example.ad_integration_sdk.utils.Logger;
//                        response.append(line);
//                    }
//                    reader.close();
//
//                    // Parse ad data
//                    AdData adData = AdParser.parseAdResponse(response.toString());
//
//                    // Return success on main thread
//                    mainHandler.post(() -> callback.onAdLoaded(adData));
//
//                } else {
//                    String error = "HTTP Error: " + responseCode;
//                    Logger.e(TAG, error);
//                    mainHandler.post(() -> callback.onAdFailedToLoad(error));
//                }
//
//                connection.disconnect();
//
//            } catch (Exception e) {
//                Logger.e(TAG, "Failed to load ad", e);
//                mainHandler.post(() -> callback.onAdFailedToLoad(e.getMessage()));
//            }
//        });
//    }
//
//    /**
//     * Track ad events (impression, click, etc.)
//     */
//    public void trackEvent(String adId, String eventType, TrackingCallback callback) {
//        executorService.execute(() -> {
//            try {
//                String baseUrl = isTestMode ? TEST_BASE_URL : BASE_URL;
//                URL url = new URL(baseUrl + "ads/track");
//
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("POST");
//                connection.setRequestProperty("Content-Type", "application/json");
//                connection.setConnectTimeout(TIMEOUT_MS);
//                connection.setReadTimeout(TIMEOUT_MS);
//                connection.setDoOutput(true);
//
//                JSONObject requestBody = new JSONObject();
//                requestBody.put("publisherId", publisherId);
//                requestBody.put("adId", adId);
//                requestBody.put("eventType", eventType);
//                requestBody.put("timestamp", System.currentTimeMillis());
//
//                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
//                writer.write(requestBody.toString());
//                writer.flush();
//                writer.close();
//
//                int responseCode = connection.getResponseCode();
//
//                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    mainHandler.post(() -> callback.onTrackingSuccess());
//                } else {
//                    mainHandler.post(() -> callback.onTrackingFailed("HTTP Error: " + responseCode));
//                }
//
//                connection.disconnect();
//
//            } catch (Exception e) {
//                Logger.e(TAG, "Failed to track event", e);
//                mainHandler.post(() -> callback.onTrackingFailed(e.getMessage()));
//            }
//        });
//    }
//
//    // Callback interfaces - FIXED TYPES
//    public interface AdLoadCallback {
//        void onAdLoaded(AdData adData); // ← CHANGED from AdParser.AdData to AdData
//        void onAdFailedToLoad(String error);
//    }
//
//    public interface TrackingCallback {
//        void onTrackingSuccess();
//        void onTrackingFailed(String error);
//    }
//}




//import com.example.ad_integration_sdk.network.AdData; // ← Add this import
//import org.json.JSONObject;
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.io.OutputStreamWriter;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class NetworkClient {
//    private static final String TAG = "NetworkClient";
//    private static final String BASE_URL = "https://your-api-backend.com/v1/";
//    private static final String TEST_BASE_URL = "https://test-api-backend.com/v1/";
//    private static final int TIMEOUT_MS = 10000;
//
//    private final String publisherId;
//    private final boolean isTestMode;
//    private final ExecutorService executorService;
//    private final Handler mainHandler;
//
//    public NetworkClient(String publisherId, boolean isTestMode) {
//        this.publisherId = publisherId;
//        this.isTestMode = isTestMode;
//        this.executorService = Executors.newFixedThreadPool(3);
//        this.mainHandler = new Handler(Looper.getMainLooper());
//    }
//
//    /**
//     * Load ad from backend
//     */
//    public void loadAd(String placementId, String adType, AdLoadCallback callback) {
//        executorService.execute(() -> {
//            try {
//                String baseUrl = isTestMode ? TEST_BASE_URL : BASE_URL;
//                URL url = new URL(baseUrl + "ads/load");
//
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("POST");
//                connection.setRequestProperty("Content-Type", "application/json");
//                connection.setConnectTimeout(TIMEOUT_MS);
//                connection.setReadTimeout(TIMEOUT_MS);
//                connection.setDoOutput(true);
//
//                // Create request body
//                JSONObject requestBody = new JSONObject();
//                requestBody.put("publisherId", publisherId);
//                requestBody.put("placementId", placementId);
//                requestBody.put("adType", adType);
//                requestBody.put("testMode", isTestMode);
//
//                // Send request
//                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
//                writer.write(requestBody.toString());
//                writer.flush();
//                writer.close();
//
//                int responseCode = connection.getResponseCode();
//
//                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    // Read response
//                    BufferedReader reader = new BufferedReader(
//                            new InputStreamReader(connection.getInputStream()));
//                    StringBuilder response = new StringBuilder();
//                    String line;
//                    while ((line = reader.readLine()) != null) {



package com.example.ad_integration_sdk.network;

import android.os.Handler;
import android.os.Looper;
import com.example.ad_integration_sdk.utils.Logger;

public class NetworkClient {
    private static final String TAG = "NetworkClient";

    private final boolean isTestMode;
    private final Handler mainHandler;
  // Constructor receives publisherId for future real API calls (not used here) and test mode flag
    public NetworkClient(String publisherId, boolean isTestMode) {
        this.isTestMode = isTestMode;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /** Load an ad: Either static mock or real (stubbed here). */
    public void loadAd(String placementId, String adType, AdLoadCallback callback) {
        Logger.d(TAG, "loadAd called (testMode=" + isTestMode + ")");
        if (isTestMode) {
            provideMockAd(placementId, adType, callback);
        } else {
            loadRealAd(placementId, adType, callback);
        }
    }

    /** Provide static mock ad data with simulated network delay. */
    private void provideMockAd(String placementId, String adType, AdLoadCallback callback) {
        mainHandler.postDelayed(() -> {
            try {
                AdData mock = MockAdFactory.create(adType, placementId);
                callback.onAdLoaded(mock);
            } catch (Exception e) {
                callback.onAdFailedToLoad("Mock error: " + e.getMessage());
            }
        }, 500);
    }

    /** Stub: Real network logic goes here if testMode is false. */
    private void loadRealAd(String placementId, String adType, AdLoadCallback callback) {
        // Stubbed: immediately fail for now
        callback.onAdFailedToLoad("Real API call not implemented");
    }

    /** Tracking stub: Always succeeds immediately. */
    public void trackEvent(String adId, String eventType, TrackingCallback callback) {
        Logger.d(TAG, "trackEvent called for adId=" + adId + " event=" + eventType);
        mainHandler.post(callback::onTrackingSuccess);
    }

    /** Callback interface for ad loading. */
    public interface AdLoadCallback {
        void onAdLoaded(AdData adData);
        void onAdFailedToLoad(String error);
    }

    /** Callback interface for event tracking. */
    public interface TrackingCallback {
        void onTrackingSuccess();
        void onTrackingFailed(String error);
    }
}
