package io.github.skvoll.cybertrophy;

import android.app.NotificationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.data.AchievementModel;
import io.github.skvoll.cybertrophy.data.GameModel;
import io.github.skvoll.cybertrophy.notifications.AchievementRemovedNotification;
import io.github.skvoll.cybertrophy.notifications.AchievementUnlockedNotification;
import io.github.skvoll.cybertrophy.notifications.GameCompleteNotification;
import io.github.skvoll.cybertrophy.notifications.GameRemovedNotification;
import io.github.skvoll.cybertrophy.notifications.GamesParserCompleteNotification;
import io.github.skvoll.cybertrophy.notifications.GamesParserNotification;
import io.github.skvoll.cybertrophy.notifications.GamesParserRetryNotification;
import io.github.skvoll.cybertrophy.notifications.NewAchievementNotification;
import io.github.skvoll.cybertrophy.notifications.NewGameNotification;
import io.github.skvoll.cybertrophy.steam.SteamGame;

public class DevToolsActivity extends AppCompatActivity {
    private static final String TAG = DevToolsActivity.class.getSimpleName();

    private static ArrayList<String> sNotificationsList = new ArrayList<>();

    static {
        sNotificationsList.add("AchievementRemoved");
        sNotificationsList.add("AchievementUnlocked");
        sNotificationsList.add("GameComplete");
        sNotificationsList.add("GameRemoved");
        sNotificationsList.add("GamesParserComplete");
        sNotificationsList.add("GamesParser");
        sNotificationsList.add("GamesParserRetry");
        sNotificationsList.add("NewAchievement");
        sNotificationsList.add("NewGame");
    }

    private AchievementRemovedNotification mAchievementRemovedNotification;
    private AchievementUnlockedNotification mAchievementUnlockedNotification;
    private GameCompleteNotification mGameCompleteNotification;
    private GameRemovedNotification mGameRemovedNotification;
    private GamesParserCompleteNotification mGamesParserCompleteNotification;
    private GamesParserNotification mGamesParserNotification;
    private GamesParserRetryNotification mGamesParserRetryNotification;
    private NewAchievementNotification mNewAchievementNotification;
    private NewGameNotification mNewGameNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_tools);

        setTitle("DevTools");

        resetNotifications();

        ArrayAdapter<String> notificationAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, sNotificationsList);

        final Spinner spNotifications = findViewById(R.id.sp_notifications);
        spNotifications.setAdapter(notificationAdapter);

        final CheckBox cbNotificationDelay = findViewById(R.id.cd_notification_delay);

        Button btnNotificationCheck = findViewById(R.id.btn_notification_check);
        btnNotificationCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNotification(spNotifications.getSelectedItem().toString(),
                        cbNotificationDelay.isChecked());
            }
        });

        Button btnNotificationReset = findViewById(R.id.btn_notification_reset);
        btnNotificationReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetNotifications();
            }
        });
    }

    private void resetNotifications() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }

        mAchievementRemovedNotification = new AchievementRemovedNotification(this);
        mAchievementUnlockedNotification = new AchievementUnlockedNotification(this);
        mGameCompleteNotification = new GameCompleteNotification(this);
        mGameRemovedNotification = new GameRemovedNotification(this);
        mGamesParserCompleteNotification = new GamesParserCompleteNotification(this);
        mGamesParserNotification = new GamesParserNotification(this);
        mGamesParserRetryNotification = new GamesParserRetryNotification(this);
        mNewAchievementNotification = new NewAchievementNotification(this);
        mNewGameNotification = new NewGameNotification(this);
    }

    private void showNotification(final String notificationName, boolean delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                GameModel gameModelFirst = GameModel.getById(getContentResolver(), 1L);
                GameModel gameModelSecond = GameModel.getById(getContentResolver(), 2L);
                AchievementModel achievementModelFirst = AchievementModel.getById(getContentResolver(), 1L);
                AchievementModel achievementModelSecond = AchievementModel.getById(getContentResolver(), 2L);
                boolean isFirst = Math.random() < 0.5;
                switch (notificationName) {
                    case "AchievementRemoved":
                        mAchievementRemovedNotification.addGame(isFirst ? gameModelFirst : gameModelSecond).show();
                        break;
                    case "AchievementUnlocked":
                        mAchievementUnlockedNotification.addAchievement(
                                isFirst ? gameModelFirst : gameModelSecond,
                                isFirst ? achievementModelFirst : achievementModelSecond).show();
                        mAchievementUnlockedNotification.show();
                        break;
                    case "GameComplete":
                        mGameCompleteNotification.addGame(isFirst ? gameModelFirst : gameModelSecond).show();
                        break;
                    case "GameRemoved":
                        mGameRemovedNotification.addGame(isFirst ? gameModelFirst : gameModelSecond).show();
                        break;
                    case "GamesParserComplete":
                        mGamesParserCompleteNotification.show();
                        break;
                    case "GamesParser":
                        Double progress = Math.random() * 10;
                        SteamGame steamGame = new SteamGame();
                        if (gameModelFirst != null && gameModelSecond != null) {
                            steamGame.name = isFirst ? gameModelFirst.getName() : gameModelSecond.getName();
                        } else {
                            steamGame.name = "Random game name";
                        }
                        mGamesParserNotification.setProgress(10, progress.intValue(), steamGame).show();
                        break;
                    case "GamesParserRetry":
                        mGamesParserRetryNotification.show();
                        break;
                    case "NewAchievement":
                        mNewAchievementNotification.addGame(isFirst ? gameModelFirst : gameModelSecond).show();
                        break;
                    case "NewGame":
                        mNewGameNotification.addGame(isFirst ? gameModelFirst : gameModelSecond).show();
                        break;
                    default:
                        (Toast.makeText(DevToolsActivity.this,
                                "Unknown notification type", Toast.LENGTH_SHORT)).show();
                        break;
                }
            }
        }, delay ? 3000 : 1);

        if (delay) {
            (Toast.makeText(this,
                    "Notification will be shown after 3 seconds", Toast.LENGTH_SHORT)).show();
        }
    }
}
