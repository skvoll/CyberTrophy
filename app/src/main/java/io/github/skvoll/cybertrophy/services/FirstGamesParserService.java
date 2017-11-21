package io.github.skvoll.cybertrophy.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import io.github.skvoll.cybertrophy.GamesParserTask;
import io.github.skvoll.cybertrophy.data.ProfileModel;

public final class FirstGamesParserService extends Service {
    public static boolean sIsRunning = false;
    private static final String TAG = FirstGamesParserService.class.getSimpleName();
    private ServiceTask mServiceTask;

    @Override
    public void onCreate() {
        ProfileModel profileModel = ProfileModel.getActive(getContentResolver());

        if (profileModel == null || profileModel.isInitialized()) {
            return;
        }

        mServiceTask = new ServiceTask(this, profileModel, GamesParserTask.ACTION_FIRST);

        Log.d(TAG, "created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mServiceTask == null) {
            return START_NOT_STICKY;
        }

        if (mServiceTask.getStatus() == AsyncTask.Status.RUNNING
                || mServiceTask.getStatus() == AsyncTask.Status.FINISHED) {
            return START_STICKY;
        }

        mServiceTask.execute();

        sIsRunning = true;

        Log.d(TAG, "started");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mServiceTask != null) {
            mServiceTask.cancel(true);
        }

        sIsRunning = false;

        Log.d(TAG, "destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static class ServiceTask extends GamesParserTask {
        private Service mService;
        private ProfileModel mProfileModel;

        ServiceTask(Service service, ProfileModel profileModel, int action) {
            super(service, profileModel, action);

            mService = service;
            mProfileModel = profileModel;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mProfileModel.setInitialized(true);
                mProfileModel.save(mService.getContentResolver());
            }

            mService.stopSelf();
        }
    }
}
