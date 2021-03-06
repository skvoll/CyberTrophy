package app.cybertrophy.data;

import android.content.ContentResolver;
import android.content.Context;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;

import java.math.BigDecimal;
import java.util.HashMap;

import app.cybertrophy.VolleySingleton;
import app.cybertrophy.steam.SteamAchievement;
import app.cybertrophy.steam.SteamApi;
import app.cybertrophy.steam.SteamGame;
import app.cybertrophy.steam.SteamPlayerAchievement;
import app.cybertrophy.steam.SteamProfile;

public final class GamesParser {
    private static final String TAG = GamesParser.class.getSimpleName();
    private static int sInstanceId = 1;
    private static String sLanguage = "english";
    private static int sMaxFailedAttempts = 3;

    public enum Status {
        IDLE,
        RUNNING,
        CANCELED,
        FINISHED,
    }

    public enum Action {
        FIRST,
        ALL,
        RECENT,
    }

    private final String mTag;
    private final ContentResolver mContentResolver;
    private final VolleySingleton mVolleySingleton;
    private final SteamApi mSteamApi;
    private final ProfileModel mProfileModel;
    private Status mStatus = Status.IDLE;
    private GamesParserProgressListener mProgressListener;

    public GamesParser(Context context, ProfileModel profileModel, GamesParserProgressListener progressListener) {
        mTag = TAG + " #" + sInstanceId++;

        mContentResolver = context.getContentResolver();
        mVolleySingleton = VolleySingleton.getInstance(context);
        mSteamApi = new SteamApi(mVolleySingleton);
        mProfileModel = profileModel;
        mProgressListener = progressListener;
    }

    public GamesParser(Context context, ProfileModel profileModel) {
        this(context, profileModel, new GamesParserProgressListener() {
        });
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
        setStatus(Status.RUNNING);

        Log.d(mTag, String.format("Parsing %s...", mProfileModel));

        LongSparseArray<GameModel> gameModels = GameModel.getMapByProfile(mContentResolver, mProfileModel);
        LongSparseArray<SteamGame> steamGames;

        switch (action) {
            case FIRST:
                updateProfile();
                steamGames = getAllGames();
                break;
            case ALL:
                if (!mProfileModel.isInitialized()) {
                    Log.d(mTag, String.format("%s is not initialized.", mProfileModel));

                    throw error(new ProfileIsNotInitializedException());
                }

                updateProfile();
                steamGames = getAllGames();
                break;
            case RECENT:
                if (!mProfileModel.isInitialized()) {
                    Log.d(mTag, String.format("%s is not initialized.", mProfileModel));

                    throw error(new ProfileIsNotInitializedException());
                }

                updateProfile();
                steamGames = getRecentGames();
                break;
            default:
                throw error(new ParsingFailureException(String.format("Unknown action: %s.", action)));
        }

        if (steamGames == null) {
            Log.d(mTag, "No games to parse.");
        } else {
            Log.d(mTag, "Parsing games...");

            for (int i = 0; i < steamGames.size(); i++) {
                if (isCanceled()) {
                    throw error(new ParsingCanceledException());
                }

                SteamGame steamGame = steamGames.valueAt(i);
                GameModel gameModel = gameModels.get(steamGame.appId, null);

                switch (action) {
                    case FIRST:
                        if (gameModel != null) {
                            Log.d(mTag, String.format("%s already parsed. Skipped.", steamGame));

                            continue;
                        }
                        break;
                    case ALL:
                        break;
                    case RECENT:
                        if (gameModel != null && gameModel.getPlaytimeForever().equals(steamGame.playtimeForever)) {
                            Log.d(mTag, String.format("%s has not been launched from last parsing. Skipped.", steamGame));

                            continue;
                        }
                        break;
                }

                mProgressListener.onProgress(i, steamGames.size(), steamGame);

                parseGame(steamGame, gameModel);
            }

            if (action == Action.ALL) {
                deleteRemovedGames(gameModels, steamGames);
            }
        }

        Log.d(mTag, String.format("%s parsing done.", mProfileModel));

        setStatus(Status.FINISHED);
    }

    public void cancel() {
        if (isRunning()) {
            Log.d(mTag, "Canceling...");

            setStatus(Status.CANCELED);
        }
    }

    public Status getStatus() {
        return mStatus;
    }

    private void setStatus(Status status) {
        mStatus = status;

        Log.d(mTag, getStatus().toString());
    }

    public boolean isIdle() {
        return mStatus == Status.IDLE;
    }

    public boolean isRunning() {
        return mStatus == Status.RUNNING;
    }

    public boolean isCanceled() {
        return mStatus == Status.CANCELED;
    }

