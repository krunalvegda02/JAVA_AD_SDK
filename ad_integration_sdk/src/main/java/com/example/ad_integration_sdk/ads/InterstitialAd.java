package com.example.ad_integration_sdk.ads;
import com.example.ad_integration_sdk.network.AdData;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ad_integration_sdk.AdConfig;
import com.example.ad_integration_sdk.AdEventListener;
import com.example.ad_integration_sdk.AdSDK;
import com.example.ad_integration_sdk.R;
import com.example.ad_integration_sdk.network.AdParser;
import com.example.ad_integration_sdk.network.NetworkClient;
import com.example.ad_integration_sdk.ui.AnimationUtils;
import com.example.ad_integration_sdk.utils.Constants;
import com.example.ad_integration_sdk.utils.Logger;

public class InterstitialAd {
    private static final String TAG = "InterstitialAd";

    // UI Components
    private Dialog adDialog;
    private ImageView adImage;
    private VideoView adVideo;
    private TextView adTitle;
    private TextView adDescription;
    private TextView adCTA;
    private ImageButton closeButton;
    private TextView countdownText;
    private ProgressBar loadingProgress;
    private View contentContainer;

    // State
    private Activity activity;
    private AdConfig.AdRequest adRequest;
    private AdData currentAdData;


    private AdEventListener.InterstitialAdListener adListener;
    private boolean isLoaded = false;
    private boolean isShowing = false;
    private boolean impressionTracked = false;

    // Close button countdown
    private Handler countdownHandler;
    private Runnable countdownRunnable;
    private int countdownSeconds = 5;

    /**
     * Create InterstitialAd instance
     * @param activity Activity context
     */
    public InterstitialAd(Activity activity) {
        this.activity = activity;
        this.countdownHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Set ad listener for events
     */
    public void setAdListener(AdEventListener.InterstitialAdListener listener) {
        this.adListener = listener;
    }

    /**
     * Load interstitial ad
     */
    public void loadAd(String placementId) {
        loadAd(new AdConfig.AdRequest(placementId));
    }

    /**
     * Load ad with detailed request
     */
    public void loadAd(AdConfig.AdRequest request) {
        if (!AdSDK.isInitialized()) {
            Logger.e(TAG, "AdSDK not initialized");
            notifyAdFailedToLoad("AdSDK not initialized");
            return;
        }

        if (request == null || request.getPlacementId() == null) {
            Logger.e(TAG, "Invalid ad request");
            notifyAdFailedToLoad("Invalid ad request");
            return;
        }

        this.adRequest = request;
        this.isLoaded = false;
        this.impressionTracked = false;

        Logger.d(TAG, "Loading interstitial ad for placement: " + request.getPlacementId());

        // Load ad from network
        AdSDK.getInstance().getNetworkClient().loadAd(
                request.getPlacementId(),
                Constants.AD_TYPE_INTERSTITIAL,
                new NetworkClient.AdLoadCallback() {
                    @Override
                    public void onAdLoaded(AdData adData) {
                        handleAdLoaded(adData);
                    }

                    @Override
                    public void onAdFailedToLoad(String error) {
                        handleAdFailedToLoad(error);
                    }
                }
        );
    }

    /**
     * Show the loaded interstitial ad
     */
    public void show() {
        if (!isLoaded || currentAdData == null) {
            Logger.w(TAG, "Ad not loaded");
            notifyAdFailedToShow("Ad not loaded");
            return;
        }

        if (isShowing) {
            Logger.w(TAG, "Ad is already showing");
            return;
        }

        if (activity == null || activity.isFinishing()) {
            Logger.e(TAG, "Activity is not valid");
            notifyAdFailedToShow("Activity is not valid");
            return;
        }

        try {
            createAndShowDialog();
            isShowing = true;
            notifyAdShown();

        } catch (Exception e) {
            Logger.e(TAG, "Failed to show interstitial ad", e);
            notifyAdFailedToShow(e.getMessage());
        }
    }

    /**
     * Check if ad is loaded
     */
    public boolean isLoaded() {
        return isLoaded && currentAdData != null;
    }

    /**
     * Destroy and cleanup
     */
    public void destroy() {
        if (adDialog != null && adDialog.isShowing()) {
            adDialog.dismiss();
        }

        if (countdownHandler != null && countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }

        if (adVideo != null) {
            adVideo.stopPlayback();
        }

        currentAdData = null;
        isLoaded = false;
        isShowing = false;
        impressionTracked = false;
    }

    private void handleAdLoaded(AdData adData) {
        if (adData == null || !adData.isValid()) {
            handleAdFailedToLoad("Invalid ad data");
            return;
        }

        this.currentAdData = adData;
        this.isLoaded = true;

        Logger.d(TAG, "Interstitial ad loaded successfully");
        notifyAdLoaded();
    }

    private void handleAdFailedToLoad(String error) {
        this.isLoaded = false;
        this.currentAdData = null;

        Logger.e(TAG, "Failed to load interstitial ad: " + error);
        notifyAdFailedToLoad(error);
    }

    private void createAndShowDialog() {
        // Create full-screen dialog
        adDialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        adDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        adDialog.setCancelable(false);
        adDialog.setCanceledOnTouchOutside(false);

        // Set window flags
        Window window = adDialog.getWindow();
        if (window != null) {
            window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            );
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }

        // Inflate layout
        View dialogView = activity.getLayoutInflater().inflate(
                R.layout.interstitial_ad_layout, null);
        adDialog.setContentView(dialogView);

        // Initialize views
        initializeDialogViews(dialogView);

        // Populate content
        populateAdContent();

        // Setup handlers
        setupClickHandlers();
        setupVideoHandlers();

        // Show with animation
        adDialog.show();
        AnimationUtils.fadeIn(dialogView);

        // Start close button countdown
        startCloseButtonCountdown();

        // Track impression
        trackImpression();
    }

