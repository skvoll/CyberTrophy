package io.github.skvoll.cybertrophy.steam;

import com.google.gson.annotations.SerializedName;

public final class SteamPlayerAchievement {
    @SerializedName("apiname")
    public String apiName;
    @SerializedName("achieved")
    public Integer achieved;
    @SerializedName("unlocktime")
    public Integer unlockTime;
    @SerializedName("name")
    public String name;
    @SerializedName("description")
    public String description;
}
