package io.github.skvoll.cybertrophy.data;

import android.content.ContentResolver;
import android.content.Context;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;

import java.math.BigDecimal;
import java.util.HashMap;

import io.github.skvoll.cybertrophy.VolleySingleton;
import io.github.skvoll.cybertrophy.steam.SteamAchievement;
import io.github.skvoll.cybertrophy.steam.SteamApi;
import io.github.skvoll.cybertrophy.steam.SteamGame;
import io.github.skvoll.cybertrophy.steam.SteamPlayerAchievement;
import io.github.skvoll.cybertrophy.steam.SteamProfile;

public final class GamesParser {
    private static final String TAG = GamesParser.class.getSimpleName();
    private static String sLanguage = "english";
    private static int sMaxFailedAttempts = 3;

    public enum Action {
        FIRST,
        ALL,
        RECENT,
    }

    private final ContentResolver mContentResolver;
    private final VolleySingleton mVolleySingleton;
    private final SteamApi mSteamApi;
    private final ProfileModel mProfileModel;
    private boolean mIsCanceled;
    private GamesParserProgressListener mProgressListener;

    public GamesParser(Context context, ProfileModel profileModel, GamesParserProgressListener progressListener) {
        mContentResolver = context.getContentResolver();
        mVolleySingleton = VolleySingleton.getInstance(context);
        mSteamApi = new SteamApi(mVolleySingleton);
        mProfileModel = profileModel;
        mProgressListener = progressListener;
    }

    public GamesParser(Context context, ProfileModel profileModel) {
        this(context, profileModel, new GamesParserProgressListener());
    }

    public static void setLanguage(String language) {
        sLanguage = language;
    }

    public static void setMaxFailedAttempts(int maxFailedAttempts) {
        sMaxFailedAttempts = maxFailedAttempts;
    }

    public void setProgressListener(GamesParserProgressListener progressListener) {
        mProgressListener = progressListener;
    }

    public void run(Action action) throws GamesParserException {
        Log.d(TAG, String.format("Parsing %s...", mProfileModel));

        updateProfile();

        LongSparseArray<GameModel> gameModels = GameModel.getMapByProfile(mContentResolver, mProfileModel);
        LongSparseArray<SteamGame> steamGames;

        switch (action) {
            case FIRST:
            case ALL:
                steamGames = getAllGames();
                break;
            case RECENT:
                steamGames = getRecentGames();
                break;
            default:
                GamesParserException exception = new ParsingFailureException(String.format("Unknown action: %s.", action));
                mProgressListener.onError(exception);

                throw exception;
        }

        if (steamGames == null) {
            Log.d(TAG, "No games to parse.");
        } else {
            Log.d(TAG, "Parsing games...");

            for (int i = 0; i < steamGames.size(); i++) {
                if (isCanceled()) {
                    GamesParserException exception = new ParsingCanceledException();
                    mProgressListener.onError(exception);

                    throw exception;
                }

                SteamGame steamGame = steamGames.valueAt(i);
                GameModel gameModel = gameModels.get(steamGame.appId, null);

                switch (action) {
                    case FIRST:
                        if (gameModel != null) {
                            Log.d(TAG, String.format("%s already parsed. Skipped.", steamGame));

                            continue;
                        }
                        break;
                    case ALL:
                        break;
                    case RECENT:
                        if (gameModel != null && gameModel.getPlaytimeForever().equals(steamGame.playtimeForever)) {
                            Log.d(TAG, String.format("%s has not been launched from last parsing. Skipped.", steamGame));

                            continue;
                        }
                        break;
                }

                mProgressListener.onProgress(i, steamGames.size(), steamGame);

                parseGame(steamGame, gameModel);
            }

            deleteRemovedGames(gameModels, steamGames);
        }

        Log.d(TAG, "Done.");
    }

    public void cancel() {
        mIsCanceled = true;
    }

