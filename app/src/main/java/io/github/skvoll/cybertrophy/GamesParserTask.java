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
import io.github.skvoll.cybertrophy.data.ProfileModel;
import io.github.skvoll.cybertrophy.notifications.AchievementRemovedNotification;
import io.github.skvoll.cybertrophy.notifications.AchievementUnlockedNotification;
import io.github.skvoll.cybertrophy.notifications.GameCompleteNotification;
import io.github.skvoll.cybertrophy.notifications.GameRemovedNotification;
import io.github.skvoll.cybertrophy.notifications.GamesParserRetryNotification;
import io.github.skvoll.cybertrophy.notifications.NewAchievementNotification;
import io.github.skvoll.cybertrophy.notifications.NewGameNotification;
import io.github.skvoll.cybertrophy.steam.SteamAchievement;
import io.github.skvoll.cybertrophy.steam.SteamApi;
import io.github.skvoll.cybertrophy.steam.SteamGame;
import io.github.skvoll.cybertrophy.steam.SteamPlayerAchievement;

public abstract class GamesParserTask extends AsyncTask<Long, GamesParserTask.ProgressParams, Boolean> {
    public static final int ACTION_FIRST = 0;
    public static final int ACTION_ALL = 1;
    public static final int ACTION_RECENT = 2;
    public static final int ACTION_EXACT = 3;

    private static final String TAG = GamesParserTask.class.getSimpleName();
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private ProfileModel mProfileModel;
    private ContentResolver mContentResolver;
    private SteamApi mSteamApi;
    private int mAction;

    private NewGameNotification mNewGameNotification;
    private GameRemovedNotification mGameRemovedNotification;
    private NewAchievementNotification mNewAchievementNotification;
    private AchievementRemovedNotification mAchievementRemovedNotification;
    private AchievementUnlockedNotification mAchievementUnlockedNotification;
    private GameCompleteNotification mGameCompleteNotification;

    private GamesParserRetryNotification mGamesParserRetryNotification;

    public GamesParserTask(Context context, ProfileModel profileModel, int action) {
        mProfileModel = profileModel;
        mContentResolver = context.getContentResolver();
        mSteamApi = new SteamApi(VolleySingleton.getInstance(context));
        mAction = action;

        mNewGameNotification = new NewGameNotification(context);
        mGameRemovedNotification = new GameRemovedNotification(context);
        mNewAchievementNotification = new NewAchievementNotification(context);
        mAchievementRemovedNotification = new AchievementRemovedNotification(context);
        mAchievementUnlockedNotification = new AchievementUnlockedNotification(context);
        mGameCompleteNotification = new GameCompleteNotification(context);

        mGamesParserRetryNotification = new GamesParserRetryNotification(context);
    }

