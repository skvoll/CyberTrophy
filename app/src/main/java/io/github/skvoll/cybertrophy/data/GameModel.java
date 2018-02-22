package io.github.skvoll.cybertrophy.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.util.LongSparseArray;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.steam.SteamGame;

import static io.github.skvoll.cybertrophy.data.DataContract.GameEntry;
import static io.github.skvoll.cybertrophy.data.DataContract.ProfileEntry;

public final class GameModel extends Model<GameModel> {
    public static final int ALL = 0;
    public static final int INCOMPLETE = 1;
    public static final int IN_PROGRESS = 2;
    public static final int COMPLETE = 3;
    public static final int NO_ACHIEVEMENTS = 4;

    private static String MEDIA_URL_TEMPLATE = "http://media.steampowered.com/steamcommunity/public/images/apps/%s/%s.jpg";
    private static String MEDIA_LOGO_TEMPLATE = "http://cdn.edgecast.steamstatic.com/steam/apps/%s/header.jpg";

    private Long mId;
    private Long mProfileId;
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

        mProfileId = cursor.getLong(cursor.getColumnIndex(GameEntry.COLUMN_PROFILE_ID));
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
        mProfileId = profileModel.getId();
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

    public static GameModel getCurrent(ContentResolver contentResolver, ProfileModel profileModel) {
        String selection = GameEntry.COLUMN_PROFILE_ID + " = ?";
        selection += " AND " + GameEntry.COLUMN_ACHIEVEMENTS_UNLOCKED_COUNT + " > 0"
                + " AND " + GameEntry.COLUMN_ACHIEVEMENTS_UNLOCKED_COUNT
                + " < " + GameEntry.COLUMN_ACHIEVEMENTS_TOTAL_COUNT;
        String[] selectionArgs = new String[]{String.valueOf(profileModel.getId())};
        String sortOrder = GameEntry.COLUMN_LAST_PLAY + " DESC LIMIT 1";

        Cursor cursor = contentResolver.query(GameEntry.URI, null, selection,
                selectionArgs, sortOrder);

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

    public static LongSparseArray<GameModel> getMapByProfile(ContentResolver contentResolver, ProfileModel profileModel) {
        String selection = GameEntry.COLUMN_PROFILE_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(profileModel.getId())};

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

    public static ArrayList<GameModel> getByProfile(
            ContentResolver contentResolver, ProfileModel profileModel, int status, int count) {
        String selection = GameEntry.COLUMN_PROFILE_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(profileModel.getId())};
        String sortOrder = GameEntry.COLUMN_NAME;

        if (status == NO_ACHIEVEMENTS) {
            selection += " AND " + GameEntry.COLUMN_ACHIEVEMENTS_TOTAL_COUNT + " == 0";
            sortOrder = GameEntry.COLUMN_NAME;
        } else {
            selection += " AND " + GameEntry.COLUMN_ACHIEVEMENTS_TOTAL_COUNT + " != 0";

            switch (status) {
                case IN_PROGRESS:
                    selection += " AND " + GameEntry.COLUMN_ACHIEVEMENTS_UNLOCKED_COUNT + " > 0"
                            + " AND " + GameEntry.COLUMN_ACHIEVEMENTS_UNLOCKED_COUNT
                            + " < " + GameEntry.COLUMN_ACHIEVEMENTS_TOTAL_COUNT;
                    sortOrder = GameEntry.COLUMN_LAST_PLAY + " DESC";
                    break;
                case INCOMPLETE:
                    selection += " AND " + GameEntry.COLUMN_ACHIEVEMENTS_UNLOCKED_COUNT + " == 0";
                    sortOrder = GameEntry.COLUMN_NAME;
                    break;
                case COMPLETE:
                    selection += " AND " + GameEntry.COLUMN_ACHIEVEMENTS_UNLOCKED_COUNT + " == "
                            + GameEntry.COLUMN_ACHIEVEMENTS_TOTAL_COUNT;
                    sortOrder = GameEntry.COLUMN_NAME;
                    break;
            }
        }

        sortOrder += " LIMIT " + count;

        Cursor cursor = contentResolver.query(GameEntry.URI, null,
                selection, selectionArgs, sortOrder);

        if (cursor == null) {
            return new ArrayList<>();
        }

        if (!cursor.moveToFirst()) {
            cursor.close();

            return new ArrayList<>();
        }

        ArrayList<GameModel> gameModels = new ArrayList<>(cursor.getCount());

        while (!cursor.isAfterLast()) {
            GameModel gameModel = new GameModel(cursor);

            gameModels.add(gameModel);

            cursor.moveToNext();
        }

        cursor.close();

        return gameModels;
    }

    public static ArrayList<GameModel> getByProfile(
            ContentResolver contentResolver, ProfileModel profileModel, int status) {
        return getByProfile(contentResolver, profileModel, status, Integer.MAX_VALUE);
    }

    public static ArrayList<GameModel> getByProfile(
            ContentResolver contentResolver, ProfileModel profileModel) {
        return getByProfile(contentResolver, profileModel, ALL);
    }

    @Override
    public Uri getUri(Long id) {
        if (id == null) {
            return GameEntry.URI;
        }

        return ContentUris.withAppendedId(GameEntry.URI, id);
    }

    @Override
    public Long getId() {
        return mId;
    }

    @Override
    GameModel setId(Long id) {
        mId = id;

        return this;
    }

    public Long getProfileId() {
        return mProfileId;
    }

    public Long getAppId() {
        return mAppId;
    }

    public String getName() {
        return mName;
    }

    public GameModel setName(String name) {
        mName = name;

        return this;
    }

    public Integer getPlaytimeForever() {
        return mPlaytimeForever;
    }

    public GameModel setPlaytimeForever(Integer playtimeForever) {
        mPlaytimeForever = playtimeForever;

        return this;
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

    public GameModel setLastPlay(Long lastPlay) {
        mLastPlay = lastPlay;

        return this;
    }

    public Integer getAchievementsTotalCount() {
        return mAchievementsTotalCount;
    }

    public GameModel setAchievementsTotalCount(Integer achievementsTotalCount) {
        mAchievementsTotalCount = achievementsTotalCount;

        return this;
    }

    public Integer getAchievementsUnlockedCount() {
        return mAchievementsUnlockedCount;
    }

    public GameModel setAchievementsUnlockedCount(Integer achievementsUnlockedCount) {
        mAchievementsUnlockedCount = achievementsUnlockedCount;

        return this;
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
            return COMPLETE;
        } else if (isInProgress()) {
            return IN_PROGRESS;
        } else {
            return INCOMPLETE;
        }
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(GameEntry.COLUMN_PROFILE_ID, mProfileId);
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