    private void updateProfile() throws GamesParserException {
        Log.d(TAG, String.format("Updating %s...", mProfileModel));

        final SteamProfile steamProfile = (new Attemptable<SteamProfile>() {
            @Override
            SteamProfile attempt() throws Exception {
                SteamApi.FutureResponseListener<LongSparseArray<SteamProfile>> listener = new SteamApi.FutureResponseListener<>();
                mSteamApi.getPlayerSummaries(new Long[]{mProfileModel.getSteamId()}, listener);
                LongSparseArray<SteamProfile> steamProfiles = listener.get();

                if (steamProfiles == null || steamProfiles.indexOfKey(mProfileModel.getSteamId()) < 0) {
                    GamesParserException exception = new ParsingFailureException(String.format("Profile %s not found", mProfileModel));
                    mProgressListener.onError(exception);

                    throw exception;
                }

                return steamProfiles.get(mProfileModel.getSteamId(), null);
            }
        }).run();

        mProfileModel.loadBackgroundImage(mVolleySingleton);

        mProfileModel.setUrl(steamProfile.profileUrl)
                .setName(steamProfile.personaName)
                .setRealName(steamProfile.realName)
                .setAvatar(steamProfile.avatar)
                .setAvatarMedium(steamProfile.avatarMedium)
                .setAvatarFull(steamProfile.avatarFull)
                .setLocCountryCode(steamProfile.loccountrycode)
                .save(mContentResolver);

        Log.d(TAG, String.format("%s updated.", mProfileModel));
    }

    private LongSparseArray<SteamGame> getAllGames() throws GamesParserException {
        Log.d(TAG, "Loading games...");

        LongSparseArray<SteamGame> steamGames = (new Attemptable<LongSparseArray<SteamGame>>() {
            @Override
            public LongSparseArray<SteamGame> attempt() throws Exception {
                SteamApi.FutureResponseListener<LongSparseArray<SteamGame>> listener =
                        new SteamApi.FutureResponseListener<>();
                mSteamApi.getOwnedGames(mProfileModel.getSteamId(),
                        true, true, new Long[]{}, listener);

                return listener.get();
            }
        }).run();

        if (steamGames == null || steamGames.size() <= 0) {
            Log.d(TAG, "No games loaded.");

            return null;
        }

        Log.d(TAG, String.format("%d games loaded.", steamGames.size()));

        return steamGames;
    }

    private LongSparseArray<SteamGame> getRecentGames() throws GamesParserException {
        Log.d(TAG, "Loading recent games...");

        LongSparseArray<SteamGame> steamGames = (new Attemptable<LongSparseArray<SteamGame>>() {
            @Override
            LongSparseArray<SteamGame> attempt() throws Exception {
                SteamApi.FutureResponseListener<LongSparseArray<SteamGame>> listener =
                        new SteamApi.FutureResponseListener<>();
                mSteamApi.getRecentlyPlayedGames(mProfileModel.getSteamId(), 100, listener);

                return listener.get();
            }
        }).run();

        if (steamGames == null || steamGames.size() <= 0) {
            Log.d(TAG, "No recent games loaded.");

            return null;
        }

        Log.d(TAG, String.format("%d games loaded.", steamGames.size()));

        return steamGames;
    }

    private boolean isCanceled() {
        return mIsCanceled;
    }

    private void parseGame(SteamGame steamGame, GameModel gameModel) throws GamesParserException {
        Log.d(TAG, String.format("Parsing %s...", steamGame));

        loadSteamGameAchievements(steamGame, mProfileModel.getSteamId());

        if (gameModel == null) {
            Log.d(TAG, String.format("%s is new. Saving...", steamGame));

            gameModel = new GameModel(mProfileModel, steamGame);

            gameModel.save(mContentResolver);

            for (SteamAchievement steamAchievement : steamGame.getSteamAchievements().values()) {
                (new AchievementModel(gameModel, steamAchievement)).save(mContentResolver);
            }

            Log.d(TAG, String.format("%s saved.", gameModel));

            mProgressListener.onNewGame(gameModel);
        } else {
            boolean wasComplete = gameModel.isComplete();

            updateGame(gameModel, steamGame);

            if (steamGame.getAchievementsTotalCount() == 0) {
                if (gameModel.getAchievementsTotalCount() > 0) {
                    Log.d(TAG, String.format("%s achievements have been removed. Deleting...", steamGame));

                    deleteGameAchievements(gameModel);
                }
            } else {
                HashMap<String, AchievementModel> achievementModels = AchievementModel.getMapByGame(mContentResolver, gameModel);

                for (SteamAchievement steamAchievement : steamGame.getSteamAchievements().values()) {
                    AchievementModel achievementModel;

                    if (!achievementModels.containsKey(steamAchievement.name)) {
                        Log.d(TAG, String.format("%s has new achievement %s. Saving...", steamGame, steamAchievement));

                        achievementModel = new AchievementModel(gameModel, steamAchievement);

                        achievementModel.save(mContentResolver);

                        Log.d(TAG, String.format("%s achievement %s saved.", gameModel, achievementModel));

                        mProgressListener.onNewAchievement(gameModel, achievementModel);

                        continue;
                    }

                    achievementModel = achievementModels.get(steamAchievement.name);

                    if (steamAchievement.isUnlocked() != achievementModel.isUnlocked()) {
                        Log.d(TAG, String.format("%s achievement %s unlocked.", steamGame, steamAchievement));

                        mProgressListener.onAchievementUnlocked(gameModel, achievementModel);
                    }

                    updateAchievement(achievementModel, steamAchievement);
                }

                for (AchievementModel achievementModel : achievementModels.values()) {
                    if (!steamGame.getSteamAchievements().containsKey(achievementModel.getCode())) {
                        Log.d(TAG, String.format("%s achievement %s has been removed. Deleting...", gameModel, achievementModel));

                        achievementModel.delete(mContentResolver);

                        Log.d(TAG, String.format("%s achievement %s deleted.", gameModel, achievementModel));

                        mProgressListener.onAchievementRemoved(gameModel, achievementModel);
                    }
                }

                if (!wasComplete && gameModel.isComplete()) {
                    Log.d(TAG, "%s completed");

                    mProgressListener.onGameComplete(gameModel);
                }
            }
        }

        Log.d(TAG, String.format("%s parsing done.", steamGame));
    }

