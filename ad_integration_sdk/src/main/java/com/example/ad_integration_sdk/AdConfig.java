package com.example.ad_integration_sdk;

public class AdConfig {

    // Ad request configuration
    public static class AdRequest {
        private String placementId;
        private AdSize adSize;
        private String keywords;
        private String userId;
        private String contentUrl;

        public AdRequest(String placementId) {
            this.placementId = placementId;
        }

        public AdRequest setAdSize(AdSize adSize) {
            this.adSize = adSize;
            return this;
        }

        public AdRequest setKeywords(String keywords) {
            this.keywords = keywords;
            return this;
        }

        public AdRequest setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public AdRequest setContentUrl(String contentUrl) {
            this.contentUrl = contentUrl;
            return this;
        }

        // Getters
        public String getPlacementId() { return placementId; }
        public AdSize getAdSize() { return adSize; }
        public String getKeywords() { return keywords; }
        public String getUserId() { return userId; }
        public String getContentUrl() { return contentUrl; }
    }

    // Ad sizes
    public enum AdSize {
        BANNER_320x50(320, 50),
        BANNER_300x250(300, 250),
        BANNER_728x90(728, 90),
        INTERSTITIAL_FULLSCREEN(-1, -1),
        REWARDED_FULLSCREEN(-1, -1);

        private final int width;
        private final int height;

        AdSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public boolean isFullscreen() { return width == -1 && height == -1; }
    }
}
