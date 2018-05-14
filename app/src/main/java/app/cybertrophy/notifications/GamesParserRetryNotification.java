package app.cybertrophy.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import app.cybertrophy.R;
import app.cybertrophy.services.GamesParserBroadcastReceiver;

public final class GamesParserRetryNotification extends BaseNotification {
    public static final int ID = 2002;

    public GamesParserRetryNotification(Context context) {
        super(context);

        Intent intent = new Intent(context, GamesParserBroadcastReceiver.class);
        intent.setAction(GamesParserBroadcastReceiver.ACTION_RETRY);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        getBuilder().setContentTitle(getResources().getString(R.string.notification_games_parser_collecting_games))
                .setContentText(getResources().getString(R.string.notification_games_parser_download_failed))
                .setSmallIcon(android.R.drawable.stat_notify_error);

        getBuilder().addAction(android.R.drawable.stat_notify_error,
                getResources().getString(R.string.notification_games_parser_retry),
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
