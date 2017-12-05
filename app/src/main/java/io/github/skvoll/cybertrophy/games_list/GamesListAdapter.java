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

    private Context mContext;

    GamesListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        mContext = context;
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

        GlideApp.with(mContext).load(gameModel.getLogoUrl())
                .placeholder(R.drawable.no_game_logo)
                .into(viewHolder.mGameLogo);

        viewHolder.mGameName.setText(gameModel.getName());
        viewHolder.mGameProgress.setText(gameModel.getAchievementsUnlockedCount() + "/" + gameModel.getAchievementsTotalCount());
        viewHolder.mGameProgressBar.setMax(gameModel.getAchievementsTotalCount());
        viewHolder.mGameProgressBar.setProgress(gameModel.getAchievementsUnlockedCount());
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mGameLogo;
        TextView mGameName;
        TextView mGameProgress;
        ProgressBar mGameProgressBar;

        ViewHolder(View itemView) {
            super(itemView);

            mGameLogo = itemView.findViewById(R.id.iv_game_logo);
            mGameName = itemView.findViewById(R.id.tv_game_name);
            mGameProgress = itemView.findViewById(R.id.tv_game_progress);
            mGameProgressBar = itemView.findViewById(R.id.pb_game_progress);
        }
    }
}
