package app.cybertrophy.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import app.cybertrophy.data.DataContract.NotificationEntry;

@Deprecated
public final class NotificationModel extends Model<NotificationModel> {
    public static final int TYPE_CATEGORY_SEPARATOR = 1;
    public static final int TYPE_DEBUG = 1000;
    public static final int TYPE_MESSAGE = 1001;
    public static final int TYPE_NEW_GAME = 3001;
    public static final int TYPE_GAME_REMOVED = 3002;
    public static final int TYPE_NEW_ACHIEVEMENT = 3011;
    public static final int TYPE_ACHIEVEMENT_REMOVED = 3012;
    public static final int TYPE_ACHIEVEMENT_UNLOCKED = 3021;
    public static final int TYPE_GAME_COMPLETE = 3022;
    public static final int TYPE_NEWS = 4001;

    private Long mId;
    private Long mTime;
    private Integer mType;
    private Integer mIsViewed;
    private String mTitle;
    private String mMessage;
    private String mImageUrl;
    private Long mProfileId;
    private Long mObjectId;
    private Integer mObjectsCount;

    public NotificationModel(int type) {
        mTime = System.currentTimeMillis() / 1000;
        mType = type;
        mIsViewed = 0;
    }

    public NotificationModel(Cursor cursor) {
        mId = cursor.getLong(cursor.getColumnIndex(NotificationEntry._ID));

        mTime = cursor.getLong(cursor.getColumnIndex(NotificationEntry.COLUMN_TIME));
        mType = cursor.getInt(cursor.getColumnIndex(NotificationEntry.COLUMN_TYPE));
        mIsViewed = cursor.getInt(cursor.getColumnIndex(NotificationEntry.COLUMN_IS_VIEWED));
        mTitle = cursor.getString(cursor.getColumnIndex(NotificationEntry.COLUMN_TITLE));
        mMessage = cursor.getString(cursor.getColumnIndex(NotificationEntry.COLUMN_MESSAGE));
        mImageUrl = cursor.getString(cursor.getColumnIndex(NotificationEntry.COLUMN_IMAGE_URL));
        mProfileId = cursor.getLong(cursor.getColumnIndex(NotificationEntry.COLUMN_PROFILE_ID));
        mObjectId = cursor.getLong(cursor.getColumnIndex(NotificationEntry.COLUMN_OBJECT_ID));
        mObjectsCount = cursor.getInt(cursor.getColumnIndex(NotificationEntry.COLUMN_OBJECTS_COUNT));
    }

    @NonNull
    public static NotificationModel separator(String title) {
        NotificationModel notificationModel = new NotificationModel(TYPE_CATEGORY_SEPARATOR);

        notificationModel.setTitle(title);

        return notificationModel;
    }

    @NonNull
    public static NotificationModel debug(String title, String message) {
        NotificationModel notificationModel = new NotificationModel(TYPE_DEBUG);

        notificationModel.setTitle(title).setMessage(message);

        return notificationModel;
    }

    @NonNull
    public static NotificationModel message(String title, String message) {
        NotificationModel notificationModel = new NotificationModel(TYPE_MESSAGE);

        notificationModel.setTitle(title).setMessage(message);

        return notificationModel;
    }

    @NonNull
    public static NotificationModel newGame(GameModel gameModel) {
        NotificationModel notificationModel = new NotificationModel(TYPE_NEW_GAME);

        notificationModel.setTitle(gameModel.getName())
                .setImageUrl(gameModel.getIconUrl())
                .setProfileId(gameModel.getProfileId())
                .setObjectId(gameModel.getId())
                .setObjectsCount(1);

        return notificationModel;
    }

    @NonNull
    public static NotificationModel gameRemoved(GameModel gameModel) {
        NotificationModel notificationModel = new NotificationModel(TYPE_GAME_REMOVED);

        notificationModel.setTitle(gameModel.getName())
                .setImageUrl(gameModel.getIconUrl())
                .setProfileId(gameModel.getProfileId())
                .setObjectId(gameModel.getId())
                .setObjectsCount(1);

        return notificationModel;
    }

