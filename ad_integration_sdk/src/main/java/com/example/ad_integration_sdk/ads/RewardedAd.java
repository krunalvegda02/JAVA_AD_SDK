    package com.example.ad_integration_sdk.ads;

    import android.app.Activity;
    import android.app.Dialog;
    import android.content.Intent;
    import android.graphics.Color;
    import android.graphics.drawable.ColorDrawable;
    import android.media.MediaPlayer;
    import android.net.Uri;
    import android.os.Handler;
    import android.os.Looper;
    import android.view.View;
    import android.view.Window;
    import android.view.WindowManager;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
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
     * Professional Google-style Rewarded Ad with clean, solid colors
     * Matches Google AdMob rewarded video experience
     */
    public class RewardedAd {

        private static final boolean STATIC_VIDEO_TESTING = true; // Set false to revert to API video

        private static final String TAG = "RewardedAd";

        // UI Components
        private Dialog adDialog;
        private VideoView adVideo;
        private ImageView adImage;
        private TextView timerText;
        private TextView adLabel;
        private ProgressBar progressBar;
        private View closeButton;
        private View topBar;
        private View bottomContainer;
        private TextView rewardTitleText;
        private TextView rewardAmountText;
        private TextView instructionText;
        private ImageView rewardIcon;
        private View rewardCompletedContainer;
        private TextView rewardCompletedText;
        private TextView collectButtonText;

        // State
        private Activity activity;
        private AdConfig.AdRequest adRequest;
        private AdData currentAdData;
        private AdEventListener.RewardedAdListener adListener;
        private boolean isLoaded = false;
        private boolean isShowing = false;
        private boolean impressionTracked = false;
        private boolean videoStarted = false;
        private boolean videoCompleted = false;
        private boolean rewardEarned = false;

        // Video tracking
        private Handler progressHandler;
        private Runnable progressRunnable;
        private int videoDuration = 0;
        private int videoCurrentPosition = 0;
        private MediaPlayer mediaPlayer;

        // Dynamic customization
        private RewardedAdCustomization customization;

        /**
         * Professional color customization class
         * Uses clean, solid professional colors
         */
        public static class RewardedAdCustomization {
            // Reward text and labels
            public String rewardTitle = "Watch to earn";
            public String instructionWatching = "Watch the full video to earn rewards";
            public String instructionCanClose = "Tap × to claim your reward";
            public String rewardCompletedTitle = "Congratulations!";
            public String collectButtonText = "COLLECT";
            public String adLabelText = "AD";

            // Fallback media
            public String fallbackImageUrl = null;
            public String fallbackVideoUrl = null;

            // Professional colors (clean, solid)
            public int backgroundColor = Color.BLACK; // #000000
            public int topBarBackgroundColor = Color.BLACK; // #000000
            public int bottomBarBackgroundColor = Color.parseColor("#121212"); // Dark grey
            public int timerBadgeBackgroundColor = Color.parseColor("#1F1F1F"); // Slightly lighter
            public int timerTextColor = Color.WHITE; // #FFFFFF
            public int progressBarColor = Color.parseColor("#1E88E5"); // Professional blue
            public int progressBarBackgroundColor = Color.parseColor("#424242"); // Medium grey
            public int rewardCardBackgroundColor = Color.parseColor("#1F1F1F"); // Dark grey
            public int rewardCardBorderColor = Color.parseColor("#37474F"); // Slate grey
            public int rewardIconBackgroundColor = Color.parseColor("#FBC02D"); // Professional amber
            public int rewardTextColor = Color.parseColor("#BDBDBD"); // Light grey text
            public int rewardAmountTextColor = Color.WHITE; // #FFFFFF
            public int instructionTextColor = Color.parseColor("#BDBDBD"); // Light grey
            public int adLabelColor = Color.parseColor("#9E9E9E"); // Medium grey
            public int closeButtonBackgroundColor = Color.WHITE; // #FFFFFF
            public int closeButtonIconColor = Color.BLACK; // #000000
            public int completedCheckIconColor = Color.parseColor("#4CAF50"); // Professional green
            public int completedCardBackgroundColor = Color.WHITE; // #FFFFFF
            public int completedTitleColor = Color.parseColor("#212121"); // Dark grey
            public int completedMessageColor = Color.parseColor("#4CAF50"); // Green
            public int collectButtonBackgroundColor = Color.parseColor("#1E88E5"); // Professional blue
            public int collectButtonTextColor = Color.WHITE; // #FFFFFF

            // Text sizes (in SP)
            public float timerTextSize = 14f;
            public float adLabelTextSize = 10f;
            public float rewardTitleTextSize = 12f;
            public float rewardAmountTextSize = 18f;
            public float instructionTextSize = 12f;
            public float completedTitleTextSize = 24f;
            public float completedMessageTextSize = 18f;
            public float collectButtonTextSize = 16f;

            // Behavior
            public double closeThreshold = 0.8; // Show close button after 80% watched
            public boolean autoGrantRewardOnComplete = true;
            public boolean showRewardDialogOnComplete = true;
            public boolean enableVideoControls = false;
            public boolean keepScreenOn = true;

            // Animation
            public boolean enableFadeAnimation = true;
            public boolean enableBounceAnimation = true;
        }

        /**
         * Create RewardedAd instance
         */
        public RewardedAd(Activity activity) {
            this.activity = activity;
            this.progressHandler = new Handler(Looper.getMainLooper());
            this.customization = new RewardedAdCustomization();
        }

        /**
         * Set custom appearance and behavior
         */
        public void setCustomization(RewardedAdCustomization customization) {
            this.customization = customization != null ? customization : new RewardedAdCustomization();
        }

        /**
         * Set ad listener
         */
        public void setAdListener(AdEventListener.RewardedAdListener listener) {
            this.adListener = listener;
        }

        /**
         * Load rewarded ad
         */
        public void loadAd(String placementId) {
            loadAd(new AdConfig.AdRequest(placementId));
        }

        /**
         * Load ad with request
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
            this.videoCompleted = false;
            this.rewardEarned = false;

            Logger.d(TAG, "Loading rewarded ad for placement: " + request.getPlacementId());

            AdSDK.getInstance().getNetworkClient().loadAd(
                    request.getPlacementId(),
                    Constants.AD_TYPE_REWARDED,
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
         * Show the loaded rewarded ad
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
                Logger.e(TAG, "Failed to show rewarded ad", e);
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
            stopProgressTracking();

            if (adDialog != null && adDialog.isShowing()) {
                adDialog.dismiss();
            }

            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            if (adVideo != null) {
                adVideo.stopPlayback();
            }

            currentAdData = null;
            isLoaded = false;
            isShowing = false;
            videoCompleted = false;
            rewardEarned = false;
        }

        private void handleAdLoaded(AdData adData) {
            if (adData == null || !adData.isValid()) {
                handleAdFailedToLoad("Invalid ad data");
                return;
            }

            if (!adData.hasReward()) {
                Logger.w(TAG, "No reward data, using default values");
            }

            this.currentAdData = adData;
            this.isLoaded = true;

            Logger.d(TAG, "Rewarded ad loaded successfully");
            notifyAdLoaded();
        }

        private void handleAdFailedToLoad(String error) {
            this.isLoaded = false;
            this.currentAdData = null;

            Logger.e(TAG, "Failed to load rewarded ad: " + error);
            notifyAdFailedToLoad(error);
        }

        private void createAndShowDialog() {
            // Create full-screen dialog
            adDialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            adDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            adDialog.setCancelable(false);
            adDialog.setCanceledOnTouchOutside(false);

            Window window = adDialog.getWindow();
            if (window != null) {
                window.setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                );
                window.setBackgroundDrawable(new ColorDrawable(customization.backgroundColor));

                int flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
                if (customization.keepScreenOn) {
                    flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                }
                window.setFlags(flags, flags);
            }

            // Inflate layout
            View dialogView = activity.getLayoutInflater().inflate(
                    R.layout.rewarded_ad_layout, null);
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

            // Show dialog
            adDialog.show();

            if (customization.enableFadeAnimation) {
                AnimationUtils.fadeIn(dialogView);
            }

            // Track impression
            trackImpression();
        }

        private void initializeDialogViews(View dialogView) {
            adVideo = dialogView.findViewById(R.id.ad_video);
            adImage = dialogView.findViewById(R.id.ad_image);
            topBar = dialogView.findViewById(R.id.top_bar);
            timerText = dialogView.findViewById(R.id.timer_text);
            adLabel = dialogView.findViewById(R.id.ad_label);
            closeButton = dialogView.findViewById(R.id.close_button);
            bottomContainer = dialogView.findViewById(R.id.bottom_container);
            progressBar = dialogView.findViewById(R.id.progress_bar);
            rewardTitleText = dialogView.findViewById(R.id.reward_title_text);
            rewardAmountText = dialogView.findViewById(R.id.reward_amount_text);
            instructionText = dialogView.findViewById(R.id.instruction_text);
    //        rewardIcon = dialogView.findViewById(R.id.reward_icon);
            rewardCompletedContainer = dialogView.findViewById(R.id.reward_completed_container);
            rewardCompletedText = dialogView.findViewById(R.id.reward_completed_text);
            collectButtonText = dialogView.findViewById(R.id.collect_button_text);

            // Initially hide elements
            closeButton.setVisibility(View.GONE);
            rewardCompletedContainer.setVisibility(View.GONE);
        }

        /**
         * Apply customization to UI elements with null-safety checks
         */
        private void applyCustomization() {
            if (customization == null) {
                Logger.w(TAG, "Customization is null, using defaults");
                customization = new RewardedAdCustomization();
                return;
            }

            try {
                // === SAFE: Apply top bar background ===
                if (topBar != null) {
                    topBar.setBackgroundColor(customization.topBarBackgroundColor);
                } else {
                    Logger.w(TAG, "topBar is null");
                }

                // === SAFE: Apply bottom container background ===
                if (bottomContainer != null) {
                    bottomContainer.setBackgroundColor(customization.bottomBarBackgroundColor);
                } else {
                    Logger.w(TAG, "bottomContainer is null");
                }

                // === SAFE: Apply ad label styling ===
                if (adLabel != null) {
                    adLabel.setText(customization.adLabelText);
                    adLabel.setTextColor(customization.adLabelColor);
                    adLabel.setTextSize(customization.adLabelTextSize);
                } else {
                    Logger.w(TAG, "adLabel is null");
                }

                // === SAFE: Apply timer text styling ===
                if (timerText != null) {
                    timerText.setTextColor(customization.timerTextColor);
                    timerText.setTextSize(customization.timerTextSize);
                } else {
                    Logger.w(TAG, "timerText is null");
                }

                // === SAFE: Apply reward title styling ===
                if (rewardTitleText != null) {
                    rewardTitleText.setText(customization.rewardTitle);
                    rewardTitleText.setTextColor(customization.rewardTextColor);
                    rewardTitleText.setTextSize(customization.rewardTitleTextSize);
                } else {
                    Logger.w(TAG, "rewardTitleText is null");
                }

                // === SAFE: Apply reward amount text styling ===
                if (rewardAmountText != null) {
                    rewardAmountText.setTextColor(customization.rewardAmountTextColor);
                    rewardAmountText.setTextSize(customization.rewardAmountTextSize);
                } else {
                    Logger.w(TAG, "rewardAmountText is null");
                }

                // === SAFE: Apply instruction text styling ===
                if (instructionText != null) {
                    instructionText.setText(customization.instructionWatching);
                    instructionText.setTextColor(customization.instructionTextColor);
                    instructionText.setTextSize(customization.instructionTextSize);
                } else {
                    Logger.w(TAG, "instructionText is null");
                }

                // === CRITICAL FIX: Safe progress bar color filtering ===
                if (progressBar != null) {
                    // Safely set progress bar color
                    try {
                        android.graphics.drawable.Drawable progressDrawable = progressBar.getProgressDrawable();
                        if (progressDrawable != null) {
                            progressDrawable.setColorFilter(
                                    customization.progressBarColor,
                                    android.graphics.PorterDuff.Mode.SRC_IN
                            );
                            Logger.d(TAG, "✅ Progress bar color set");
                        } else {
                            Logger.w(TAG, "⚠️ progressBar.getProgressDrawable() is null - skipping color");
                        }
                    } catch (Exception e) {
                        Logger.w(TAG, "⚠️ Failed to set progress bar color: " + e.getMessage());
                    }

                    // Safely set progress bar background color
                    try {
                        android.graphics.drawable.Drawable backgroundDrawable = progressBar.getBackground();
                        if (backgroundDrawable != null) {
                            backgroundDrawable.setColorFilter(
                                    customization.progressBarBackgroundColor,
                                    android.graphics.PorterDuff.Mode.SRC_IN
                            );
                            Logger.d(TAG, "✅ Progress background color set");
                        } else {
                            Logger.w(TAG, "⚠️ progressBar.getBackground() is null - skipping color");
                        }
                    } catch (Exception e) {
                        Logger.w(TAG, "⚠️ Failed to set progress background color: " + e.getMessage());
                    }
                } else {
                    Logger.w(TAG, "⚠️ progressBar view is null");
                }

                // === SAFE: Apply reward icon background ===
                if (rewardIcon != null) {
                    rewardIcon.setBackgroundColor(customization.rewardIconBackgroundColor);
                } else {
                    Logger.w(TAG, "rewardIcon is null");
                }

                Logger.d(TAG, "✅✅✅ Customization applied successfully - NO CRASHES");

            } catch (NullPointerException e) {
                Logger.e(TAG, "❌ NullPointerException in applyCustomization: " + e.getMessage(), e);
            } catch (Exception e) {
                Logger.e(TAG, "❌ Exception in applyCustomization: " + e.getMessage(), e);
            }
        }



        private void populateAdContent() {
            if (currentAdData == null) return;

            // Set reward information
            String rewardAmount = currentAdData.getRewardAmount() > 0 ?
                    String.valueOf(currentAdData.getRewardAmount()) : "50";
            String rewardType = currentAdData.getRewardType() != null ?
                    currentAdData.getRewardType() : "coins";

            rewardAmountText.setText(rewardAmount + " " + rewardType);

            String completedMessage = "You earned " + rewardAmount + " " + rewardType + "!";
            rewardCompletedText.setText(completedMessage);

            // Load video or image with fallback
            if (currentAdData.hasVideo()) {
                setupVideoContent(currentAdData.getVideoUrl());
            } else if (currentAdData.hasImage()) {
                setupImageContent(currentAdData.getImageUrl());
            } else if (customization.fallbackVideoUrl != null) {
                setupVideoContent(customization.fallbackVideoUrl);
            } else if (customization.fallbackImageUrl != null) {
                setupImageContent(customization.fallbackImageUrl);
            } else {
                adImage.setVisibility(View.VISIBLE);
                adVideo.setVisibility(View.GONE);
            }
        }

//        private void setupVideoContent(String videoUrl) {
//            adImage.setVisibility(View.GONE);
//            adVideo.setVisibility(View.VISIBLE);
//
//            try {
//                adVideo.setVideoURI(Uri.parse(videoUrl));
//            } catch (Exception e) {
//                Logger.e(TAG, "Failed to setup video", e);
//                if (currentAdData != null && currentAdData.hasImage()) {
//                    setupImageContent(currentAdData.getImageUrl());
//                } else if (customization.fallbackImageUrl != null) {
//                    setupImageContent(customization.fallbackImageUrl);
//                }
//            }
//        }


        // STATIC VIDEO TESTING: Remove this block when integrating real ad assets
        private void setupVideoContent(String videoUrl) {
            adImage.setVisibility(View.GONE);
            adVideo.setVisibility(View.VISIBLE);

            try {
                String uriToUse = videoUrl;
                if (STATIC_VIDEO_TESTING) {
                    uriToUse = "android.resource://" + activity.getPackageName() + "/" + R.raw.sample_reward;
                }
                adVideo.setVideoURI(Uri.parse(uriToUse));

                adVideo.setVideoURI(Uri.parse(uriToUse));
                adVideo.requestFocus();
                adVideo.setMediaController(null);
            } catch (Exception e) {
                Logger.e(TAG, "Failed to setup video", e);
                if (currentAdData != null && currentAdData.hasImage()) {
                    setupImageContent(currentAdData.getImageUrl());
                } else if (customization.fallbackImageUrl != null) {
                    setupImageContent(customization.fallbackImageUrl);
                }
            }
        }

        private void setupImageContent(String imageUrl) {
            adVideo.setVisibility(View.GONE);
            adImage.setVisibility(View.VISIBLE);

            Glide.with(activity)
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ad_placeholder)
                    .error(customization.fallbackImageUrl != null ?
                            Glide.with(activity).load(customization.fallbackImageUrl) :
                            R.drawable.ad_error)
                    .into(adImage);
        }

        private void setupClickHandlers() {
            closeButton.setOnClickListener(v -> {
                if (customization.enableBounceAnimation) {
                    AnimationUtils.pulseAnimation(v);
                }
                handleClose();
            });

            collectButtonText.setOnClickListener(v -> {
                handleCollect();
            });
        }

        private void setupVideoHandlers() {
            adVideo.setOnPreparedListener(mp -> {
                mediaPlayer = mp;
                videoDuration = mp.getDuration();
                progressBar.setMax(100);

                mp.start();
                videoStarted = true;
                startProgressTracking();
                notifyVideoStarted();

                Logger.d(TAG, "Video started, duration: " + videoDuration + "ms");
            });

            adVideo.setOnCompletionListener(mp -> {
                videoCompleted = true;
                onVideoCompleted();
            });

            adVideo.setOnErrorListener((mp, what, extra) -> {
                Logger.e(TAG, "Video error: " + what + ", " + extra);
                if (currentAdData != null && currentAdData.hasImage()) {
                    setupImageContent(currentAdData.getImageUrl());
                } else if (customization.fallbackImageUrl != null) {
                    setupImageContent(customization.fallbackImageUrl);
                }
                return true;
            });
        }

        private void startProgressTracking() {
            progressRunnable = new Runnable() {
                @Override
                public void run() {
                    if (adVideo != null && videoStarted && !videoCompleted) {
                        videoCurrentPosition = adVideo.getCurrentPosition();
                        updateProgress();
                        progressHandler.postDelayed(this, 100);
                    }
                }
            };
            progressHandler.post(progressRunnable);
        }

        private void stopProgressTracking() {
            if (progressHandler != null && progressRunnable != null) {
                progressHandler.removeCallbacks(progressRunnable);
            }
        }

        private void updateProgress() {
            if (videoDuration <= 0) return;

            double progress = (double) videoCurrentPosition / videoDuration;
            int progressPercent = (int) (progress * 100);
            progressBar.setProgress(progressPercent);

            int remainingSeconds = (videoDuration - videoCurrentPosition) / 1000;
            String timerDisplay = remainingSeconds > 0 ? remainingSeconds + "s" : "Done";
            timerText.setText(timerDisplay);

            // Show close button after threshold
            if (progress >= customization.closeThreshold && closeButton.getVisibility() == View.GONE) {
                showCloseButton();
                instructionText.setText(customization.instructionCanClose);
            }
        }

        private void showCloseButton() {
            closeButton.setVisibility(View.VISIBLE);
            if (customization.enableBounceAnimation) {
                AnimationUtils.bounceIn(closeButton);
            }
            Logger.d(TAG, "Close button enabled");
        }

        private void onVideoCompleted() {
            stopProgressTracking();
            videoCompleted = true;
            progressBar.setProgress(100);
            timerText.setText("Done");

            if (customization.autoGrantRewardOnComplete && !rewardEarned) {
                rewardEarned = true;
                trackReward();
                notifyVideoCompleted();
                notifyUserEarnedReward();
            }

            if (customization.showRewardDialogOnComplete) {
                showRewardCompletedScreen();
            } else {
                showCloseButton();
            }

            Logger.d(TAG, "Video completed, reward earned: " + rewardEarned);
        }

        private void showRewardCompletedScreen() {
            if (customization.enableFadeAnimation) {
                AnimationUtils.fadeOut(bottomContainer, () -> {
                    bottomContainer.setVisibility(View.GONE);
                    rewardCompletedContainer.setVisibility(View.VISIBLE);
                    AnimationUtils.fadeIn(rewardCompletedContainer);
                    if (customization.enableBounceAnimation) {
                        AnimationUtils.bounceIn(rewardCompletedContainer);
                    }
                });
            } else {
                bottomContainer.setVisibility(View.GONE);
                rewardCompletedContainer.setVisibility(View.VISIBLE);
            }

            showCloseButton();
        }

        private void handleClose() {
            double progress = videoDuration > 0 ? (double) videoCurrentPosition / videoDuration : 0;

            if (!videoCompleted && progress < customization.closeThreshold) {
                instructionText.setText("Watch " +
                        (int)((customization.closeThreshold - progress) * 100) +
                        "% more to earn reward!");
                if (customization.enableBounceAnimation) {
                    AnimationUtils.pulseAnimation(instructionText);
                }
                return;
            }

            if (!rewardEarned && progress >= customization.closeThreshold) {
                rewardEarned = true;
                trackReward();
                notifyUserEarnedReward();
            }

            dismissAd();
        }

        private void handleCollect() {
            if (rewardEarned) {
                dismissAd();
            }
        }

        private void dismissAd() {
            if (adDialog != null && adDialog.isShowing()) {
                View rootView = adDialog.findViewById(R.id.rewarded_root);

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
            stopProgressTracking();

            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            if (adVideo != null) {
                adVideo.stopPlayback();
            }

            isShowing = false;
            isLoaded = false;
            videoStarted = false;

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

        private void trackReward() {
            if (currentAdData == null) return;

            AdSDK.getInstance().getNetworkClient().trackEvent(
                    currentAdData.getAdId(),
                    Constants.EVENT_REWARD,
                    new NetworkClient.TrackingCallback() {
                        @Override
                        public void onTrackingSuccess() {
                            Logger.d(TAG, "Reward tracked");
                        }

                        @Override
                        public void onTrackingFailed(String error) {
                            Logger.w(TAG, "Failed to track reward: " + error);
                        }
                    }
            );
        }

        // Event notifications
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

        private void notifyAdImpression() {
            if (adListener != null) {
                activity.runOnUiThread(() -> adListener.onAdImpression());
            }
        }

        private void notifyUserEarnedReward() {
            if (adListener != null && currentAdData != null) {
                String rewardType = currentAdData.getRewardType() != null ?
                        currentAdData.getRewardType() : "coins";
                int rewardAmount = currentAdData.getRewardAmount() > 0 ?
                        currentAdData.getRewardAmount() : 50;

                activity.runOnUiThread(() -> adListener.onUserEarnedReward(rewardType, rewardAmount));
            }
        }

        private void notifyAdDismissed() {
            if (adListener != null) {
                activity.runOnUiThread(() -> adListener.onAdDismissed());
            }
        }

        private void notifyVideoStarted() {
            if (adListener != null) {
                activity.runOnUiThread(() -> adListener.onVideoStarted());
            }
        }

        private void notifyVideoCompleted() {
            if (adListener != null) {
                activity.runOnUiThread(() -> adListener.onVideoCompleted());
            }
        }
    }
