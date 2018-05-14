package app.cybertrophy.services;

import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.net.Uri;

import java.lang.ref.WeakReference;

import app.cybertrophy.GamesParserAsyncTask;
import app.cybertrophy.R;
import app.cybertrophy.data.AchievementModel;
import app.cybertrophy.data.GameModel;
import app.cybertrophy.data.GamesParser;
import app.cybertrophy.data.ProfileModel;
import app.cybertrophy.notifications.AchievementRemovedNotification;
import app.cybertrophy.notifications.AchievementUnlockedNotification;
import app.cybertrophy.notifications.BaseNotification;
import app.cybertrophy.notifications.GameCompleteNotification;
import app.cybertrophy.notifications.GameRemovedNotification;
import app.cybertrophy.notifications.GamesParserRetryNotification;
import app.cybertrophy.notifications.NewAchievementNotification;
import app.cybertrophy.notifications.NewGameNotification;

public final class GamesParserJobAsyncTask extends GamesParserAsyncTask {
    private final WeakReference<JobService> mServiceWeakReference;
    private final JobParameters mJobParameters;
    private final ProfileModel mProfileModel;

    private final NewGameNotification mNewGameNotification;
    private final GameRemovedNotification mGameRemovedNotification;
    private final NewAchievementNotification mNewAchievementNotification;
    private final AchievementRemovedNotification mAchievementRemovedNotification;
    private final AchievementUnlockedNotification mAchievementUnlockedNotification;
    private final GameCompleteNotification mGameCompleteNotification;

    GamesParserJobAsyncTask(JobService jobService, JobParameters jobParameters, ProfileModel profileModel) {
        super(jobService, profileModel);

        mServiceWeakReference = new WeakReference<>(jobService);
        mJobParameters = jobParameters;
        mProfileModel = profileModel;

        mNewGameNotification = new NewGameNotification(jobService);
        mGameRemovedNotification = new GameRemovedNotification(jobService);
        mNewAchievementNotification = new NewAchievementNotification(jobService);
        mAchievementRemovedNotification = new AchievementRemovedNotification(jobService);
        mAchievementUnlockedNotification = new AchievementUnlockedNotification(jobService);
        mGameCompleteNotification = new GameCompleteNotification(jobService);

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

        @Override
        public void onNewGame(GameModel gameModel) {
            mNewGameNotification.addGame(gameModel).show();
        }

        @Override
        public void onGameRemoved(GameModel gameModel) {
            mGameRemovedNotification.addGame(gameModel).show();
        }

        @Override
        public void onNewAchievement(GameModel gameModel, AchievementModel achievementModel) {
            mNewAchievementNotification.addGame(gameModel).show();
        }

        @Override
        public void onAchievementRemoved(GameModel gameModel, AchievementModel achievementModel) {
            mAchievementRemovedNotification.addGame(gameModel).show();
        }

        @Override
        public void onAchievementUnlocked(GameModel gameModel, AchievementModel achievementModel) {
            mAchievementUnlockedNotification.addAchievement(gameModel, achievementModel).show();
        }

        @Override
        public void onGameComplete(GameModel gameModel) {
            mGameCompleteNotification.addGame(gameModel).show();
        }
    }
}
