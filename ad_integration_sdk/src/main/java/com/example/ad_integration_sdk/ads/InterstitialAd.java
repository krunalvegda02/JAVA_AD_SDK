package com.example.ad_integration_sdk.ads;

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
import android.widget.Button;
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
import com.example.ad_integration_sdk.network.AdData;
import com.example.ad_integration_sdk.network.NetworkClient;
import com.example.ad_integration_sdk.ui.AnimationUtils;
import com.example.ad_integration_sdk.utils.Constants;
import com.example.ad_integration_sdk.utils.Logger;

/**
 * Google-style Interstitial Ad with full dynamic customization
 * Professional, fullscreen ad experience matching Google Ads UI/UX
 */
public class InterstitialAd {

    private static final boolean STATIC_IMAGE_TESTING = true;

    // Helper for static image path from raw
    private String getStaticImageUri() {
        return "android.resource://" + activity.getPackageName() + "/" + R.raw.sample_interstitial;
    }




    private static final String TAG = "InterstitialAd";

    // UI Components
    private Dialog adDialog;
    private ImageView adImage;
    private VideoView adVideo;
    private TextView adTitle;
    private TextView adBody;
    private Button adCTA;
    private TextView closeButtonText;
    private ProgressBar loadingProgress;
    private View topBar;
    private View mainContent;

    // State
    private Activity activity;
    private AdConfig.AdRequest adRequest;
    private AdData currentAdData;
    private AdEventListener.InterstitialAdListener adListener;
    private boolean isLoaded = false;
    private boolean isShowing = false;
    private boolean impressionTracked = false;

    // Dynamic customization options
    private InterstitialAdCustomization customization;

    /**
     * Customization class for dynamic ad appearance and behavior
     */
    public static class InterstitialAdCustomization {
        // Text customization
        public String titleText = "Sponsored Ad";
        public String bodyText = null; // Will use AdData description if null
        public String ctaButtonText = null; // Will use AdData CTA if null
        public String closeButtonText = "Close Ad";

        // Fallback image when ad fails to load
        public String fallbackImageUrl = null;

        // UI customization
        public int backgroundColor = Color.BLACK;
        public int topBarBackgroundColor = Color.parseColor("#000000");
        public int titleTextColor = Color.WHITE;
        public int bodyTextColor = Color.parseColor("#DDDDDD");
        public int closeTextColor = Color.parseColor("#DDDDDD");
        public int ctaBackgroundColor = Color.parseColor("#2196F3"); // Blue
        public int ctaTextColor = Color.WHITE;

        // Text sizes (in SP)
        public float titleTextSize = 18f;
        public float bodyTextSize = 16f;
        public float ctaTextSize = 16f;
        public float closeTextSize = 16f;

        // Behavior
        public boolean enableCountdown = true;
        public int countdownSeconds = 5;
        public boolean dismissOnCtaClick = true;
        public boolean fullScreenClickable = false;

        // Animation
        public boolean enableFadeAnimation = true;
        public boolean enableCtaPulseAnimation = true;
    }

    /**
     * Create InterstitialAd instance
     * @param activity Activity context
     */
    public InterstitialAd(Activity activity) {
        this.activity = activity;
        this.customization = new InterstitialAdCustomization();
    }

    /**
     * Set custom appearance and behavior
     * @param customization Customization options
     */
    public void setCustomization(InterstitialAdCustomization customization) {
        this.customization = customization != null ? customization : new InterstitialAdCustomization();
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
            window.setBackgroundDrawable(new ColorDrawable(customization.backgroundColor));
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

        // Apply customization
        applyCustomization();

        // Populate content
        populateAdContent();

        // Setup handlers
        setupClickHandlers();
        setupVideoHandlers();

        // Show with animation
        adDialog.show();
        if (customization.enableFadeAnimation) {
            AnimationUtils.fadeIn(dialogView);
        }

        // Track impression
        trackImpression();
    }

