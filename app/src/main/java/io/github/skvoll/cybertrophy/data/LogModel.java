package io.github.skvoll.cybertrophy.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import io.github.skvoll.cybertrophy.data.DataContract.LogEntry;

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
    private Long mProfileId;
    private Long mGameId;
    private Long mAchievementId;

    private LogModel(int type) {
        mTime = System.currentTimeMillis() / 1000;
        mType = type;
    }

    public LogModel(Cursor cursor) {
        mId = cursor.getLong(cursor.getColumnIndex(LogEntry._ID));

        mTime = cursor.getLong(cursor.getColumnIndex(LogEntry.COLUMN_TIME));
        mType = cursor.getInt(cursor.getColumnIndex(LogEntry.COLUMN_TYPE));
        mMessage = cursor.getString(cursor.getColumnIndex(LogEntry.COLUMN_MESSAGE));
        mProfileId = cursor.getLong(cursor.getColumnIndex(LogEntry.COLUMN_PROFILE_ID));
        mGameId = cursor.getLong(cursor.getColumnIndex(LogEntry.COLUMN_GAME_ID));
        mAchievementId = cursor.getLong(cursor.getColumnIndex(LogEntry.COLUMN_ACHIEVEMENT_ID));
    }

    public static LogModel debug(ProfileModel profileModel, String message) {
        LogModel logModel = new LogModel(TYPE_DEBUG);

        logModel.setMessage(message);
        logModel.setProfileId(profileModel.getId());

        return logModel;
    }

    public static LogModel log(ProfileModel profileModel, String message) {
        LogModel logModel = new LogModel(TYPE_MESSAGE);

        logModel.setMessage(message);
        logModel.setProfileId(profileModel.getId());

        return logModel;
    }

    public static LogModel newGame(GameModel gameModel) {
        LogModel logModel = new LogModel(TYPE_NEW_GAME);

        logModel.setProfileId(gameModel.getProfileId());
        logModel.setGameId(gameModel.getId());

        return logModel;
    }

    public static LogModel gameRemoved(GameModel gameModel) {
        LogModel logModel = new LogModel(TYPE_GAME_REMOVED);

        logModel.setProfileId(gameModel.getProfileId());
        logModel.setGameId(gameModel.getId());

        return logModel;
    }

    public static LogModel newAchievement(AchievementModel achievementModel) {
        LogModel logModel = new LogModel(TYPE_NEW_ACHIEVEMENT);

        logModel.setProfileId(achievementModel.getProfileId());
        logModel.setGameId(achievementModel.getGameId());
        logModel.setAchievementId(achievementModel.getId());

        return logModel;
    }

    public static LogModel achievementRemoved(AchievementModel achievementModel) {
        LogModel logModel = new LogModel(TYPE_ACHIEVEMENT_REMOVED);

        logModel.setProfileId(achievementModel.getProfileId());
        logModel.setGameId(achievementModel.getGameId());
        logModel.setAchievementId(achievementModel.getId());

        return logModel;
    }

    public static LogModel achievementUnlocked(AchievementModel achievementModel) {
        LogModel logModel = new LogModel(TYPE_ACHIEVEMENT_UNLOCKED);

        logModel.setProfileId(achievementModel.getProfileId());
        logModel.setGameId(achievementModel.getGameId());
        logModel.setAchievementId(achievementModel.getId());

        return logModel;
    }

    public static LogModel gameComplete(GameModel gameModel) {
        LogModel logModel = new LogModel(TYPE_GAME_COMPLETE);

        logModel.setProfileId(gameModel.getProfileId());
        logModel.setGameId(gameModel.getId());

        return logModel;
    }

    @Override
    public Uri getUri(Long id) {
        if (id == null) {
            return LogEntry.URI;
        }

        return ContentUris.withAppendedId(LogEntry.URI, id);
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

    public Long getProfileId() {
        return mProfileId;
    }

    public void setProfileId(Long profileId) {
        mProfileId = profileId;
    }

    public Long getGameId() {
        return mGameId;
    }

    public void setGameId(Long gameId) {
        mGameId = gameId;
    }

    public Long getAchievementId() {
        return mAchievementId;
    }

    public void setAchievementId(Long achievementId) {
        mAchievementId = achievementId;
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(LogEntry.COLUMN_TIME, mTime);
        contentValues.put(LogEntry.COLUMN_TYPE, mType);
        contentValues.put(LogEntry.COLUMN_MESSAGE, mMessage);
        contentValues.put(LogEntry.COLUMN_PROFILE_ID, mProfileId);
        contentValues.put(LogEntry.COLUMN_GAME_ID, mGameId);
        contentValues.put(LogEntry.COLUMN_ACHIEVEMENT_ID, mAchievementId);

        return contentValues;
    }
}
