package io.github.skvoll.cybertrophy.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.GameActivity;
import io.github.skvoll.cybertrophy.MainActivity;
import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.GameModel;

public final class GameCompleteNotification extends BaseNotification {
    public static final int ID = 1022;

    private ArrayList<String> mGames = new ArrayList<>();

    public GameCompleteNotification(Context context) {
        super(context);

        mBuilder.setContentTitle(mResources.getQuantityString(R.plurals.notification_games_complete, 1))
                .setContentText(mResources.getString(R.string.empty));
    }

    public GameCompleteNotification addGame(GameModel gameModel) {
        mGames.add(gameModel.getName());

        Intent intent;
        PendingIntent pendingIntent;

        if (mGames.size() > 1) {
            intent = new Intent(mContext, MainActivity.class);
            intent.putExtra(MainActivity.KEY_FRAGMENT, MainActivity.FRAGMENT_GAMES);
            pendingIntent = PendingIntent.getActivity(
                    mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            intent = new Intent(mContext, GameActivity.class);
            intent.putExtra(GameActivity.KEY_GAME_ID, gameModel.getId());
            pendingIntent = PendingIntent.getActivity(
                    mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        mBuilder.setContentIntent(pendingIntent);

        mBuilder.setContentTitle(mResources.getQuantityString(
                R.plurals.notification_games_complete, mGames.size(), mGames.size()));

        String contentText = TextUtils.join("\n", mGames);
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
        mBuilder.setContentText(contentText);

        return this;
    }

    @Override
    public int getId() {
        return ID;
    }
}
