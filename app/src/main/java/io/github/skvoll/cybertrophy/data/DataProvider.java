package io.github.skvoll.cybertrophy.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import static io.github.skvoll.cybertrophy.data.DataContract.AUTHORITY;
import static io.github.skvoll.cybertrophy.data.DataContract.AchievementEntry;
import static io.github.skvoll.cybertrophy.data.DataContract.GameEntry;
import static io.github.skvoll.cybertrophy.data.DataContract.NotificationEntry;
import static io.github.skvoll.cybertrophy.data.DataContract.ProfileEntry;

public final class DataProvider extends ContentProvider {
    private static final String TAG = DataProvider.class.getSimpleName();

    private static final int PROFILES = 100;
    private static final int PROFILES_ID = 101;
    private static final int GAMES = 200;
    private static final int GAMES_ID = 201;
    private static final int ACHIEVEMENTS = 300;
    private static final int ACHIEVEMENTS_ID = 301;

    private static final int NOTIFICATION = 1000;
    private static final int NOTIFICATION_ID = 1001;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, ProfileEntry.TABLE_NAME, PROFILES);
        sUriMatcher.addURI(AUTHORITY, ProfileEntry.TABLE_NAME + "/#", PROFILES_ID);
        sUriMatcher.addURI(AUTHORITY, GameEntry.TABLE_NAME, GAMES);
        sUriMatcher.addURI(AUTHORITY, GameEntry.TABLE_NAME + "/#", GAMES_ID);
        sUriMatcher.addURI(AUTHORITY, AchievementEntry.TABLE_NAME, ACHIEVEMENTS);
        sUriMatcher.addURI(AUTHORITY, AchievementEntry.TABLE_NAME + "/#", ACHIEVEMENTS_ID);

        sUriMatcher.addURI(AUTHORITY, NotificationEntry.TABLE_NAME, NOTIFICATION);
        sUriMatcher.addURI(AUTHORITY, NotificationEntry.TABLE_NAME + "/#", NOTIFICATION_ID);
    }

    private DatabaseHelper mDatabaseHelper;
    private ContentResolver mContentResolver;

    @Override
    public boolean onCreate() {
        if (getContext() == null) {
            return false;
        }

        mDatabaseHelper = new DatabaseHelper(getContext());
        mContentResolver = getContext().getContentResolver();

        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] columns, String selection, String[] selectionArgs, String orderBy) {
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        String tableName;

        switch (sUriMatcher.match(uri)) {
            case PROFILES:
                tableName = ProfileEntry.TABLE_NAME;
                break;
            case PROFILES_ID:
                tableName = ProfileEntry.TABLE_NAME;
                selection = ProfileEntry._ID + " = ?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                break;
            case GAMES:
                tableName = GameEntry.TABLE_NAME;
                break;
            case GAMES_ID:
                tableName = GameEntry.TABLE_NAME;
                selection = GameEntry._ID + " = ?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                break;
            case ACHIEVEMENTS:
                tableName = AchievementEntry.TABLE_NAME;
                break;
            case ACHIEVEMENTS_ID:
                tableName = AchievementEntry.TABLE_NAME;
                selection = AchievementEntry._ID + " = ?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                break;
            case NOTIFICATION:
                tableName = NotificationEntry.TABLE_NAME;
                break;
            case NOTIFICATION_ID:
                tableName = NotificationEntry.TABLE_NAME;
                selection = NotificationEntry._ID + " = ?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI \"" + uri + "\" for query.");
        }

        return database.query(tableName, columns, selection, selectionArgs,
                null, null, orderBy);
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        return insert(uri, contentValues, null);
    }

    public Uri insert(@NonNull Uri uri, ContentValues contentValues, ContentObserver contentObserver) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();

        String tableName;

        switch (sUriMatcher.match(uri)) {
            case PROFILES:
                tableName = ProfileEntry.TABLE_NAME;
                break;
            case GAMES:
                tableName = GameEntry.TABLE_NAME;
                break;
            case ACHIEVEMENTS:
                tableName = AchievementEntry.TABLE_NAME;
                break;

            case NOTIFICATION:
                tableName = NotificationEntry.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI \"" + uri + "\" for insert.");
        }

        long id = database.insert(tableName, null, contentValues);
        Uri resultUri = ContentUris.withAppendedId(uri, id);

        mContentResolver.notifyChange(resultUri, contentObserver);

        return resultUri;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String whereClause, String[] whereArgs) {
        return update(uri, contentValues, whereClause, whereArgs, null);
    }

    public int update(@NonNull Uri uri, ContentValues contentValues,
                      String whereClause, String[] whereArgs, ContentObserver contentObserver) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();

        String tableName;

        switch (sUriMatcher.match(uri)) {
            case PROFILES:
                tableName = ProfileEntry.TABLE_NAME;
                break;
            case PROFILES_ID:
                tableName = ProfileEntry.TABLE_NAME;
                whereClause = ProfileEntry._ID + " = ?";
                whereArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                break;
            case GAMES:
                tableName = GameEntry.TABLE_NAME;
                break;
            case GAMES_ID:
                tableName = GameEntry.TABLE_NAME;
                whereClause = GameEntry._ID + " = ?";
                whereArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                break;
            case ACHIEVEMENTS:
                tableName = AchievementEntry.TABLE_NAME;
                break;
            case ACHIEVEMENTS_ID:
                tableName = AchievementEntry.TABLE_NAME;
                whereClause = AchievementEntry._ID + " = ?";
                whereArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                break;

            case NOTIFICATION:
                tableName = NotificationEntry.TABLE_NAME;
                break;
            case NOTIFICATION_ID:
                tableName = NotificationEntry.TABLE_NAME;
                whereClause = NotificationEntry._ID + " = ?";
                whereArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI \"" + uri + "\" for update.");
        }

        mContentResolver.notifyChange(uri, contentObserver);

        return database.update(tableName, contentValues, whereClause, whereArgs);
    }

    @Override
    public int delete(@NonNull Uri uri, String whereClause, String[] whereArgs) {
        return delete(uri, whereClause, whereArgs, null);
    }

    public int delete(@NonNull Uri uri, String whereClause,
                      String[] whereArgs, ContentObserver contentObserver) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();

        String tableName;

        switch (sUriMatcher.match(uri)) {
            case PROFILES:
                tableName = ProfileEntry.TABLE_NAME;
                break;
            case PROFILES_ID:
                tableName = ProfileEntry.TABLE_NAME;
                whereClause = ProfileEntry._ID + " = ?";
                whereArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                break;
            case GAMES:
                tableName = GameEntry.TABLE_NAME;
                break;
            case GAMES_ID:
                tableName = GameEntry.TABLE_NAME;
                whereClause = GameEntry._ID + " = ?";
                whereArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                break;
            case ACHIEVEMENTS:
                tableName = AchievementEntry.TABLE_NAME;
                break;
            case ACHIEVEMENTS_ID:
                tableName = AchievementEntry.TABLE_NAME;
                whereClause = AchievementEntry._ID + " = ?";
                whereArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                break;

            case NOTIFICATION:
                tableName = NotificationEntry.TABLE_NAME;
                break;
            case NOTIFICATION_ID:
                tableName = NotificationEntry.TABLE_NAME;
                whereClause = NotificationEntry._ID + " = ?";
                whereArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI \"" + uri + "\" for delete.");
        }

        mContentResolver.notifyChange(uri, contentObserver);

        return database.delete(tableName, whereClause, whereArgs);
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
}
