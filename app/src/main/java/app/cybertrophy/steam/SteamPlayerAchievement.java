package app.cybertrophy.steam;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;

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

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s: %s(%s)",
                this.getClass().getSimpleName(), name, apiName);
    }
}
