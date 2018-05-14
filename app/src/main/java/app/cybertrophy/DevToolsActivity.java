package app.cybertrophy;

import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
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

import app.cybertrophy.data.AchievementModel;
import app.cybertrophy.data.DataContract;
import app.cybertrophy.data.DatabaseHelper;
import app.cybertrophy.data.GameModel;
import app.cybertrophy.data.ProfileModel;
import app.cybertrophy.notifications.AchievementRemovedNotification;
import app.cybertrophy.notifications.AchievementUnlockedNotification;
import app.cybertrophy.notifications.GameCompleteNotification;
import app.cybertrophy.notifications.GameRemovedNotification;
import app.cybertrophy.notifications.GamesParserCompleteNotification;
import app.cybertrophy.notifications.GamesParserNotification;
import app.cybertrophy.notifications.GamesParserRetryNotification;
import app.cybertrophy.notifications.NewAchievementNotification;
import app.cybertrophy.notifications.NewGameNotification;
import app.cybertrophy.steam.SteamGame;

public final class DevToolsActivity extends AppCompatActivity {
    private static final String TAG = DevToolsActivity.class.getSimpleName();

    private static final ArrayList<String> sNotificationsList = new ArrayList<>();

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

        Button btnShowAuth = findViewById(R.id.btn_show_auth);
        btnShowAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAuth();
            }
        });

        Button btnShowPreview = findViewById(R.id.btn_show_preview);
        btnShowPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPreview();
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
                ProfileModel profileModel = ProfileModel.getActive(getContentResolver());
                ArrayList<GameModel> gameModels = GameModel.getByProfile(
                        getContentResolver(), profileModel, GameModel.STATUS_IN_PROGRESS, 2);
                ArrayList<AchievementModel> achievementModels = AchievementModel.getByGame(
                        getContentResolver(), gameModels.get(0), AchievementModel.STATUS_LOCKED, 2);
                boolean isFirst = Math.random() < 0.5;
                switch (notificationName) {
                    case "AchievementRemoved":
                        mAchievementRemovedNotification.addGame(isFirst ? gameModels.get(0) : gameModels.get(1)).show();
                        break;
                    case "AchievementUnlocked":
                        mAchievementUnlockedNotification.addAchievement(
                                isFirst ? gameModels.get(0) : gameModels.get(1),
                                isFirst ? achievementModels.get(0) : achievementModels.get(1)).show();
                        mAchievementUnlockedNotification.show();
                        break;
                    case "GameComplete":
                        mGameCompleteNotification.addGame(isFirst ? gameModels.get(0) : gameModels.get(1)).show();
                        break;
                    case "GameRemoved":
                        mGameRemovedNotification.addGame(isFirst ? gameModels.get(0) : gameModels.get(1)).show();
                        break;
                    case "NewAchievement":
                        mNewAchievementNotification.addGame(isFirst ? gameModels.get(0) : gameModels.get(1)).show();
                        break;
                    case "NewGame":
                        mNewGameNotification.addGame(isFirst ? gameModels.get(0) : gameModels.get(1)).show();
                        break;

                    // services
                    case "GamesParserComplete":
                        mGamesParserCompleteNotification.show();
                        break;
                    case "GamesParser":
                        Double progress = Math.random() * 10;
                        SteamGame steamGame = new SteamGame();
                        if (gameModels.get(0) != null && gameModels.get(1) != null) {
                            steamGame.name = isFirst ? gameModels.get(0).getName() : gameModels.get(1).getName();
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
                            sqLiteDatabase.execSQL("DELETE FROM " + DataContract.NotificationEntry.TABLE_NAME);
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

    private void showAuth() {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
    }

    private void showPreview() {
        setTheme(R.style.AppTheme);
        setContentView(R.layout.theme_preview);
    }
}
