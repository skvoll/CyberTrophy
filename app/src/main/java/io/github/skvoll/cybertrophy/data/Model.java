package io.github.skvoll.cybertrophy.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;

abstract class Model<T> {
    public abstract Uri getUri(Long id);

    public abstract Long getId();

    abstract T setId(Long id);

    public abstract ContentValues toContentValues();

    public void save(ContentResolver contentResolver) {
        if (getId() == null) {
            Uri uri = contentResolver.insert(getUri(null), toContentValues());

            setId(ContentUris.parseId(uri));
        } else {
            contentResolver.update(getUri(getId()), toContentValues(), null, null);
        }
    }

    public void delete(ContentResolver contentResolver) {
        contentResolver.delete(getUri(getId()), null, null);

        setId(null);
    }
}
