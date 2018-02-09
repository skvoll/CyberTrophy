package io.github.skvoll.cybertrophy.notifications;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import java.util.ArrayList;

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
