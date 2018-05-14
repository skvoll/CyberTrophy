package app.cybertrophy.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import app.cybertrophy.MainActivity;
import app.cybertrophy.R;

public final class GamesParserCompleteNotification extends BaseNotification {
    public static final int ID = 2003;

    public GamesParserCompleteNotification(Context context) {
        super(context);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        getBuilder().setContentTitle(getResources().getString(R.string.notification_games_parser_collecting_games))
                .setContentText(getResources().getString(R.string.notification_games_parser_complete))
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
