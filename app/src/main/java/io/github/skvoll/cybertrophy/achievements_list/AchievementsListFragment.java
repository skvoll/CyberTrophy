package io.github.skvoll.cybertrophy.achievements_list;

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

import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.AchievementModel;
import io.github.skvoll.cybertrophy.data.DataContract.AchievementEntry;

public class AchievementsListFragment extends ListFragment implements
        SwipeRefreshLayout.OnRefreshListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    public static final int LOADER_ID = 0;
    public static final int TYPE_ALL = 0;
    public static final int TYPE_LOCKED = 1;
    public static final int TYPE_UNLOCKED = 2;

    private static final String TAG = AchievementsListFragment.class.getSimpleName();
    private static final String KEY_STEAM_ID = "STEAM_ID";
    private static final String KEY_APP_ID = "APP_ID";
    private static final String KEY_TYPE = "TYPE";

    private Long mSteamId;
    private Long mAppId;
    private int mType;
    private OnItemClickListener mOnItemClickListener;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AchievementsListAdapter mAchievementsListAdapter;

    public AchievementsListFragment() {
    }

    public static AchievementsListFragment newInstance(Long steamId, Long appId, int type, OnItemClickListener onItemClickListener) {
        AchievementsListFragment fragment = new AchievementsListFragment();
        fragment.setOnItemClickListener(onItemClickListener);

        Bundle bundle = new Bundle();
        bundle.putLong(KEY_STEAM_ID, steamId);
        bundle.putLong(KEY_APP_ID, appId);
        bundle.putInt(KEY_TYPE, type);
        fragment.setArguments(bundle);

        return fragment;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() == null) {
            throw new IllegalArgumentException();
        }

        mSteamId = getArguments().getLong(KEY_STEAM_ID, -1);
        mAppId = getArguments().getLong(KEY_APP_ID, -1);
        mType = getArguments().getInt(KEY_TYPE, -1);

        if (mSteamId == -1 || mAppId == -1 || mType == -1) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_achievements_list, container, false);

        mSwipeRefreshLayout = mRootView.findViewById(R.id.srl_refresh);

        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.secondaryColor));
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.primaryColor));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mAchievementsListAdapter = new AchievementsListAdapter(getContext(), null, 0);

        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(mAchievementsListAdapter);

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onRefresh() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (mOnItemClickListener == null) {
            return;
        }

        if (getContext() == null) {
            return;
        }

        AchievementModel achievementModel = AchievementModel.getById(getContext().getContentResolver(), id);

        if (achievementModel == null) {
            return;
        }

        mOnItemClickListener.onClick(achievementModel);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (getContext() == null) {
            return null;
        }

        String select;
        String sortOrder;

        select = AchievementEntry.COLUMN_STEAM_ID + " == " + mSteamId + " AND " +
                AchievementEntry.COLUMN_APP_ID + " == " + mAppId;

        switch (mType) {
            case TYPE_LOCKED:
                select += " AND " + AchievementEntry.COLUMN_IS_UNLOCKED + " == 0";
                sortOrder = AchievementEntry.COLUMN_PERCENT + " DESC";
                break;
            case TYPE_UNLOCKED:
                select += " AND " + AchievementEntry.COLUMN_IS_UNLOCKED + " == 1";
                sortOrder = AchievementEntry.COLUMN_UNLOCK_TIME + " DESC";
                break;
            default:
                sortOrder = AchievementEntry.COLUMN_UNLOCK_TIME + " DESC";
                break;
        }

        return new CursorLoader(getContext(), AchievementEntry.URI,
                null, select, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAchievementsListAdapter.swapCursor(cursor);

        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAchievementsListAdapter.swapCursor(null);
    }

    public interface OnItemClickListener {
        void onClick(AchievementModel achievementModel);
    }
}
