package io.github.skvoll.cybertrophy.data;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;

abstract class Model<T> {
    public abstract Uri getUri(Long id);

    public abstract Long getId();

    abstract T setId(Long id);

    public abstract ContentValues toContentValues();

    public void save(ContentResolver contentResolver, ContentObserver contentObserver) {
        Uri uri = getUri(getId());
        DataProvider dataProvider = getDataProvider(contentResolver, uri);

        if (dataProvider == null) {
            return;
        }

        if (getId() == null) {
            Uri resultUri = dataProvider.insert(uri, toContentValues(), contentObserver);

            setId(ContentUris.parseId(resultUri));
        } else {
            dataProvider.update(uri, toContentValues(), null, null, contentObserver);
        }
    }

    public void save(ContentResolver contentResolver) {
        save(contentResolver, null);
    }

    public void delete(ContentResolver contentResolver, ContentObserver contentObserver) {
        Uri uri = getUri(getId());
        DataProvider dataProvider = getDataProvider(contentResolver, uri);

        if (dataProvider == null) {
            return;
        }

        dataProvider.delete(uri, null, null, contentObserver);

        setId(null);
    }

    public void delete(ContentResolver contentResolver) {
        delete(contentResolver, null);
    }

    private DataProvider getDataProvider(ContentResolver contentResolver, Uri uri) {
        if (contentResolver == null) {
            return null;
        }

        ContentProviderClient contentProviderClient = contentResolver.acquireContentProviderClient(uri);

        if (contentProviderClient == null) {
            return null;
        }

        DataProvider dataProvider = (DataProvider) contentProviderClient.getLocalContentProvider();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            contentProviderClient.close();
        } else {
            contentProviderClient.release();
        }

        if (dataProvider == null) {
            return null;
        }

        return dataProvider;
    }
}