    private void initializeDialogViews(View dialogView) {
        topBar = dialogView.findViewById(R.id.top_bar);
        mainContent = dialogView.findViewById(R.id.main_content);
        adImage = dialogView.findViewById(R.id.ad_image);
        adVideo = dialogView.findViewById(R.id.ad_video);
        adTitle = dialogView.findViewById(R.id.ad_title);
        adBody = dialogView.findViewById(R.id.ad_body);
        adCTA = dialogView.findViewById(R.id.ad_cta);
        closeButtonText = dialogView.findViewById(R.id.close_button_text);
        loadingProgress = dialogView.findViewById(R.id.loading_progress);
    }

    /**
     * Apply customization to UI elements with null-safety checks
     */
    private void applyCustomization() {
        // If no customization set, use defaults
        if (customization == null) {
            Logger.w(TAG, "No customization provided, using defaults");
            customization = new InterstitialAdCustomization();
            return;
        }

        try {
            // Apply top bar background color
            if (topBar != null) {
                topBar.setBackgroundColor(customization.topBarBackgroundColor);
            }

            // Apply title styling - safe null check
            if (adTitle != null) {
                adTitle.setTextColor(customization.titleTextColor);
                adTitle.setTextSize(customization.titleTextSize);
            } else {
                Logger.w(TAG, "adTitle view is null");
            }

            // Apply body styling - safe null check
            if (adBody != null) {
                adBody.setTextColor(customization.bodyTextColor);
                adBody.setTextSize(customization.bodyTextSize);
            } else {
                Logger.w(TAG, "adBody view is null");
            }

            // Apply close button styling - safe null check
            if (closeButtonText != null) {
                closeButtonText.setTextColor(customization.closeTextColor);
                closeButtonText.setTextSize(customization.closeTextSize);
                closeButtonText.setText(customization.closeButtonText);
            } else {
                Logger.w(TAG, "closeButtonText view is null");
            }

            // Apply CTA button styling - safe null check
            if (adCTA != null) {
                adCTA.setBackgroundColor(customization.ctaBackgroundColor);
                adCTA.setTextColor(customization.ctaTextColor);
                adCTA.setTextSize(customization.ctaTextSize);
            } else {
                Logger.w(TAG, "adCTA button is null");
            }

            Logger.d(TAG, "✅ Customization applied successfully");

        } catch (NullPointerException e) {
            Logger.e(TAG, "❌ NullPointerException in applyCustomization: " + e.getMessage(), e);
        } catch (Exception e) {
            Logger.e(TAG, "❌ Exception in applyCustomization: " + e.getMessage(), e);
        }
    }