    private void deleteRemovedGames(LongSparseArray<GameModel> gameModels, LongSparseArray<SteamGame> steamGames) throws GamesParserException {
        Log.d(TAG, "Deleting removed games...");

        for (int i = 0; i < gameModels.size(); i++) {
            if (isCanceled()) {
                GamesParserException exception = new ParsingCanceledException();
                mProgressListener.onError(exception);

                throw exception;
            }

            GameModel gameModel = gameModels.valueAt(i);

            if (steamGames == null || steamGames.indexOfKey(gameModel.getAppId()) < 0) {
                Log.d(TAG, String.format("%s has been removed. Deleting...", gameModel));

                deleteGameAchievements(gameModel);

                gameModel.delete(mContentResolver);

                Log.d(TAG, String.format("%s deleted.", gameModel));

                mProgressListener.onGameRemoved(gameModel);
            }
        }
    }

    private void loadSteamGameAchievements(final SteamGame steamGame, final Long steamId) throws GamesParserException {
        Log.d(TAG, String.format("Loading %s achievements...", steamGame));

        HashMap<String, SteamAchievement> steamAchievements = (new Attemptable<HashMap<String, SteamAchievement>>() {
            @Override
            HashMap<String, SteamAchievement> attempt() throws Exception {
                SteamApi.FutureResponseListener<HashMap<String, SteamAchievement>> schemaListener =
                        new SteamApi.FutureResponseListener<>();
                mSteamApi.getSchemaForGame(steamGame.appId, sLanguage, schemaListener);
                return schemaListener.get();
            }
        }).run();

        if (steamAchievements == null || steamAchievements.size() <= 0) {
            Log.d(TAG, "No achievements loaded.");

            return;
        }

        HashMap<String, BigDecimal> percentages = (new Attemptable<HashMap<String, BigDecimal>>() {
            @Override
            HashMap<String, BigDecimal> attempt() throws Exception {
                SteamApi.FutureResponseListener<HashMap<String, BigDecimal>> percentagesListener =
                        new SteamApi.FutureResponseListener<>();
                mSteamApi.getGlobalAchievementPercentagesForApp(steamGame.appId, percentagesListener);
                return percentagesListener.get();
            }
        }).run();

        HashMap<String, SteamPlayerAchievement> playerAchievements = (new Attemptable<HashMap<String, SteamPlayerAchievement>>() {
            @Override
            HashMap<String, SteamPlayerAchievement> attempt() throws Exception {
                SteamApi.FutureResponseListener<HashMap<String, SteamPlayerAchievement>> playerAchievementsListener =
                        new SteamApi.FutureResponseListener<>();
                mSteamApi.getPlayerAchievements(steamId, steamGame.appId,
                        sLanguage, playerAchievementsListener);
                return playerAchievementsListener.get();
            }
        }).run();

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

        Log.d(TAG, String.format("%d achievements loaded.", steamGame.getAchievementsTotalCount()));
    }

