package io.github.skvoll.cybertrophy.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import java.util.ArrayList;
import java.util.Objects;

import io.github.skvoll.cybertrophy.data.DataContract.NotificationEntry;

public final class NotificationModel extends Model {
    public static final int TYPE_CATEGORY_SEPARATOR = 1;
    public static final int TYPE_DEBUG = 1000;
    public static final int TYPE_MESSAGE = 1001;
    public static final int TYPE_NEW_GAME = 2001;
    public static final int TYPE_GAME_REMOVED = 2002;
    public static final int TYPE_NEW_ACHIEVEMENT = 2003;
    public static final int TYPE_ACHIEVEMENT_REMOVED = 2004;
    public static final int TYPE_ACHIEVEMENT_UNLOCKED = 2011;
    public static final int TYPE_GAME_COMPLETE = 2012;
    public static final int TYPE_NEWS = 3001;

    private Long mId;
    private Long mTime;
    private Integer mType;
    private Integer mIsViewed;
    private String mTitle;
    private String mMessage;
    private String mImageUrl;
    private Long mProfileId;
    private Long mObjectId;

    private NotificationModel(int type) {
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

        notificationModel.setTitle(title);
        notificationModel.setMessage(message);

        return notificationModel;
    }

    @NonNull
    public static NotificationModel message(String title, String message) {
        NotificationModel notificationModel = new NotificationModel(TYPE_MESSAGE);

        notificationModel.setTitle(title);
        notificationModel.setMessage(message);

        return notificationModel;
    }

    @NonNull
    public static NotificationModel newGame(GameModel gameModel) {
        NotificationModel notificationModel = new NotificationModel(TYPE_NEW_GAME);

        notificationModel.setTitle(gameModel.getName());
        notificationModel.setImageUrl(gameModel.getIconUrl());
        notificationModel.setProfileId(gameModel.getProfileId());
        notificationModel.setObjectId(gameModel.getId());

        return notificationModel;
    }

    @NonNull
    public static NotificationModel gameRemoved(GameModel gameModel) {
        NotificationModel notificationModel = new NotificationModel(TYPE_GAME_REMOVED);

        notificationModel.setTitle(gameModel.getName());
        notificationModel.setImageUrl(gameModel.getIconUrl());
        notificationModel.setProfileId(gameModel.getProfileId());
        notificationModel.setObjectId(gameModel.getId());

        return notificationModel;
    }

    @NonNull
    public static NotificationModel newAchievement(GameModel gameModel) {
        NotificationModel notificationModel = new NotificationModel(TYPE_NEW_ACHIEVEMENT);

        notificationModel.setTitle(gameModel.getName());
        notificationModel.setImageUrl(gameModel.getIconUrl());
        notificationModel.setProfileId(gameModel.getProfileId());
        notificationModel.setObjectId(gameModel.getId());

        return notificationModel;
    }

    @NonNull
    public static NotificationModel achievementRemoved(GameModel gameModel) {
        NotificationModel notificationModel = new NotificationModel(TYPE_ACHIEVEMENT_REMOVED);

        notificationModel.setTitle(gameModel.getName());
        notificationModel.setImageUrl(gameModel.getIconUrl());
        notificationModel.setProfileId(gameModel.getProfileId());
        notificationModel.setObjectId(gameModel.getId());

        return notificationModel;
    }

    @NonNull
    public static NotificationModel achievementUnlocked(AchievementModel achievementModel) {
        NotificationModel notificationModel = new NotificationModel(TYPE_ACHIEVEMENT_UNLOCKED);

        notificationModel.setTitle(achievementModel.getName());
        notificationModel.setImageUrl(achievementModel.getIconUrl());
        notificationModel.setProfileId(achievementModel.getProfileId());
        notificationModel.setObjectId(achievementModel.getId());

        return notificationModel;
    }

    @NonNull
    public static NotificationModel gameComplete(GameModel gameModel) {
        NotificationModel notificationModel = new NotificationModel(TYPE_GAME_COMPLETE);

        notificationModel.setTitle(gameModel.getName());
        notificationModel.setImageUrl(gameModel.getIconUrl());
        notificationModel.setProfileId(gameModel.getProfileId());
        notificationModel.setObjectId(gameModel.getId());

        return notificationModel;
    }

    public static ArrayList<NotificationModel> getByProfile(
            ContentResolver contentResolver, ProfileModel profileModel, int count, boolean addSeparator) {

        String selection = NotificationEntry.COLUMN_PROFILE_ID + " = ?";
        selection += " OR " + NotificationEntry.COLUMN_PROFILE_ID + " IS NULL";
        String[] selectionArgs = new String[]{String.valueOf(profileModel.getId())};
        String sortOrder = NotificationEntry.COLUMN_TIME + " DESC";

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

    public boolean isViewed() {
        return mIsViewed == 1;
    }

    public void setViewed(boolean viewed) {
        mIsViewed = viewed ? 1 : 0;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public Long getProfileId() {
        return mProfileId;
    }

    public void setProfileId(Long profileId) {
        mProfileId = profileId;
    }

    public Long getObjectId() {
        return mObjectId;
    }

    public void setObjectId(Long objectId) {
        mObjectId = objectId;
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

        return contentValues;
    }
}
