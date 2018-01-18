package io.github.skvoll.cybertrophy.notifications;

import android.content.Context;

import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.GameModel;

public final class GameCompleteNotification extends BaseNotification {
    public static final int ID = 1022;

    public GameCompleteNotification(Context context) {
        super(context);

        mBuilder.setContentTitle(mResources.getString(R.string.empty))
                .setContentText(mResources.getString(R.string.empty))
                .setSmallIcon(android.R.drawable.ic_menu_add);
    }

    public GameCompleteNotification addGame(GameModel gameModel) {
        mBuilder.setContentTitle(gameModel.getName());
        mBuilder.setContentText(mResources.getString(R.string.notification_game_complete));

        return this;
    }

    @Override
    public int getId() {
        return 0;
    }
}