    private void populateAdContent() {
        if (currentAdData == null) return;

        // Set title (use customization or AdData)
        String title = customization.titleText != null ? customization.titleText : "Sponsored Ad";
        adTitle.setText(title);
        adTitle.setVisibility(View.VISIBLE);

        // Set body text (use customization or AdData description)
        String bodyText = customization.bodyText != null ?
                customization.bodyText : currentAdData.getDescription();
        if (bodyText != null && !bodyText.isEmpty()) {
            adBody.setText(bodyText);
            adBody.setVisibility(View.VISIBLE);
        } else {
            adBody.setVisibility(View.GONE);
        }

        // Set CTA text (use customization or AdData)
        String ctaText = customization.ctaButtonText != null ?
                customization.ctaButtonText : currentAdData.getCtaText();
        adCTA.setText(ctaText != null ? ctaText : "Learn More");

        // Load media with fallback support
        if (currentAdData.hasVideo()) {
            setupVideoContent();
        } else if (currentAdData.hasImage()) {
            setupImageContent(currentAdData.getImageUrl());
        } else if (customization.fallbackImageUrl != null) {
            setupImageContent(customization.fallbackImageUrl);
        } else {
            // Hide media container if no media available
            adImage.setVisibility(View.GONE);
            adVideo.setVisibility(View.GONE);
        }

        // Hide loading progress once content is ready
        if (loadingProgress != null) {
            loadingProgress.setVisibility(View.GONE);
        }
    }

//    private void setupImageContent(String imageUrl) {
//        adVideo.setVisibility(View.GONE);
//        adImage.setVisibility(View.VISIBLE);
//
//        Glide.with(activity)
//                .load(imageUrl)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .placeholder(R.drawable.ad_placeholder)
//                .error(customization.fallbackImageUrl != null ?
//                        Glide.with(activity).load(customization.fallbackImageUrl) :
//                        R.drawable.ad_error)
//                .into(adImage);
//    }


//    static image remove this  and uncomment upper part
    private void setupImageContent(String imageUrl) {
        adVideo.setVisibility(View.GONE);
        adImage.setVisibility(View.VISIBLE);

        String uriToUse = imageUrl;
        if (STATIC_IMAGE_TESTING) {
            uriToUse = getStaticImageUri();
        }

        Glide.with(activity)
                .load(uriToUse)
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
                if (loadingProgress != null) {
                    loadingProgress.setVisibility(View.GONE);
                }
            });
        } catch (Exception e) {
            Logger.e(TAG, "Failed to setup video", e);
            // Fallback to image
            if (currentAdData.hasImage()) {
                setupImageContent(currentAdData.getImageUrl());
            } else if (customization.fallbackImageUrl != null) {
                setupImageContent(customization.fallbackImageUrl);
            }
        }
    }

    private void setupClickHandlers() {
        // Close button click
        closeButtonText.setOnClickListener(v -> {
            Logger.d(TAG, "Close button clicked");
            dismissAd();
        });

        // CTA button click
        adCTA.setOnClickListener(v -> {
            if (customization.enableCtaPulseAnimation) {
                AnimationUtils.pulseAnimation(v);
            }
            handleAdClick();

            if (customization.dismissOnCtaClick) {
                new Handler(Looper.getMainLooper()).postDelayed(this::dismissAd, 300);
            }
        });

        // Optional: Make entire content clickable
        if (customization.fullScreenClickable && mainContent != null) {
            mainContent.setOnClickListener(v -> handleAdClick());
        }
    }

    private void setupVideoHandlers() {
        if (adVideo != null) {
            adVideo.setOnErrorListener((mp, what, extra) -> {
                Logger.e(TAG, "Video error: " + what + ", " + extra);

                // Fallback to image on video error
                if (currentAdData != null && currentAdData.hasImage()) {
                    setupImageContent(currentAdData.getImageUrl());
                } else if (customization.fallbackImageUrl != null) {
                    setupImageContent(customization.fallbackImageUrl);
                }

                if (loadingProgress != null) {
                    loadingProgress.setVisibility(View.GONE);
                }
                return true;
            });
        }
    }

    private void handleAdClick() {
        if (currentAdData == null) return;

        Logger.d(TAG, "Ad clicked");

        // Track click
        trackClick();

        // Open URL if available
        if (currentAdData.getClickUrl() != null && !currentAdData.getClickUrl().isEmpty()) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentAdData.getClickUrl()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            } catch (Exception e) {
                Logger.e(TAG, "Failed to open click URL", e);
            }
        }

        notifyAdClicked();
    }

    private void dismissAd() {
        if (adDialog != null && adDialog.isShowing()) {
            View rootView = adDialog.findViewById(R.id.interstitial_root);

            if (customization.enableFadeAnimation && rootView != null) {
                AnimationUtils.fadeOut(rootView, () -> {
                    adDialog.dismiss();
                    cleanup();
                });
            } else {
                adDialog.dismiss();
                cleanup();
            }
        }
    }

    private void cleanup() {
        isShowing = false;
        isLoaded = false;

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
            activity.runOnUiThread(() -> adListener.onAdLoaded());
        }
    }

    private void notifyAdFailedToLoad(String error) {
        if (adListener != null) {
            activity.runOnUiThread(() -> adListener.onAdFailedToLoad(error));
        }
    }

    private void notifyAdShown() {
        if (adListener != null) {
            activity.runOnUiThread(() -> adListener.onAdShown());
        }
    }

    private void notifyAdFailedToShow(String error) {
        if (adListener != null) {
            activity.runOnUiThread(() -> adListener.onAdFailedToShow(error));
        }
    }

    private void notifyAdClicked() {
        if (adListener != null) {
            activity.runOnUiThread(() -> adListener.onAdClicked());
        }
    }

    private void notifyAdImpression() {
        if (adListener != null) {
            activity.runOnUiThread(() -> adListener.onAdImpression());
        }
    }

    private void notifyAdDismissed() {
        if (adListener != null) {
            activity.runOnUiThread(() -> adListener.onAdDismissed());
        }
    }
}