    @NonNull
    public static NotificationModel newAchievement(GameModel gameModel) {
        NotificationModel notificationModel = new NotificationModel(TYPE_NEW_ACHIEVEMENT);

        notificationModel.setTitle(gameModel.getName())
                .setImageUrl(gameModel.getIconUrl())
                .setProfileId(gameModel.getProfileId())
                .setObjectId(gameModel.getId())
                .setObjectsCount(1);

        return notificationModel;
    }

    @NonNull
    public static NotificationModel achievementRemoved(GameModel gameModel) {
        NotificationModel notificationModel = new NotificationModel(TYPE_ACHIEVEMENT_REMOVED);

        notificationModel.setTitle(gameModel.getName())
                .setImageUrl(gameModel.getIconUrl())
                .setProfileId(gameModel.getProfileId())
                .setObjectId(gameModel.getId())
                .setObjectsCount(1);

        return notificationModel;
    }

    @NonNull
    public static NotificationModel achievementUnlocked(AchievementModel achievementModel) {
        NotificationModel notificationModel = new NotificationModel(TYPE_ACHIEVEMENT_UNLOCKED);

        notificationModel.setTitle(achievementModel.getName())
                .setImageUrl(achievementModel.getIconUrl())
                .setProfileId(achievementModel.getProfileId())
                .setObjectId(achievementModel.getId())
                .setObjectsCount(1);

        return notificationModel;
    }

    @NonNull
    public static NotificationModel gameComplete(GameModel gameModel) {
        NotificationModel notificationModel = new NotificationModel(TYPE_GAME_COMPLETE);

        notificationModel.setTitle(gameModel.getName())
                .setImageUrl(gameModel.getIconUrl())
                .setProfileId(gameModel.getProfileId())
                .setObjectId(gameModel.getId())
                .setObjectsCount(1);

        return notificationModel;
    }

    public static ArrayList<NotificationModel> getByProfile(
            ContentResolver contentResolver, ProfileModel profileModel, int count, boolean addSeparator) {
        String selection = NotificationEntry.COLUMN_PROFILE_ID + " = ?";
        selection += " OR " + NotificationEntry.COLUMN_PROFILE_ID + " = 0";
        selection += " OR " + NotificationEntry.COLUMN_PROFILE_ID + " IS NULL";
        String[] selectionArgs = new String[]{String.valueOf(profileModel.getId())};
        String sortOrder = NotificationEntry._ID + " DESC";

        sortOrder += " LIMIT " + count;

        Cursor cursor = contentResolver.query(NotificationEntry.URI, null,
                selection, selectionArgs, sortOrder);

        if (cursor == null) {
            return new ArrayList<>();
        }

        if (!cursor.moveToFirst()) {
            cursor.close();

            return new ArrayList<>();
        }

        ArrayList<NotificationModel> notificationModels = new ArrayList<>(cursor.getCount());
        String currentSeparator = null, separator;

        while (!cursor.isAfterLast()) {
            NotificationModel notificationModel = new NotificationModel(cursor);

            if (addSeparator) {
                separator = DateUtils.getRelativeTimeSpanString(
                        notificationModel.getTime() * 1000L).toString();

                if (currentSeparator != null && !Objects.equals(currentSeparator, separator)) {
                    Date date = new Date();
                    date.setTime(notificationModel.getTime() * 1000L);
                    DateFormat dateFormat = SimpleDateFormat.getDateInstance(
                            DateFormat.FULL, Resources.getSystem().getConfiguration().locale);

                    separator = dateFormat.format(date);
                }

                if (currentSeparator == null || !Objects.equals(currentSeparator, separator)) {
                    currentSeparator = separator;
                    notificationModels.add(separator(currentSeparator));
                }
            }

            notificationModels.add(notificationModel);

            cursor.moveToNext();
        }

        cursor.close();

        return notificationModels;
    }

