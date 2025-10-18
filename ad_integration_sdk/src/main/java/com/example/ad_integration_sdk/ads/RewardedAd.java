package com.example.ad_integration_sdk.ads;
import com.example.ad_integration_sdk.network.AdData;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.ad_integration_sdk.AdConfig;
import com.example.ad_integration_sdk.AdEventListener;
import com.example.ad_integration_sdk.AdSDK;
import com.example.ad_integration_sdk.R;
import com.example.ad_integration_sdk.network.AdParser;
import com.example.ad_integration_sdk.network.NetworkClient;
import com.example.ad_integration_sdk.ui.AnimationUtils;
import com.example.ad_integration_sdk.utils.Constants;
import com.example.ad_integration_sdk.utils.Logger;

public class RewardedAd {
    private static final String TAG = "RewardedAd";

    // UI Components
    private Dialog adDialog;
    private VideoView adVideo;
    private ImageView adImage;
    private TextView adTitle;
    private TextView videoProgressText;
    private ProgressBar videoProgressBar;
    private ImageButton closeButton;
    private TextView rewardText;
    private TextView skipWarningText;
    private View rewardContainer;
    private View contentContainer;

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

    /**
     * Create RewardedAd instance
     */
    public RewardedAd(Activity activity) {
        this.activity = activity;
        this.progressHandler = new Handler(Looper.getMainLooper());
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
            handleAdFailedToLoad("No reward data");
            return;
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
            window.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }

        // Inflate layout
        View dialogView = activity.getLayoutInflater().inflate(
                R.layout.rewarded_ad_layout, null);
        adDialog.setContentView(dialogView);

        // Initialize views
        initializeDialogViews(dialogView);

        // Populate content
        populateAdContent();

        // Setup handlers
        setupClickHandlers();
        setupVideoHandlers();

        // Show dialog
        adDialog.show();
        AnimationUtils.fadeIn(dialogView);

