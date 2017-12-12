package io.github.skvoll.cybertrophy.dashboard;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.data.DataContract.AchievementEntry;
import io.github.skvoll.cybertrophy.data.DataContract.GameEntry;
import io.github.skvoll.cybertrophy.data.DataContract.LogEntry;
import io.github.skvoll.cybertrophy.data.DatabaseHelper;
import io.github.skvoll.cybertrophy.data.LogModel;
import io.github.skvoll.cybertrophy.data.ProfileModel;

final class DashboardItem {
    static final int TYPE_DEBUG = LogModel.TYPE_DEBUG;
    static final int TYPE_MESSAGE = LogModel.TYPE_MESSAGE;
    static final int TYPE_NEW_GAME = LogModel.TYPE_NEW_GAME;
    static final int TYPE_GAME_REMOVED = LogModel.TYPE_GAME_REMOVED;
    static final int TYPE_NEW_ACHIEVEMENT = LogModel.TYPE_NEW_ACHIEVEMENT;
    static final int TYPE_ACHIEVEMENT_REMOVED = LogModel.TYPE_ACHIEVEMENT_REMOVED;
    static final int TYPE_ACHIEVEMENT_UNLOCKED = LogModel.TYPE_ACHIEVEMENT_UNLOCKED;
    static final int TYPE_GAME_COMPLETE = LogModel.TYPE_GAME_COMPLETE;

    static final int TYPE_CURRENT_GAME = 1001;

    private static final String TAG = DashboardItem.class.getSimpleName();

    private Integer mTime;
    private Integer mType;
    private String mMessage;
    private Long mSteamId;
    private Long mAppId;
    private String mAchievementCode;

    private String mAppName;
    private String mAppIconUrl;
    private String mAppLogoUrl;
    private Integer mAppAchievementsTotalCount;
    private Integer mAppAchievementsUnlockedCount;

    private String mAchievementName;
    private String mAchievementIconUrl;
    private Integer mAchievementUnlockTime;

    private DashboardItem(Cursor cursor) {
        mTime = cursor.getInt(cursor.getColumnIndex(LogEntry.TABLE_NAME + "_" + LogEntry.COLUMN_TIME));
        mType = cursor.getInt(cursor.getColumnIndex(LogEntry.TABLE_NAME + "_" + LogEntry.COLUMN_TYPE));
        mMessage = cursor.getString(cursor.getColumnIndex(LogEntry.TABLE_NAME + "_" + LogEntry.COLUMN_MESSAGE));
        mSteamId = cursor.getLong(cursor.getColumnIndex(LogEntry.TABLE_NAME + "_" + LogEntry.COLUMN_STEAM_ID));
        mAppId = cursor.getLong(cursor.getColumnIndex(LogEntry.TABLE_NAME + "_" + LogEntry.COLUMN_APP_ID));
        mAchievementCode = cursor.getString(cursor.getColumnIndex(LogEntry.TABLE_NAME + "_" + LogEntry.COLUMN_ACHIEVEMENT_CODE));
        mAppName = cursor.getString(cursor.getColumnIndex(GameEntry.TABLE_NAME + "_" + GameEntry.COLUMN_NAME));
        mAppIconUrl = cursor.getString(cursor.getColumnIndex(GameEntry.TABLE_NAME + "_" + GameEntry.COLUMN_ICON_URL));
        mAppLogoUrl = cursor.getString(cursor.getColumnIndex(GameEntry.TABLE_NAME + "_" + GameEntry.COLUMN_LOGO_URL));
        mAppAchievementsTotalCount = cursor.getInt(cursor.getColumnIndex(GameEntry.TABLE_NAME + "_" + GameEntry.COLUMN_ACHIEVEMENTS_TOTAL_COUNT));
        mAppAchievementsUnlockedCount = cursor.getInt(cursor.getColumnIndex(GameEntry.TABLE_NAME + "_" + GameEntry.COLUMN_ACHIEVEMENTS_UNLOCKED_COUNT));
        mAchievementName = cursor.getString(cursor.getColumnIndex(AchievementEntry.TABLE_NAME + "_" + AchievementEntry.COLUMN_NAME));
        mAchievementIconUrl = cursor.getString(cursor.getColumnIndex(AchievementEntry.TABLE_NAME + "_" + AchievementEntry.COLUMN_ICON_URL));
        mAchievementUnlockTime = cursor.getInt(cursor.getColumnIndex(AchievementEntry.TABLE_NAME + "_" + AchievementEntry.COLUMN_UNLOCK_TIME));
    }

