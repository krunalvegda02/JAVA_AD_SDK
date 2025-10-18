package com.example.ad_integration_sdk.network;

public class MockAdFactory {
    public static AdData create(String adType, String placementId) {
        AdData ad = new AdData();

        // Use adId same as placementId + adType for uniqueness
        ad.setAdId(placementId + "-" + adType);
        ad.setClickUrl("https://example.com/click");

        switch (adType.toLowerCase()) {
            case "banner":
                ad.setTitle("Mock Banner Ad");
                ad.setDescription("This is a static banner ad.");
                ad.setCtaText("Shop Now");
                ad.setImageUrl("https://via.placeholder.com/320x50?text=Banner+Ad");
                ad.setSponsoredLabel("Sponsored");
                break;

            case "interstitial":
                ad.setTitle("Mock Interstitial Ad");
                ad.setDescription("This is a static fullscreen interstitial ad.");
                ad.setCtaText("Learn More");
                ad.setImageUrl("https://via.placeholder.com/800x600?text=Interstitial+Ad");
                ad.setSponsoredLabel("Sponsored");
                break;

            case "rewarded":
                ad.setTitle("Mock Rewarded Ad");
                ad.setDescription("Watch the video to earn rewards");
                ad.setCtaText("Claim Reward");
                ad.setVideoUrl("https://example.com/mock_video.mp4");
                ad.setRewardType("coins");
                ad.setRewardAmount(100);
                ad.setSponsoredLabel("Sponsored");
                break;

            default:
                ad.setTitle("Unknown Ad Type");
                ad.setDescription("No ad data available");
                ad.setCtaText("OK");
                ad.setImageUrl("https://via.placeholder.com/320x50?text=Ad");
                ad.setSponsoredLabel("Sponsored");
                break;
        }

        return ad;
    }
}
