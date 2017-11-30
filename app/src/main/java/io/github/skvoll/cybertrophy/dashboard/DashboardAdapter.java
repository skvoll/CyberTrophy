package io.github.skvoll.cybertrophy.dashboard;

import android.content.Context;
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

    private Context mContext;
    private ArrayList<DashboardItem> mDashboardItems;

    DashboardAdapter(Context context, ArrayList<DashboardItem> dashboardItems) {
        mContext = context;
        mDashboardItems = dashboardItems;
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

    private static class NewGameViewHolder extends RecyclerView.ViewHolder {
        ImageView gameLogo;
        TextView gameName;

        NewGameViewHolder(View itemView) {
            super(itemView);

            gameLogo = itemView.findViewById(R.id.iv_game_logo);
            gameName = itemView.findViewById(R.id.tv_game_name);
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
