package io.github.skvoll.cybertrophy.notifications;

import android.content.Context;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.AchievementModel;
import io.github.skvoll.cybertrophy.data.GameModel;

public final class AchievementUnlockedNotification extends BaseNotification {
    public static final int ID = 1021;

    private ArrayList<String> mGames = new ArrayList<>();
    private ArrayList<String> mAchievements = new ArrayList<>();

    public AchievementUnlockedNotification(Context context) {
        super(context);

        mBuilder.setContentTitle(mResources.getQuantityString(R.plurals.notification_achievements_unlocked, 1))
                .setContentText(mResources.getString(R.string.empty));
    }

    public AchievementUnlockedNotification addAchievement(GameModel gameModel, AchievementModel achievementModel) {
        if (mGames.indexOf(gameModel.getName()) < 0) {
            mGames.add(gameModel.getName());
        }
        mAchievements.add(achievementModel.getName());

        if (mGames.size() > 1) {
            mBuilder.setContentTitle(mResources.getQuantityString(
                    R.plurals.notification_achievements_unlocked, mAchievements.size(), mAchievements.size()));
            mBuilder.setContentText(mResources.getString(
                    R.string.notification_achievements_unlocked_in_games,
                    mAchievements.size(), mGames.size()));
        } else {
            if (mAchievements.size() > 1) {
                mBuilder.setContentText(mGames.get(0));
                mBuilder.setContentTitle(mResources.getQuantityString(
                        R.plurals.notification_achievements_unlocked, mAchievements.size(), mAchievements.size()));
            } else {
                mBuilder.setContentText(mAchievements.get(0));
                mBuilder.setContentTitle(mResources.getQuantityString(
                        R.plurals.notification_achievements_unlocked, 1));
            }
        }

        return this;
    }

    @Override
    public int getId() {
        return ID;
    }
}
