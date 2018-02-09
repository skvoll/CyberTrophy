package io.github.skvoll.cybertrophy.notifications;

import android.content.Context;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.GameModel;

public final class NewAchievementNotification extends BaseNotification {
    public static final int ID = 1011;

    private ArrayList<String> mGames = new ArrayList<>();
    private int mAchievementsCount = 0;

    public NewAchievementNotification(Context context) {
        super(context);

        mBuilder.setContentTitle(mResources.getQuantityString(R.plurals.notification_new_achievements, 1))
                .setContentText(mResources.getString(R.string.empty));
    }

    public NewAchievementNotification addGame(GameModel gameModel) {
        if (mGames.indexOf(gameModel.getName()) < 0) {
            mGames.add(gameModel.getName());
        }
        mAchievementsCount++;

        mBuilder.setContentTitle(mResources.getQuantityString(
                R.plurals.notification_new_achievements, mAchievementsCount));

        if (mGames.size() > 1) {
            mBuilder.setContentText(mResources.getString(
                    R.string.notification_new_achievements_in_games,
                    mAchievementsCount, mGames.size()));
        } else {
            mBuilder.setContentText(mResources.getQuantityString(
                    R.plurals.notification_new_achievements_in_game,
                    mAchievementsCount, mAchievementsCount, mGames.get(0)));
        }

        return this;
    }

    @Override
    public int getId() {
        return ID;
    }
}