    private void initializeDialogViews(View dialogView) {
        adImage = dialogView.findViewById(R.id.ad_image);
        adVideo = dialogView.findViewById(R.id.ad_video);
        adTitle = dialogView.findViewById(R.id.ad_title);
        adDescription = dialogView.findViewById(R.id.ad_description);
        adCTA = dialogView.findViewById(R.id.ad_cta);
        closeButton = dialogView.findViewById(R.id.close_button);
        countdownText = dialogView.findViewById(R.id.countdown_text);
        loadingProgress = dialogView.findViewById(R.id.loading_progress);
        contentContainer = dialogView.findViewById(R.id.content_container);

        // Initially hide close button
        closeButton.setVisibility(View.GONE);
        countdownText.setVisibility(View.VISIBLE);
    }

    private void populateAdContent() {
        if (currentAdData == null) return;

        // Set text content
        if (currentAdData.getTitle() != null) {
            adTitle.setText(currentAdData.getTitle());
            adTitle.setVisibility(View.VISIBLE);
        }

        if (currentAdData.getDescription() != null) {
            adDescription.setText(currentAdData.getDescription());
            adDescription.setVisibility(View.VISIBLE);
        }

        adCTA.setText(currentAdData.getCtaText());

        // Load media
        if (currentAdData.hasVideo()) {
            setupVideoContent();
        } else if (currentAdData.hasImage()) {
            setupImageContent();
        }
    }

    private void setupImageContent() {
        adVideo.setVisibility(View.GONE);
        adImage.setVisibility(View.VISIBLE);

        Glide.with(activity)
                .load(currentAdData.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ad_placeholder)
                .error(R.drawable.ad_error)
                .into(adImage);
    }

    private void setupVideoContent() {
        adImage.setVisibility(View.GONE);
        adVideo.setVisibility(View.VISIBLE);

        try {
            adVideo.setVideoURI(Uri.parse(currentAdData.getVideoUrl()));
            adVideo.setOnPreparedListener(mediaPlayer -> {
                mediaPlayer.setLooping(true);
                adVideo.start();
            });
        } catch (Exception e) {
            Logger.e(TAG, "Failed to setup video", e);
            if (currentAdData.hasImage()) {
                setupImageContent();
            }
        }
    }