    public boolean isFinished() {
        return mStatus == Status.FINISHED;
    }

    private GamesParserException error(GamesParserException e) {
        setStatus(Status.FINISHED);

        mProgressListener.onError(e);

        return e;
    }

    private void updateProfile() throws GamesParserException {
        Log.d(mTag, String.format("Updating %s...", mProfileModel));

        final SteamProfile steamProfile = (new Attemptable<SteamProfile>() {
            @Override
            SteamProfile attempt() throws Exception {
                SteamApi.FutureResponseListener<LongSparseArray<SteamProfile>> listener = new SteamApi.FutureResponseListener<>();
                mSteamApi.getPlayerSummaries(new Long[]{mProfileModel.getSteamId()}, listener);
                LongSparseArray<SteamProfile> steamProfiles = listener.get();

                if (steamProfiles == null || steamProfiles.indexOfKey(mProfileModel.getSteamId()) < 0) {
                    throw error(new ParsingFailureException(String.format("Profile %s not found", mProfileModel)));
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

        Log.d(mTag, String.format("%s updated.", mProfileModel));
    }

    private LongSparseArray<SteamGame> getAllGames() throws GamesParserException {
        Log.d(mTag, "Loading games...");

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
            Log.d(mTag, "No games loaded.");

            return null;
        }

        Log.d(mTag, String.format("%d games loaded.", steamGames.size()));

        return steamGames;
    }

    private LongSparseArray<SteamGame> getRecentGames() throws GamesParserException {
        Log.d(mTag, "Loading recent games...");

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
            Log.d(mTag, "No recent games loaded.");

            return null;
        }

        Log.d(mTag, String.format("%d games loaded.", steamGames.size()));

        return steamGames;
    }

    private void parseGame(SteamGame steamGame, GameModel gameModel) throws GamesParserException {
        Log.d(mTag, String.format("Parsing %s...", steamGame));

        loadSteamGameAchievements(steamGame, mProfileModel.getSteamId());

        if (gameModel == null) {
            Log.d(mTag, String.format("%s is new. Saving...", steamGame));

            gameModel = new GameModel(mProfileModel, steamGame);

            gameModel.save(mContentResolver);

            for (SteamAchievement steamAchievement : steamGame.getSteamAchievements().values()) {
                (new AchievementModel(gameModel, steamAchievement)).save(mContentResolver);
            }

            Log.d(mTag, String.format("%s saved.", gameModel));

            mProgressListener.onNewGame(gameModel);
        } else {
            boolean wasComplete = gameModel.isComplete();

            updateGame(gameModel, steamGame);

            if (steamGame.getAchievementsTotalCount() == 0) {
                if (gameModel.getAchievementsTotalCount() > 0) {
                    Log.d(mTag, String.format("%s achievements have been removed. Deleting...", steamGame));

                    deleteGameAchievements(gameModel);
                }
            } else {
                HashMap<String, AchievementModel> achievementModels = AchievementModel.getMapByGame(mContentResolver, gameModel);

                for (SteamAchievement steamAchievement : steamGame.getSteamAchievements().values()) {
                    AchievementModel achievementModel;

                    if (!achievementModels.containsKey(steamAchievement.name)) {
                        Log.d(mTag, String.format("%s has new achievement %s. Saving...", steamGame, steamAchievement));

                        achievementModel = new AchievementModel(gameModel, steamAchievement);

                        achievementModel.save(mContentResolver);

                        Log.d(mTag, String.format("%s achievement %s saved.", gameModel, achievementModel));

                        mProgressListener.onNewAchievement(gameModel, achievementModel);

                        continue;
                    }

                    achievementModel = achievementModels.get(steamAchievement.name);

                    if (steamAchievement.isUnlocked() != achievementModel.isUnlocked()) {
                        Log.d(mTag, String.format("%s achievement %s unlocked.", steamGame, steamAchievement));

                        mProgressListener.onAchievementUnlocked(gameModel, achievementModel);
                    }

                    updateAchievement(achievementModel, steamAchievement);
                }

                for (AchievementModel achievementModel : achievementModels.values()) {
                    if (!steamGame.getSteamAchievements().containsKey(achievementModel.getCode())) {
                        Log.d(mTag, String.format("%s achievement %s has been removed. Deleting...", gameModel, achievementModel));

                        achievementModel.delete(mContentResolver);

                        Log.d(mTag, String.format("%s achievement %s deleted.", gameModel, achievementModel));

                        mProgressListener.onAchievementRemoved(gameModel, achievementModel);
                    }
                }

                if (!wasComplete && gameModel.isComplete()) {
                    Log.d(mTag, "%s completed");

                    mProgressListener.onGameComplete(gameModel);
                }
            }
        }

        Log.d(mTag, String.format("%s parsing done.", steamGame));
    }

    private void deleteRemovedGames(LongSparseArray<GameModel> gameModels, LongSparseArray<SteamGame> steamGames) throws GamesParserException {
        Log.d(mTag, "Deleting removed games...");

        for (int i = 0; i < gameModels.size(); i++) {
            if (isCanceled()) {
                throw error(new ParsingCanceledException());
            }

            GameModel gameModel = gameModels.valueAt(i);

            if (steamGames == null || steamGames.indexOfKey(gameModel.getAppId()) < 0) {
                Log.d(mTag, String.format("%s has been removed. Deleting...", gameModel));

                deleteGameAchievements(gameModel);

                gameModel.delete(mContentResolver);

                Log.d(mTag, String.format("%s deleted.", gameModel));

                mProgressListener.onGameRemoved(gameModel);
            }
        }
    }

    private void loadSteamGameAchievements(final SteamGame steamGame, final Long steamId) throws GamesParserException {
        Log.d(mTag, String.format("Loading %s achievements...", steamGame));

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
            Log.d(mTag, "No achievements loaded.");

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

        Log.d(mTag, String.format("%d achievements loaded.", steamGame.getAchievementsTotalCount()));
    }

    private void updateGame(GameModel gameModel, SteamGame steamGame) {
        Log.d(mTag, String.format("Updating %s...", gameModel));

        if (steamGame.playtimeForever > gameModel.getPlaytimeForever()) {
            gameModel.setLastPlay(System.currentTimeMillis() / 1000);
        }

        gameModel.setPlaytimeForever(steamGame.playtimeForever)
                .setAchievementsUnlockedCount(steamGame.getAchievementsUnlockedCount())
                .setAchievementsTotalCount(steamGame.getAchievementsTotalCount())
                .save(mContentResolver);

        Log.d(mTag, String.format("%s updated.", gameModel));
    }

    private void deleteGameAchievements(GameModel gameModel) {
        Log.d(mTag, String.format("Deleting %s achievements...", gameModel));

        String where = DataContract.AchievementEntry.COLUMN_GAME_ID + " = ?";
        String[] selectionArgs = {gameModel.getId().toString()};
        mContentResolver.delete(DataContract.AchievementEntry.URI, where, selectionArgs);

        Log.d(mTag, String.format("%s achievements deleted.", gameModel));
    }

    private void updateAchievement(AchievementModel achievementModel, SteamAchievement steamAchievement) {
        Log.d(mTag, String.format("Updating %s...", achievementModel));

        achievementModel.setName(steamAchievement.displayName)
                .setHidden(steamAchievement.hidden == 1)
                .setDescription(steamAchievement.description)
                .setPercent(steamAchievement.getPercent())
                .setUnlocked(steamAchievement.isUnlocked())
                .setUnlockTime(steamAchievement.getUnlockTime())
                .save(mContentResolver);

        Log.d(mTag, String.format("%s updated.", achievementModel));
    }

    public interface GamesParserProgressListener {
        default void onError(GamesParserException e) {
        }

        default void onProgress(int processed, int total, SteamGame steamGame) {
        }

        default void onNewGame(GameModel gameModel) {
        }

        default void onGameRemoved(GameModel gameModel) {
        }

        default void onNewAchievement(GameModel gameModel, AchievementModel achievementModel) {
        }

        default void onAchievementRemoved(GameModel gameModel, AchievementModel achievementModel) {
        }

        default void onAchievementUnlocked(GameModel gameModel, AchievementModel achievementModel) {
        }

        default void onGameComplete(GameModel gameModel) {
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
    }

    public static final class ParsingCanceledException extends GamesParserException {
    }

    public static final class ProfileIsNotInitializedException extends GamesParserException {
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
                    Log.e(mTag, "Attempt failed.", e);

                    throw error(new ProfileIsPrivateException(e));
                } catch (InterruptedException | VolleyError e) {
                    Log.w(mTag, String.format("Attempt %d failed.", mAttempt + 1), e);

                    mAttempt++;

                    if (mAttempt >= attempts) {
                        Log.e(mTag, "Attempt failed.", e);

                        throw error(new ProfileIsPrivateException(e));
                    }

                    Log.w(mTag, String.format("Parsing error. Retrying. Remaining attempts: %d.", attempts - mAttempt));
                } catch (Exception e) {
                    Log.e(mTag, "Attempt failed.", e);

                    throw error(new ProfileIsPrivateException(e));
                }
            }
        }

        abstract T attempt() throws Exception;
    }
}
