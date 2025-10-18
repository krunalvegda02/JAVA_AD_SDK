package com.example.ad_integration_sdk;

public class AdEventListener {

    /**
     * Banner ad event listener
     */
    public interface BannerAdListener {
        /**
         * Called when banner ad is loaded successfully
         */
        void onAdLoaded();

        /**
         * Called when banner ad fails to load
         * @param error Error message
         */
        void onAdFailedToLoad(String error);

        /**
         * Called when banner ad is clicked
         */
        void onAdClicked();

        /**
         * Called when banner ad impression is recorded
         */
        void onAdImpression();

        /**
         * Called when banner ad is opened (user leaves app)
         */
        void onAdOpened();

        /**
         * Called when banner ad is closed (user returns to app)
         */
        void onAdClosed();
    }

    /**
     * Interstitial ad event listener
     */
    public interface InterstitialAdListener {
        /**
         * Called when interstitial ad is loaded successfully
         */
        void onAdLoaded();

        /**
         * Called when interstitial ad fails to load
         * @param error Error message
         */
        void onAdFailedToLoad(String error);

        /**
         * Called when interstitial ad is shown to user
         */
        void onAdShown();

        /**
         * Called when interstitial ad fails to show
         * @param error Error message
         */
        void onAdFailedToShow(String error);

        /**
         * Called when interstitial ad is clicked
         */
        void onAdClicked();

        /**
         * Called when interstitial ad impression is recorded
         */
        void onAdImpression();

        /**
         * Called when interstitial ad is dismissed
         */
        void onAdDismissed();
    }

    /**
     * Rewarded ad event listener
     */
    public interface RewardedAdListener {
        /**
         * Called when rewarded ad is loaded successfully
         */
        void onAdLoaded();

        /**
         * Called when rewarded ad fails to load
         * @param error Error message
         */
        void onAdFailedToLoad(String error);

        /**
         * Called when rewarded ad is shown to user
         */
        void onAdShown();

        /**
         * Called when rewarded ad fails to show
         * @param error Error message
         */
        void onAdFailedToShow(String error);

        /**
         * Called when rewarded ad is clicked
         */
        void onAdClicked();

        /**
         * Called when rewarded ad impression is recorded
         */
        void onAdImpression();

        /**
         * Called when user earns reward
         * @param rewardType Type of reward (coins, gems, etc.)
         * @param rewardAmount Amount of reward
         */
        void onUserEarnedReward(String rewardType, int rewardAmount);

        /**
         * Called when rewarded ad is dismissed
         */
        void onAdDismissed();

        /**
         * Called when rewarded video starts playing
         */
        void onVideoStarted();

        /**
         * Called when rewarded video completes
         */
        void onVideoCompleted();
    }
}
