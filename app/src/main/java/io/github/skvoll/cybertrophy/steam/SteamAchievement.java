package io.github.skvoll.cybertrophy.steam;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.Locale;

public final class SteamAchievement {
    @SerializedName("name")
    public String name;
    @SerializedName("defaultvalue")
    public Integer defaultValue;
    @SerializedName("displayName")
    public String displayName;
    @SerializedName("hidden")
    public Integer hidden;
    @SerializedName("description")
    public String description;
    @SerializedName("icon")
    public String icon;
    @SerializedName("icongray")
    public String iconGray;

    // additional
    private BigDecimal mPercent = new BigDecimal(0);
    private Boolean mIsUnlocked = false;
    private Integer mUnlockTime;

    public BigDecimal getPercent() {
        return mPercent;
    }

    public void setPercent(BigDecimal percent) {
        if (percent.compareTo(new BigDecimal("0")) > 0 && percent.compareTo(new BigDecimal("0.1")) < 0) {
            percent = new BigDecimal("0.1");
        }

        mPercent = percent.setScale(1, BigDecimal.ROUND_DOWN);
    }

    public Boolean isUnlocked() {
        return mIsUnlocked;
    }

    public void setUnlocked(Boolean isUnlocked) {
        mIsUnlocked = isUnlocked;
    }

    public Integer getUnlockTime() {
        return mUnlockTime;
    }

    public void setUnlockTime(Integer unlockTime) {
        mUnlockTime = unlockTime;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s: %s(%s)",
                this.getClass().getSimpleName(), displayName, name);
    }
}