    static ArrayList<DashboardItem> getItems(Context context, ProfileModel profileModel, Integer[] types, Integer limit, Integer offset) {
        SQLiteDatabase database = (new DatabaseHelper(context)).getReadableDatabase();

        String query = "" +
                "SELECT " +
                getSelectFields() + " " +
                "FROM " + LogEntry.TABLE_NAME + " " +
                "LEFT JOIN " + GameEntry.TABLE_NAME + " " +
                "ON " + GameEntry.TABLE_NAME + "." + GameEntry.COLUMN_APP_ID + " = " + LogEntry.TABLE_NAME + "." + LogEntry.COLUMN_APP_ID + " " +
                "LEFT JOIN " + AchievementEntry.TABLE_NAME + " ON " +
                AchievementEntry.TABLE_NAME + "." + AchievementEntry.COLUMN_APP_ID + " = " + LogEntry.TABLE_NAME + "." + LogEntry.COLUMN_APP_ID + " AND " +
                AchievementEntry.TABLE_NAME + "." + AchievementEntry.COLUMN_CODE + " = " + LogEntry.TABLE_NAME + "." + LogEntry.COLUMN_ACHIEVEMENT_CODE + " " +
                "WHERE " + LogEntry.TABLE_NAME + "." + LogEntry.COLUMN_STEAM_ID + " = " + profileModel.getSteamId() + " " +
                "AND " + LogEntry.TABLE_NAME + "." + LogEntry.COLUMN_TYPE + " IN (" + TextUtils.join(", ", types) + ") " +
                "ORDER BY " + LogEntry.TABLE_NAME + "." + LogEntry._ID + " DESC " +
                "LIMIT " + limit + " OFFSET " + offset;

        Cursor cursor = database.rawQuery(query, null);

        if (cursor == null) {
            database.close();
            return new ArrayList<>(0);
        }

        int count = cursor.getCount();

        if (count <= 0) {
            cursor.close();
            database.close();

            return new ArrayList<>(0);
        }

        ArrayList<DashboardItem> dashboardItems = new ArrayList<>(count);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            dashboardItems.add(new DashboardItem(cursor));

            cursor.moveToNext();
        }

        cursor.close();
        database.close();

