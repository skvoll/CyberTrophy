package io.github.skvoll.cybertrophy.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.GameActivity;
import io.github.skvoll.cybertrophy.MainActivity;
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

        Intent intent;
        PendingIntent pendingIntent;

        if (mGames.size() > 1) {
            intent = new Intent(mContext, MainActivity.class);
            intent.putExtra(MainActivity.KEY_FRAGMENT, MainActivity.FRAGMENT_GAMES);
            pendingIntent = PendingIntent.getActivity(
                    mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentText(mResources.getString(
                    R.string.notification_achievements_removed_from_games,
                    mAchievementsCount, mGames.size()));
        } else {
            intent = new Intent(mContext, GameActivity.class);
            intent.putExtra(GameActivity.KEY_GAME_ID, gameModel.getId());
            pendingIntent = PendingIntent.getActivity(
                    mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentText(mResources.getQuantityString(
                    R.plurals.notification_achievements_removed_from_game,
                    mAchievementsCount, mAchievementsCount, mGames.get(0)));
        }

        mBuilder.setContentIntent(pendingIntent);

        return this;
    }

    @Override
    public int getId() {
        return ID;
    }
}