        // Track impression
        trackImpression();
    }

    private void initializeDialogViews(View dialogView) {
        adVideo = dialogView.findViewById(R.id.ad_video);
        adImage = dialogView.findViewById(R.id.ad_image);
        adTitle = dialogView.findViewById(R.id.ad_title);
        videoProgressText = dialogView.findViewById(R.id.video_progress_text);
        videoProgressBar = dialogView.findViewById(R.id.video_progress_bar);
        closeButton = dialogView.findViewById(R.id.close_button);
        rewardText = dialogView.findViewById(R.id.reward_text);
        skipWarningText = dialogView.findViewById(R.id.skip_warning_text);
        rewardContainer = dialogView.findViewById(R.id.reward_container);
        contentContainer = dialogView.findViewById(R.id.content_container);

        // Initially hide close button and reward
        closeButton.setVisibility(View.GONE);
        rewardContainer.setVisibility(View.GONE);
    }

    private void populateAdContent() {
        if (currentAdData == null) return;

        // Set title
        if (currentAdData.getTitle() != null) {
            adTitle.setText(currentAdData.getTitle());
        }

        // Set reward text
        String rewardDisplayText = "Watch to earn " +
                currentAdData.getRewardAmount() + " " +
                currentAdData.getRewardType();
        rewardText.setText(rewardDisplayText);

        // Load video or image
        if (currentAdData.hasVideo()) {
            setupVideoContent();
        } else if (currentAdData.hasImage()) {
            setupImageContent();
        }
    }

    private void setupVideoContent() {
        adImage.setVisibility(View.GONE);
        adVideo.setVisibility(View.VISIBLE);

        try {
            adVideo.setVideoURI(Uri.parse(currentAdData.getVideoUrl()));

        } catch (Exception e) {
            Logger.e(TAG, "Failed to setup video", e);
            if (currentAdData.hasImage()) {
                setupImageContent();
            }
        }
    }

    private void setupImageContent() {
        adVideo.setVisibility(View.GONE);
        adImage.setVisibility(View.VISIBLE);

        Glide.with(activity)
                .load(currentAdData.getImageUrl())
                .into(adImage);
    }

    private void setupClickHandlers() {
        closeButton.setOnClickListener(v -> {
            AnimationUtils.pulseAnimation(v);
            handleClose();
        });

        contentContainer.setOnClickListener(v -> {
            if (videoCompleted && rewardEarned) {
                handleAdClick();
            }
        });
    }

    private void setupVideoHandlers() {
        adVideo.setOnPreparedListener(mp -> {
            mediaPlayer = mp;
            videoDuration = mp.getDuration();
            videoProgressBar.setMax(videoDuration);

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
                setupImageContent();
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
                    progressHandler.postDelayed(this, 500);
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

        videoProgressBar.setProgress(videoCurrentPosition);

        int remainingSeconds = (videoDuration - videoCurrentPosition) / 1000;
        videoProgressText.setText(remainingSeconds + "s remaining");

        // Show close button after minimum view duration
        if (videoCurrentPosition >= Constants.MIN_VIDEO_DURATION_MS && !videoCompleted) {
            showCloseButton();
        }
    }

    private void showCloseButton() {
        if (closeButton.getVisibility() == View.GONE) {
            closeButton.setVisibility(View.VISIBLE);
            AnimationUtils.bounceIn(closeButton);

            skipWarningText.setText("You can close now");
            skipWarningText.setTextColor(activity.getResources().getColor(R.color.ad_success_color));
        }
    }

    private void onVideoCompleted() {
        stopProgressTracking();

        videoProgressText.setText("Video completed!");
        videoProgressBar.setProgress(videoDuration);

        // Grant reward
        rewardEarned = true;
        trackReward();
        showRewardAnimation();
        notifyVideoCompleted();
        notifyUserEarnedReward();

        Logger.d(TAG, "Video completed, reward earned");
    }

    private void showRewardAnimation() {
        // Hide video content
        AnimationUtils.fadeOut(contentContainer, () -> {
            contentContainer.setVisibility(View.GONE);

            // Show reward screen
            rewardContainer.setVisibility(View.VISIBLE);
            AnimationUtils.bounceIn(rewardContainer);

            // Update reward display
            TextView rewardEarnedText = rewardContainer.findViewById(R.id.reward_earned_text);
            rewardEarnedText.setText("You earned " +
                    currentAdData.getRewardAmount() + " " +
                    currentAdData.getRewardType() + "!");

            // Show close button
            showCloseButton();
        });
    }

    private void handleAdClick() {
        if (currentAdData == null) return;

        trackClick();

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentAdData.getClickUrl()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            Logger.e(TAG, "Failed to open URL", e);
        }

        notifyAdClicked();
    }

    private void handleClose() {
        if (!videoCompleted && !rewardEarned) {
            skipWarningText.setText("Watch full video to earn reward!");
            skipWarningText.setTextColor(activity.getResources().getColor(R.color.ad_warning_color));
            AnimationUtils.pulseAnimation(skipWarningText);
            return;
        }

        dismissAd();
    }

    private void dismissAd() {
        if (adDialog != null && adDialog.isShowing()) {
            AnimationUtils.fadeOut(adDialog.findViewById(R.id.rewarded_root), () -> {
                adDialog.dismiss();
                cleanup();
            });
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
        if (adListener != null) adListener.onAdLoaded();
    }

    private void notifyAdFailedToLoad(String error) {
        if (adListener != null) adListener.onAdFailedToLoad(error);
    }

    private void notifyAdShown() {
        if (adListener != null) adListener.onAdShown();
    }

    private void notifyAdFailedToShow(String error) {
        if (adListener != null) adListener.onAdFailedToShow(error);
    }

    private void notifyAdClicked() {
        if (adListener != null) adListener.onAdClicked();
    }

    private void notifyAdImpression() {
        if (adListener != null) adListener.onAdImpression();
    }

    private void notifyUserEarnedReward() {
        if (adListener != null && currentAdData != null) {
            adListener.onUserEarnedReward(
                    currentAdData.getRewardType(),
                    currentAdData.getRewardAmount()
            );
        }
    }

    private void notifyAdDismissed() {
        if (adListener != null) adListener.onAdDismissed();
    }

    private void notifyVideoStarted() {
        if (adListener != null) adListener.onVideoStarted();
    }

    private void notifyVideoCompleted() {
        if (adListener != null) adListener.onVideoCompleted();
    }
}