        return dashboardItems;
    }

    static DashboardItem currentGame(Context context, ProfileModel profileModel) {
        SQLiteDatabase database = (new DatabaseHelper(context)).getReadableDatabase();

        String query = "" +
                "SELECT " +
                getSelectFields() + " " +
                "FROM " + GameEntry.TABLE_NAME + " " +
                "LEFT JOIN " + LogEntry.TABLE_NAME + " " +
                "ON " + LogEntry.TABLE_NAME + "." + LogEntry.COLUMN_APP_ID + " = " + GameEntry.TABLE_NAME + "." + GameEntry.COLUMN_APP_ID + " " +
                "LEFT JOIN " + AchievementEntry.TABLE_NAME + " ON " +
                AchievementEntry.TABLE_NAME + "." + AchievementEntry.COLUMN_APP_ID + " = " + LogEntry.TABLE_NAME + "." + LogEntry.COLUMN_APP_ID + " AND " +
                AchievementEntry.TABLE_NAME + "." + AchievementEntry.COLUMN_CODE + " = " + LogEntry.TABLE_NAME + "." + LogEntry.COLUMN_ACHIEVEMENT_CODE + " " +
                "WHERE " + LogEntry.TABLE_NAME + "." + LogEntry.COLUMN_STEAM_ID + " = " + profileModel.getSteamId() + " " +
                "AND " + GameEntry.TABLE_NAME + "." + GameEntry.COLUMN_ACHIEVEMENTS_TOTAL_COUNT + " > 0 " +
                "AND " + GameEntry.TABLE_NAME + "." + GameEntry.COLUMN_ACHIEVEMENTS_UNLOCKED_COUNT + " > 0 " +
                "AND " + GameEntry.TABLE_NAME + "." + GameEntry.COLUMN_ACHIEVEMENTS_UNLOCKED_COUNT + " < " + GameEntry.TABLE_NAME + "." + GameEntry.COLUMN_ACHIEVEMENTS_TOTAL_COUNT + " " +
                "ORDER BY " + GameEntry.TABLE_NAME + "." + GameEntry.COLUMN_LAST_PLAY + " DESC " +
                "LIMIT " + 1;

        Cursor cursor = database.rawQuery(query, null);

        if (cursor == null) {
            database.close();
            return null;
        }

        if (cursor.getCount() != 1) {
            cursor.close();
            database.close();

            return null;
        }

        DashboardItem dashboardItem;

        cursor.moveToFirst();
        dashboardItem = new DashboardItem(cursor);
        dashboardItem.setType(TYPE_CURRENT_GAME);

        cursor.close();
        database.close();

        return dashboardItem;
    }

    private static String getSelectFields() {
        return "" +
                LogEntry.TABLE_NAME + "." + LogEntry.COLUMN_TIME + " AS " + LogEntry.TABLE_NAME + "_" + LogEntry.COLUMN_TIME + ", " +
                LogEntry.TABLE_NAME + "." + LogEntry.COLUMN_TYPE + " AS " + LogEntry.TABLE_NAME + "_" + LogEntry.COLUMN_TYPE + ", " +
                LogEntry.TABLE_NAME + "." + LogEntry.COLUMN_MESSAGE + " AS " + LogEntry.TABLE_NAME + "_" + LogEntry.COLUMN_MESSAGE + ", " +
                LogEntry.TABLE_NAME + "." + LogEntry.COLUMN_STEAM_ID + " AS " + LogEntry.TABLE_NAME + "_" + LogEntry.COLUMN_STEAM_ID + ", " +
                LogEntry.TABLE_NAME + "." + LogEntry.COLUMN_APP_ID + " AS " + LogEntry.TABLE_NAME + "_" + LogEntry.COLUMN_APP_ID + ", " +
                LogEntry.TABLE_NAME + "." + LogEntry.COLUMN_ACHIEVEMENT_CODE + " AS " + LogEntry.TABLE_NAME + "_" + LogEntry.COLUMN_ACHIEVEMENT_CODE + ", " +
                GameEntry.TABLE_NAME + "." + GameEntry.COLUMN_NAME + " AS " + GameEntry.TABLE_NAME + "_" + GameEntry.COLUMN_NAME + ", " +
                GameEntry.TABLE_NAME + "." + GameEntry.COLUMN_ICON_URL + " AS " + GameEntry.TABLE_NAME + "_" + GameEntry.COLUMN_ICON_URL + ", " +
                GameEntry.TABLE_NAME + "." + GameEntry.COLUMN_LOGO_URL + " AS " + GameEntry.TABLE_NAME + "_" + GameEntry.COLUMN_LOGO_URL + ", " +
                GameEntry.TABLE_NAME + "." + GameEntry.COLUMN_ACHIEVEMENTS_TOTAL_COUNT + " AS " + GameEntry.TABLE_NAME + "_" + GameEntry.COLUMN_ACHIEVEMENTS_TOTAL_COUNT + ", " +
                GameEntry.TABLE_NAME + "." + GameEntry.COLUMN_ACHIEVEMENTS_UNLOCKED_COUNT + " AS " + GameEntry.TABLE_NAME + "_" + GameEntry.COLUMN_ACHIEVEMENTS_UNLOCKED_COUNT + ", " +
                AchievementEntry.TABLE_NAME + "." + AchievementEntry.COLUMN_NAME + " AS " + AchievementEntry.TABLE_NAME + "_" + AchievementEntry.COLUMN_NAME + ", " +
                AchievementEntry.TABLE_NAME + "." + AchievementEntry.COLUMN_ICON_URL + " AS " + AchievementEntry.TABLE_NAME + "_" + AchievementEntry.COLUMN_ICON_URL + ", " +
                AchievementEntry.TABLE_NAME + "." + AchievementEntry.COLUMN_UNLOCK_TIME + " AS " + AchievementEntry.TABLE_NAME + "_" + AchievementEntry.COLUMN_UNLOCK_TIME;
    }

    Integer getTime() {
        return mTime;
    }

    Integer getType() {
        return mType;
    }

    void setType(int type) {
        mType = type;
    }

    String getMessage() {
        return mMessage;
    }

    Long getSteamId() {
        return mSteamId;
    }

    Long getAppId() {
        return mAppId;
    }

    String getAchievementCode() {
        return mAchievementCode;
    }

    String getAppName() {
        return mAppName;
    }

    String getAppIconUrl() {
        return mAppIconUrl;
    }

    String getAppLogoUrl() {
        return mAppLogoUrl;
    }

    Integer getAppAchievementsTotalTount() {
        return mAppAchievementsTotalCount;
    }

    Integer getAppAchievementsUnlockedCount() {
        return mAppAchievementsUnlockedCount;
    }

    String getAchievementName() {
        return mAchievementName;
    }

    String getAchievementIconUrl() {
        return mAchievementIconUrl;
    }

    Integer getAchievementUnlockTime() {
        return mAchievementUnlockTime;
    }
}
