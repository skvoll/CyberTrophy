package io.github.skvoll.cybertrophy.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import io.github.skvoll.cybertrophy.data.DataContract.AchievementEntry;
import io.github.skvoll.cybertrophy.steam.SteamAchievement;

public final class AchievementModel extends Model<AchievementModel> {
    public static final int STATUS_ALL = 0;
    public static final int STATUS_LOCKED = 1;
    public static final int STATUS_UNLOCKED = 2;
    public static final int RARITY_COMMON = 0;
    public static final int RARITY_RARE = 1;
    public static final int RARITY_EPIC = 2;
    public static final int RARITY_LEGENDARY = 4;

    private Long mId;
    private final Long mProfileId;
    private final Long mGameId;
    private final Long mAppId;
    private final String mCode;
    private String mName;
    private Integer mIsHidden;
    private String mDescription;
    private final String mIconUrl;
    private final String mIconGrayUrl;
    private BigDecimal mPercent;
    private Integer mIsUnlocked;
    private Integer mUnlockTime;

    public AchievementModel(Cursor cursor) {
        mId = cursor.getLong(cursor.getColumnIndex(AchievementEntry._ID));

        mProfileId = cursor.getLong(cursor.getColumnIndex(AchievementEntry.COLUMN_PROFILE_ID));
        mGameId = cursor.getLong(cursor.getColumnIndex(AchievementEntry.COLUMN_GAME_ID));
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
        mProfileId = gameModel.getProfileId();
        mGameId = gameModel.getId();
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

    public static HashMap<String, AchievementModel> getMapByGame(
            ContentResolver contentResolver, GameModel gameModel) {
        String selection = AchievementEntry.COLUMN_GAME_ID + " = ?";
        String[] selectionArgs = new String[]{gameModel.getId().toString()};

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

    public static ArrayList<AchievementModel> getByGame(
            ContentResolver contentResolver, GameModel gameModel, int status, int count) {
        String selection = AchievementEntry.COLUMN_GAME_ID + " = ?";
        String[] selectionArgs = new String[]{gameModel.getId().toString()};

        String sortOrder;

        switch (status) {
            case STATUS_LOCKED:
                selection += " AND " + AchievementEntry.COLUMN_IS_UNLOCKED + " = 0";
                sortOrder = AchievementEntry.COLUMN_PERCENT + " DESC";
                break;
            case STATUS_UNLOCKED:
                selection += " AND " + AchievementEntry.COLUMN_IS_UNLOCKED + " = 1";
                sortOrder = AchievementEntry.COLUMN_UNLOCK_TIME + " DESC";
                break;
            default:
                sortOrder = AchievementEntry.COLUMN_NAME;
                break;
        }

        sortOrder += " LIMIT " + count;

        Cursor cursor = contentResolver.query(AchievementEntry.URI, null,
                selection, selectionArgs, sortOrder);

        if (cursor == null) {
            return new ArrayList<>();
        }

        if (!cursor.moveToFirst()) {
            cursor.close();

            return new ArrayList<>();
        }

        ArrayList<AchievementModel> achievementModels = new ArrayList<>(cursor.getCount());

        while (!cursor.isAfterLast()) {
            AchievementModel achievementModel = new AchievementModel(cursor);

            achievementModels.add(achievementModel);

            cursor.moveToNext();
        }

        cursor.close();

        return achievementModels;
    }

    public static ArrayList<AchievementModel> getByGame(
            ContentResolver contentResolver, GameModel gameModel, int status) {
        return getByGame(contentResolver, gameModel, status, Integer.MAX_VALUE);
    }

    public static ArrayList<AchievementModel> getByGame(
            ContentResolver contentResolver, GameModel gameModel) {
        return getByGame(contentResolver, gameModel, STATUS_ALL);
    }

    @Override
    public Uri getUri(Long id) {
        if (id == null) {
            return AchievementEntry.URI;
        }

        return ContentUris.withAppendedId(AchievementEntry.URI, id);
    }

    @Override
    public Long getId() {
        return mId;
    }

    @Override
    AchievementModel setId(Long id) {
        mId = id;

        return this;
    }

    public Long getProfileId() {
        return mProfileId;
    }

    public Long getGameId() {
        return mGameId;
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

    public AchievementModel setName(String name) {
        mName = name;

        return this;
    }

    public Boolean isHidden() {
        return mIsHidden == 1;
    }

    public AchievementModel setHidden(Boolean isHidden) {
        mIsHidden = isHidden ? 1 : 0;

        return this;
    }

    public String getDescription() {
        return mDescription;
    }

    public AchievementModel setDescription(String description) {
        mDescription = description;

        return this;
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

    public AchievementModel setPercent(BigDecimal percent) {
        mPercent = percent;

        return this;
    }

    public Boolean isUnlocked() {
        return mIsUnlocked == 1;
    }

    public AchievementModel setUnlocked(Boolean isUnlocked) {
        mIsUnlocked = isUnlocked ? 1 : 0;

        return this;
    }

    public Integer getUnlockTime() {
        return mUnlockTime;
    }

    public AchievementModel setUnlockTime(Integer unlockTime) {
        mUnlockTime = unlockTime;

        return this;
    }

    public Integer getRarity() {
        if (mPercent.compareTo(new BigDecimal(5)) < 0) {
            return RARITY_LEGENDARY;
        } else if (mPercent.compareTo(new BigDecimal(30)) < 0) {
            return RARITY_EPIC;
        } else if (mPercent.compareTo(new BigDecimal(70)) < 0) {
            return RARITY_RARE;
        } else {
            return RARITY_COMMON;
        }
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(AchievementEntry.COLUMN_PROFILE_ID, mProfileId);
        contentValues.put(AchievementEntry.COLUMN_GAME_ID, mGameId);
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

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s: %s(%d)",
                this.getClass().getSimpleName(), getName(), getId());
    }
}
