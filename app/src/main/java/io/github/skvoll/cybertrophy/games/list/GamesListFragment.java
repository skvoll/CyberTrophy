package io.github.skvoll.cybertrophy.games.list;

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
import io.github.skvoll.cybertrophy.data.GameModel;
import io.github.skvoll.cybertrophy.data.ProfileModel;

public class GamesListFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = GamesListFragment.class.getSimpleName();
    private static final String KEY_PROFILE_ID = "PROFILE_ID";
    private static final String KEY_GAME_STATUS = "GAME_STATUS";

    private OnItemClickListener mOnItemClickListener;
    private Long mProfileId;
    private int mGameStatus;

    private SwipeRefreshLayout mSrlRefresh;
    private RecyclerView mRvList;
    private View mIvPlaceholder;
    private View mLlProgress;
    private ProfileModel mProfileModel;

    public GamesListFragment() {
    }

    public static GamesListFragment newInstance(
            Long profileId, int gameStatus, OnItemClickListener onItemClickListener) {
        GamesListFragment fragment = new GamesListFragment();
        fragment.setOnItemClickListener(onItemClickListener);

        Bundle bundle = new Bundle();
        bundle.putLong(KEY_PROFILE_ID, profileId);
        bundle.putInt(KEY_GAME_STATUS, gameStatus);
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
        mGameStatus = getArguments().getInt(KEY_GAME_STATUS, GameModel.ALL);

        if (mProfileId == -1) {
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
        mRvList.setAdapter(new GamesListAdapter(getContext(),
                new ArrayList<GameModel>(), mOnItemClickListener));

        mProfileModel = ProfileModel.getById(getContext().getContentResolver(), mProfileId);
        (new LoadDataTask(this, mGameStatus, mProfileModel)).execute();

        return rootView;
    }

    @Override
    public void onRefresh() {
        (new LoadDataTask(this, mGameStatus, mProfileModel)).execute();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    void setData(ArrayList<GameModel> gameModels) {
        GamesListAdapter adapter = new GamesListAdapter(getContext(),
                gameModels, mOnItemClickListener);

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
        void onClick(GameModel gameModel);
    }

    private static class LoadDataTask extends AsyncTask<Void, Void, ArrayList<GameModel>> {
        private WeakReference<GamesListFragment> mFragmentWeakReference;
        private int mGameStatus;
        private ProfileModel mProfileModel;
        private ContentResolver mContentResolver;

        LoadDataTask(GamesListFragment fragment, int gameStatus, ProfileModel profileModel) {
            if (fragment.getContext() == null) {
                return;
            }

            mFragmentWeakReference = new WeakReference<>(fragment);
            mGameStatus = gameStatus;
            mProfileModel = profileModel;
            mContentResolver = fragment.getContext().getContentResolver();
        }

        @Override
        protected ArrayList<GameModel> doInBackground(Void... voids) {
            if (mContentResolver == null) {
                return null;
            }

            return GameModel.getByProfile(mContentResolver, mProfileModel, mGameStatus);
        }

        @Override
        protected void onPostExecute(ArrayList<GameModel> gameModels) {
            if (mFragmentWeakReference == null) {
                return;
            }

            GamesListFragment fragment = mFragmentWeakReference.get();

            if (fragment == null) {
                return;
            }

            fragment.setData(gameModels);
        }
    }
}
