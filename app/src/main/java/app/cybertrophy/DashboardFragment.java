package app.cybertrophy;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import app.cybertrophy.achievements.list.AchievementsListAdapter;
import app.cybertrophy.data.AchievementModel;
import app.cybertrophy.data.GameModel;
import app.cybertrophy.data.GamesParser;
import app.cybertrophy.data.ProfileModel;

public final class DashboardFragment extends Fragment implements
        AchievementsListAdapter.OnItemClickListener,
        LoaderManager.LoaderCallbacks<DashboardFragment.DataLoader.DataLoaderResult>,
        SwipeRefreshLayout.OnRefreshListener {
    private static int LOADER_ID = 1;

    private final ProfileObserver mProfileObserver = new ProfileObserver(new Handler());

    private ProfileModel mProfileModel;
    private ViewGroup mRootView;
    private SwipeRefreshLayout mSrlRefresh;

    public DashboardFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getContext() == null) {
            return null;
        }

        mProfileModel = ProfileModel.getActive(getContext().getContentResolver());

        if (mProfileModel == null) {
            return null;
        }

        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_dashboard, container, false);

        mSrlRefresh = mRootView.findViewById(R.id.srl_refresh);
        mSrlRefresh.setColorSchemeColors(getResources().getColor(R.color.secondary));
        mSrlRefresh.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.primary));
        mSrlRefresh.setOnRefreshListener(this);

        RecyclerView rvLockedAchievements = mRootView.findViewById(R.id.rv_locked_achievements);
        rvLockedAchievements.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvLockedAchievements.setAdapter(new AchievementsListAdapter(getContext(), new ArrayList<AchievementModel>(),
                this, AchievementsListAdapter.TYPE_SMALL));

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getContext() == null) {
            return;
        }

        if (!mProfileModel.isInitialized()) {
            getContext().getContentResolver().registerContentObserver(
                    mProfileModel.getUri(mProfileModel.getId()), true, mProfileObserver);

            mProfileObserver.checkProfile();
        } else {
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (getContext() == null) {
            return;
        }

        getContext().getContentResolver().unregisterContentObserver(mProfileObserver);
    }

    @Override
    public void onClick(AchievementModel achievementModel) {
        showAchievement(achievementModel);
    }

    @NonNull
    @Override
    public Loader<DataLoader.DataLoaderResult> onCreateLoader(int id, @Nullable Bundle args) {
        return new DataLoader(getContext(), mProfileModel);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<DataLoader.DataLoaderResult> loader, DataLoader.DataLoaderResult data) {
        setData(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<DataLoader.DataLoaderResult> loader) {
    }

    @Override
    public void onRefresh() {
        if (getContext() == null) {
            return;
        }

        Loader loader = getLoaderManager().getLoader(LOADER_ID);

        if (loader == null) {
            return;
        }

        loader.startLoading();
    }

    private void setData(DataLoader.DataLoaderResult result) {
        mSrlRefresh.setRefreshing(false);
        mRootView.findViewById(android.R.id.progress).setVisibility(View.GONE);

        if (result == null) {
            mRootView.findViewById(R.id.sv_container).setVisibility(View.GONE);
            mRootView.findViewById(android.R.id.empty).setVisibility(View.VISIBLE);

            return;
        }

        setGame(result.gameModel);
        setRecentAchievements(result.recentAchievementModels);
        setLockedAchievements(result.lockedAchievementModels);
    }

    private void setGame(final GameModel gameModel) {
        LinearLayout llGame = mRootView.findViewById(R.id.ll_game);

        CardView cvGame = llGame.findViewById(R.id.cv_item);
        ImageView ivGameLogo = llGame.findViewById(R.id.iv_game_logo);
        TextView tvGameName = llGame.findViewById(R.id.tv_game_name);
        TextView tvGameProgress = llGame.findViewById(R.id.tv_game_progress);
        ProgressBar pbGameProgress = llGame.findViewById(R.id.pb_game_progress);

        GlideApp.with(this).load(gameModel.getLogoUrl())
                .placeholder(R.drawable.game_logo_empty)
                .into(ivGameLogo);
        tvGameName.setText(gameModel.getName());
        tvGameProgress.setText(getResources().getString(R.string.dashboard_game_achievements_progress,
                gameModel.getAchievementsUnlockedCount(),
                gameModel.getAchievementsTotalCount()));
        pbGameProgress.setMax(gameModel.getAchievementsTotalCount());
        pbGameProgress.setProgress(gameModel.getAchievementsUnlockedCount());

        cvGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getContext() == null || getActivity() == null) {
                    return;
                }

                Intent intent = new Intent(getContext(), GameActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra(GameActivity.KEY_GAME_ID, gameModel.getId());
                startActivity(intent);
            }
        });

        mRootView.findViewById(android.R.id.empty).setVisibility(View.GONE);
        mRootView.findViewById(R.id.sv_container).setVisibility(View.VISIBLE);
    }

    private void setRecentAchievements(ArrayList<AchievementModel> achievementModels) {
        if (achievementModels.size() == 0) {
            return;
        }

        LinearLayout llRecentAchievements = mRootView.findViewById(R.id.ll_recent_achievements);

        View vRecentAchievement;
        CardView cvContainer;
        ImageView ivAchievementIcon;
        TextView tvAchievementName;
        TextView tvAchievementDescription;
        TextView tvAchievementTime;

        llRecentAchievements.removeAllViews();

        for (final AchievementModel achievementModel : achievementModels) {
            vRecentAchievement = getLayoutInflater().inflate(
                    R.layout.fragment_dashboard_achievement, llRecentAchievements, false);

            cvContainer = vRecentAchievement.findViewById(R.id.cv_item);
            ivAchievementIcon = vRecentAchievement.findViewById(R.id.iv_achievement_icon);
            tvAchievementName = vRecentAchievement.findViewById(R.id.tv_achievement_name);
            tvAchievementDescription = vRecentAchievement.findViewById(R.id.tv_achievement_description);
            tvAchievementTime = vRecentAchievement.findViewById(R.id.tv_achievement_time);

            GlideApp.with(this).load(achievementModel.getIconUrl())
                    .placeholder(R.drawable.achievement_icon_empty)
                    .into(ivAchievementIcon);
            tvAchievementName.setText(achievementModel.getName());
            if (achievementModel.getDescription() != null) {
                tvAchievementDescription.setText(achievementModel.getDescription());
            }
            tvAchievementTime.setText(DateUtils.getRelativeTimeSpanString(
                    achievementModel.getUnlockTime() * 1000L));

            cvContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAchievement(achievementModel);
                }
            });

            llRecentAchievements.addView(vRecentAchievement);
        }

        mRootView.findViewById(R.id.pb_recent_achievements).setVisibility(View.GONE);
        llRecentAchievements.setVisibility(View.VISIBLE);
    }

    private void setLockedAchievements(ArrayList<AchievementModel> achievementModels) {
        if (achievementModels.size() == 0) {
            return;
        }

        RecyclerView rvLockedAchievements = mRootView.findViewById(R.id.rv_locked_achievements);
        rvLockedAchievements.swapAdapter(new AchievementsListAdapter(getContext(), achievementModels,
                this, AchievementsListAdapter.TYPE_SMALL), false);

        mRootView.findViewById(R.id.pb_locked_achievements).setVisibility(View.GONE);
    }

    private void showAchievement(AchievementModel achievementModel) {
        if (getActivity() == null) {
            return;
        }

        AchievementPreviewDialogFragment achievementPreviewDialogFragment
                = AchievementPreviewDialogFragment.newInstance(achievementModel);

        achievementPreviewDialogFragment.show(getActivity().getSupportFragmentManager(),
                achievementPreviewDialogFragment.getTag());
    }

    static class DataLoader extends AsyncTaskLoader<DataLoader.DataLoaderResult> {
        private final ProfileModel mProfileModel;
        private final ContentResolver mContentResolver;
        private GamesParser mGamesParser;
        private DataLoaderResult mResult;

        DataLoader(Context context, ProfileModel profileModel) {
            super(context);

            mProfileModel = profileModel;
            mContentResolver = context.getContentResolver();
            mGamesParser = new GamesParser(getContext(), mProfileModel);
        }

        @Override
        public DataLoaderResult loadInBackground() {
            try {
                if (mGamesParser.isIdle() || mGamesParser.isFinished()) {
                    mGamesParser.run(GamesParser.Action.RECENT);
                } else {
                    return mResult;
                }
            } catch (GamesParser.GamesParserException e) {
                return mResult;
            }

            GameModel gameModel = GameModel.getCurrent(mContentResolver, mProfileModel);

            if (gameModel == null) {
                return null;
            }

            ArrayList<AchievementModel> recentAchievementModels = AchievementModel.getByGame(
                    mContentResolver, gameModel, AchievementModel.STATUS_UNLOCKED, 3);
            ArrayList<AchievementModel> lockedAchievementModels = AchievementModel.getByGame(
                    mContentResolver, gameModel, AchievementModel.STATUS_LOCKED);

            mResult = new DataLoaderResult(gameModel, recentAchievementModels, lockedAchievementModels);

            return mResult;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        protected boolean onCancelLoad() {
            mGamesParser.cancel();

            return super.onCancelLoad();
        }

        @Override
        protected void onStopLoading() {
            mGamesParser.cancel();
        }

        static class DataLoaderResult {
            final GameModel gameModel;
            final ArrayList<AchievementModel> recentAchievementModels;
            final ArrayList<AchievementModel> lockedAchievementModels;

            DataLoaderResult(GameModel gameModel,
                             ArrayList<AchievementModel> recentAchievementModels,
                             ArrayList<AchievementModel> lockedAchievementModels) {
                this.gameModel = gameModel;
                this.recentAchievementModels = recentAchievementModels;
                this.lockedAchievementModels = lockedAchievementModels;
            }
        }
    }

    private class ProfileObserver extends ContentObserver {
        ProfileObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            checkProfile();
        }

        void checkProfile() {
            if (getContext() == null) {
                return;
            }

            ProfileModel profileModel = ProfileModel.getById(
                    getContext().getContentResolver(), mProfileModel.getId());

            if (profileModel == null || !profileModel.isInitialized()) {
                return;
            }

            mProfileModel = profileModel;
            getContext().getContentResolver().unregisterContentObserver(this);
            getLoaderManager().initLoader(LOADER_ID, null, DashboardFragment.this);
        }
    }
}
