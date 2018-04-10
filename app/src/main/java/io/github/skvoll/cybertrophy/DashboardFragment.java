package io.github.skvoll.cybertrophy;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import io.github.skvoll.cybertrophy.achievements.list.AchievementsListAdapter;
import io.github.skvoll.cybertrophy.data.AchievementModel;
import io.github.skvoll.cybertrophy.data.GameModel;
import io.github.skvoll.cybertrophy.data.ProfileModel;

public final class DashboardFragment extends Fragment implements
        AchievementsListAdapter.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {
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

        (new LoadDataTask(this)).execute(mProfileModel);

        return mRootView;
    }

    private void setData(LoadDataTask.LoadDataTaskResult result) {
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
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
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

    @Override
    public void onClick(AchievementModel achievementModel) {
        showAchievement(achievementModel);
    }

    @Override
    public void onRefresh() {
        if (getContext() == null) {
            mSrlRefresh.setRefreshing(false);

            return;
        }

        GameModel gameModel = GameModel.getCurrent(getContext().getContentResolver(), mProfileModel);

        if (gameModel == null) {
            mSrlRefresh.setRefreshing(false);

            setData(null);

            return;
        }

        if (mProfileModel.isInitialized()) {
            (new UpdateGameTask(this, mProfileModel)).execute();
        } else {
            (new LoadDataTask(this)).execute(mProfileModel);
        }
    }

    private static class LoadDataTask extends AsyncTask<ProfileModel, Void, LoadDataTask.LoadDataTaskResult> {
        private WeakReference<DashboardFragment> mFragmentWeakReference;
        private ContentResolver mContentResolver;

        LoadDataTask(DashboardFragment fragment) {
            if (fragment.getContext() == null) {
                return;
            }

            mFragmentWeakReference = new WeakReference<>(fragment);
            mContentResolver = fragment.getContext().getContentResolver();
        }

        @Override
        protected LoadDataTaskResult doInBackground(ProfileModel... profileModels) {
            if (mContentResolver == null) {
                return null;
            }

            ProfileModel profileModel = profileModels[0];

            if (profileModel == null) {
                return null;
            }

            GameModel gameModel = GameModel.getCurrent(mContentResolver, profileModel);

            if (gameModel == null) {
                return null;
            }

            ArrayList<AchievementModel> recentAchievementModels = AchievementModel.getByGame(
                    mContentResolver, gameModel, AchievementModel.STATUS_UNLOCKED, 3);
            ArrayList<AchievementModel> lockedAchievementModels = AchievementModel.getByGame(
                    mContentResolver, gameModel, AchievementModel.STATUS_LOCKED);

            return new LoadDataTaskResult(gameModel, recentAchievementModels, lockedAchievementModels);
        }

        @Override
        protected void onPostExecute(LoadDataTaskResult result) {
            if (mFragmentWeakReference == null) {
                return;
            }

            DashboardFragment fragment = mFragmentWeakReference.get();

            if (fragment == null) {
                return;
            }

            fragment.setData(result);
        }

        static class LoadDataTaskResult {
            GameModel gameModel;
            ArrayList<AchievementModel> recentAchievementModels;
            ArrayList<AchievementModel> lockedAchievementModels;

            LoadDataTaskResult(GameModel gameModel,
                               ArrayList<AchievementModel> recentAchievementModels,
                               ArrayList<AchievementModel> lockedAchievementModels) {
                this.gameModel = gameModel;
                this.recentAchievementModels = recentAchievementModels;
                this.lockedAchievementModels = lockedAchievementModels;
            }
        }
    }

    private static class UpdateGameTask extends GamesParserTask {
        private WeakReference<DashboardFragment> mFragmentWeakReference;
        private ProfileModel mProfileModel;

        UpdateGameTask(DashboardFragment fragment, ProfileModel profileModel) {
            super(fragment.getContext(), profileModel, ACTION_RECENT);

            mFragmentWeakReference = new WeakReference<>(fragment);
            mProfileModel = profileModel;
        }

        @Override
        protected Boolean doInBackground(Long... appIds) {
            if (!mProfileModel.isInitialized()) {
                return false;
            }

            return super.doInBackground(appIds);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (mFragmentWeakReference == null) {
                return;
            }

            DashboardFragment fragment = mFragmentWeakReference.get();

            if (fragment == null) {
                return;
            }

            if (success) {
                (new LoadDataTask(fragment)).execute(mProfileModel);
            }
        }
    }
}
