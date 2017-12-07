package io.github.skvoll.cybertrophy.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.util.LongSparseArray;

import io.github.skvoll.cybertrophy.steam.SteamGame;

import static io.github.skvoll.cybertrophy.data.DataContract.*;

public final class GameModel extends Model {
    public static final int STATUS_INCOMPLETE = 0;
    public static final int STATUS_IN_PROGRESS = 1;
    public static final int STATUS_COMPLETE = 2;

    private static String MEDIA_URL_TEMPLATE = "http://media.steampowered.com/steamcommunity/public/images/apps/%s/%s.jpg";
    private static String MEDIA_LOGO_TEMPLATE = "http://cdn.edgecast.steamstatic.com/steam/apps/%s/header.jpg";

    private Long mId;
    private Long mSteamId;
    private Long mAppId;
    private String mName;
    private Integer mPlaytimeForever;
    private String mIconUrl;
    private String mLogoUrl;
    private Long mLastPlay;
    private Integer mAchievementsTotalCount;
    private Integer mAchievementsUnlockedCount;

    public GameModel(Cursor cursor) {
        mId = cursor.getLong(cursor.getColumnIndex(ProfileEntry._ID));

        mSteamId = cursor.getLong(cursor.getColumnIndex(ProfileEntry.COLUMN_STEAM_ID));
        mAppId = cursor.getLong(cursor.getColumnIndex(GameEntry.COLUMN_APP_ID));
        mName = cursor.getString(cursor.getColumnIndex(GameEntry.COLUMN_NAME));
        mPlaytimeForever = cursor.getInt(cursor.getColumnIndex(GameEntry.COLUMN_PLAYTIME_FOREVER));
        mIconUrl = cursor.getString(cursor.getColumnIndex(GameEntry.COLUMN_ICON_URL));
        mLogoUrl = cursor.getString(cursor.getColumnIndex(GameEntry.COLUMN_LOGO_URL));
        mLastPlay = cursor.getLong(cursor.getColumnIndex(GameEntry.COLUMN_LAST_PLAY));
        mAchievementsTotalCount = cursor.getInt(cursor.getColumnIndex(GameEntry.COLUMN_ACHIEVEMENTS_TOTAL_COUNT));
        mAchievementsUnlockedCount = cursor.getInt(cursor.getColumnIndex(GameEntry.COLUMN_ACHIEVEMENTS_UNLOCKED_COUNT));
    }

    public GameModel(ProfileModel profileModel, SteamGame steamGame) {
        mSteamId = profileModel.getSteamId();
        mAppId = steamGame.appId;
        mName = steamGame.name;
        mPlaytimeForever = steamGame.playtimeForever;
        mIconUrl = String.format(MEDIA_URL_TEMPLATE, steamGame.appId, steamGame.imgIconUrl);
        mLogoUrl = String.format(MEDIA_LOGO_TEMPLATE, steamGame.appId);
        mLastPlay = System.currentTimeMillis() / 1000;
        mAchievementsTotalCount = steamGame.getAchievementsTotalCount();
        mAchievementsUnlockedCount = steamGame.getAchievementsUnlockedCount();
    }

    public static GameModel getById(ContentResolver contentResolver, Long id) {
        Uri uri = ContentUris.withAppendedId(GameEntry.URI, id);
        Cursor cursor = contentResolver.query(uri,
                null, null, null, null);

        if (cursor == null) {
            return null;
        }

        if (!cursor.moveToFirst()) {
            cursor.close();

            return null;
        }

        GameModel gameModel = new GameModel(cursor);

        cursor.close();

        return gameModel;
    }

    public static GameModel getByAppId(ContentResolver contentResolver, Long appId) {
        String selection = GameEntry.COLUMN_APP_ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(appId)};

        Cursor cursor = contentResolver.query(GameEntry.URI, null,
                selection, selectionArgs, null);

        if (cursor == null) {
            return null;
        }

        if (!cursor.moveToFirst()) {
            cursor.close();

            return null;
        }

        GameModel gameModel = new GameModel(cursor);

        cursor.close();

        return gameModel;
    }

    public static LongSparseArray<GameModel> getByProfile(ContentResolver contentResolver, ProfileModel profileModel) {
        String selection = GameEntry.COLUMN_STEAM_ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(profileModel.getSteamId())};

        Cursor cursor = contentResolver.query(GameEntry.URI, null,
                selection, selectionArgs, null);

        if (cursor == null) {
            return new LongSparseArray<>();
        }

        if (!cursor.moveToFirst()) {
            cursor.close();

            return new LongSparseArray<>();
        }

        LongSparseArray<GameModel> gameModels = new LongSparseArray<>(cursor.getCount());

        while (!cursor.isAfterLast()) {
            GameModel gameModel = new GameModel(cursor);

            gameModels.put(gameModel.getAppId(), gameModel);

            cursor.moveToNext();
        }

        cursor.close();

        return gameModels;
    }

    @Override
    Uri getUri(Long id) {
        if (id == null) {
            return GameEntry.URI;
        }

        return ContentUris.withAppendedId(GameEntry.URI, id);
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

    public Long getAppId() {
        return mAppId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Integer getPlaytimeForever() {
        return mPlaytimeForever;
    }

    public void setPlaytimeForever(Integer playtimeForever) {
        mPlaytimeForever = playtimeForever;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public String getLogoUrl() {
        return mLogoUrl;
    }

    public Long getLastPlay() {
        return mLastPlay;
    }

    public void setLastPlay(Long lastPlay) {
        mLastPlay = lastPlay;
    }

    public Integer getAchievementsTotalCount() {
        return mAchievementsTotalCount;
    }

    public void setAchievementsTotalCount(Integer achievementsTotalCount) {
        mAchievementsTotalCount = achievementsTotalCount;
    }

    public Integer getAchievementsUnlockedCount() {
        return mAchievementsUnlockedCount;
    }

    public void setAchievementsUnlockedCount(Integer achievementsUnlockedCount) {
        mAchievementsUnlockedCount = achievementsUnlockedCount;
    }

    public Boolean isIncomplete() {
        return getAchievementsUnlockedCount().equals(0);
    }

    public Boolean isInProgress() {
        return getAchievementsUnlockedCount() > 0;
    }

    public Boolean isComplete() {
        return getAchievementsTotalCount().equals(getAchievementsUnlockedCount());
    }

    public int getStatus() {
        if (isComplete()) {
            return STATUS_COMPLETE;
        } else if (isInProgress()) {
            return STATUS_IN_PROGRESS;
        } else {
            return STATUS_INCOMPLETE;
        }
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(ProfileEntry.COLUMN_STEAM_ID, mSteamId);
        contentValues.put(GameEntry.COLUMN_APP_ID, mAppId);
        contentValues.put(GameEntry.COLUMN_NAME, mName);
        contentValues.put(GameEntry.COLUMN_PLAYTIME_FOREVER, mPlaytimeForever);
        contentValues.put(GameEntry.COLUMN_ICON_URL, mIconUrl);
        contentValues.put(GameEntry.COLUMN_LOGO_URL, mLogoUrl);
        contentValues.put(GameEntry.COLUMN_LAST_PLAY, mLastPlay);
        contentValues.put(GameEntry.COLUMN_ACHIEVEMENTS_TOTAL_COUNT, mAchievementsTotalCount);
        contentValues.put(GameEntry.COLUMN_ACHIEVEMENTS_UNLOCKED_COUNT, mAchievementsUnlockedCount);

        return contentValues;
    }
}
