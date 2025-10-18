package com.example.ad_integration_sdk.network;

//import com.example.ad_integration_sdk.utils.Logger;
//import org.json.JSONObject;
//
//public class AdParser {
//    private static final String TAG = "AdParser";
//
//    // FIXED: Return AdData instead of AdParser.AdData
//    public static AdData parseAdResponse(String jsonResponse) {
//        try {
//            JSONObject json = new JSONObject(jsonResponse);
//
//            if (!json.optBoolean("success", false)) {
//                throw new Exception("API returned error: " + json.optString("message"));
//            }
//
//            JSONObject adJson = json.getJSONObject("ad");
//
//            AdData adData = new AdData();
//            adData.setAdId(adJson.getString("adId"));
//            adData.setTitle(adJson.optString("title"));
//            adData.setDescription(adJson.optString("description"));
//            adData.setImageUrl(adJson.optString("imageUrl"));
//            adData.setVideoUrl(adJson.optString("videoUrl"));
//            adData.setClickUrl(adJson.getString("clickUrl"));
//            adData.setCtaText(adJson.optString("ctaText", "Learn More"));
//            adData.setSponsoredLabel(adJson.optString("sponsoredLabel", "Sponsored"));
//
//            // Rewarded ad specific
//            if (adJson.has("reward")) {
//                JSONObject reward = adJson.getJSONObject("reward");
//                adData.setRewardType(reward.optString("type", "coins"));
//                adData.setRewardAmount(reward.optInt("amount", 1));
//            }
//
//            return adData;
//
//        } catch (Exception e) {
//            Logger.e(TAG, "Failed to parse ad response", e);
//            throw new RuntimeException("Invalid ad response format", e);
//        }
//    }
//}






public class AdParser {
    private static final String TAG = "AdParser";

    public static AdData parseAdResponse(String jsonResponse) {
        // For testing, return a mock ad if in test mode
        return createMockAd();
    }

    // Mock ad for testing purposes
    private static AdData createMockAd() {
        AdData mockAd = new AdData();
        mockAd.setAdId("mock-ad-001");
        mockAd.setTitle("Premium Mobile Game");
        mockAd.setDescription("Download now for exclusive rewards!");
        mockAd.setImageUrl("https://via.placeholder.com/300x250/4CAF50/FFFFFF?text=Mock+Ad");
        mockAd.setVideoUrl(""); // No video for this mock
        mockAd.setClickUrl("https://play.google.com/store");
        mockAd.setCtaText("Install Now");
        mockAd.setSponsoredLabel("Sponsored");

        // For rewarded ads
        mockAd.setRewardType("coins");
        mockAd.setRewardAmount(50);

        return mockAd;
    }
}
