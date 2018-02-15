package io.github.skvoll.cybertrophy.achievements.list;

import android.content.ContentResolver;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class AchievementsListFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = AchievementsListFragment.class.getSimpleName();
    private static final String KEY_GAME_ID = "GAME_ID";
    private static final String KEY_ACHIEVEMENTS_STATUS = "ACHIEVEMENTS_STATUS";

    private OnItemClickListener mOnItemClickListener;
    private Long mGameId;
    private int mAchievementsStatus;
    private SwipeRefreshLayout mSrlRefresh;
    private RecyclerView mRvList;
    private View mIvPlaceholder;
    private View mLlProgress;
    private GameModel mGameModel;

    public AchievementsListFragment() {
    }

    public static AchievementsListFragment newInstance(
            Long gameId, int achievementsStatus, OnItemClickListener onItemClickListener) {
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
        mAchievementsStatus = getArguments().getInt(KEY_ACHIEVEMENTS_STATUS, AchievementModel.ALL);

        if (mGameId == -1) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getContext() == null) {
            return null;
        }

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_achievements_list, container, false);

        mSrlRefresh = rootView.findViewById(R.id.srl_refresh);
        mRvList = rootView.findViewById(android.R.id.list);
        mIvPlaceholder = rootView.findViewById(android.R.id.empty);
        mLlProgress = rootView.findViewById(android.R.id.progress);

        mSrlRefresh.setColorSchemeColors(getResources().getColor(R.color.secondaryColor));
        mSrlRefresh.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.primaryColor));
        mSrlRefresh.setOnRefreshListener(this);

        mRvList.addItemDecoration(new DividerItemDecoration(mRvList.getContext(), DividerItemDecoration.VERTICAL));
        mRvList.setLayoutManager(new LinearLayoutManager(getContext()));

        mGameModel = GameModel.getById(getContext().getContentResolver(), mGameId);
        (new LoadDataTask(this, mAchievementsStatus)).execute(mGameModel);

        return rootView;
    }

    @Override
    public void onRefresh() {
        (new LoadDataTask(this, mAchievementsStatus)).execute(mGameModel);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    void setData(ArrayList<AchievementModel> achievementModels) {
        AchievementsListAdapter adapter = new AchievementsListAdapter(getContext(),
                achievementModels, mOnItemClickListener, AchievementsListAdapter.TYPE_FULL);

        mRvList.swapAdapter(adapter, false);

        mSrlRefresh.setRefreshing(false);
        mLlProgress.setVisibility(View.GONE);

        if (adapter.getItemCount() > 0) {
            mRvList.setVisibility(View.VISIBLE);
            mIvPlaceholder.setVisibility(View.GONE);
        } else {
            mRvList.setVisibility(View.GONE);
            mIvPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    public interface OnItemClickListener {
        void onClick(AchievementModel achievementModel);
    }

    private static class LoadDataTask extends AsyncTask<GameModel, Void, ArrayList<AchievementModel>> {
        private WeakReference<AchievementsListFragment> mFragmentWeakReference;
        private int mAchievementsStatus;
        private ContentResolver mContentResolver;

        LoadDataTask(AchievementsListFragment fragment, int achievementsStatus) {
            if (fragment.getContext() == null) {
                return;
            }

            mFragmentWeakReference = new WeakReference<>(fragment);
            mAchievementsStatus = achievementsStatus;
            mContentResolver = fragment.getContext().getContentResolver();
        }

        @Override
        protected ArrayList<AchievementModel> doInBackground(GameModel... gameModels) {
            GameModel gameModel = gameModels[0];

            if (gameModel == null) {
                return null;
            }

            return AchievementModel.getByGame(mContentResolver, gameModel, mAchievementsStatus);
        }

        @Override
        protected void onPostExecute(ArrayList<AchievementModel> achievementModels) {
            AchievementsListFragment fragment = mFragmentWeakReference.get();

            if (fragment == null) {
                return;
            }

            fragment.setData(achievementModels);
        }
    }
}
