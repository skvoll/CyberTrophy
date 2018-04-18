package io.github.skvoll.cybertrophy.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.LongSparseArray;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.GameActivity;
import io.github.skvoll.cybertrophy.MainActivity;
import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.GameModel;
import io.github.skvoll.cybertrophy.data.NotificationModel;

public final class NewAchievementNotification extends BaseNotification {
    public static final int ID = 3011;

    private Context mContext;
    private ArrayList<String> mGames = new ArrayList<>();
    private int mAchievementsCount = 0;
    private LongSparseArray<NotificationModel> mNotificationModels
            = new LongSparseArray<>();

    public NewAchievementNotification(Context context) {
        super(context);

        mContext = context;

        getBuilder().setContentTitle(getResources().getQuantityString(R.plurals.notification_new_achievements, 1))
                .setContentText(getResources().getString(R.string.empty));
    }

    public NewAchievementNotification addGame(GameModel gameModel) {
        NotificationModel notificationModel;
        if (mNotificationModels.indexOfKey(gameModel.getId()) < 0) {
            notificationModel = NotificationModel.newAchievement(gameModel);
            notificationModel.save(mContext.getContentResolver());
            mNotificationModels.append(gameModel.getId(), notificationModel);
        } else {
            notificationModel = mNotificationModels.get(gameModel.getId());
            Integer objectsCount = notificationModel.getObjectsCount();
            notificationModel.setObjectsCount(++objectsCount).save(mContext.getContentResolver());
        }

        if (mGames.indexOf(gameModel.getName()) < 0) {
            mGames.add(gameModel.getName());
        }
        mAchievementsCount++;

        getBuilder().setContentTitle(getResources().getQuantityString(
                R.plurals.notification_new_achievements, mAchievementsCount));

        Intent intent;
        PendingIntent pendingIntent;

        if (mGames.size() > 1) {
            intent = new Intent(mContext, MainActivity.class);
            intent.putExtra(MainActivity.KEY_FRAGMENT, MainActivity.FRAGMENT_GAMES);
            pendingIntent = PendingIntent.getActivity(
                    mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            getBuilder().setContentText(getResources().getString(
                    R.string.notification_new_achievements_in_games,
                    mAchievementsCount, mGames.size()));
        } else {
            intent = new Intent(mContext, GameActivity.class);
            intent.putExtra(GameActivity.KEY_GAME_ID, gameModel.getId());
            pendingIntent = PendingIntent.getActivity(
                    mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            getBuilder().setContentText(getResources().getQuantityString(
                    R.plurals.notification_new_achievements_in_game,
                    mAchievementsCount, mAchievementsCount, mGames.get(0)));
        }

        getBuilder().setContentIntent(pendingIntent);

        return this;
    }

    @Override
    public int getId() {
        return ID;
    }
}
