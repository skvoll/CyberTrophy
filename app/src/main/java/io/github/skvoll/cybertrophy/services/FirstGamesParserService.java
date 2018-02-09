package io.github.skvoll.cybertrophy.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import io.github.skvoll.cybertrophy.GamesParserTask;
import io.github.skvoll.cybertrophy.data.ProfileModel;
import io.github.skvoll.cybertrophy.notifications.GamesParserCompleteNotification;
import io.github.skvoll.cybertrophy.notifications.GamesParserNotification;

public final class FirstGamesParserService extends Service {
    private static final String TAG = FirstGamesParserService.class.getSimpleName();
    public static boolean sIsRunning = false;
    private ServiceTask mServiceTask;
    private GamesParserNotification mNotification;

    @Override
    public void onCreate() {
        ProfileModel profileModel = ProfileModel.getActive(getContentResolver());

        if (profileModel == null || profileModel.isInitialized()) {
            return;
        }

        mServiceTask = new ServiceTask(this, profileModel, GamesParserTask.ACTION_FIRST);
        mNotification = new GamesParserNotification(this);

        Log.d(TAG, "Created.");
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

        startForeground(mNotification.getId(), mNotification.build());

        mServiceTask.execute();

        sIsRunning = true;

        Log.d(TAG, "Started.");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mServiceTask != null) {
            mServiceTask.cancel(true);
        }

        stopForeground(true);

        sIsRunning = false;

        Log.d(TAG, "Destroyed.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static class ServiceTask extends GamesParserTask {
        private FirstGamesParserService mService;
        private GamesParserNotification mNotification;
        private ProfileModel mProfileModel;

        ServiceTask(FirstGamesParserService service, ProfileModel profileModel, int action) {
            super(service, profileModel, action);

            mService = service;
            mNotification = new GamesParserNotification(mService);
            mProfileModel = profileModel;
        }

        @Override
        protected void onProgressUpdate(ProgressParams... values) {
            ProgressParams progressParams = values[0];

            mService.startForeground(mNotification.getId(), mNotification.setProgress(progressParams.getMax(),
                    progressParams.getMin(), progressParams.getSteamGame()).build());
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mProfileModel.setInitialized(true);
                mProfileModel.save(mService.getContentResolver());

                (new GamesParserCompleteNotification(mService)).show();
            }

            mService.stopSelf();
        }
    }
}
