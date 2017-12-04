package io.github.skvoll.cybertrophy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.data.AchievementModel;
import io.github.skvoll.cybertrophy.data.GameModel;

public class NotificationHelper {
    public static final String PRIMARY_CHANNEL = "default";
    private Context mContext;
    private NotificationManager mManager;

    public NotificationHelper(Context context) {
        mContext = context;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(PRIMARY_CHANNEL,
                    PRIMARY_CHANNEL, NotificationManager.IMPORTANCE_DEFAULT);
            getManager().createNotificationChannel(channel);
        }
    }

    public void notify(int id, String channel, String title, String text) {
        getManager().notify(id, make(channel, title, text));
    }

    public void notify(int id, Notification notification) {
        getManager().notify(id, notification);
    }

    public void notify(int id, NotificationCompat.Builder notificationBuilder) {
        getManager().notify(id, notificationBuilder.build());
    }

    public NotificationCompat.Builder getBuilder(String channel) {
        return new NotificationCompat.Builder(mContext, channel);
    }

    public Notification make(String channel, String title, String text) {
        return new NotificationCompat.Builder(mContext, channel)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(getSmallIcon())
                .build();
    }

    private int getSmallIcon() {
        return android.R.mipmap.sym_def_app_icon;
    }

    private NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return mManager;
    }

    public static class NewGameNotification {
        public static final int ID = 1001;

        private NotificationHelper mNotificationHelper;
        private ArrayList<String> mGames = new ArrayList<>();

        public NewGameNotification(NotificationHelper notificationHelper) {
            mNotificationHelper = notificationHelper;
        }

        public void show(GameModel gameModel) {
            mGames.add(gameModel.getName());

            NotificationCompat.Builder builder = mNotificationHelper.getBuilder(NotificationHelper.PRIMARY_CHANNEL);
            builder.setSmallIcon(android.R.drawable.ic_menu_add);

            // TODO: translations
            if (mGames.size() > 1) {
                builder.setContentTitle(mGames.size() + " new games in library");
            } else {
                builder.setContentTitle("New game in library");
            }

            String contentText = TextUtils.join("\n", mGames);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
            builder.setContentText(contentText);

            mNotificationHelper.notify(ID, builder);
        }
    }

    public static class GameRemovedNotification {
        public static final int ID = 1002;

        private NotificationHelper mNotificationHelper;
        private ArrayList<String> mGames = new ArrayList<>();

        public GameRemovedNotification(NotificationHelper notificationHelper) {
            mNotificationHelper = notificationHelper;
        }

        public void show(GameModel gameModel) {
            mGames.add(gameModel.getName());

            NotificationCompat.Builder builder = mNotificationHelper.getBuilder(NotificationHelper.PRIMARY_CHANNEL);
            builder.setSmallIcon(android.R.drawable.ic_menu_add);

            // TODO: translations
            if (mGames.size() > 1) {
                builder.setContentTitle(mGames.size() + " games removed from library");
            } else {
                builder.setContentTitle("Game removed from library");
            }

            String contentText = TextUtils.join("\n", mGames);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
            builder.setContentText(contentText);

            mNotificationHelper.notify(ID, builder);
        }
    }

    public static class NewAchievementNotification {
        public static final int ID = 1011;

        private NotificationHelper mNotificationHelper;
        private ArrayList<String> mGames = new ArrayList<>();
        private int mAchievementsCount = 0;

        public NewAchievementNotification(NotificationHelper notificationHelper) {
            mNotificationHelper = notificationHelper;
        }

        public void show(GameModel gameModel) {
            if (mGames.indexOf(gameModel.getName()) < 0) {
                mGames.add(gameModel.getName());
            }
            mAchievementsCount++;

            NotificationCompat.Builder builder = mNotificationHelper.getBuilder(NotificationHelper.PRIMARY_CHANNEL);
            builder.setSmallIcon(android.R.drawable.ic_menu_add);

            // TODO: translations
            builder.setContentTitle("New achievements");
            if (mGames.size() > 1) {
                builder.setContentText(mAchievementsCount + " new achievement in " + mGames.size() + " games");
            } else {
                builder.setContentText(mAchievementsCount + " new achievement in " + mGames.get(0));
            }

            mNotificationHelper.notify(ID, builder);
        }
    }

    public static class AchievementRemovedNotification {
        public static final int ID = 1012;

        private NotificationHelper mNotificationHelper;
        private ArrayList<String> mGames = new ArrayList<>();
        private int mAchievementsCount = 0;

        public AchievementRemovedNotification(NotificationHelper notificationHelper) {
            mNotificationHelper = notificationHelper;
        }

        public void show(GameModel gameModel) {
            if (mGames.indexOf(gameModel.getName()) < 0) {
                mGames.add(gameModel.getName());
            }
            mAchievementsCount++;

            NotificationCompat.Builder builder = mNotificationHelper.getBuilder(NotificationHelper.PRIMARY_CHANNEL);
            builder.setSmallIcon(android.R.drawable.ic_menu_add);

            // TODO: translations
            builder.setContentTitle("Achievements removed");
            if (mGames.size() > 1) {
                builder.setContentText(mAchievementsCount + " achievement removed in " + mGames.size() + " games");
            } else {
                builder.setContentText(mAchievementsCount + " achievement removed in " + mGames.get(0));
            }

            mNotificationHelper.notify(ID, builder);
        }
    }

    public static class AchievementUnlockedNotification {
        public static final int ID = 1021;

        private NotificationHelper mNotificationHelper;
        private ArrayList<String> mGames = new ArrayList<>();
        private ArrayList<String> mAchievements = new ArrayList<>();

        public AchievementUnlockedNotification(NotificationHelper notificationHelper) {
            mNotificationHelper = notificationHelper;
        }

        public void show(GameModel gameModel, AchievementModel achievementModel) {
            if (mGames.indexOf(gameModel.getName()) < 0) {
                mGames.add(gameModel.getName());
            }
            mAchievements.add(achievementModel.getName());

            NotificationCompat.Builder builder = mNotificationHelper.getBuilder(NotificationHelper.PRIMARY_CHANNEL);
            builder.setSmallIcon(android.R.drawable.ic_menu_add);

            // TODO: translations
            if (mGames.size() > 1) {
                builder.setContentTitle(mAchievements.size() + " achievements unlocked");
                builder.setContentText(mAchievements.size() + " achievements unlocked in " + mGames.size() + " games");
            } else {
                builder.setContentTitle(mGames.get(0));
                String contentText;
                if (mAchievements.size() > 1) {
                    contentText = TextUtils.join(" unlocked\n", mAchievements) + " unlocked";
                } else {
                    contentText = mAchievements.get(0) + " unlocked";
                }
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
                builder.setContentText(contentText);
            }

            mNotificationHelper.notify(ID, builder);
        }
    }

    public static class GameCompleteNotification {
        public static final int ID = 1022;

        private NotificationHelper mNotificationHelper;

        public GameCompleteNotification(NotificationHelper notificationHelper) {
            mNotificationHelper = notificationHelper;
        }

        public void show(GameModel gameModel) {
            NotificationCompat.Builder builder = mNotificationHelper.getBuilder(NotificationHelper.PRIMARY_CHANNEL);
            builder.setSmallIcon(android.R.drawable.ic_menu_add);

            builder.setContentTitle(gameModel.getName());
            builder.setContentText("Game complete");

            mNotificationHelper.notify(ID, builder);
        }
    }
}
