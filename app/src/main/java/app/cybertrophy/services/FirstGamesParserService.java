package app.cybertrophy.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.WeakReference;

import app.cybertrophy.GamesParserAsyncTask;
import app.cybertrophy.R;
import app.cybertrophy.data.GamesParser;
import app.cybertrophy.data.ProfileModel;
import app.cybertrophy.notifications.BaseNotification;
import app.cybertrophy.notifications.GamesParserCompleteNotification;
import app.cybertrophy.notifications.GamesParserNotification;
import app.cybertrophy.notifications.GamesParserRetryNotification;
import app.cybertrophy.steam.SteamGame;

public final class FirstGamesParserService extends Service {
    private static final String TAG = FirstGamesParserService.class.getSimpleName();

    private ServiceAsyncTask mServiceAsyncTask;
    private GamesParserNotification mNotification;

    @Override
    public void onCreate() {
        ProfileModel profileModel = ProfileModel.getActive(getContentResolver());

        if (profileModel == null || profileModel.isInitialized()) {
            return;
        }

        mServiceAsyncTask = new ServiceAsyncTask(this, profileModel);
        mNotification = new GamesParserNotification(this);

        Log.d(TAG, "Created.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mServiceAsyncTask == null) {
            return START_NOT_STICKY;
        }

        if (mServiceAsyncTask.getStatus() == AsyncTask.Status.RUNNING
                || mServiceAsyncTask.getStatus() == AsyncTask.Status.FINISHED) {
            return START_STICKY;
        }

        startForeground(mNotification.getId(), mNotification.build());

        mServiceAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, GamesParser.Action.FIRST);

        Log.d(TAG, "Started.");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mServiceAsyncTask != null) {
            mServiceAsyncTask.cancel();
        }

        stopForeground(true);

        Log.d(TAG, "Destroyed.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static class ServiceAsyncTask extends GamesParserAsyncTask {
        private final WeakReference<Service> mServiceWeakReference;
        private final ProfileModel mProfileModel;

        ServiceAsyncTask(Service service, ProfileModel profileModel) {
            super(service, profileModel);

            mServiceWeakReference = new WeakReference<>(service);
            mProfileModel = profileModel;

            setProgressListener(new ProgressListener());
        }

        @Override
        protected void onPostExecute(Boolean success) {
            Service service = mServiceWeakReference.get();

            if (service == null) {
                return;
            }

            if (success) {
                mProfileModel.setInitialized(true);
                mProfileModel.save(service.getContentResolver());

                (new GamesParserCompleteNotification(service)).show();
            }

            service.stopSelf();
        }

        private class ProgressListener implements GamesParser.GamesParserProgressListener {
            @Override
            public void onError(GamesParser.GamesParserException e) {
                Service service = mServiceWeakReference.get();

                if (service == null) {
                    return;
                }

                BaseNotification notification;

                if (e instanceof GamesParser.ProfileIsPrivateException) {
                    notification = new GamesParserRetryNotification(service);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse(mProfileModel.getUrl() + "/edit/settings"));
                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    notification.getBuilder()
                            .setContentText("Profile is probably private");

                    notification.getBuilder().addAction(
                            R.drawable.ic_steam_black_24,
                            "Profile settings",
                            pendingIntent);

                    notification.show();

                    return;
                }

                if (e instanceof GamesParser.ParsingFailureException) {
                    notification = new GamesParserRetryNotification(service);
                    notification.show();
                }
            }

            @Override
            public void onProgress(int processed, int total, SteamGame steamGame) {
                Service service = mServiceWeakReference.get();

                if (service == null) {
                    return;
                }

                GamesParserNotification notification = new GamesParserNotification(service);
                notification.setProgress(processed, total, steamGame);

                service.startForeground(notification.getId(), notification.build());
            }
        }
    }
}
