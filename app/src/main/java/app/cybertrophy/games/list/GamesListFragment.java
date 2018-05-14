package app.cybertrophy.games.list;

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

import app.cybertrophy.R;
import app.cybertrophy.data.GameModel;
import app.cybertrophy.data.ProfileModel;

import static app.cybertrophy.data.DataContract.GameEntry;

public final class GamesListFragment extends Fragment {
    private static final String TAG = GamesListFragment.class.getSimpleName();
    private static final String KEY_PROFILE_ID = "PROFILE_ID";
    private static final String KEY_GAME_STATUS = "GAME_STATUS";

    private GamesListAdapter.OnItemClickListener mOnItemClickListener;
    private Long mProfileId;
    private int mGameStatus;

    private final GamesObserver mGamesObserver = new GamesObserver(new Handler());

    private RecyclerView mRvList;
    private View mIvPlaceholder;
    private View mLlProgress;
    private ProfileModel mProfileModel;

    public GamesListFragment() {
    }

    public static GamesListFragment newInstance(
            Long profileId, int gameStatus, GamesListAdapter.OnItemClickListener onItemClickListener) {
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

        if (getContext() == null) {
            return;
        }

        if (getArguments() == null) {
            throw new IllegalArgumentException();
        }

        mProfileId = getArguments().getLong(KEY_PROFILE_ID, -1);
        mGameStatus = getArguments().getInt(KEY_GAME_STATUS, GameModel.STATUS_ALL);

        if (mProfileId == -1) {
            throw new IllegalArgumentException();
        }

        getContext().getContentResolver().registerContentObserver(
                GameEntry.URI, true, mGamesObserver);
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
        mRvList.setAdapter(new GamesListAdapter(getContext(),
                new ArrayList<GameModel>(), mOnItemClickListener));

        mProfileModel = ProfileModel.getById(getContext().getContentResolver(), mProfileId);

        mGamesObserver.loadData();

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (getContext() == null) {
            return;
        }

        getContext().getContentResolver().unregisterContentObserver(mGamesObserver);
    }

    public void setOnItemClickListener(GamesListAdapter.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    void setData(ArrayList<GameModel> gameModels) {
        GamesListAdapter adapter = new GamesListAdapter(getContext(),
                gameModels, mOnItemClickListener);

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

    private class GamesObserver extends ContentObserver {
        GamesObserver(Handler handler) {
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
            (new LoadDataTask(GamesListFragment.this, mGameStatus, mProfileModel)).execute();
        }
    }
}
