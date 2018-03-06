package io.github.skvoll.cybertrophy.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.MainActivity;
import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.GameModel;
import io.github.skvoll.cybertrophy.data.NotificationModel;

public final class GameRemovedNotification extends BaseNotification {
    public static final int ID = 2002;

    private ArrayList<String> mGames = new ArrayList<>();

    public GameRemovedNotification(Context context) {
        super(context);

        mBuilder.setContentTitle(mResources.getQuantityString(R.plurals.notification_games_removed_from_library, 1))
                .setContentText(mResources.getString(R.string.empty));
    }

    public GameRemovedNotification addGame(GameModel gameModel) {
        NotificationModel.gameRemoved(gameModel).save(mContext.getContentResolver());

        mGames.add(gameModel.getName());

        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra(MainActivity.KEY_FRAGMENT, MainActivity.FRAGMENT_GAMES);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);

        mBuilder.setContentTitle(mResources.getQuantityString(
                R.plurals.notification_games_removed_from_library, mGames.size(), mGames.size()));

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
