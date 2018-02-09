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
    public static final String CHANNEL_DEFAULT = "default";
    public static final String CHANNEL_SERVICES = "services";
    public static final int ID = 1;

    private static final String TAG = BaseNotification.class.getSimpleName();

    final Context mContext;
    final NotificationCompat.Builder mBuilder;
    final Resources mResources;

    private final NotificationManager mManager;

    public BaseNotification(Context context) {
        mContext = context;
        mManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext, getChannel());
        mBuilder.setSmallIcon(R.drawable.ic_notification);
        mResources = mContext.getResources();
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
            channelDefault.setLightColor(context.getColor(R.color.accentColor));
            notificationChannels.add(channelDefault);

            NotificationChannel channelServices = new NotificationChannel(
                    CHANNEL_SERVICES, CHANNEL_SERVICES, NotificationManager.IMPORTANCE_LOW);
            channelServices.setLockscreenVisibility(VISIBILITY_PUBLIC);
            notificationChannels.add(channelServices);

            for (NotificationChannel notificationChannel : notificationChannels) {
                manager.createNotificationChannel(notificationChannel);
                Log.d(TAG, "Notification channel " + notificationChannel.getName() + " created.");
            }
        }
    }

    public abstract int getId();

    public String getChannel() {
        return CHANNEL_DEFAULT;
    }

    public Notification build() {
        return mBuilder.build();
    }

    public void show() {
        mManager.notify(getId(), build());
    }

    public void cancel() {
        mManager.cancel(getId());
    }
}