    private void updateGame(GameModel gameModel, SteamGame steamGame) {
        Log.d(TAG, String.format("Updating %s...", gameModel));

        if (steamGame.playtimeForever > gameModel.getPlaytimeForever()) {
            gameModel.setLastPlay(System.currentTimeMillis() / 1000);
        }

        gameModel.setPlaytimeForever(steamGame.playtimeForever)
                .setAchievementsUnlockedCount(steamGame.getAchievementsUnlockedCount())
                .setAchievementsTotalCount(steamGame.getAchievementsTotalCount())
                .save(mContentResolver);

        Log.d(TAG, String.format("%s updated.", gameModel));
    }

    private void deleteGameAchievements(GameModel gameModel) {
        Log.d(TAG, String.format("Deleting %s achievements...", gameModel));

        String where = DataContract.AchievementEntry.COLUMN_GAME_ID + " = ?";
        String[] selectionArgs = {gameModel.getId().toString()};
        mContentResolver.delete(DataContract.AchievementEntry.URI, where, selectionArgs);

        Log.d(TAG, String.format("%s achievements deleted.", gameModel));
    }

    private void updateAchievement(AchievementModel achievementModel, SteamAchievement steamAchievement) {
        Log.d(TAG, String.format("Updating %s...", achievementModel));

        achievementModel.setName(steamAchievement.displayName)
                .setHidden(steamAchievement.hidden == 1)
                .setDescription(steamAchievement.description)
                .setPercent(steamAchievement.getPercent())
                .setUnlocked(steamAchievement.isUnlocked())
                .setUnlockTime(steamAchievement.getUnlockTime())
                .save(mContentResolver);

        Log.d(TAG, String.format("%s updated.", achievementModel));
    }

    public static class GamesParserProgressListener {
        public void onError(GamesParserException e) {
            // Implementation is not necessary
        }

        public void onProgress(int processed, int total, SteamGame steamGame) {
            // Implementation is not necessary
        }

        public void onNewGame(GameModel gameModel) {
            // Implementation is not necessary
        }

        public void onGameRemoved(GameModel gameModel) {
            // Implementation is not necessary
        }

        public void onNewAchievement(GameModel gameModel, AchievementModel achievementModel) {
            // Implementation is not necessary
        }

        public void onAchievementRemoved(GameModel gameModel, AchievementModel achievementModel) {
            // Implementation is not necessary
        }

        public void onAchievementUnlocked(GameModel gameModel, AchievementModel achievementModel) {
            // Implementation is not necessary
        }

        public void onGameComplete(GameModel gameModel) {
            // Implementation is not necessary
        }
    }

    public static abstract class GamesParserException extends Exception {
        GamesParserException() {
        }

        GamesParserException(String message) {
            super(message);
        }

        GamesParserException(Throwable cause) {
            super(cause);
        }
    }

    public static final class ParsingFailureException extends GamesParserException {
        ParsingFailureException(String message) {
            super(message);
        }

        ParsingFailureException(Throwable cause) {
            super(cause);
        }
    }

    public static final class ParsingCanceledException extends GamesParserException {
    }

    public static final class ProfileIsPrivateException extends GamesParserException {
        ProfileIsPrivateException(Throwable cause) {
            super(cause);
        }
    }

    private abstract class Attemptable<T> {
        private final int mMaxAttempts = sMaxFailedAttempts;

        private int mAttempt = 0;

        T run() throws GamesParserException {
            return run(mMaxAttempts);
        }

        T run(int attempts) throws GamesParserException {
            while (true) {
                try {
                    return attempt();
                } catch (AuthFailureError e) {
                    Log.e(TAG, "Attempt failed.", e);

                    GamesParserException exception = new ProfileIsPrivateException(e);
                    mProgressListener.onError(exception);

                    throw exception;
                } catch (InterruptedException | VolleyError e) {
                    Log.w(TAG, String.format("Attempt %d failed.", mAttempt + 1), e);

                    mAttempt++;

                    if (mAttempt >= attempts) {
                        Log.e(TAG, "Attempt failed.", e);

                        GamesParserException exception = new ParsingFailureException(e);
                        mProgressListener.onError(exception);

                        throw exception;
                    }

                    Log.w(TAG, String.format("Parsing error. Retrying. Remaining attempts: %d.", attempts - mAttempt));
                } catch (Exception e) {
                    Log.e(TAG, "Attempt failed.", e);

                    GamesParserException exception = new ParsingFailureException(e);
                    mProgressListener.onError(exception);

                    throw exception;
                }
            }
        }

        abstract T attempt() throws Exception;
    }
}
