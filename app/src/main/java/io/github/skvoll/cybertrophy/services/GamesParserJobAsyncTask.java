package io.github.skvoll.cybertrophy.services;

import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.net.Uri;

import java.lang.ref.WeakReference;

import io.github.skvoll.cybertrophy.GamesParserAsyncTask;
import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.GamesParser;
import io.github.skvoll.cybertrophy.data.ProfileModel;
import io.github.skvoll.cybertrophy.notifications.BaseNotification;
import io.github.skvoll.cybertrophy.notifications.GamesParserRetryNotification;

public final class GamesParserJobAsyncTask extends GamesParserAsyncTask {
    private final WeakReference<JobService> mServiceWeakReference;
    private final JobParameters mJobParameters;
    private final ProfileModel mProfileModel;

    GamesParserJobAsyncTask(JobService jobService, JobParameters jobParameters, ProfileModel profileModel) {
        super(jobService, profileModel);

        mServiceWeakReference = new WeakReference<>(jobService);
        mJobParameters = jobParameters;
        mProfileModel = profileModel;

        setProgressListener(new ProgressListener());
    }

    @Override
    protected void onPostExecute(Boolean success) {
        JobService service = mServiceWeakReference.get();

        if (service == null) {
            return;
        }

        service.jobFinished(mJobParameters, !success);
    }

    private class ProgressListener extends GamesParser.GamesParserProgressListener {
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
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                notification.getBuilder()
                        .setContentText("Profile is probably private");

                notification.getBuilder().addAction(
                        R.drawable.ic_settings_black_24dp,
                        "Check preferences",
                        pendingIntent);

                notification.show();
            }
        }
    }
}
