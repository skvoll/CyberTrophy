package io.github.skvoll.cybertrophy.games_list;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import io.github.skvoll.cybertrophy.GameActivity;
import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.DataContract.GameEntry;
import io.github.skvoll.cybertrophy.data.GameModel;

public class GamesListFragment extends ListFragment implements
        SwipeRefreshLayout.OnRefreshListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    public static final int LOADER_ID = 0;
    public static final int TYPE_ALL = 0;
    public static final int TYPE_IN_PROGRESS = 1;
    public static final int TYPE_INCOMPLETE = 2;
    public static final int TYPE_COMPLETE = 3;
    public static final int TYPE_NO_ACHIEVEMENTS = 4;

    private static final String TAG = GamesListFragment.class.getSimpleName();
    private static final String KEY_PROFILE_ID = "PROFILE_ID";
    private static final String KEY_TYPE = "TYPE";

    private Long mProfileId;
    private int mType;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private GamesListAdapter mGamesListAdapter;

    public GamesListFragment() {
    }

    public static GamesListFragment newInstance(Long profileId, int type) {
        GamesListFragment fragment = new GamesListFragment();

        Bundle bundle = new Bundle();
        bundle.putLong(KEY_PROFILE_ID, profileId);
        bundle.putInt(KEY_TYPE, type);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() == null) {
            throw new IllegalArgumentException();
        }

        mProfileId = getArguments().getLong(KEY_PROFILE_ID, -1);
        mType = getArguments().getInt(KEY_TYPE, -1);

        if (mProfileId == -1 || mType == -1) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_games_list, container, false);

        mSwipeRefreshLayout = mRootView.findViewById(R.id.srl_refresh);

        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.secondaryColor));
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.primaryColor));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mGamesListAdapter = new GamesListAdapter(getContext(), null, 0);

        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(mGamesListAdapter);

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onRefresh() {
        final LoaderManager.LoaderCallbacks<Cursor> listFragment = this;

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                getLoaderManager().restartLoader(LOADER_ID, null, listFragment);
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (getContext() == null) {
            return;
        }

        GameModel gameModel = GameModel.getById(getContext().getContentResolver(), id);

        if (gameModel == null) {
            return;
        }

        Intent intent = new Intent(getContext(), GameActivity.class);
        intent.putExtra(GameActivity.KEY_PROFILE_ID, gameModel.getProfileId());
        intent.putExtra(GameActivity.KEY_GAME_ID, gameModel.getId());

        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (getContext() == null) {
            return null;
        }

        String select;
        String sortOrder = null;

        if (mType == TYPE_NO_ACHIEVEMENTS) {
            select = GameEntry.COLUMN_PROFILE_ID + " == " + mProfileId
                    + " AND " + GameEntry.COLUMN_ACHIEVEMENTS_TOTAL_COUNT + " == 0";
            sortOrder = GameEntry.COLUMN_NAME + " ASC";
        } else {
            select = GameEntry.COLUMN_PROFILE_ID + " == " + mProfileId
                    + " AND " + GameEntry.COLUMN_ACHIEVEMENTS_TOTAL_COUNT + " != 0 AND ";

            switch (mType) {
                case TYPE_IN_PROGRESS:
                    select += GameEntry.COLUMN_ACHIEVEMENTS_UNLOCKED_COUNT + " > 0"
                            + " AND " + GameEntry.COLUMN_ACHIEVEMENTS_UNLOCKED_COUNT
                            + " < " + GameEntry.COLUMN_ACHIEVEMENTS_TOTAL_COUNT;
                    sortOrder = GameEntry.COLUMN_LAST_PLAY + " DESC";
                    break;
                case TYPE_INCOMPLETE:
                    select += GameEntry.COLUMN_ACHIEVEMENTS_UNLOCKED_COUNT + " == 0";
                    sortOrder = GameEntry.COLUMN_NAME + " ASC";
                    break;
                case TYPE_COMPLETE:
                    select += GameEntry.COLUMN_ACHIEVEMENTS_UNLOCKED_COUNT + " == "
                            + GameEntry.COLUMN_ACHIEVEMENTS_TOTAL_COUNT;
                    sortOrder = GameEntry.COLUMN_NAME + " ASC";
                    break;
            }
        }

        return new CursorLoader(getContext(), GameEntry.URI,
                null, select, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mGamesListAdapter.swapCursor(cursor);

        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mGamesListAdapter.swapCursor(null);
    }
}
