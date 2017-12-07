package io.github.skvoll.cybertrophy.games_list;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
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

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        GlideApp.with(context).load(gameModel.getLogoUrl())
                .placeholder(R.drawable.no_game_logo)
                .into(viewHolder.gameLogo);

        viewHolder.gameName.setText(gameModel.getName());

        switch (gameModel.getStatus()) {
            case GameModel.STATUS_INCOMPLETE:
                viewHolder.gameInfo.setText(context.getString(
                        R.string.games_list_item_achievements_count,
                        gameModel.getAchievementsTotalCount()
                ));
                viewHolder.gameInfo.setVisibility(View.VISIBLE);
                break;
            case GameModel.STATUS_IN_PROGRESS:
                viewHolder.gameProgress.setText(context.getString(
                        R.string.games_list_item_achievements_progress,
                        gameModel.getAchievementsUnlockedCount(),
                        gameModel.getAchievementsTotalCount()
                ));
                viewHolder.gameProgress.setVisibility(View.VISIBLE);

                viewHolder.gameProgressBar.setMax(gameModel.getAchievementsTotalCount());
                viewHolder.gameProgressBar.setProgress(gameModel.getAchievementsUnlockedCount());
                viewHolder.gameProgressBar.setVisibility(View.VISIBLE);
                break;
            case GameModel.STATUS_COMPLETE:
                break;
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        ImageView gameLogo;
        TextView gameName;
        TextView gameInfo;
        TextView gameProgress;
        ProgressBar gameProgressBar;

        ViewHolder(View itemView) {
            super(itemView);

            gameLogo = itemView.findViewById(R.id.iv_game_logo);
            gameName = itemView.findViewById(R.id.tv_game_name);
            gameInfo = itemView.findViewById(R.id.tv_game_info);
            gameProgress = itemView.findViewById(R.id.tv_game_progress);
            gameProgressBar = itemView.findViewById(R.id.pb_game_progress);
        }
    }
}
