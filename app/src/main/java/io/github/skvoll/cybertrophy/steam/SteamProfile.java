package io.github.skvoll.cybertrophy.steam;

import com.google.gson.annotations.SerializedName;

public final class SteamProfile {
    @SerializedName("steamid")
    public Long steamId;
    @SerializedName("communityvisibilitystate")
    public Integer communityVisibilityState;
    @SerializedName("profilestate")
    public Integer profileState;
    @SerializedName("personaname")
    public String personaName;
    @SerializedName("lastlogoff")
    public Integer lastLogoff;
    @SerializedName("commentpermission")
    public Integer commentPermission;
    @SerializedName("profileurl")
    public String profileUrl;
    @SerializedName("avatar")
    public String avatar;
    @SerializedName("avatarmedium")
    public String avatarMedium;
    @SerializedName("avatarfull")
    public String avatarFull;
    @SerializedName("personastate")
    public Integer personaState;
    @SerializedName("realname")
    public String realName;
    @SerializedName("primaryclanid")
    public Long primaryClanId;
    @SerializedName("timecreated")
    public Integer timeCreated;
    @SerializedName("personastateflags")
    public Integer personaStateFlags;
    @SerializedName("loccountrycode")
    public String loccountrycode;
    @SerializedName("locstatecode")
    public String locstatecode;
    @SerializedName("loccityid")
    public Integer loccityid;
}
