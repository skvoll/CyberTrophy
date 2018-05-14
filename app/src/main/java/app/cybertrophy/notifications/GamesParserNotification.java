package app.cybertrophy.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import app.cybertrophy.MainActivity;
import app.cybertrophy.R;
import app.cybertrophy.services.GamesParserBroadcastReceiver;
import app.cybertrophy.steam.SteamGame;

public final class GamesParserNotification extends BaseNotification {
    public static final int ID = 2001;

    public GamesParserNotification(Context context) {
        super(context);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent postponeIntent = new Intent(context, GamesParserBroadcastReceiver.class);
        postponeIntent.setAction(GamesParserBroadcastReceiver.ACTION_STOP);
        PendingIntent postponePendingIntent = PendingIntent.getBroadcast(
                context, 0, postponeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        getBuilder().setProgress(1, 0, true)
                .setContentTitle(getResources().getString(R.string.notification_games_parser_collecting_games))
                .setContentText(getResources().getString(R.string.notification_games_parser_downloading))
                .setSmallIcon(android.R.drawable.stat_sys_download);

        getBuilder().setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                        getResources().getString(R.string.notification_games_parser_postpone),
                        postponePendingIntent);
    }

    @Override
    public String getChannel() {
        return CHANNEL_SERVICES;
    }

    @Override
    public int getId() {
        return ID;
    }

    public GamesParserNotification setProgress(Integer processed, Integer total, SteamGame steamGame) {
        Double progress = processed.doubleValue() / total.doubleValue() * 100;
        getBuilder().setSubText(progress.intValue() + "%")
                .setProgress(total, processed, false)
                .setContentText(steamGame.name);

        return this;
    }
}
