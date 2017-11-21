package io.github.skvoll.cybertrophy.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

public final class DataProvider extends ContentProvider {
    private static final String TAG = DataProvider.class.getSimpleName();

    private static final int PROFILES = 100;
    private static final int PROFILES_ID = 101;
    private static final int GAMES = 200;
    private static final int GAMES_ID = 201;
    private static final int ACHIEVEMENTS = 300;
    private static final int ACHIEVEMENTS_ID = 301;

    private static final int LOG = 1000;
    private static final int LOG_ID = 1001;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(DataContract.AUTHORITY, DataContract.ProfileEntry.TABLE_NAME, PROFILES);
        sUriMatcher.addURI(DataContract.AUTHORITY, DataContract.ProfileEntry.TABLE_NAME + "/#", PROFILES_ID);
        sUriMatcher.addURI(DataContract.AUTHORITY, DataContract.GameEntry.TABLE_NAME, GAMES);
        sUriMatcher.addURI(DataContract.AUTHORITY, DataContract.GameEntry.TABLE_NAME + "/#", GAMES_ID);
        sUriMatcher.addURI(DataContract.AUTHORITY, DataContract.AchievementEntry.TABLE_NAME, ACHIEVEMENTS);
        sUriMatcher.addURI(DataContract.AUTHORITY, DataContract.AchievementEntry.TABLE_NAME + "/#", ACHIEVEMENTS_ID);

        sUriMatcher.addURI(DataContract.AUTHORITY, DataContract.LogEntry.TABLE_NAME, LOG);
        sUriMatcher.addURI(DataContract.AUTHORITY, DataContract.LogEntry.TABLE_NAME + "/#", LOG_ID);
    }

    private DatabaseHelper mDatabaseHelper;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] columns, String selection, String[] selectionArgs, String orderBy) {
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        switch (sUriMatcher.match(uri)) {
            case PROFILES:
                return database.query(DataContract.ProfileEntry.TABLE_NAME,
                        columns, selection, selectionArgs, null, null, orderBy);
            case PROFILES_ID:
                selection = DataContract.ProfileEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return database.query(DataContract.ProfileEntry.TABLE_NAME,
                        columns, selection, selectionArgs, null, null, null);
            case GAMES:
                return database.query(DataContract.GameEntry.TABLE_NAME,
                        columns, selection, selectionArgs, null, null, orderBy);
            case GAMES_ID:
                selection = DataContract.GameEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return database.query(DataContract.GameEntry.TABLE_NAME,
                        columns, selection, selectionArgs, null, null, null);
            case ACHIEVEMENTS:
                return database.query(DataContract.AchievementEntry.TABLE_NAME,
                        columns, selection, selectionArgs, null, null, orderBy);
            case ACHIEVEMENTS_ID:
                selection = DataContract.AchievementEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

            case LOG:
                return database.query(DataContract.LogEntry.TABLE_NAME,
                        columns, selection, selectionArgs, null, null, orderBy);
            case LOG_ID:
                selection = DataContract.LogEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return database.query(DataContract.LogEntry.TABLE_NAME,
                        columns, selection, selectionArgs, null, null, null);
            default:
                throw new IllegalArgumentException("Unsupported URI \"" + uri + "\" for query");
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();

        long id;

        switch (sUriMatcher.match(uri)) {
            case PROFILES:
                id = database.insert(DataContract.ProfileEntry.TABLE_NAME, null, contentValues);
                break;
            case GAMES:
                id = database.insert(DataContract.GameEntry.TABLE_NAME, null, contentValues);
                break;
            case ACHIEVEMENTS:
                id = database.insert(DataContract.AchievementEntry.TABLE_NAME, null, contentValues);
                break;

            case LOG:
                id = database.insert(DataContract.LogEntry.TABLE_NAME, null, contentValues);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI \"" + uri + "\" for insert");
        }

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String whereClause, String[] whereArgs) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case PROFILES:
                return database.update(DataContract.ProfileEntry.TABLE_NAME,
                        contentValues, whereClause, whereArgs);
            case PROFILES_ID:
                whereClause = DataContract.ProfileEntry._ID + "=?";
                whereArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return database.update(DataContract.ProfileEntry.TABLE_NAME,
                        contentValues, whereClause, whereArgs);
            case GAMES:
                return database.update(DataContract.GameEntry.TABLE_NAME,
                        contentValues, whereClause, whereArgs);
            case GAMES_ID:
                whereClause = DataContract.GameEntry._ID + "=?";
                whereArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return database.update(DataContract.GameEntry.TABLE_NAME,
                        contentValues, whereClause, whereArgs);
            case ACHIEVEMENTS:
                return database.update(DataContract.AchievementEntry.TABLE_NAME,
                        contentValues, whereClause, whereArgs);
            case ACHIEVEMENTS_ID:
                whereClause = DataContract.AchievementEntry._ID + "=?";
                whereArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return database.update(DataContract.AchievementEntry.TABLE_NAME,
                        contentValues, whereClause, whereArgs);

            case LOG:
                return database.update(DataContract.LogEntry.TABLE_NAME,
                        contentValues, whereClause, whereArgs);
            case LOG_ID:
                whereClause = DataContract.LogEntry._ID + "=?";
                whereArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return database.update(DataContract.LogEntry.TABLE_NAME,
                        contentValues, whereClause, whereArgs);
            default:
                throw new IllegalArgumentException("Unsupported URI \"" + uri + "\" for update");
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String whereClause, String[] whereArgs) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case PROFILES:
                return database.delete(DataContract.ProfileEntry.TABLE_NAME, whereClause, whereArgs);
            case PROFILES_ID:
                whereClause = DataContract.ProfileEntry._ID + "=?";
                whereArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return database.delete(DataContract.ProfileEntry.TABLE_NAME, whereClause, whereArgs);
            case GAMES:
                return database.delete(DataContract.GameEntry.TABLE_NAME, whereClause, whereArgs);
            case GAMES_ID:
                whereClause = DataContract.GameEntry._ID + "=?";
                whereArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return database.delete(DataContract.GameEntry.TABLE_NAME, whereClause, whereArgs);
            case ACHIEVEMENTS:
                return database.delete(DataContract.AchievementEntry.TABLE_NAME, whereClause, whereArgs);
            case ACHIEVEMENTS_ID:
                whereClause = DataContract.AchievementEntry._ID + "=?";
                whereArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return database.delete(DataContract.AchievementEntry.TABLE_NAME, whereClause, whereArgs);

            case LOG:
                return database.delete(DataContract.LogEntry.TABLE_NAME, whereClause, whereArgs);
            case LOG_ID:
                whereClause = DataContract.LogEntry._ID + "=?";
                whereArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return database.delete(DataContract.LogEntry.TABLE_NAME, whereClause, whereArgs);
            default:
                throw new IllegalArgumentException("Unsupported URI \"" + uri + "\" for delete");
        }
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
}
