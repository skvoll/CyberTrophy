package io.github.skvoll.cybertrophy;

import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import com.android.volley.TimeoutError;

import java.math.BigDecimal;
import java.util.HashMap;

import io.github.skvoll.cybertrophy.data.AchievementModel;
import io.github.skvoll.cybertrophy.data.DataContract;
import io.github.skvoll.cybertrophy.data.GameModel;
import io.github.skvoll.cybertrophy.data.LogModel;
import io.github.skvoll.cybertrophy.data.ProfileModel;
import io.github.skvoll.cybertrophy.steam.SteamAchievement;
import io.github.skvoll.cybertrophy.steam.SteamApi;
import io.github.skvoll.cybertrophy.steam.SteamGame;
import io.github.skvoll.cybertrophy.steam.SteamPlayerAchievement;

public abstract class GamesParserTask extends AsyncTask<Long, SteamGame, Boolean> {
    public static final int ACTION_FIRST = 0;
    public static final int ACTION_ALL = 1;
    public static final int ACTION_RECENT = 2;
    public static final int ACTION_EXACT = 3;
    private static final String TAG = GamesParserTask.class.getSimpleName();

    private ProfileModel mProfileModel;
    private ContentResolver mContentResolver;
    private SteamApi mSteamApi;
    private int mAction;

    private NotificationHelper.NewGameNotification mNewGameNotification;
    private NotificationHelper.GameRemovedNotification mGameRemovedNotification;
    private NotificationHelper.NewAchievementNotification mNewAchievementNotification;
    private NotificationHelper.AchievementRemovedNotification mAchievementRemovedNotification;
    private NotificationHelper.AchievementUnlockedNotification mAchievementUnlockedNotification;
    private NotificationHelper.GameCompleteNotification mGameCompleteNotification;

    public GamesParserTask(Context context, ProfileModel profileModel, int action) {
        mProfileModel = profileModel;
        mContentResolver = context.getContentResolver();
        mSteamApi = new SteamApi(VolleySingleton.getInstance(context));
        mAction = action;

        NotificationHelper notificationHelper = new NotificationHelper(context);

        mNewGameNotification = new NotificationHelper.NewGameNotification(notificationHelper);
        mGameRemovedNotification = new NotificationHelper.GameRemovedNotification(notificationHelper);
        mNewAchievementNotification = new NotificationHelper.NewAchievementNotification(notificationHelper);
        mAchievementRemovedNotification = new NotificationHelper.AchievementRemovedNotification(notificationHelper);
        mAchievementUnlockedNotification = new NotificationHelper.AchievementUnlockedNotification(notificationHelper);
        mGameCompleteNotification = new NotificationHelper.GameCompleteNotification(notificationHelper);
    }

