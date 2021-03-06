package app.cybertrophy.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.util.Patterns;

import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.cybertrophy.VolleySingleton;
import app.cybertrophy.data.DataContract.ProfileEntry;
import app.cybertrophy.steam.SteamProfile;

@Deprecated
public final class ProfileModel extends Model<ProfileModel> {
    private static final String TAG = ProfileModel.class.getSimpleName();

    private static final String sBackgroundImagePattern = "url\\(\\s'(.*)'\\s\\);";

    private Long mId;
    private final Long mSteamId;
    private String mUrl;
    private String mName;
    private String mRealName;
    private String mAvatar;
    private String mAvatarMedium;
    private String mAvatarFull;
    private String mLocCountryCode;
    private String mBackgroundImage;
    private Integer mIsInitialized;
    private final Integer mIsActive;

    public ProfileModel(Cursor cursor) {
        mId = cursor.getLong(cursor.getColumnIndex(ProfileEntry._ID));

        mSteamId = cursor.getLong(cursor.getColumnIndex(ProfileEntry.COLUMN_STEAM_ID));
        mUrl = cursor.getString(cursor.getColumnIndex(ProfileEntry.COLUMN_URL));
        mName = cursor.getString(cursor.getColumnIndex(ProfileEntry.COLUMN_NAME));
        mRealName = cursor.getString(cursor.getColumnIndex(ProfileEntry.COLUMN_REAL_NAME));
        mAvatar = cursor.getString(cursor.getColumnIndex(ProfileEntry.COLUMN_AVATAR));
        mAvatarMedium = cursor.getString(cursor.getColumnIndex(ProfileEntry.COLUMN_AVATAR_MEDIUM));
        mAvatarFull = cursor.getString(cursor.getColumnIndex(ProfileEntry.COLUMN_AVATAR_FULL));
        mLocCountryCode = cursor.getString(cursor.getColumnIndex(ProfileEntry.COLUMN_LOC_COUNTRY_CODE));
        mBackgroundImage = cursor.getString(cursor.getColumnIndex(ProfileEntry.COLUMN_BACKGROUND_IMAGE));
        mIsInitialized = cursor.getInt(cursor.getColumnIndex(ProfileEntry.COLUMN_IS_INITIALIZED));
        mIsActive = cursor.getInt(cursor.getColumnIndex(ProfileEntry.COLUMN_IS_ACTIVE));
    }

    public ProfileModel(SteamProfile steamProfile) {
        mSteamId = steamProfile.steamId;
        mUrl = steamProfile.profileUrl;
        mName = steamProfile.personaName;
        mRealName = steamProfile.realName;
        mAvatar = steamProfile.avatar;
        mAvatarMedium = steamProfile.avatarMedium;
        mAvatarFull = steamProfile.avatarFull;
        mLocCountryCode = steamProfile.loccountrycode;
        mBackgroundImage = null;
        mIsInitialized = 0;
        mIsActive = 1;
    }

    public static ProfileModel getById(ContentResolver contentResolver, Long id) {
        String selection = ProfileEntry._ID + " = ?";
        String[] selectionArgs = new String[]{id.toString()};

        Cursor cursor = contentResolver.query(ProfileEntry.URI, null,
                selection, selectionArgs, null);

        if (cursor == null) {
            return null;
        }

        if (!cursor.moveToFirst()) {
            cursor.close();

            return null;
        }

        ProfileModel profileModel = new ProfileModel(cursor);

        cursor.close();

        return profileModel;
    }

    public static ProfileModel getActive(ContentResolver contentResolver) {
        String selection = ProfileEntry.COLUMN_IS_ACTIVE + " = ?";
        String[] selectionArgs = new String[]{"1"};

        Cursor cursor = contentResolver.query(ProfileEntry.URI, null,
                selection, selectionArgs, null);

        if (cursor == null) {
            return null;
        }

        if (!cursor.moveToFirst()) {
            cursor.close();

            return null;
        }

        ProfileModel profileModel = new ProfileModel(cursor);

        cursor.close();

        return profileModel;
    }

