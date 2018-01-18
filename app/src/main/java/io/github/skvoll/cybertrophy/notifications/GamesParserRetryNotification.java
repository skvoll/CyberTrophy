package io.github.skvoll.cybertrophy.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.services.GamesParserBroadcastReceiver;

public final class GamesParserRetryNotification extends BaseNotification {
    public static final int ID = 2002;

    public GamesParserRetryNotification(Context context) {
        super(context);

        Intent intent = new Intent(mContext, GamesParserBroadcastReceiver.class);
        intent.setAction(GamesParserBroadcastReceiver.ACTION_RETRY);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentTitle(mResources.getString(R.string.notification_games_parser_collecting_games))
                .setContentText(mResources.getString(R.string.notification_games_parser_downloading_failed))
                .setSmallIcon(android.R.drawable.stat_sys_warning);

        mBuilder.addAction(android.R.drawable.stat_sys_warning,
                mContext.getResources().getString(R.string.notification_games_parser_retry),
                pendingIntent);
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
