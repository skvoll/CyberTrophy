package io.github.skvoll.cybertrophy.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.WeakReference;

import io.github.skvoll.cybertrophy.GamesParserTask;
import io.github.skvoll.cybertrophy.data.NotificationModel;
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

        mServiceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
        private WeakReference<FirstGamesParserService> mServiceWeakReference;
        private ProfileModel mProfileModel;

        ServiceTask(FirstGamesParserService service, ProfileModel profileModel, int action) {
            super(service, profileModel, action);

            mServiceWeakReference = new WeakReference<>(service);
            mProfileModel = profileModel;
        }

        @Override
        protected void onProgressUpdate(ProgressParams... values) {
            ProgressParams progressParams = values[0];

            FirstGamesParserService service = mServiceWeakReference.get();

            if (service == null) {
                return;
            }

            GamesParserNotification notification = new GamesParserNotification(service);

            service.startForeground(notification.getId(), notification.setProgress(progressParams.getMax(),
                    progressParams.getMin(), progressParams.getSteamGame()).build());
        }

        @Override
        protected void onPostExecute(Boolean success) {
            FirstGamesParserService service = mServiceWeakReference.get();

            if (service == null) {
                return;
            }

            NotificationModel.debug(TAG, success ? "Successfully done." : "Failed.").save(service.getContentResolver());

            if (success) {
                mProfileModel.setInitialized(true);
                mProfileModel.save(service.getContentResolver());

                (new GamesParserCompleteNotification(service)).show();
            }

            service.stopSelf();
        }
    }
}