    public static ArrayList<NotificationModel> getByProfile(
            ContentResolver contentResolver, ProfileModel profileModel) {
        return getByProfile(contentResolver, profileModel, Integer.MAX_VALUE, true);
    }

    public static Integer getUnviewedCountByProfile(
            ContentResolver contentResolver, ProfileModel profileModel) {
        String selection = "(" + NotificationEntry.COLUMN_PROFILE_ID + " = ?";
        selection += " OR " + NotificationEntry.COLUMN_PROFILE_ID + " = 0";
        selection += " OR " + NotificationEntry.COLUMN_PROFILE_ID + " IS NULL)";
        selection += " AND " + NotificationEntry.COLUMN_IS_VIEWED + " = 0";
        String[] selectionArgs = new String[]{String.valueOf(profileModel.getId())};

        Cursor cursor = contentResolver.query(NotificationEntry.URI, null,
                selection, selectionArgs, null);

        if (cursor == null) {
            return 0;
        }

        if (!cursor.moveToFirst()) {
            cursor.close();

            return 0;
        }

        Integer count = cursor.getCount();

        cursor.close();

        return count;
    }

    @Override
    public Uri getUri(Long id) {
        if (id == null) {
            return NotificationEntry.URI;
        }

        return ContentUris.withAppendedId(NotificationEntry.URI, id);
    }

    @Override
    public Long getId() {
        return mId;
    }

    @Override
    public NotificationModel setId(Long id) {
        mId = id;

        return this;
    }

    public Long getTime() {
        return mTime;
    }

    public NotificationModel setTime(Long time) {
        mTime = time;

        return this;
    }

    public Integer getType() {
        return mType;
    }

    public NotificationModel setType(Integer type) {
        mType = type;

        return this;
    }

    public boolean isViewed() {
        return mIsViewed == 1;
    }

    public NotificationModel setViewed(boolean viewed) {
        mIsViewed = viewed ? 1 : 0;

        return this;
    }

    public String getTitle() {
        return mTitle;
    }

    public NotificationModel setTitle(String title) {
        mTitle = title;

        return this;
    }

    public String getMessage() {
        return mMessage;
    }

    public NotificationModel setMessage(String message) {
        mMessage = message;

        return this;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public NotificationModel setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;

        return this;
    }

    public Long getProfileId() {
        return mProfileId;
    }

    public NotificationModel setProfileId(Long profileId) {
        mProfileId = profileId;

        return this;
    }

    public Long getObjectId() {
        return mObjectId;
    }

    public NotificationModel setObjectId(Long objectId) {
        mObjectId = objectId;

        return this;
    }

    public Integer getObjectsCount() {
        return mObjectsCount;
    }

    public NotificationModel setObjectsCount(Integer objectsCount) {
        mObjectsCount = objectsCount;

        return this;
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(NotificationEntry.COLUMN_TIME, mTime);
        contentValues.put(NotificationEntry.COLUMN_TYPE, mType);
        contentValues.put(NotificationEntry.COLUMN_IS_VIEWED, mIsViewed);
        contentValues.put(NotificationEntry.COLUMN_TITLE, mTitle);
        contentValues.put(NotificationEntry.COLUMN_MESSAGE, mMessage);
        contentValues.put(NotificationEntry.COLUMN_IMAGE_URL, mImageUrl);
        contentValues.put(NotificationEntry.COLUMN_PROFILE_ID, mProfileId);
        contentValues.put(NotificationEntry.COLUMN_OBJECT_ID, mObjectId);
        contentValues.put(NotificationEntry.COLUMN_OBJECTS_COUNT, mObjectsCount);

        return contentValues;
    }

    @Override
    public void save(ContentResolver contentResolver, ContentObserver contentObserver) {
        if (getType() == TYPE_CATEGORY_SEPARATOR) {
            return;
        }

        super.save(contentResolver, contentObserver);
    }
}
