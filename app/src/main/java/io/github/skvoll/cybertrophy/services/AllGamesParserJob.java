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

import io.github.skvoll.cybertrophy.data.GamesParser;
import io.github.skvoll.cybertrophy.data.ProfileModel;

public final class AllGamesParserJob extends JobService {
    public static final int ID = 2001;

    private static final String TAG = AllGamesParserJob.class.getSimpleName();
    private static final long RUN_PERIOD_MILLISECONDS = 60000 * 60 * 12; // 12 hours

    private GamesParserJobAsyncTask mJobAsyncTask;

    public static int setup(Context context) {
        JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(ID,
                new ComponentName(context, AllGamesParserJob.class))
                .setPersisted(true)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setOverrideDeadline(RUN_PERIOD_MILLISECONDS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            jobInfoBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NOT_ROAMING);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            jobInfoBuilder.setRequiresBatteryNotLow(true);
        }

        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        if (scheduler == null) {
            return -1;
        }

        return scheduler.schedule(jobInfoBuilder.build());
    }

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        ProfileModel profileModel = ProfileModel.getActive(getContentResolver());

        if (profileModel == null || !profileModel.isInitialized()) {
            return false;
        }

        mJobAsyncTask = new GamesParserJobAsyncTask(this, jobParameters, profileModel);

        mJobAsyncTask.execute(GamesParser.Action.ALL);

        Log.d(TAG, "Started.");

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Boolean needsReschedule = false;

        if (mJobAsyncTask != null) {
            if (mJobAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
                needsReschedule = true;
            }

            mJobAsyncTask.cancel();

            Log.d(TAG, "Async task canceled.");
        }

        Log.d(TAG, "Stopped.");

        return needsReschedule;
    }
}
