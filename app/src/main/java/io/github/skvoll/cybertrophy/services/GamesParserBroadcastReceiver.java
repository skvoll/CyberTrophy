package io.github.skvoll.cybertrophy.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import io.github.skvoll.cybertrophy.notifications.GamesParserRetryNotification;

public final class GamesParserBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_STOP = "STOP";
    public static final String ACTION_RETRY = "RETRY";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        final String action = intent.getAction();

        if (action == null) {
            throw new IllegalArgumentException("Action is missing.");
        }

        switch (action) {
            case ACTION_STOP:
                context.stopService(new Intent(context, FirstGamesParserService.class));
                break;
            case ACTION_RETRY:
                context.startService(new Intent(context, FirstGamesParserService.class));
                (new GamesParserRetryNotification(context)).cancel();
                break;
            default:
                throw new IllegalArgumentException(String.format("Unsupported action: %s.", action));
        }
    }
}
