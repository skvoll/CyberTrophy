package io.github.skvoll.cybertrophy.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import io.github.skvoll.cybertrophy.MainActivity;
import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.services.GamesParserBroadcastReceiver;
import io.github.skvoll.cybertrophy.steam.SteamGame;

public final class GamesParserNotification extends BaseNotification {
    public static final int ID = 2001;

    public GamesParserNotification(Context context) {
        super(context);

        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent postponeIntent = new Intent(mContext, GamesParserBroadcastReceiver.class);
        postponeIntent.setAction(GamesParserBroadcastReceiver.ACTION_STOP);
        PendingIntent postponePendingIntent = PendingIntent.getBroadcast(
                mContext, 0, postponeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentTitle(mResources.getString(R.string.notification_games_parser_collecting_games))
                .setContentText(mResources.getString(R.string.notification_games_parser_downloading))
                .setSmallIcon(android.R.drawable.stat_sys_download);

        mBuilder.setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                        mContext.getResources().getString(R.string.notification_games_parser_postpone),
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

    public GamesParserNotification setGame(SteamGame steamGame) {
        mBuilder.setContentText(steamGame.name);

        return this;
    }
}