    @Override
    public Uri getUri(Long id) {
        if (id == null) {
            return ProfileEntry.URI;
        }

        return ContentUris.withAppendedId(ProfileEntry.URI, id);
    }

    @Override
    public Long getId() {
        return mId;
    }

    @Override
    ProfileModel setId(Long id) {
        mId = id;

        return this;
    }

    public Long getSteamId() {
        return mSteamId;
    }

    public String getUrl() {
        return mUrl;
    }

    public ProfileModel setUrl(String url) {
        mUrl = url;

        return this;
    }

    public String getName() {
        return mName;
    }

    public ProfileModel setName(String name) {
        mName = name;

        return this;
    }

    public String getRealName() {
        return mRealName;
    }

    public ProfileModel setRealName(String realName) {
        mRealName = realName;

        return this;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public ProfileModel setAvatar(String avatar) {
        mAvatar = avatar;

        return this;
    }

    public String getAvatarMedium() {
        return mAvatarMedium;
    }

    public ProfileModel setAvatarMedium(String avatarMedium) {
        mAvatarMedium = avatarMedium;

        return this;
    }

    public String getAvatarFull() {
        return mAvatarFull;
    }

    public ProfileModel setAvatarFull(String avatarFull) {
        mAvatarFull = avatarFull;

        return this;
    }

    public String getLocCountryCode() {
        return mLocCountryCode;
    }

    public ProfileModel setLocCountryCode(String locCountryCode) {
        mLocCountryCode = locCountryCode;

        return this;
    }

    public String getBackgroundImage() {
        return mBackgroundImage;
    }

    public Boolean isInitialized() {
        return mIsInitialized == 1;
    }

    public ProfileModel setInitialized(boolean isInitialized) {
        mIsInitialized = isInitialized ? 1 : 0;

        return this;
    }

    public Boolean isActive() {
        return mIsActive == 1;
    }

    public void loadBackgroundImage(VolleySingleton volleySingleton) {
        RequestFuture<String> requestFuture = RequestFuture.newFuture();
        StringRequest stringRequest = new StringRequest(mUrl, requestFuture, requestFuture);
        volleySingleton.addToRequestQueue(stringRequest);
        String response;

        try {
            response = requestFuture.get();

            Pattern pattern = Pattern.compile(ProfileModel.sBackgroundImagePattern);
            Matcher matcher = pattern.matcher(response);
            if (matcher.find()) {
                response = matcher.group(1);
            }

            if (Patterns.WEB_URL.matcher(response).matches()) {
                mBackgroundImage = response;
            }
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "Error loading profile background image", e);
        }
    }

    public void loadBackgroundImage(Context context) {
        loadBackgroundImage(VolleySingleton.getInstance(context));
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(ProfileEntry.COLUMN_STEAM_ID, mSteamId);
        contentValues.put(ProfileEntry.COLUMN_URL, mUrl);
        contentValues.put(ProfileEntry.COLUMN_NAME, mName);
        contentValues.put(ProfileEntry.COLUMN_REAL_NAME, mRealName);
        contentValues.put(ProfileEntry.COLUMN_AVATAR, mAvatar);
        contentValues.put(ProfileEntry.COLUMN_AVATAR_MEDIUM, mAvatarMedium);
        contentValues.put(ProfileEntry.COLUMN_AVATAR_FULL, mAvatarFull);
        contentValues.put(ProfileEntry.COLUMN_LOC_COUNTRY_CODE, mLocCountryCode);
        contentValues.put(ProfileEntry.COLUMN_BACKGROUND_IMAGE, mBackgroundImage);
        contentValues.put(ProfileEntry.COLUMN_IS_INITIALIZED, mIsInitialized);
        contentValues.put(ProfileEntry.COLUMN_IS_ACTIVE, mIsActive);

        return contentValues;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s: %s(%d)",
                this.getClass().getSimpleName(), getName(), getId());
    }
}