    @Override
    protected Boolean doInBackground(Long... appIds) {
        try {
            Log.d(TAG, "Parsing \"" + mProfileModel.getName() + "(" + mProfileModel.getSteamId() + ")\" profile.");

            LongSparseArray<GameModel> gameModels = GameModel.getByProfile(mContentResolver, mProfileModel);
            LongSparseArray<SteamGame> steamGames;

            switch (mAction) {
                case ACTION_FIRST:
                case ACTION_ALL:
                    steamGames = getAllGames();
                    break;
                case ACTION_RECENT:
                    steamGames = getRecentGames();
                    break;
                case ACTION_EXACT:
                    steamGames = getGames(appIds);
                    break;
                default:
                    Log.d(TAG, "Done.");

                    return true;
            }

            if (steamGames == null || steamGames.size() <= 0) {
                Log.d(TAG, "\"" + mProfileModel.getName() + "(" + mProfileModel.getSteamId() + ")\" has no games. Terminate.");

                if (mAction == ACTION_ALL) {
                    checkGamesForDeleting(gameModels, steamGames);
                }

                Log.d(TAG, "Done.");

                return true;
            }

            Log.d(TAG, steamGames.size() + " game(s) loaded. Parsing.");

            for (int i = 0; i < steamGames.size(); i++) {
                if (isCancelled()) {
                    Log.d(TAG, "Canceled.");

                    return false;
                }

                SteamGame steamGame = steamGames.valueAt(i);
                GameModel gameModel = gameModels.get(steamGame.appId, null);

                if (mAction == ACTION_FIRST && gameModel != null) {
                    continue;
                }

                if (mAction == ACTION_RECENT) {
                    if (gameModel != null && gameModel.getPlaytimeForever().equals(steamGame.playtimeForever)) {
                        Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" has not been launched. Skipping.");

                        continue;
                    }
                }

                loadSteamGameAchievements(steamGame, mProfileModel.getSteamId());

                publishProgress(steamGame);

                if (steamGame.getAchievementsTotalCount() == 0) {
                    Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" has no achievements.");

                    if (gameModel != null) {
                        updateGame(gameModel, steamGame);

                        if (gameModel.getAchievementsTotalCount() != 0) {
                            Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" achievements were removed. Deleting.");

                            deleteGameAchievement(gameModel);
                        }
                    } else {
                        if (mAction != ACTION_FIRST) {
                            Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" is new. Saving.");
                        }

                        new GameModel(mProfileModel, steamGame).save(mContentResolver);
                    }

                    Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" saved.");

                    continue;
                }

                Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" has " + steamGame.getAchievementsTotalCount() + " achievement(s). Parsing.");

                if (gameModel != null) {
                    updateGame(gameModel, steamGame);

                    HashMap<String, AchievementModel> achievementModels = AchievementModel.getByGame(mContentResolver, gameModel);

                    for (SteamAchievement steamAchievement : steamGame.getSteamAchievements().values()) {
                        if (!achievementModels.containsKey(steamAchievement.name)) {
                            Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" has new achievement \"" + steamAchievement.displayName + "\".");

                            if (mProfileModel.isInitialized()) {
                                mNewAchievementNotification.show(gameModel);
                            }

                            new AchievementModel(gameModel, steamAchievement).save(mContentResolver);

                            continue;
                        }

                        AchievementModel achievementModel = achievementModels.get(steamAchievement.name);

                        if (steamAchievement.isUnlocked() != achievementModel.isUnlocked()) {
                            Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" achievement \"" + steamAchievement.displayName + "\" unlocked.");

                            if (mProfileModel.isInitialized()) {
                                mAchievementUnlockedNotification.show(gameModel, achievementModel);
                            }

                            LogModel.achievementUnlocked(achievementModel).save(mContentResolver);
                        }

                        updateAchievement(achievementModel, steamAchievement);
                    }

                    for (AchievementModel achievementModel : achievementModels.values()) {
                        if (!steamGame.getSteamAchievements().containsKey(achievementModel.getCode())) {
                            Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" achievement \"" + achievementModel.getName() + "\" was removed. Deleting.");

                            if (mProfileModel.isInitialized()) {
                                mAchievementRemovedNotification.show(gameModel);
                            }

                            achievementModel.delete(mContentResolver);
                        }
                    }

                    if (gameModel.isComplete()) {
                        Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" is complete.");

                        if (mAction != ACTION_FIRST) {
                            if (mProfileModel.isInitialized()) {
                                mGameCompleteNotification.show(gameModel);

                                LogModel.gameComplete(gameModel).save(mContentResolver);
                            }
                        }
                    }

                    Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" saved.");
                } else {
                    gameModel = new GameModel(mProfileModel, steamGame);

                    if (mAction != ACTION_FIRST) {
                        Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" is new. Saving.");

                        if (mProfileModel.isInitialized()) {
                            mNewGameNotification.show(gameModel);

                            // TODO: uncomment
//                            LogModel.newGame(gameModel).save(mContentResolver);
                        }
                    }

                    // TODO: remove
                    LogModel.newGame(gameModel).save(mContentResolver);

                    for (SteamAchievement steamAchievement : steamGame.getSteamAchievements().values()) {
                        AchievementModel achievementModel = new AchievementModel(gameModel, steamAchievement);
                        achievementModel.save(mContentResolver);

                        // TODO: remove
                        if (achievementModel.isUnlocked()) {
                            Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" achievement \"" + steamAchievement.displayName + "\" unlocked.");
                            LogModel.achievementUnlocked(achievementModel).save(mContentResolver);
                        }
                    }

                    gameModel.save(mContentResolver);

                    Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" saved.");
                }
            }

            if (isCancelled()) {
                Log.d(TAG, "Canceled.");

                return false;
            }

            if (mAction == ACTION_ALL) {
                checkGamesForDeleting(gameModels, steamGames);
            }
        } catch (InterruptedException | TimeoutError | Error e) {
            if (!isCancelled()) {
                e.printStackTrace();
            }

            Log.d(TAG, "Failed.");

            return false;
        }

        Log.d(TAG, "Done.");

        return true;
    }

    private LongSparseArray<SteamGame> getAllGames() throws InterruptedException, TimeoutError, Error {
        SteamApi.FutureResponseListener<LongSparseArray<SteamGame>> listener =
                new SteamApi.FutureResponseListener<>();
        mSteamApi.getOwnedGames(mProfileModel.getSteamId(),
                true, true, new Long[]{}, listener);

        return listener.get(SteamApi.REQUEST_TIMEOUT);
    }

    private LongSparseArray<SteamGame> getRecentGames() throws InterruptedException, TimeoutError, Error {
        SteamApi.FutureResponseListener<LongSparseArray<SteamGame>> listener =
                new SteamApi.FutureResponseListener<>();
        mSteamApi.getRecentlyPlayedGames(mProfileModel.getSteamId(), 100, listener);

        return listener.get(SteamApi.REQUEST_TIMEOUT);
    }

    private LongSparseArray<SteamGame> getGames(Long[] appIds) throws InterruptedException, TimeoutError, Error {
        SteamApi.FutureResponseListener<LongSparseArray<SteamGame>> listener =
                new SteamApi.FutureResponseListener<>();
        mSteamApi.getOwnedGames(mProfileModel.getSteamId(),
                true, true, appIds, listener);

        return listener.get(SteamApi.REQUEST_TIMEOUT);
    }

    private void loadSteamGameAchievements(SteamGame steamGame, Long steamId) throws TimeoutError, InterruptedException {
        if (isCancelled()) {
            return;
        }

        SteamApi.FutureResponseListener<HashMap<String, SteamAchievement>> schemaListener =
                new SteamApi.FutureResponseListener<>();
        mSteamApi.getSchemaForGame(steamGame.appId, "russian", schemaListener);
        HashMap<String, SteamAchievement> steamAchievements = schemaListener.get(SteamApi.REQUEST_TIMEOUT);

        if (steamAchievements == null || steamAchievements.size() <= 0) {
            return;
        }

        SteamApi.FutureResponseListener<HashMap<String, BigDecimal>> percentagesListener =
                new SteamApi.FutureResponseListener<>();
        mSteamApi.getGlobalAchievementPercentagesForApp(steamGame.appId, percentagesListener);
        HashMap<String, BigDecimal> percentages = percentagesListener.get(SteamApi.REQUEST_TIMEOUT);

        SteamApi.FutureResponseListener<HashMap<String, SteamPlayerAchievement>> playerAchievementsListener =
                new SteamApi.FutureResponseListener<>();
        mSteamApi.getPlayerAchievements(steamId, steamGame.appId,
                "russian", playerAchievementsListener);
        HashMap<String, SteamPlayerAchievement> playerAchievements = playerAchievementsListener.get(SteamApi.REQUEST_TIMEOUT);

        for (SteamAchievement steamAchievement : steamAchievements.values()) {
            if (percentages.containsKey(steamAchievement.name)) {
                steamAchievement.setPercent(percentages.get(steamAchievement.name));
            }

            if (playerAchievements.containsKey(steamAchievement.name)) {
                SteamPlayerAchievement steamPlayerAchievement = playerAchievements.get(steamAchievement.name);

                if (steamPlayerAchievement.achieved == 1) {
                    steamAchievement.setUnlocked(true);
                    steamAchievement.setUnlockTime(steamPlayerAchievement.unlockTime);
                }
            }

            steamGame.addAchievement(steamAchievement);
        }
    }

    private void checkGamesForDeleting(LongSparseArray<GameModel> gameModels, LongSparseArray<SteamGame> steamGames) {
        for (int i = 0; i < gameModels.size(); i++) {
            if (isCancelled()) {
                break;
            }

            GameModel gameModel = gameModels.valueAt(i);

            if (steamGames == null || steamGames.indexOfKey(gameModel.getAppId()) < 0) {
                Log.d(TAG, "\"" + gameModel.getName() + "(" + gameModel.getAppId() + ")\" was removed. Deleting.");

                if (mProfileModel.isInitialized()) {
                    mGameRemovedNotification.show(gameModel);
                }

                deleteGame(gameModel);
            }
        }
    }

    private void updateGame(GameModel gameModel, SteamGame steamGame) {
        if (steamGame.playtimeForever > gameModel.getPlaytimeForever()) {
            gameModel.setLastPlay(System.currentTimeMillis() / 1000);
        }
        gameModel.setPlaytimeForever(steamGame.playtimeForever);
        gameModel.setAchievementsUnlockedCount(steamGame.getAchievementsUnlockedCount());
        gameModel.setAchievementsTotalCount(steamGame.getAchievementsTotalCount());

        gameModel.save(mContentResolver);
    }

    private void deleteGame(GameModel gameModel) {
        deleteGameAchievement(gameModel);

        gameModel.delete(mContentResolver);
    }

    private void updateAchievement(AchievementModel achievementModel, SteamAchievement steamAchievement) {
        achievementModel.setName(steamAchievement.displayName);
        achievementModel.setHidden(steamAchievement.hidden == 1);
        achievementModel.setDescription(steamAchievement.description);
        achievementModel.setPercent(steamAchievement.getPercent());
        achievementModel.setUnlocked(steamAchievement.isUnlocked());
        achievementModel.setUnlockTime(steamAchievement.getUnlockTime());

        achievementModel.save(mContentResolver);
    }

    private void deleteGameAchievement(GameModel gameModel) {
        String where = "steam_id = ? AND app_id = ?";
        String[] selectionArgs = {gameModel.getSteamId().toString(), gameModel.getAppId().toString()};
        mContentResolver.delete(DataContract.AchievementEntry.URI, where, selectionArgs);
    }
}
