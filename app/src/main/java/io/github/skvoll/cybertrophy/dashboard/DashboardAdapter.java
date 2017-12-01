package io.github.skvoll.cybertrophy.dashboard;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.GlideApp;
import io.github.skvoll.cybertrophy.R;

public final class DashboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = DashboardAdapter.class.getSimpleName();

    private static int VISIBLE_THRESHOLD = 10;
    private Context mContext;
    private DashboardAdapter.onEndReachListener mOnEndReachListener;
    private ArrayList<DashboardItem> mDashboardItems;

    private boolean mIsLoading = false;

    DashboardAdapter(Context context, RecyclerView recyclerView, ArrayList<DashboardItem> dashboardItems) {
        mContext = context;
        mDashboardItems = dashboardItems;

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                int itemsCount = linearLayoutManager.getItemCount();
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!mIsLoading && itemsCount <= (lastVisibleItem + VISIBLE_THRESHOLD)) {
                    if (mOnEndReachListener != null) {
                        mOnEndReachListener.onEndReached();
                    }

                    mIsLoading = true;
                }
            }
        });
    }

    void setLoaded() {
        mIsLoading = false;
    }

    void setOnEndReachListener(DashboardAdapter.onEndReachListener onEndReachListener) {
        mOnEndReachListener = onEndReachListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mDashboardItems.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
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
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        DashboardItem dashboardItem = mDashboardItems.get(position);

        switch (viewHolder.getItemViewType()) {
            case DashboardItem.TYPE_NEW_GAME:
                NewGameViewHolder newGameViewHolder = (NewGameViewHolder) viewHolder;
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
                GlideApp.with(mContext).load(dashboardItem.getAchievementIconUrl())
                        .placeholder(R.drawable.no_achievement_icon)
                        .into(achievementUnlockedViewHolder.achievementIcon);
                achievementUnlockedViewHolder.achievementName.setText(dashboardItem.getAchievementName());
                achievementUnlockedViewHolder.achievementTime.setText(
                        DateUtils.getRelativeTimeSpanString(dashboardItem.getAchievementUnlockTime() * 1000L)
                );
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mDashboardItems.size();
    }

    interface onEndReachListener {
        void onEndReached();
    }

    private static class NewGameViewHolder extends RecyclerView.ViewHolder {
        ImageView gameLogo;
        TextView gameName;
        TextView time;

        NewGameViewHolder(View itemView) {
            super(itemView);

            gameLogo = itemView.findViewById(R.id.iv_game_logo);
            gameName = itemView.findViewById(R.id.tv_game_name);
            time = itemView.findViewById(R.id.tv_time);
        }
    }

    private static class AchievementUnlockedViewHolder extends RecyclerView.ViewHolder {
        ImageView achievementIcon;
        TextView achievementName;
        TextView achievementTime;

        AchievementUnlockedViewHolder(View itemView) {
            super(itemView);

            achievementIcon = itemView.findViewById(R.id.iv_achievement_icon);
            achievementName = itemView.findViewById(R.id.tv_achievement_name);
            achievementTime = itemView.findViewById(R.id.tv_achievement_time);
        }
    }
}
