package io.github.skvoll.cybertrophy.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.math.BigDecimal;
import java.util.HashMap;

import io.github.skvoll.cybertrophy.data.DataContract.AchievementEntry;
import io.github.skvoll.cybertrophy.steam.SteamAchievement;

public final class AchievementModel extends Model {
    private Long mId;
    private Long mSteamId;
    private Long mAppId;
    private String mCode;
    private String mName;
    private Integer mIsHidden;
    private String mDescription;
    private String mIconUrl;
    private String mIconGrayUrl;
    private BigDecimal mPercent;
    private Integer mIsUnlocked;
    private Integer mUnlockTime;

    public AchievementModel(Cursor cursor) {
        mId = cursor.getLong(cursor.getColumnIndex(DataContract.ProfileEntry._ID));

        mSteamId = cursor.getLong(cursor.getColumnIndex(DataContract.ProfileEntry.COLUMN_STEAM_ID));
        mAppId = cursor.getLong(cursor.getColumnIndex(AchievementEntry.COLUMN_APP_ID));
        mCode = cursor.getString(cursor.getColumnIndex(AchievementEntry.COLUMN_CODE));
        mName = cursor.getString(cursor.getColumnIndex(AchievementEntry.COLUMN_NAME));
        mIsHidden = cursor.getInt(cursor.getColumnIndex(AchievementEntry.COLUMN_IS_HIDDEN));
        mDescription = cursor.getString(cursor.getColumnIndex(AchievementEntry.COLUMN_DESCRIPTION));
        mIconUrl = cursor.getString(cursor.getColumnIndex(AchievementEntry.COLUMN_ICON_URL));
        mIconGrayUrl = cursor.getString(cursor.getColumnIndex(AchievementEntry.COLUMN_ICON_GRAY_URL));
        mPercent = BigDecimal.valueOf(cursor.getDouble(cursor.getColumnIndex(AchievementEntry.COLUMN_PERCENT)));
        mIsUnlocked = cursor.getInt(cursor.getColumnIndex(AchievementEntry.COLUMN_IS_UNLOCKED));
        mUnlockTime = cursor.getInt(cursor.getColumnIndex(AchievementEntry.COLUMN_UNLOCK_TIME));
    }

    public AchievementModel(GameModel gameModel, SteamAchievement steamAchievement) {
        mSteamId = gameModel.getSteamId();
        mAppId = gameModel.getAppId();
        mCode = steamAchievement.name;
        mName = steamAchievement.displayName;
        mIsHidden = steamAchievement.hidden;
        mDescription = steamAchievement.description;
        mIconUrl = steamAchievement.icon;
        mIconGrayUrl = steamAchievement.iconGray;
        mPercent = steamAchievement.getPercent();
        mIsUnlocked = steamAchievement.isUnlocked() ? 1 : 0;
        mUnlockTime = steamAchievement.getUnlockTime();
    }

    public static AchievementModel getById(ContentResolver contentResolver, Long id) {
        Uri uri = ContentUris.withAppendedId(DataContract.AchievementEntry.URI, id);
        Cursor cursor = contentResolver.query(uri,
                null, null, null, null);

        if (cursor == null) {
            return null;
        }

        if (!cursor.moveToFirst()) {
            cursor.close();

            return null;
        }

        AchievementModel achievementModel = new AchievementModel(cursor);

        cursor.close();

        return achievementModel;
    }

    public static AchievementModel getByCode(ContentResolver contentResolver, String code) {
        String selection = AchievementEntry.COLUMN_CODE + "=?";
        String[] selectionArgs = new String[]{code};

        Cursor cursor = contentResolver.query(AchievementEntry.URI, null,
                selection, selectionArgs, null);

        if (cursor == null) {
            return null;
        }

        if (!cursor.moveToFirst()) {
            cursor.close();

            return null;
        }

        AchievementModel achievementEntry = new AchievementModel(cursor);

        cursor.close();

        return achievementEntry;
    }

    public static HashMap<String, AchievementModel> getByGame(ContentResolver contentResolver, GameModel gameModel) {
        String selection = AchievementEntry.COLUMN_STEAM_ID + "=? AND " + AchievementEntry.COLUMN_APP_ID + "=?";
        String[] selectionArgs = new String[]{gameModel.getSteamId().toString(), gameModel.getAppId().toString()};

        Cursor cursor = contentResolver.query(AchievementEntry.URI, null,
                selection, selectionArgs, null);

        if (cursor == null) {
            return new HashMap<>();
        }

        if (!cursor.moveToFirst()) {
            cursor.close();

            return new HashMap<>();
        }

        HashMap<String, AchievementModel> achievementModels = new HashMap<>(cursor.getCount());

        while (!cursor.isAfterLast()) {
            AchievementModel achievementModel = new AchievementModel(cursor);

            achievementModels.put(achievementModel.getCode(), achievementModel);

            cursor.moveToNext();
        }

        cursor.close();

        return achievementModels;
    }

    @Override
    Uri getUri(Long id) {
        // TODO: check with null id
        if (id == null) {
            return AchievementEntry.URI;
        }

        return ContentUris.withAppendedId(AchievementEntry.URI, id);
    }

    @Override
    Long getId() {
        return mId;
    }

    @Override
    void setId(Long id) {
        mId = id;
    }

    public Long getSteamId() {
        return mSteamId;
    }

    public void setSteamId(Long steamId) {
        mSteamId = steamId;
    }

    public Long getAppId() {
        return mAppId;
    }

    public String getCode() {
        return mCode;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Boolean isHidden() {
        return mIsHidden == 1;
    }

    public void setHidden(Boolean isHidden) {
        mIsHidden = isHidden ? 1 : 0;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public String getIconGrayUrl() {
        return mIconGrayUrl;
    }

    public BigDecimal getPercent() {
        return mPercent;
    }

    public void setPercent(BigDecimal percent) {
        mPercent = percent;
    }

    public Boolean isUnlocked() {
        return mIsUnlocked == 1;
    }

    public void setUnlocked(Boolean isUnlocked) {
        mIsUnlocked = isUnlocked ? 1 : 0;
    }

    public Integer getUnlockTime() {
        return mUnlockTime;
    }

    public void setUnlockTime(Integer unlockTime) {
        mUnlockTime = unlockTime;
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(DataContract.ProfileEntry.COLUMN_STEAM_ID, mSteamId);
        contentValues.put(AchievementEntry.COLUMN_APP_ID, mAppId);
        contentValues.put(AchievementEntry.COLUMN_CODE, mCode);
        contentValues.put(AchievementEntry.COLUMN_NAME, mName);
        contentValues.put(AchievementEntry.COLUMN_IS_HIDDEN, mIsHidden);
        contentValues.put(AchievementEntry.COLUMN_DESCRIPTION, mDescription);
        contentValues.put(AchievementEntry.COLUMN_ICON_URL, mIconUrl);
        contentValues.put(AchievementEntry.COLUMN_ICON_GRAY_URL, mIconGrayUrl);
        contentValues.put(AchievementEntry.COLUMN_PERCENT, mPercent.doubleValue());
        contentValues.put(AchievementEntry.COLUMN_IS_UNLOCKED, mIsUnlocked);
        contentValues.put(AchievementEntry.COLUMN_UNLOCK_TIME, mUnlockTime);

        return contentValues;
    }
}
