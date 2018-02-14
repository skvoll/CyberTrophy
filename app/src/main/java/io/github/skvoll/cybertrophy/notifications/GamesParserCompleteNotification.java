package io.github.skvoll.cybertrophy.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import io.github.skvoll.cybertrophy.MainActivity;
import io.github.skvoll.cybertrophy.R;

public final class GamesParserCompleteNotification extends BaseNotification {
    public static final int ID = 2003;

    public GamesParserCompleteNotification(Context context) {
        super(context);

        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentTitle(mResources.getString(R.string.notification_games_parser_collecting_games))
                .setContentText(mResources.getString(R.string.notification_games_parser_complete))
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentIntent(pendingIntent);
    }

    @Override
    public String getChannel() {
        return CHANNEL_SERVICES;
    }

    @Override
    public int getId() {
        return ID;
    }
}
