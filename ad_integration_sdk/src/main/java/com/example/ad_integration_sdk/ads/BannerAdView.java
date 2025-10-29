package com.example.ad_integration_sdk.ads;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ad_integration_sdk.AdEventListener;
import com.example.ad_integration_sdk.AdSDK;
import com.example.ad_integration_sdk.R;
import com.example.ad_integration_sdk.network.AdData;
import com.example.ad_integration_sdk.network.NetworkClient;
import com.example.ad_integration_sdk.utils.AdSize;
import com.example.ad_integration_sdk.utils.Logger;

/**
 * BannerAdView — Handles displaying, tracking, and user interactions for banner ads.
 * Supports only predefined AdSizes from AdSize.java
 */
public class BannerAdView extends FrameLayout {

    private static final String TAG = "BannerAdView";

    // UI components
    private FrameLayout loadingContainer;
    private FrameLayout errorContainer;
    private FrameLayout contentContainer;
    private ImageView adImage;
    private TextView adLabel;

    // Ad state
    private AdData currentAdData;
    private AdEventListener.BannerAdListener adListener;
    private boolean isLoaded = false;
    private boolean impressionTracked = false;

    // Fixed ad size
    private AdSize adSize = AdSize.BANNER; // Default

    public BannerAdView(Context context) {
        super(context);
        init();
    }

    public BannerAdView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BannerAdView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        try {
            LayoutInflater.from(getContext()).inflate(R.layout.banner_ad_layout, this, true);

            loadingContainer = findViewById(R.id.loading_container);
            errorContainer = findViewById(R.id.error_container);
            contentContainer = findViewById(R.id.content_container);
            adImage = findViewById(R.id.ad_image);
            adLabel = findViewById(R.id.ad_label);

            if (loadingContainer == null || errorContainer == null || contentContainer == null ||
                    adImage == null || adLabel == null) {
                Logger.e(TAG, "Missing required views in banner_ad_layout");
                throw new RuntimeException("Banner layout inflation failed - missing required views");
            }

            setupClickHandling();
            showLoading();

        } catch (Exception e) {
            Logger.e(TAG, "Failed to initialize BannerAdView", e);
            throw new RuntimeException("BannerAdView initialization failed", e);
        }
    }

    /**
     * Sets the predefined ad size for this banner.
     * Only AdSize constants are allowed.
     */
    public void setAdSize(AdSize size) {
        if (size == null) {
            Logger.w(TAG, "Null AdSize passed, using default BANNER");
            size = AdSize.BANNER;
        }
        this.adSize = size;

        // Apply layout params dynamically
        int widthPx = size.getWidthInPixels(getContext());
        int heightPx = size.getHeightInPixels(getContext());

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(widthPx, heightPx);
        setLayoutParams(params);

        Logger.d(TAG, "BannerAdView size set to: " + size.toString());
    }

    public AdSize getAdSize() {
        return adSize;
    }

    public void setAdListener(AdEventListener.BannerAdListener listener) {
        this.adListener = listener;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void destroy() {
        currentAdData = null;
        isLoaded = false;
        impressionTracked = false;
    }

    /**
     * Loads an ad using a placement ID.
     */
    public void loadAd(String placementId) {
        if (!AdSDK.isInitialized()) {
            Logger.e(TAG, "Ad SDK not initialized");
            showError();
            notifyAdFailedToLoad("SDK not initialized");
            return;
        }

        resetState();
        showLoading();

        AdSDK.getInstance().getNetworkClient().loadAd(placementId, "banner", new NetworkClient.AdLoadCallback() {
            @Override
            public void onAdLoaded(AdData adData) {
                if (adData == null || !adData.isValid() || !adData.hasImage()) {
                    Logger.w(TAG, "Invalid ad data received");
                    showError();
                    notifyAdFailedToLoad("Invalid ad data");
                    return;
                }

                currentAdData = adData;
                isLoaded = true;
                impressionTracked = false;

                populateAdContent();
                showContent();
                trackImpression();
                notifyAdLoaded();
            }

            @Override
            public void onAdFailedToLoad(String error) {
                Logger.e(TAG, "Ad failed to load: " + error);
                showError();
                notifyAdFailedToLoad(error);
            }
        });
    }

    private void resetState() {
        currentAdData = null;
        isLoaded = false;
        impressionTracked = false;
    }

    private void populateAdContent() {
        if (currentAdData == null || !currentAdData.hasImage()) {
            Logger.w(TAG, "No valid ad image to load");
            return;
        }

        Glide.with(getContext())
                .load(currentAdData.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ad_placeholder)
                .error(R.drawable.ad_error)
                .into(adImage);

        adLabel.setText("Ad");
    }

    private void setupClickHandling() {
        OnClickListener clickListener = v -> handleAdClick();

        this.setOnClickListener(clickListener);
        if (contentContainer != null) contentContainer.setOnClickListener(clickListener);
        if (adImage != null) adImage.setOnClickListener(clickListener);
    }

    private void handleAdClick() {
        if (currentAdData == null || currentAdData.getClickUrl() == null) return;

        trackClick();
        notifyAdClicked();

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentAdData.getClickUrl()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            notifyAdOpened();
        } catch (Exception e) {
            Logger.e(TAG, "Failed to open ad URL", e);
        }
    }

    // UI helpers
    private void showLoading() {
        if (loadingContainer != null) loadingContainer.setVisibility(VISIBLE);
        if (errorContainer != null) errorContainer.setVisibility(GONE);
        if (contentContainer != null) contentContainer.setVisibility(GONE);
    }

    private void showError() {
        if (loadingContainer != null) loadingContainer.setVisibility(GONE);
        if (errorContainer != null) errorContainer.setVisibility(VISIBLE);
        if (contentContainer != null) contentContainer.setVisibility(GONE);
    }

    private void showContent() {
        if (loadingContainer != null) loadingContainer.setVisibility(GONE);
        if (errorContainer != null) errorContainer.setVisibility(GONE);
        if (contentContainer != null) contentContainer.setVisibility(VISIBLE);
    }

    private void trackImpression() {
        if (impressionTracked || currentAdData == null) return;

        impressionTracked = true;

        AdSDK.getInstance().getNetworkClient().trackEvent(currentAdData.getAdId(), "impression",
                new NetworkClient.TrackingCallback() {
                    @Override
                    public void onTrackingSuccess() {
                        Logger.d(TAG, "Impression tracked");
                        notifyAdImpression();
                    }

                    @Override
                    public void onTrackingFailed(String error) {
                        Logger.w(TAG, "Impression tracking failed: " + error);
                    }
                });
    }

    private void trackClick() {
        if (currentAdData == null) return;

        AdSDK.getInstance().getNetworkClient().trackEvent(currentAdData.getAdId(), "click",
                new NetworkClient.TrackingCallback() {
                    @Override
                    public void onTrackingSuccess() {
                        Logger.d(TAG, "Click tracked");
                    }

                    @Override
                    public void onTrackingFailed(String error) {
                        Logger.w(TAG, "Click tracking failed: " + error);
                    }
                });
    }

    // Ad Event Callbacks
    private void notifyAdLoaded() {
        if (adListener != null) adListener.onAdLoaded();
    }

    private void notifyAdFailedToLoad(String error) {
        if (adListener != null) adListener.onAdFailedToLoad(error);
    }

    private void notifyAdClicked() {
        if (adListener != null) adListener.onAdClicked();
    }

    private void notifyAdImpression() {
        if (adListener != null) adListener.onAdImpression();
    }

    private void notifyAdOpened() {
        if (adListener != null) adListener.onAdOpened();
    }
}
