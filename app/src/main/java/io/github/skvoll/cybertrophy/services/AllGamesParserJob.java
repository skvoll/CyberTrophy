package io.github.skvoll.cybertrophy.services;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.lang.ref.WeakReference;

import io.github.skvoll.cybertrophy.GamesParserTask;
import io.github.skvoll.cybertrophy.data.NotificationModel;
import io.github.skvoll.cybertrophy.data.ProfileModel;

public final class AllGamesParserJob extends JobService {
    public static final int ID = 1001;
    private static final String TAG = AllGamesParserJob.class.getSimpleName();
    private static final long RUN_PERIOD_MILLISECONDS = 60000 * 60 * 12;
    public static boolean sIsRunning = false;
    private GamesParserTask mJobTask;

    public static int setup(Context context) {
        JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(ID,
                new ComponentName(context, AllGamesParserJob.class))
                .setPersisted(true)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(true)
                .setOverrideDeadline(RUN_PERIOD_MILLISECONDS)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            jobInfoBuilder.setRequiresBatteryNotLow(true);
            jobInfoBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NOT_ROAMING);
        }

        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        if (scheduler == null) {
            return -1;
        }

        return scheduler.schedule(jobInfoBuilder.build());
    }

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        final ProfileModel profileModel = ProfileModel.getActive(getContentResolver());

        if (profileModel == null || !profileModel.isInitialized()) {
            return false;
        }

        mJobTask = new JobTask(this, jobParameters, profileModel);

        mJobTask.execute();

        sIsRunning = true;

        Log.d(TAG, "Started.");

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Boolean needsReschedule = false;

        if (mJobTask != null) {
            if (mJobTask.getStatus() != AsyncTask.Status.FINISHED) {
                needsReschedule = true;
            }

            mJobTask.cancel(true);
        }

        sIsRunning = false;

        Log.d(TAG, "Stopped.");

        return needsReschedule;
    }

    private static class JobTask extends GamesParserTask {
        private WeakReference<JobService> mJobServiceWeakReference;
        private JobParameters mJobParameters;

        JobTask(JobService service, JobParameters jobParameters, ProfileModel profileModel) {
            super(service, profileModel, GamesParserTask.ACTION_ALL);

            mJobServiceWeakReference = new WeakReference<>(service);
            mJobParameters = jobParameters;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            JobService service = mJobServiceWeakReference.get();

            if (service == null) {
                return;
            }

            sIsRunning = false;
            service.jobFinished(mJobParameters, !success);
        }
    }
}
