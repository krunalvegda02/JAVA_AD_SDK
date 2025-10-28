package com.example.ad_integration_sdk.network;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Model class representing an advertisement
 * Contains all the data needed to display an ad
 */
public class AdData {
    private String adId;
    private String title;
    private String description;
    private String imageUrl;
    private String videoUrl;
    private String clickUrl;
    private String ctaText;
    private String sponsoredLabel;
    private String rewardType;
    private int rewardAmount;
    private long timestamp;

    // Constructors
    public AdData() {
        this.timestamp = System.currentTimeMillis();
    }

    public AdData(String adId, String clickUrl) {
        this.adId = adId;
        this.clickUrl = clickUrl;
        this.timestamp = System.currentTimeMillis();
    }

    public Intent getClickIntent(Context context) {
        if (clickUrl == null || clickUrl.trim().isEmpty()) {
            return null;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(clickUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }


    // Validation methods
    public boolean isValid() {
        return adId != null && !adId.trim().isEmpty() &&
                clickUrl != null && !clickUrl.trim().isEmpty() &&
                (hasImage() || hasVideo());
    }

    public boolean hasImage() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }

    public boolean hasVideo() {
        return videoUrl != null && !videoUrl.trim().isEmpty();
    }

    public boolean hasReward() {
        return rewardType != null && !rewardType.trim().isEmpty() && rewardAmount > 0;
    }

    public boolean hasTitle() {
        return title != null && !title.trim().isEmpty();
    }

    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }

    public boolean isExpired(long maxAgeMs) {
        return (System.currentTimeMillis() - timestamp) > maxAgeMs;
    }

    // Getters and Setters
    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getClickUrl() {
        return clickUrl;
    }

    public void setClickUrl(String clickUrl) {
        this.clickUrl = clickUrl;
    }

    public String getCtaText() {
        return ctaText != null ? ctaText : "Learn More";
    }

    public void setCtaText(String ctaText) {
        this.ctaText = ctaText;
    }

    public String getSponsoredLabel() {
        return sponsoredLabel != null ? sponsoredLabel : "Sponsored";
    }

    public void setSponsoredLabel(String sponsoredLabel) {
        this.sponsoredLabel = sponsoredLabel;
    }

    public String getRewardType() {
        return rewardType;
    }

    public void setRewardType(String rewardType) {
        this.rewardType = rewardType;
    }

    public int getRewardAmount() {
        return rewardAmount;
    }

    public void setRewardAmount(int rewardAmount) {
        this.rewardAmount = rewardAmount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "AdData{" +
                "adId='" + adId + '\'' +
                ", title='" + title + '\'' +
                ", hasImage=" + hasImage() +
                ", hasVideo=" + hasVideo() +
                ", hasReward=" + hasReward() +
                '}';
    }
}
