package io.github.skvoll.cybertrophy.dashboard;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.GlideApp;
import io.github.skvoll.cybertrophy.R;

final class DashboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = DashboardAdapter.class.getSimpleName();

    private static int VISIBLE_THRESHOLD = 10;

    private Context mContext;
    private DashboardOnEndReachListener mDashboardOnEndReachListener;
    private ArrayList<DashboardItem> mDashboardItems;
    private DashboardOnItemClickListener mDashboardOnItemClickListener;

    private boolean mIsLoading = false;

    DashboardAdapter(
            Context context,
            RecyclerView recyclerView,
            ArrayList<DashboardItem> dashboardItems,
            DashboardOnItemClickListener dashboardOnItemClickListener) {
        mContext = context;
        mDashboardItems = dashboardItems;
        mDashboardOnItemClickListener = dashboardOnItemClickListener;

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                int itemsCount = linearLayoutManager.getItemCount();
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!mIsLoading && itemsCount <= (lastVisibleItem + VISIBLE_THRESHOLD)) {
                    if (mDashboardOnEndReachListener != null) {
                        mDashboardOnEndReachListener.onEndReached();
                    }

                    mIsLoading = true;
                }
            }
        });
    }

    void setLoaded() {
        mIsLoading = false;
    }

    void setOnEndReachListener(DashboardOnEndReachListener dashboardOnEndReachListener) {
        mDashboardOnEndReachListener = dashboardOnEndReachListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mDashboardItems.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case DashboardItem.TYPE_CURRENT_GAME:
                return new CurrentGameViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.fragment_dashboard_item_current_game, parent, false)
                );
            case DashboardItem.TYPE_NEW_GAME:
                return new NewGameViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.fragment_dashboard_item_new_game, parent, false)
                );
            case DashboardItem.TYPE_ACHIEVEMENT_UNLOCKED:
                return new AchievementUnlockedViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.fragment_dashboard_item_achievement_unlocked, parent, false)
                );
            case DashboardItem.TYPE_GAME_COMPLETE:
                return new GameCompleteViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.fragment_dashboard_item_game_complete, parent, false)
                );
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final DashboardItem dashboardItem = mDashboardItems.get(position);

        switch (viewHolder.getItemViewType()) {
            case DashboardItem.TYPE_CURRENT_GAME:
                CurrentGameViewHolder currentGameViewHolder = (CurrentGameViewHolder) viewHolder;

                currentGameViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDashboardOnItemClickListener.onClick(dashboardItem);
                    }
                });

                GlideApp.with(mContext).load(dashboardItem.getAppLogoUrl())
                        .placeholder(R.drawable.no_game_logo)
                        .into(currentGameViewHolder.gameLogo);
                currentGameViewHolder.gameName.setText(dashboardItem.getAppName());
                currentGameViewHolder.gameProgress.setText(
                        mContext.getResources().getString(R.string.game_achievements_progress,
                                dashboardItem.getAppAchievementsUnlockedCount(),
                                dashboardItem.getAppAchievementsTotalTount())
                );
                currentGameViewHolder.gameProgressBar.setScaleY(2f);
                currentGameViewHolder.gameProgressBar.setMax(dashboardItem.getAppAchievementsTotalTount());
                currentGameViewHolder.gameProgressBar.setProgress(dashboardItem.getAppAchievementsUnlockedCount());
                break;
            case DashboardItem.TYPE_NEW_GAME:
                NewGameViewHolder newGameViewHolder = (NewGameViewHolder) viewHolder;

                newGameViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDashboardOnItemClickListener.onClick(dashboardItem);
                    }
                });

                GlideApp.with(mContext).load(dashboardItem.getAppLogoUrl())
                        .placeholder(R.drawable.no_game_logo)
                        .into(newGameViewHolder.gameLogo);
                newGameViewHolder.gameName.setText(dashboardItem.getAppName());
                newGameViewHolder.time.setText(
                        DateUtils.getRelativeTimeSpanString(dashboardItem.getTime() * 1000L)
                );
                break;
            case DashboardItem.TYPE_ACHIEVEMENT_UNLOCKED:
                AchievementUnlockedViewHolder achievementUnlockedViewHolder = (AchievementUnlockedViewHolder) viewHolder;

                achievementUnlockedViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDashboardOnItemClickListener.onClick(dashboardItem);
                    }
                });

                GlideApp.with(mContext).load(dashboardItem.getAchievementIconUrl())
                        .placeholder(R.drawable.no_achievement_icon)
                        .into(achievementUnlockedViewHolder.achievementIcon);
                achievementUnlockedViewHolder.achievementName.setText(dashboardItem.getAchievementName());
                achievementUnlockedViewHolder.achievementTime.setText(
                        DateUtils.getRelativeTimeSpanString(dashboardItem.getAchievementUnlockTime() * 1000L)
                );
                break;
            case DashboardItem.TYPE_GAME_COMPLETE:
                GameCompleteViewHolder gameCompleteViewHolder = (GameCompleteViewHolder) viewHolder;

                gameCompleteViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDashboardOnItemClickListener.onClick(dashboardItem);
                    }
                });

                GlideApp.with(mContext).load(dashboardItem.getAppLogoUrl())
                        .placeholder(R.drawable.no_game_logo)
                        .into(gameCompleteViewHolder.gameLogo);
                gameCompleteViewHolder.gameName.setText(dashboardItem.getAppName());
                gameCompleteViewHolder.time.setText(
                        DateUtils.getRelativeTimeSpanString(dashboardItem.getTime() * 1000L)
                );
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mDashboardItems.size();
    }

    interface DashboardOnEndReachListener {
        void onEndReached();
    }

    interface DashboardOnItemClickListener {
        void onClick(DashboardItem dashboardItem);
    }

    private static class CurrentGameViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView gameLogo;
        TextView gameName;
        TextView gameProgress;
        ProgressBar gameProgressBar;

        CurrentGameViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cv_item);

            gameLogo = itemView.findViewById(R.id.iv_game_logo);
            gameName = itemView.findViewById(R.id.tv_game_name);
            gameProgress = itemView.findViewById(R.id.tv_game_progress);
            gameProgressBar = itemView.findViewById(R.id.pb_game_progress);
        }
    }

    private static class NewGameViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView gameLogo;
        TextView gameName;
        TextView time;

        NewGameViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cv_item);

            gameLogo = itemView.findViewById(R.id.iv_game_logo);
            gameName = itemView.findViewById(R.id.tv_game_name);
            time = itemView.findViewById(R.id.tv_time);
        }
    }

    private static class AchievementUnlockedViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView achievementIcon;
        TextView achievementName;
        TextView achievementTime;

        AchievementUnlockedViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cv_item);

            achievementIcon = itemView.findViewById(R.id.iv_achievement_icon);
            achievementName = itemView.findViewById(R.id.tv_achievement_name);
            achievementTime = itemView.findViewById(R.id.tv_achievement_time);
        }
    }

    private static class GameCompleteViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView gameLogo;
        TextView gameName;
        TextView time;

        GameCompleteViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cv_item);

            gameLogo = itemView.findViewById(R.id.iv_game_logo);
            gameName = itemView.findViewById(R.id.tv_game_name);
            time = itemView.findViewById(R.id.tv_time);
        }
    }
}
