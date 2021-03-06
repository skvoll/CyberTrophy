package app.cybertrophy.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

@Deprecated
public final class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "database.db";
    // TODO: set to 1
    private static final Integer DATABASE_VERSION = 27;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DataContract.ProfileEntry.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(DataContract.GameEntry.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(DataContract.AchievementEntry.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(DataContract.NotificationEntry.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(DataContract.ProfileEntry.SQL_DROP_TABLE);
        sqLiteDatabase.execSQL(DataContract.GameEntry.SQL_DROP_TABLE);
        sqLiteDatabase.execSQL(DataContract.AchievementEntry.SQL_DROP_TABLE);
        sqLiteDatabase.execSQL(DataContract.NotificationEntry.SQL_DROP_TABLE);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(DataContract.ProfileEntry.SQL_DROP_TABLE);
        sqLiteDatabase.execSQL(DataContract.GameEntry.SQL_DROP_TABLE);
        sqLiteDatabase.execSQL(DataContract.AchievementEntry.SQL_DROP_TABLE);
        sqLiteDatabase.execSQL(DataContract.NotificationEntry.SQL_DROP_TABLE);
        onCreate(sqLiteDatabase);
    }
}
