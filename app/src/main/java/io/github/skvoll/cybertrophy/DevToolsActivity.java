package io.github.skvoll.cybertrophy;

import android.app.NotificationManager;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.data.AchievementModel;
import io.github.skvoll.cybertrophy.data.DataContract;
import io.github.skvoll.cybertrophy.data.DatabaseHelper;
import io.github.skvoll.cybertrophy.data.GameModel;
import io.github.skvoll.cybertrophy.data.ProfileModel;
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
        sNotificationsList.add("NewAchievement");
        sNotificationsList.add("NewGame");
        sNotificationsList.add("-");
        sNotificationsList.add("GamesParserComplete");
        sNotificationsList.add("GamesParser");
        sNotificationsList.add("GamesParserRetry");
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

        Button btnWipeData = findViewById(R.id.btn_wipe_data);
        btnWipeData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wipeData();
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
                AchievementModel achievementModelFirst = AchievementModel.getById(getContentResolver(), 1L);
                AchievementModel achievementModelSecond = AchievementModel.getById(getContentResolver(), 2L);
                GameModel gameModelFirst = GameModel.getById(getContentResolver(), 1L);
                GameModel gameModelSecond = GameModel.getById(getContentResolver(), 2L);
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
                    case "NewAchievement":
                        mNewAchievementNotification.addGame(isFirst ? gameModelFirst : gameModelSecond).show();
                        break;
                    case "NewGame":
                        mNewGameNotification.addGame(isFirst ? gameModelFirst : gameModelSecond).show();
                        break;

                    // services
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

                    default:
                        (Toast.makeText(DevToolsActivity.this,
                                "Unknown notification type", Toast.LENGTH_SHORT)).show();
                        break;
                }
            }
        }, delay ? 5000 : 1);

        if (delay) {
            (Toast.makeText(this,
                    "Notification will be shown after 5 seconds", Toast.LENGTH_SHORT)).show();
        }
    }

    private void wipeData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Wipe data?")
                .setPositiveButton("agree", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DatabaseHelper databaseHelper = new DatabaseHelper(DevToolsActivity.this);
                        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();

                        ProfileModel profileModel = ProfileModel.getActive(getContentResolver());

                        if (profileModel != null) {
                            sqLiteDatabase.execSQL("DELETE FROM " + DataContract.LogEntry.TABLE_NAME);
                            sqLiteDatabase.execSQL("DELETE FROM " + DataContract.AchievementEntry.TABLE_NAME);
                            sqLiteDatabase.execSQL("DELETE FROM " + DataContract.GameEntry.TABLE_NAME);

                            profileModel.setInitialized(false);
                            profileModel.save(getContentResolver());
                        }

                        sqLiteDatabase.close();

                        (new GamesParserRetryNotification(DevToolsActivity.this)).show();
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder.create().show();
    }
}
