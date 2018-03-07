package io.github.skvoll.cybertrophy.achievements.list;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.AchievementModel;
import io.github.skvoll.cybertrophy.data.GameModel;

public class AchievementsListFragment extends Fragment {
    private static final String TAG = AchievementsListFragment.class.getSimpleName();
    private static final String KEY_GAME_ID = "GAME_ID";
    private static final String KEY_ACHIEVEMENTS_STATUS = "ACHIEVEMENTS_STATUS";

    private AchievementsListAdapter.OnItemClickListener mOnItemClickListener;
    private Long mGameId;
    private int mAchievementsStatus;

    private AchievementsObserver mAchievementsObserver = new AchievementsObserver(new Handler());

    private RecyclerView mRvList;
    private View mIvPlaceholder;
    private View mLlProgress;
    private GameModel mGameModel;

    public AchievementsListFragment() {
    }

    public static AchievementsListFragment newInstance(
            Long gameId, int achievementsStatus, AchievementsListAdapter.OnItemClickListener onItemClickListener) {
        AchievementsListFragment fragment = new AchievementsListFragment();
        fragment.setOnItemClickListener(onItemClickListener);

        Bundle bundle = new Bundle();
        bundle.putLong(KEY_GAME_ID, gameId);
        bundle.putInt(KEY_ACHIEVEMENTS_STATUS, achievementsStatus);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() == null) {
            throw new IllegalArgumentException();
        }

        mGameId = getArguments().getLong(KEY_GAME_ID, -1);
        mAchievementsStatus = getArguments().getInt(KEY_ACHIEVEMENTS_STATUS, AchievementModel.STATUS_ALL);

        if (mGameId == -1) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getContext() == null) {
            return null;
        }

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.base_list, container, false);

        SwipeRefreshLayout srlRefresh = rootView.findViewById(R.id.srl_refresh);
        srlRefresh.setEnabled(false);

        mRvList = rootView.findViewById(android.R.id.list);
        mIvPlaceholder = rootView.findViewById(android.R.id.empty);
        mLlProgress = rootView.findViewById(android.R.id.progress);

        mRvList.addItemDecoration(new DividerItemDecoration(mRvList.getContext(), DividerItemDecoration.VERTICAL));
        mRvList.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvList.setAdapter(new AchievementsListAdapter(getContext(), new ArrayList<AchievementModel>(),
                mOnItemClickListener, AchievementsListAdapter.TYPE_FULL));

        mGameModel = GameModel.getById(getContext().getContentResolver(), mGameId);

        mAchievementsObserver.loadData();

        return rootView;
    }

    public void setOnItemClickListener(AchievementsListAdapter.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    void setData(ArrayList<AchievementModel> achievementModels) {
        AchievementsListAdapter adapter = new AchievementsListAdapter(getContext(),
                achievementModels, mOnItemClickListener, AchievementsListAdapter.TYPE_FULL);

        mRvList.swapAdapter(adapter, false);

        mLlProgress.setVisibility(View.GONE);

        if (adapter.getItemCount() > 0) {
            mRvList.setVisibility(View.VISIBLE);
            mIvPlaceholder.setVisibility(View.GONE);
        } else {
            mRvList.setVisibility(View.GONE);
            mIvPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private static class LoadDataTask extends AsyncTask<Void, Void, ArrayList<AchievementModel>> {
        private WeakReference<AchievementsListFragment> mFragmentWeakReference;
        private int mAchievementsStatus;
        private GameModel mGameModel;
        private ContentResolver mContentResolver;

        LoadDataTask(AchievementsListFragment fragment, int achievementsStatus, GameModel gameModel) {
            if (fragment.getContext() == null) {
                return;
            }

            mFragmentWeakReference = new WeakReference<>(fragment);
            mAchievementsStatus = achievementsStatus;
            mGameModel = gameModel;
            mContentResolver = fragment.getContext().getContentResolver();
        }

        @Override
        protected ArrayList<AchievementModel> doInBackground(Void... voids) {
            if (mContentResolver == null) {
                return null;
            }

            return AchievementModel.getByGame(mContentResolver, mGameModel, mAchievementsStatus);
        }

        @Override
        protected void onPostExecute(ArrayList<AchievementModel> achievementModels) {
            if (mFragmentWeakReference == null) {
                return;
            }

            AchievementsListFragment fragment = mFragmentWeakReference.get();

            if (fragment == null) {
                return;
            }

            fragment.setData(achievementModels);
        }
    }

    private class AchievementsObserver extends ContentObserver {
        AchievementsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            loadData();
        }

        void loadData() {
            (new LoadDataTask(AchievementsListFragment.this, mAchievementsStatus, mGameModel)).execute();
        }
    }
}
