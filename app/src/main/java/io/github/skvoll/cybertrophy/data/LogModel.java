package io.github.skvoll.cybertrophy.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public final class LogModel extends Model {
    public static final int TYPE_DEBUG = 0;
    public static final int TYPE_MESSAGE = 1;
    public static final int TYPE_NEW_GAME = 2;
    public static final int TYPE_GAME_REMOVED = 3;
    public static final int TYPE_NEW_ACHIEVEMENT = 4;
    public static final int TYPE_ACHIEVEMENT_REMOVED = 5;
    public static final int TYPE_ACHIEVEMENT_UNLOCKED = 6;
    public static final int TYPE_GAME_COMPLETE = 7;

    private Long mId;
    private Long mTime;
    private Integer mType;
    private String mMessage;
    private Long mSteamId;
    private Long mAppId;
    private String mAchievementCode;

    private LogModel(int type) {
        mTime = System.currentTimeMillis() / 1000;
        mType = type;
    }

    public LogModel(Cursor cursor) {
        mId = cursor.getLong(cursor.getColumnIndex(DataContract.LogEntry._ID));

        mTime = cursor.getLong(cursor.getColumnIndex(DataContract.LogEntry.COLUMN_TIME));
        mType = cursor.getInt(cursor.getColumnIndex(DataContract.LogEntry.COLUMN_TYPE));
        mMessage = cursor.getString(cursor.getColumnIndex(DataContract.LogEntry.COLUMN_MESSAGE));
        mSteamId = cursor.getLong(cursor.getColumnIndex(DataContract.LogEntry.COLUMN_STEAM_ID));
        mAppId = cursor.getLong(cursor.getColumnIndex(DataContract.LogEntry.COLUMN_APP_ID));
        mAchievementCode = cursor.getString(cursor.getColumnIndex(DataContract.LogEntry.COLUMN_ACHIEVEMENT_CODE));
    }

    public static LogModel debug(ProfileModel profileModel, String message) {
        LogModel logModel = new LogModel(TYPE_DEBUG);

        logModel.setMessage(message);
        logModel.setSteamId(profileModel.getSteamId());

        return logModel;
    }

    public static LogModel log(ProfileModel profileModel, String message) {
        LogModel logModel = new LogModel(TYPE_MESSAGE);

        logModel.setMessage(message);
        logModel.setSteamId(profileModel.getSteamId());

        return logModel;
    }

    public static LogModel newGame(GameModel gameModel) {
        LogModel logModel = new LogModel(TYPE_NEW_GAME);

        logModel.setSteamId(gameModel.getSteamId());
        logModel.setAppId(gameModel.getAppId());

        return logModel;
    }

    public static LogModel gameRemoved(GameModel gameModel) {
        LogModel logModel = new LogModel(TYPE_GAME_REMOVED);

        logModel.setSteamId(gameModel.getSteamId());
        logModel.setAppId(gameModel.getAppId());

        return logModel;
    }

    public static LogModel newAchievement(AchievementModel achievementModel) {
        LogModel logModel = new LogModel(TYPE_NEW_ACHIEVEMENT);

        logModel.setSteamId(achievementModel.getSteamId());
        logModel.setAppId(achievementModel.getAppId());
        logModel.setAchievementCode(achievementModel.getCode());

        return logModel;
    }

    public static LogModel achievementRemoved(AchievementModel achievementModel) {
        LogModel logModel = new LogModel(TYPE_ACHIEVEMENT_REMOVED);

        logModel.setSteamId(achievementModel.getSteamId());
        logModel.setAppId(achievementModel.getAppId());
        logModel.setAchievementCode(achievementModel.getCode());

        return logModel;
    }

    public static LogModel achievementUnlocked(AchievementModel achievementModel) {
        LogModel logModel = new LogModel(TYPE_ACHIEVEMENT_UNLOCKED);

        logModel.setSteamId(achievementModel.getSteamId());
        logModel.setAppId(achievementModel.getAppId());
        logModel.setAchievementCode(achievementModel.getCode());

        return logModel;
    }

    public static LogModel gameComplete(GameModel gameModel) {
        LogModel logModel = new LogModel(TYPE_GAME_COMPLETE);

        logModel.setSteamId(gameModel.getSteamId());
        logModel.setAppId(gameModel.getAppId());

        return logModel;
    }

    @Override
    Uri getUri(Long id) {
        if (id == null) {
            return DataContract.LogEntry.URI;
        }

        return ContentUris.withAppendedId(DataContract.LogEntry.URI, id);
    }

    @Override
    public Long getId() {
        return mId;
    }

    @Override
    public void setId(Long id) {
        mId = mId;
    }

    public Long getTime() {
        return mTime;
    }

    public void setTime(Long time) {
        mTime = time;
    }

    public Integer getType() {
        return mType;
    }

    public void setType(Integer type) {
        mType = type;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public Long getSteamId() {
        return mSteamId;
    }

    public void setSteamId(Long profileId) {
        mSteamId = profileId;
    }

    public Long getAppId() {
        return mAppId;
    }

    public void setAppId(Long appId) {
        mAppId = appId;
    }

    public String getAchievementCode() {
        return mAchievementCode;
    }

    public void setAchievementCode(String achievement) {
        mAchievementCode = achievement;
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(DataContract.LogEntry.COLUMN_TIME, mTime);
        contentValues.put(DataContract.LogEntry.COLUMN_TYPE, mType);
        contentValues.put(DataContract.LogEntry.COLUMN_MESSAGE, mMessage);
        contentValues.put(DataContract.LogEntry.COLUMN_STEAM_ID, mSteamId);
        contentValues.put(DataContract.LogEntry.COLUMN_APP_ID, mAppId);
        contentValues.put(DataContract.LogEntry.COLUMN_ACHIEVEMENT_CODE, mAchievementCode);

        return contentValues;
    }
}
