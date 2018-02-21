package io.github.skvoll.cybertrophy.games.list;

import android.content.Context;
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
import io.github.skvoll.cybertrophy.data.GameModel;

public final class GamesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<GameModel> mItems;
    private GamesListFragment.OnItemClickListener mOnItemClickListener;

    GamesListAdapter(Context context, ArrayList<GameModel> gameModels,
                     GamesListFragment.OnItemClickListener onItemClickListener) {
        mContext = context;
        mItems = gameModels;
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GameViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_games_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final GameModel gameModel = mItems.get(position);
        float playTime = gameModel.getPlaytimeForever() / 60;
        GameViewHolder gameViewHolder = (GameViewHolder) viewHolder;

        GlideApp.with(mContext).load(gameModel.getLogoUrl())
                .placeholder(R.drawable.game_logo_empty)
                .into(gameViewHolder.ivLogo);

        gameViewHolder.tvName.setText(gameModel.getName());

        gameViewHolder.tvLastPLay.setText(mContext.getString(
                R.string.games_list_item_last_play,
                DateUtils.getRelativeTimeSpanString(gameModel.getLastPlay() * 1000L)
        ));

        if (playTime <= 1) {
            playTime = gameModel.getPlaytimeForever();
            gameViewHolder.tvPlaytime.setText(mContext.getResources().getQuantityString(
                    R.plurals.games_list_item_playtime_minutes,
                    (int) playTime,
                    (int) playTime
            ));
        } else if (playTime <= (24 * 7)) {
            gameViewHolder.tvPlaytime.setText(mContext.getResources().getQuantityString(
                    R.plurals.games_list_item_playtime_hours,
                    (int) playTime,
                    (int) playTime
            ));
        } else {
            playTime = playTime / 24;
            gameViewHolder.tvPlaytime.setText(mContext.getResources().getQuantityString(
                    R.plurals.games_list_item_playtime_days,
                    (int) playTime,
                    (int) playTime
            ));
        }

        gameViewHolder.tvAchievements.setText(mContext.getString(
                R.string.games_list_item_achievements_count,
                gameModel.getAchievementsTotalCount()
        ));

        gameViewHolder.tvProgress.setText(mContext.getString(
                R.string.games_list_item_achievements_progress,
                gameModel.getAchievementsUnlockedCount(),
                gameModel.getAchievementsTotalCount()
        ));

        gameViewHolder.pbProgress.setMax(gameModel.getAchievementsTotalCount());
        gameViewHolder.pbProgress.setProgress(gameModel.getAchievementsUnlockedCount());

        switch (gameModel.getStatus()) {
            case GameModel.INCOMPLETE:
                if (playTime > 0) {
                    gameViewHolder.tvLastPLay.setVisibility(View.VISIBLE);
                    gameViewHolder.tvPlaytime.setVisibility(View.VISIBLE);
                } else {
                    gameViewHolder.tvLastPLay.setVisibility(View.GONE);
                    gameViewHolder.tvPlaytime.setVisibility(View.GONE);
                }
                gameViewHolder.tvAchievements.setVisibility(View.VISIBLE);
                gameViewHolder.tvProgress.setVisibility(View.GONE);
                gameViewHolder.pbProgress.setVisibility(View.GONE);
                break;
            case GameModel.IN_PROGRESS:
                gameViewHolder.tvLastPLay.setVisibility(View.VISIBLE);
                gameViewHolder.tvPlaytime.setVisibility(View.GONE);
                gameViewHolder.tvAchievements.setVisibility(View.GONE);
                gameViewHolder.tvProgress.setVisibility(View.VISIBLE);
                gameViewHolder.pbProgress.setVisibility(View.VISIBLE);
                break;
            case GameModel.COMPLETE:
                gameViewHolder.tvLastPLay.setVisibility(View.GONE);
                gameViewHolder.tvPlaytime.setVisibility(View.VISIBLE);
                gameViewHolder.tvAchievements.setVisibility(View.VISIBLE);
                gameViewHolder.tvProgress.setVisibility(View.GONE);
                gameViewHolder.pbProgress.setVisibility(View.GONE);
                break;
        }

        gameViewHolder.vContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemClickListener.onClick(gameModel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private static final class GameViewHolder extends RecyclerView.ViewHolder {
        View vContainer;
        ImageView ivLogo;
        TextView tvName;
        TextView tvLastPLay;
        TextView tvPlaytime;
        TextView tvAchievements;
        TextView tvProgress;
        ProgressBar pbProgress;

        GameViewHolder(View itemView) {
            super(itemView);

            vContainer = itemView;

            ivLogo = itemView.findViewById(R.id.iv_logo);
            tvName = itemView.findViewById(R.id.tv_name);
            tvLastPLay = itemView.findViewById(R.id.tv_last_play);
            tvPlaytime = itemView.findViewById(R.id.tv_playtime);
            tvAchievements = itemView.findViewById(R.id.tv_achievements);
            tvProgress = itemView.findViewById(R.id.tv_progress);
            pbProgress = itemView.findViewById(R.id.pb_progress);
        }
    }
}