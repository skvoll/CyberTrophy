package io.github.skvoll.cybertrophy;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.achievements.list.AchievementsListFragment;
import io.github.skvoll.cybertrophy.data.AchievementModel;
import io.github.skvoll.cybertrophy.data.GameModel;
import io.github.skvoll.cybertrophy.data.ProfileModel;

public class DashboardFragment extends Fragment implements
        AchievementsListFragment.OnItemClickListener {
    private ProfileModel mProfileModel;
    private View mRootView;

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

        mRootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        setGame();

        return mRootView;
    }

    private void setGame() {
        if (getContext() == null || getActivity() == null) {
            return;
        }

        ContentResolver contentResolver = getContext().getContentResolver();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        LinearLayout llGame = mRootView.findViewById(R.id.ll_game);

        final GameModel gameModel = GameModel.getCurrent(contentResolver, mProfileModel);

        if (gameModel == null) {
            llGame.setVisibility(View.GONE);

            return;
        }

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
        pbGameProgress.setScaleY(2f);
        pbGameProgress.setMax(gameModel.getAchievementsUnlockedCount());
        pbGameProgress.setProgress(gameModel.getAchievementsTotalCount());

        cvGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), GameActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra(GameActivity.KEY_GAME_ID, gameModel.getId());

                startActivity(intent);
            }
        });

        ArrayList<AchievementModel> achievementModels = AchievementModel.getByGame(
                contentResolver, gameModel, AchievementModel.UNLOCKED, 3);

        if (achievementModels.size() > 0) {
            LinearLayout llRecentAchievements = llGame.findViewById(R.id.ll_recent_achievements);
            View vRecentAchievement;
            CardView cvContainer;
            ImageView ivAchievementIcon;
            TextView tvAchievementName;
            TextView tvAchievementDescription;
            TextView tvAchievementTime;

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
        }

        AchievementsListFragment achievementsListFragment = AchievementsListFragment.newInstance(
                gameModel.getId(), AchievementsListFragment.ACHIEVEMENTS_STATUS_LOCKED,
                AchievementsListFragment.VIEW_TYPE_SMALL, this);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_locked_achievements, achievementsListFragment).commit();
    }

    @Override
    public void onClick(AchievementModel achievementModel) {
        showAchievement(achievementModel);
    }

    private void showAchievement(AchievementModel achievementModel) {
        if (getActivity() == null) {
            return;
        }

        Fragment fragment = AchievementFragment.newInstance(achievementModel);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_drawer, fragment).commit();

        ((DrawerLayout) mRootView).setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        ((DrawerLayout) mRootView).openDrawer(Gravity.END);
    }
}
