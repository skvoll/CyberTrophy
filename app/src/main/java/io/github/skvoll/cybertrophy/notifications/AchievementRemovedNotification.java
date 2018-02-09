package io.github.skvoll.cybertrophy.notifications;

import android.content.Context;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.GameModel;

public final class AchievementRemovedNotification extends BaseNotification {
    public static final int ID = 1012;

    private ArrayList<String> mGames = new ArrayList<>();
    private int mAchievementsCount = 0;

    public AchievementRemovedNotification(Context context) {
        super(context);

        mBuilder.setContentTitle(mResources.getQuantityString(R.plurals.notification_achievements_removed, 1))
                .setContentText(mResources.getString(R.string.empty));
    }

    public AchievementRemovedNotification addGame(GameModel gameModel) {
        if (mGames.indexOf(gameModel.getName()) < 0) {
            mGames.add(gameModel.getName());
        }
        mAchievementsCount++;

        mBuilder.setContentTitle(mResources.getQuantityString(
                R.plurals.notification_achievements_removed, mAchievementsCount));

        if (mGames.size() > 1) {
            mBuilder.setContentText(mResources.getString(
                    R.string.notification_achievements_removed_from_games,
                    mAchievementsCount, mGames.size()));
        } else {
            mBuilder.setContentText(mResources.getQuantityString(
                    R.plurals.notification_achievements_removed_from_game,
                    mAchievementsCount, mAchievementsCount, mGames.get(0)));
        }

        return this;
    }

    @Override
    public int getId() {
        return ID;
    }
}
