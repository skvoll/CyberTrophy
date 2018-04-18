package io.github.skvoll.cybertrophy.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.R;

import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

public abstract class BaseNotification {
    static final String CHANNEL_DEFAULT = "default";
    static final String CHANNEL_SERVICES = "services";

    private static final String TAG = BaseNotification.class.getSimpleName();

    private final NotificationCompat.Builder mBuilder;
    private final Resources mResources;
    private final NotificationManager mManager;

    public BaseNotification(Context context) {
        mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context, getChannel());
        mBuilder.setSmallIcon(R.drawable.ic_notification);
        mResources = context.getResources();
    }

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            ArrayList<NotificationChannel> notificationChannels = new ArrayList<>();

            if (manager == null) {
                return;
            }

            NotificationChannel channelDefault = new NotificationChannel(
                    CHANNEL_DEFAULT, CHANNEL_DEFAULT, NotificationManager.IMPORTANCE_LOW);
            channelDefault.setLockscreenVisibility(VISIBILITY_PUBLIC);
            channelDefault.enableLights(true);
            channelDefault.setLightColor(context.getColor(R.color.accent));
            notificationChannels.add(channelDefault);

            NotificationChannel channelServices = new NotificationChannel(
                    CHANNEL_SERVICES, CHANNEL_SERVICES, NotificationManager.IMPORTANCE_LOW);
            channelServices.setLockscreenVisibility(VISIBILITY_PUBLIC);
            notificationChannels.add(channelServices);

            for (NotificationChannel notificationChannel : notificationChannels) {
                manager.createNotificationChannel(notificationChannel);
                Log.d(TAG, String.format("Notification channel %s created.", notificationChannel.getName()));
            }
        }
    }

    public static void cancelNotifications(Context context) {
        (new AchievementRemovedNotification(context)).cancel();
        (new AchievementUnlockedNotification(context)).cancel();
        (new GameCompleteNotification(context)).cancel();
        (new GameRemovedNotification(context)).cancel();
        (new NewAchievementNotification(context)).cancel();
        (new NewGameNotification(context)).cancel();

        // services
        (new GamesParserRetryNotification(context)).cancel();
        (new GamesParserCompleteNotification(context)).cancel();
    }

    public NotificationCompat.Builder getBuilder() {
        return mBuilder;
    }

    protected abstract int getId();

    protected String getChannel() {
        return CHANNEL_DEFAULT;
    }

    protected Resources getResources() {
        return mResources;
    }

    public final Notification build() {
        return mBuilder.build();
    }

    public final void show() {
        mManager.notify(getId(), build());
    }

    public final void cancel() {
        mManager.cancel(getId());
    }
}
