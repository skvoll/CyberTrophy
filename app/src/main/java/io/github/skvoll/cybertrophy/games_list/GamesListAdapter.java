package io.github.skvoll.cybertrophy.games_list;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.github.skvoll.cybertrophy.GlideApp;
import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.GameModel;

final class GamesListAdapter extends CursorAdapter {
    private static final String TAG = GamesListAdapter.class.getSimpleName();

    GamesListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_games_list_item, parent, false);

        view.setTag(new ViewHolder(view));

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        GameModel gameModel = new GameModel(cursor);
        float playTime = gameModel.getPlaytimeForever() / 60;

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        GlideApp.with(context).load(gameModel.getLogoUrl())
                .placeholder(R.drawable.game_logo_empty)
                .into(viewHolder.gameLogo);

        viewHolder.gameName.setText(gameModel.getName());

        viewHolder.gameLastPLay.setText(context.getString(
                R.string.games_list_item_last_play,
                DateUtils.getRelativeTimeSpanString(gameModel.getLastPlay() * 1000L)
        ));

        if (playTime <= 1) {
            playTime = gameModel.getPlaytimeForever();
            viewHolder.gamePlaytime.setText(context.getResources().getQuantityString(
                    R.plurals.games_list_item_playtime_minutes,
                    (int) playTime,
                    (int) playTime
            ));
        } else if (playTime <= (24 * 7)) {
            viewHolder.gamePlaytime.setText(context.getResources().getQuantityString(
                    R.plurals.games_list_item_playtime_hours,
                    (int) playTime,
                    (int) playTime
            ));
        } else {
            playTime = playTime / 24;
            viewHolder.gamePlaytime.setText(context.getResources().getQuantityString(
                    R.plurals.games_list_item_playtime_days,
                    (int) playTime,
                    (int) playTime
            ));
        }

        viewHolder.gameAchievements.setText(context.getString(
                R.string.games_list_item_achievements_count,
                gameModel.getAchievementsTotalCount()
        ));

        viewHolder.gameProgress.setText(context.getString(
                R.string.games_list_item_achievements_progress,
                gameModel.getAchievementsUnlockedCount(),
                gameModel.getAchievementsTotalCount()
        ));

        viewHolder.gameProgressBar.setMax(gameModel.getAchievementsTotalCount());
        viewHolder.gameProgressBar.setProgress(gameModel.getAchievementsUnlockedCount());

        switch (gameModel.getStatus()) {
            case GameModel.STATUS_INCOMPLETE:
                if (playTime > 0) {
                    viewHolder.gameLastPLay.setVisibility(View.VISIBLE);
                    viewHolder.gamePlaytime.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.gameLastPLay.setVisibility(View.GONE);
                    viewHolder.gamePlaytime.setVisibility(View.GONE);
                }
                viewHolder.gameAchievements.setVisibility(View.VISIBLE);
                viewHolder.gameProgress.setVisibility(View.GONE);
                viewHolder.gameProgressBar.setVisibility(View.GONE);
                break;
            case GameModel.STATUS_IN_PROGRESS:
                viewHolder.gameLastPLay.setVisibility(View.VISIBLE);
                viewHolder.gamePlaytime.setVisibility(View.GONE);
                viewHolder.gameAchievements.setVisibility(View.GONE);
                viewHolder.gameProgress.setVisibility(View.VISIBLE);
                viewHolder.gameProgressBar.setVisibility(View.VISIBLE);
                break;
            case GameModel.STATUS_COMPLETE:
                viewHolder.gameLastPLay.setVisibility(View.GONE);
                viewHolder.gamePlaytime.setVisibility(View.VISIBLE);
                viewHolder.gameAchievements.setVisibility(View.VISIBLE);
                viewHolder.gameProgress.setVisibility(View.GONE);
                viewHolder.gameProgressBar.setVisibility(View.GONE);
                break;
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        ImageView gameLogo;
        TextView gameName;
        TextView gameLastPLay;
        TextView gamePlaytime;
        TextView gameAchievements;
        TextView gameProgress;
        ProgressBar gameProgressBar;

        ViewHolder(View itemView) {
            super(itemView);

            gameLogo = itemView.findViewById(R.id.iv_game_logo);
            gameName = itemView.findViewById(R.id.tv_game_name);
            gameLastPLay = itemView.findViewById(R.id.tv_game_last_play);
            gamePlaytime = itemView.findViewById(R.id.tv_game_playtime);
            gameAchievements = itemView.findViewById(R.id.tv_game_achievements);
            gameProgress = itemView.findViewById(R.id.tv_game_progress);
            gameProgressBar = itemView.findViewById(R.id.pb_game_progress);
        }
    }
}
