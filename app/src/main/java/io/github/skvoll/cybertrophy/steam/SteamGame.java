package io.github.skvoll.cybertrophy.steam;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Locale;

public final class SteamGame {
    @SerializedName("appid")
    public Long appId;
    @SerializedName("name")
    public String name;
    @SerializedName("playtime_forever")
    public Integer playtimeForever;
    @SerializedName("img_icon_url")
    public String imgIconUrl;
    @SerializedName("img_logo_url")
    public String imgLogoUrl;
    @SerializedName("has_community_visible_stats")
    public Boolean hasCommunityVisibleStats;

    // additional
    private Integer mAchievementsTotalCount = 0;
    private Integer mAchievementsUnlockedCount = 0;
    private HashMap<String, SteamAchievement> mSteamAchievements = new HashMap<>();

    public Integer getAchievementsTotalCount() {
        return mAchievementsTotalCount;
    }

    public Integer getAchievementsUnlockedCount() {
        return mAchievementsUnlockedCount;
    }

    public HashMap<String, SteamAchievement> getSteamAchievements() {
        return mSteamAchievements;
    }

    public void addAchievement(SteamAchievement steamAchievement) {
        mAchievementsTotalCount++;

        if (steamAchievement.isUnlocked()) {
            mAchievementsUnlockedCount++;
        }

        mSteamAchievements.put(steamAchievement.name, steamAchievement);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s: %s(%d)",
                this.getClass().getSimpleName(), name, appId);
    }
}