    private void setupClickHandlers() {
        closeButton.setOnClickListener(v -> {
            AnimationUtils.pulseAnimation(v);
            dismissAd();
        });

        adCTA.setOnClickListener(v -> {
            AnimationUtils.pulseAnimation(v);
            handleAdClick();
        });

        // Make entire ad clickable
        contentContainer.setOnClickListener(v -> handleAdClick());
    }

    private void setupVideoHandlers() {
        if (adVideo != null) {
            adVideo.setOnErrorListener((mp, what, extra) -> {
                Logger.e(TAG, "Video error: " + what + ", " + extra);
                if (currentAdData != null && currentAdData.hasImage()) {
                    setupImageContent();
                }
                return true;
            });
        }
    }

    private void handleAdClick() {
        if (currentAdData == null) return;

        // Track click
        trackClick();

        // Open URL
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentAdData.getClickUrl()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);

        } catch (Exception e) {
            Logger.e(TAG, "Failed to open click URL", e);
        }

        notifyAdClicked();
    }

    private void startCloseButtonCountdown() {
        countdownSeconds = Constants.INTERSTITIAL_CLOSE_BUTTON_DELAY_MS / 1000;

        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                if (countdownSeconds > 0) {
                    countdownText.setText("Close in " + countdownSeconds + "s");
                    countdownSeconds--;
                    countdownHandler.postDelayed(this, 1000);
                } else {
                    // Enable close button
                    closeButton.setVisibility(View.VISIBLE);
                    countdownText.setVisibility(View.GONE);
                    AnimationUtils.bounceIn(closeButton);
                }
            }
        };

        countdownHandler.post(countdownRunnable);
    }

    private void dismissAd() {
        if (adDialog != null && adDialog.isShowing()) {
            AnimationUtils.fadeOut(adDialog.findViewById(R.id.interstitial_root), () -> {
                adDialog.dismiss();
                cleanup();
            });
        }
    }

    private void cleanup() {
        isShowing = false;
        isLoaded = false;

        if (countdownHandler != null && countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }

        if (adVideo != null) {
            adVideo.stopPlayback();
        }

        notifyAdDismissed();
    }

    private void trackImpression() {
        if (currentAdData == null || impressionTracked) return;

        impressionTracked = true;
        AdSDK.getInstance().getNetworkClient().trackEvent(
                currentAdData.getAdId(),
                Constants.EVENT_IMPRESSION,
                new NetworkClient.TrackingCallback() {
                    @Override
                    public void onTrackingSuccess() {
                        Logger.d(TAG, "Impression tracked");
                        notifyAdImpression();
                    }

                    @Override
                    public void onTrackingFailed(String error) {
                        Logger.w(TAG, "Failed to track impression: " + error);
                    }
                }
        );
    }

    private void trackClick() {
        if (currentAdData == null) return;

        AdSDK.getInstance().getNetworkClient().trackEvent(
                currentAdData.getAdId(),
                Constants.EVENT_CLICK,
                new NetworkClient.TrackingCallback() {
                    @Override
                    public void onTrackingSuccess() {
                        Logger.d(TAG, "Click tracked");
                    }

                    @Override
                    public void onTrackingFailed(String error) {
                        Logger.w(TAG, "Failed to track click: " + error);
                    }
                }
        );
    }

    // Event notification methods
    private void notifyAdLoaded() {
        if (adListener != null) {
            adListener.onAdLoaded();
        }
    }

    private void notifyAdFailedToLoad(String error) {
        if (adListener != null) {
            adListener.onAdFailedToLoad(error);
        }
    }

    private void notifyAdShown() {
        if (adListener != null) {
            adListener.onAdShown();
        }
    }

    private void notifyAdFailedToShow(String error) {
        if (adListener != null) {
            adListener.onAdFailedToShow(error);
        }
    }

    private void notifyAdClicked() {
        if (adListener != null) {
            adListener.onAdClicked();
        }
    }

    private void notifyAdImpression() {
        if (adListener != null) {
            adListener.onAdImpression();
        }
    }

    private void notifyAdDismissed() {
        if (adListener != null) {
            adListener.onAdDismissed();
        }
    }
}
