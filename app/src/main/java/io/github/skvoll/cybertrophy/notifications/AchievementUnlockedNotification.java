package io.github.skvoll.cybertrophy.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.GameActivity;
import io.github.skvoll.cybertrophy.MainActivity;
import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.AchievementModel;
import io.github.skvoll.cybertrophy.data.GameModel;
import io.github.skvoll.cybertrophy.data.NotificationModel;

public final class AchievementUnlockedNotification extends BaseNotification {
    public static final int ID = 3021;

    private final Context mContext;
    private final ArrayList<String> mGames = new ArrayList<>();
    private final ArrayList<String> mAchievements = new ArrayList<>();

    public AchievementUnlockedNotification(Context context) {
        super(context);

        mContext = context;

        getBuilder().setContentTitle(getResources().getQuantityString(R.plurals.notification_achievements_unlocked, 1))
                .setContentText(getResources().getString(R.string.empty));
    }

    public AchievementUnlockedNotification addAchievement(GameModel gameModel, AchievementModel achievementModel) {
        NotificationModel.achievementUnlocked(achievementModel).save(mContext.getContentResolver());

        if (mGames.indexOf(gameModel.getName()) < 0) {
            mGames.add(gameModel.getName());
        }
        mAchievements.add(achievementModel.getName());

        Intent intent;
        PendingIntent pendingIntent;

        if (mGames.size() > 1) {
            intent = new Intent(mContext, MainActivity.class);
            intent.putExtra(MainActivity.KEY_FRAGMENT, MainActivity.FRAGMENT_GAMES);
            pendingIntent = PendingIntent.getActivity(
                    mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            getBuilder().setContentTitle(getResources().getQuantityString(
                    R.plurals.notification_achievements_unlocked, mAchievements.size(), mAchievements.size()));
            getBuilder().setContentText(getResources().getString(
                    R.string.notification_achievements_unlocked_in_games,
                    mAchievements.size(), mGames.size()));
        } else {
            intent = new Intent(mContext, GameActivity.class);
            intent.putExtra(GameActivity.KEY_GAME_ID, gameModel.getId());
            pendingIntent = PendingIntent.getActivity(
                    mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (mAchievements.size() > 1) {
                getBuilder().setContentText(mGames.get(0));
                getBuilder().setContentTitle(getResources().getQuantityString(
                        R.plurals.notification_achievements_unlocked, mAchievements.size(), mAchievements.size()));
            } else {
                getBuilder().setContentText(mAchievements.get(0));
                getBuilder().setContentTitle(getResources().getQuantityString(
                        R.plurals.notification_achievements_unlocked, 1));
            }
        }

        getBuilder().setContentIntent(pendingIntent);

        return this;
    }

    @Override
    public int getId() {
        return ID;
    }
}
