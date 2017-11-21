package io.github.skvoll.cybertrophy.data;

import android.net.Uri;
import android.provider.BaseColumns;

public abstract class DataContract {
    public static final String AUTHORITY = "io.github.skvoll.cybertrophy";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static abstract class ProfileEntry implements BaseColumns {
        public static final String TABLE_NAME = "profile";

        public static final Uri URI = Uri.withAppendedPath(CONTENT_URI, TABLE_NAME);

        public static final String COLUMN_STEAM_ID = "steam_id";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_REAL_NAME = "real_name";
        public static final String COLUMN_AVATAR = "avatar";
        public static final String COLUMN_AVATAR_MEDIUM = "avatar_medium";
        public static final String COLUMN_AVATAR_FULL = "avatar_full";
        public static final String COLUMN_LOC_COUNTRY_CODE = "loc_country_code";
        public static final String COLUMN_BACKGROUND_IMAGE = "background_image";
        public static final String COLUMN_IS_INITIALIZED = "is_initialized";
        public static final String COLUMN_IS_ACTIVE = "is_active";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_STEAM_ID + " INTEGER NOT NULL,"
                + COLUMN_URL + " TEXT NOT NULL,"
                + COLUMN_NAME + " TEXT NOT NULL,"
                + COLUMN_REAL_NAME + " TEXT,"
                + COLUMN_AVATAR + " TEXT NOT NULL,"
                + COLUMN_AVATAR_MEDIUM + " TEXT NOT NULL,"
                + COLUMN_AVATAR_FULL + " TEXT NOT NULL,"
                + COLUMN_LOC_COUNTRY_CODE + " TEXT NOT NULL,"
                + COLUMN_BACKGROUND_IMAGE + " TEXT,"
                + COLUMN_IS_INITIALIZED + " BOOLEAN NOT NULL DEFAULT 1,"
                + COLUMN_IS_ACTIVE + " BOOLEAN NOT NULL DEFAULT 1);";

        public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class GameEntry implements BaseColumns {
        public static final String TABLE_NAME = "game";

        public static final Uri URI = Uri.withAppendedPath(CONTENT_URI, TABLE_NAME);

        public static final String COLUMN_STEAM_ID = "steam_id";
        public static final String COLUMN_APP_ID = "app_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PLAYTIME_FOREVER = "playtime_forever";
        public static final String COLUMN_ICON_URL = "icon_url";
        public static final String COLUMN_LOGO_URL = "logo_url";
        public static final String COLUMN_LAST_PLAY = "last_play";
        public static final String COLUMN_ACHIEVEMENTS_TOTAL_COUNT = "achievements_total_count";
        public static final String COLUMN_ACHIEVEMENTS_UNLOCKED_COUNT = "achievements_unlocked_count";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_STEAM_ID + " INTEGER NOT NULL,"
                + COLUMN_APP_ID + " INTEGER NOT NULL,"
                + COLUMN_NAME + " TEXT NOT NULL,"
                + COLUMN_PLAYTIME_FOREVER + " INTEGER NOT NULL DEFAULT 0,"
                + COLUMN_ICON_URL + " TEXT NOT NULL,"
                + COLUMN_LOGO_URL + " TEXT NOT NULL,"
                + COLUMN_LAST_PLAY + " INTEGER NOT NULL DEFAULT 0,"
                + COLUMN_ACHIEVEMENTS_TOTAL_COUNT + " INTEGER DEFAULT NULL,"
                + COLUMN_ACHIEVEMENTS_UNLOCKED_COUNT + " INTEGER DEFAULT NULL);";

        public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class AchievementEntry implements BaseColumns {
        public static final String TABLE_NAME = "achievement";

        public static final Uri URI = Uri.withAppendedPath(CONTENT_URI, TABLE_NAME);

        public static final String COLUMN_STEAM_ID = "steam_id";
        public static final String COLUMN_APP_ID = "app_id";
        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_IS_HIDDEN = "is_hidden";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_ICON_URL = "icon_url";
        public static final String COLUMN_ICON_GRAY_URL = "icon_gray_url";
        public static final String COLUMN_PERCENT = "percent";
        public static final String COLUMN_IS_UNLOCKED = "is_unlocked";
        public static final String COLUMN_UNLOCK_TIME = "unlock_time";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_STEAM_ID + " INTEGER NOT NULL,"
                + COLUMN_APP_ID + " INTEGER NOT NULL,"
                + COLUMN_CODE + " TEXT NOT NULL,"
                + COLUMN_NAME + " TEXT NOT NULL,"
                + COLUMN_IS_HIDDEN + " BOOLEAN NOT NULL DEFAULT 1,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_ICON_URL + " TEXT NOT NULL,"
                + COLUMN_ICON_GRAY_URL + " TEX NOT NULL,"
                + COLUMN_PERCENT + " REAL NOT NULL DEFAULT 0.0,"
                + COLUMN_IS_UNLOCKED + " BOOLEAN NOT NULL DEFAULT 0,"
                + COLUMN_UNLOCK_TIME + " INTEGER DEFAULT NULL)";

        public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class LogEntry implements BaseColumns {
        public static final String TABLE_NAME = "log";

        public static final Uri URI = Uri.withAppendedPath(CONTENT_URI, TABLE_NAME);

        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_MESSAGE = "message";
        public static final String COLUMN_STEAM_ID = "steam_id";
        public static final String COLUMN_APP_ID = "app_id";
        public static final String COLUMN_ACHIEVEMENT_CODE = "achievement_code";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TIME + " INTEGER NOT NULL,"
                + COLUMN_TYPE + " INTEGER NOT NULL,"
                + COLUMN_MESSAGE + " TEXT DEFAULT NULL,"
                + COLUMN_STEAM_ID + " INTEGER DEFAULT NULL,"
                + COLUMN_APP_ID + " INTEGER DEFAULT NULL,"
                + COLUMN_ACHIEVEMENT_CODE + " TEXT DEFAULT NULL)";

        public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