    @Override
    protected Boolean doInBackground(Long... appIds) {
        Log.d(TAG, "Parsing \"" + mProfileModel.getName() + "(" + mProfileModel.getSteamId() + ")\" profile.");

        int retryAttempt = 0;

        LongSparseArray<GameModel> gameModels = GameModel.getMapByProfile(mContentResolver, mProfileModel);
        LongSparseArray<SteamGame> steamGames;

        try {
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
                    Log.d(TAG, "Unknown action. Terminate.");

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
        } catch (InterruptedException | TimeoutError | Error e) {
            Log.d(TAG, "Failed.");

            if (isCancelled()) {
                return false;
            }

            e.printStackTrace();

            if (mAction == ACTION_FIRST) {
                mGamesParserRetryNotification.show();
            }

            return false;
        }

        Log.d(TAG, steamGames.size() + " game(s) loaded. Parsing.");

        for (int i = 0; i < steamGames.size(); i++) {
            try {
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

                publishProgress(new ProgressParams(steamGames.size(), i, steamGame));

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
                    boolean wasComplete = gameModel.isComplete();

                    updateGame(gameModel, steamGame);

                    HashMap<String, AchievementModel> achievementModels = AchievementModel.getMapByGame(mContentResolver, gameModel);

                    for (SteamAchievement steamAchievement : steamGame.getSteamAchievements().values()) {
                        AchievementModel achievementModel;

                        if (!achievementModels.containsKey(steamAchievement.name)) {
                            Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" has new achievement \"" + steamAchievement.displayName + "\".");

                            new AchievementModel(gameModel, steamAchievement).save(mContentResolver);

                            if (mProfileModel.isInitialized()) {
                                mNewAchievementNotification.addGame(gameModel).show();
                            }

                            continue;
                        }

                        achievementModel = achievementModels.get(steamAchievement.name);

                        if (steamAchievement.isUnlocked() != achievementModel.isUnlocked()) {
                            Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" achievement \"" + steamAchievement.displayName + "\" unlocked.");

                            if (mProfileModel.isInitialized()) {
                                mAchievementUnlockedNotification.addAchievement(gameModel, achievementModel).show();
                            }
                        }

                        updateAchievement(achievementModel, steamAchievement);
                    }

                    for (AchievementModel achievementModel : achievementModels.values()) {
                        if (!steamGame.getSteamAchievements().containsKey(achievementModel.getCode())) {
                            Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" achievement \"" + achievementModel.getName() + "\" was removed. Deleting.");

                            if (mProfileModel.isInitialized()) {
                                mAchievementRemovedNotification.addGame(gameModel).show();
                            }

                            achievementModel.delete(mContentResolver);
                        }
                    }

                    if (!wasComplete && gameModel.isComplete()) {
                        Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" is complete.");

                        if (mAction != ACTION_FIRST) {
                            if (mProfileModel.isInitialized()) {
                                mGameCompleteNotification.addGame(gameModel).show();
                            }
                        }
                    }

                    Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" saved.");
                } else {
                    gameModel = new GameModel(mProfileModel, steamGame);

                    gameModel.save(mContentResolver);

                    if (mAction != ACTION_FIRST) {
                        Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" is new. Saving.");

                        if (mProfileModel.isInitialized()) {
                            mNewGameNotification.addGame(gameModel).show();
                        }
                    }

                    for (SteamAchievement steamAchievement : steamGame.getSteamAchievements().values()) {
                        AchievementModel achievementModel = new AchievementModel(gameModel, steamAchievement);
                        achievementModel.save(mContentResolver);
                    }

                    Log.d(TAG, "\"" + steamGame.name + "(" + steamGame.appId + ")\" saved.");
                }

                retryAttempt = 0;
            } catch (InterruptedException | TimeoutError | Error e) {
                Log.d(TAG, "Failed.");

                if (isCancelled()) {
                    return false;
                }

                retryAttempt++;

                if (retryAttempt >= MAX_RETRY_ATTEMPTS) {
                    if (mAction == ACTION_FIRST) {
                        mGamesParserRetryNotification.show();
                    }

                    e.printStackTrace();

                    return false;
                }

                i--;

                Log.w(TAG, "Parsing error. Retrying. Remaining attempts: " + (MAX_RETRY_ATTEMPTS - retryAttempt) + ".");
            }
        }

        if (isCancelled()) {
            Log.d(TAG, "Canceled.");

            return false;
        }

        if (mAction == ACTION_ALL) {
            checkGamesForDeleting(gameModels, steamGames);
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
                    mGameRemovedNotification.addGame(gameModel).show();
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
        String where = DataContract.AchievementEntry.COLUMN_GAME_ID + " = ?";
        String[] selectionArgs = {gameModel.getId().toString()};
        mContentResolver.delete(DataContract.AchievementEntry.URI, where, selectionArgs);
    }

    protected class ProgressParams {
        private int mMax;
        private int mMin;
        private SteamGame mSteamGame;

        ProgressParams(int max, int min, SteamGame steamGame) {
            mMax = max;
            mMin = min;
            mSteamGame = steamGame;
        }

        public int getMax() {
            return mMax;
        }

        public int getMin() {
            return mMin;
        }

        public SteamGame getSteamGame() {
            return mSteamGame;
        }
    }
}
