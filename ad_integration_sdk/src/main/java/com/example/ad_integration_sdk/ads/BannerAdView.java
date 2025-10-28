package com.example.ad_integration_sdk.ads;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
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
import com.example.ad_integration_sdk.utils.Logger;

public class BannerAdView extends FrameLayout {
    private static final String TAG = "BannerAdView";

    // UI Components
    private FrameLayout loadingContainer;
    private FrameLayout errorContainer;
    private FrameLayout contentContainer;
    private ImageView adImage;
    private TextView adLabel;

    // State
    private AdData currentAdData;
    private AdEventListener.BannerAdListener adListener;
    private boolean isLoaded;
    private boolean impressionTracked;

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

            // Find views
            loadingContainer = findViewById(R.id.loading_container);
            errorContainer = findViewById(R.id.error_container);
            contentContainer = findViewById(R.id.content_container);
            adImage = findViewById(R.id.ad_image);
            adLabel = findViewById(R.id.ad_label);

            // Verify all views are found
            if (loadingContainer == null || errorContainer == null ||
                    contentContainer == null || adImage == null || adLabel == null) {
                Logger.e(TAG, "Failed to find required views in banner_ad_layout");
                throw new RuntimeException("Banner layout inflation failed - missing required views");
            }

            // Setup click handling
            setupClickHandling();

            // Start in loading state
            showLoading();

        } catch (Exception e) {
            Logger.e(TAG, "Failed to initialize BannerAdView", e);
            throw new RuntimeException("BannerAdView initialization failed", e);
        }
    }


    public void setAdListener(AdEventListener.BannerAdListener listener) {
        this.adListener = listener;
    }

    public void loadAd(String placementId) {
        if (!AdSDK.isInitialized()) {
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
                showError();
                notifyAdFailedToLoad(error);
            }
        });
    }

    public void setBannerSize(int widthDp, int heightDp) {
        int widthPx = (int) (widthDp * getResources().getDisplayMetrics().density);
        int heightPx = (int) (heightDp * getResources().getDisplayMetrics().density);

        if (getLayoutParams() != null) {
            getLayoutParams().width = widthPx;
            getLayoutParams().height = heightPx;
            requestLayout();
        } else {
            setLayoutParams(new FrameLayout.LayoutParams(widthPx, heightPx));
        }
    }

    public void destroy() {
        currentAdData = null;
        isLoaded = false;
        impressionTracked = false;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    private void resetState() {
        currentAdData = null;
        isLoaded = false;
        impressionTracked = false;
    }

    private void populateAdContent() {
        if (currentAdData == null || !currentAdData.hasImage()) return;

        // Load ad image
        Glide.with(getContext())
                .load(currentAdData.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(adImage);

        // Set "Ad" label (always shows "Ad")
        adLabel.setText("Ad");
    }

    private void setupClickHandling() {
        OnClickListener clickListener = v -> handleAdClick();

        setOnClickListener(clickListener);
        contentContainer.setOnClickListener(clickListener);
        adImage.setOnClickListener(clickListener);
    }

    private void handleAdClick() {
        if (currentAdData == null) return;

        trackClick();
        notifyAdClicked();

        String clickUrl = currentAdData.getClickUrl();
        if (clickUrl != null && !clickUrl.isEmpty()) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(clickUrl));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
                notifyAdOpened();
            } catch (Exception e) {
                Logger.e(TAG, "Failed to open URL", e);
            }
        }
    }

    private void showLoading() {
        loadingContainer.setVisibility(VISIBLE);
        errorContainer.setVisibility(GONE);
        contentContainer.setVisibility(GONE);
    }

    private void showError() {
        loadingContainer.setVisibility(GONE);
        errorContainer.setVisibility(VISIBLE);
        contentContainer.setVisibility(GONE);
    }

    private void showContent() {
        loadingContainer.setVisibility(GONE);
        errorContainer.setVisibility(GONE);
        contentContainer.setVisibility(VISIBLE);
    }

    private void trackImpression() {
        if (impressionTracked || currentAdData == null) return;

        impressionTracked = true;
        AdSDK.getInstance().getNetworkClient().trackEvent(currentAdData.getAdId(), "impression", new NetworkClient.TrackingCallback() {
            @Override
            public void onTrackingSuccess() {
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

        AdSDK.getInstance().getNetworkClient().trackEvent(currentAdData.getAdId(), "click", new NetworkClient.TrackingCallback() {
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

    // Event notifications
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