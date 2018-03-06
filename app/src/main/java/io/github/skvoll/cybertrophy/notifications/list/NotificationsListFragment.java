package io.github.skvoll.cybertrophy.notifications.list;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import io.github.skvoll.cybertrophy.AchievementPreviewDialogFragment;
import io.github.skvoll.cybertrophy.GameActivity;
import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.AchievementModel;
import io.github.skvoll.cybertrophy.data.GameModel;
import io.github.skvoll.cybertrophy.data.NotificationModel;
import io.github.skvoll.cybertrophy.data.ProfileModel;

public class NotificationsListFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        NotificationsListAdapter.OnItemClickListener {
    private static final String TAG = NotificationsListFragment.class.getSimpleName();

    private SwipeRefreshLayout mSrlRefresh;
    private RecyclerView mRvList;
    private View mIvPlaceholder;
    private View mLlProgress;
    private ProfileModel mProfileModel;

    public NotificationsListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getContext() == null) {
            return null;
        }

        mProfileModel = ProfileModel.getActive(getContext().getContentResolver());

        if (mProfileModel == null) {
            return null;
        }

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_notifications, container, false);

        mSrlRefresh = rootView.findViewById(R.id.srl_refresh);
        mRvList = rootView.findViewById(android.R.id.list);
        mIvPlaceholder = rootView.findViewById(android.R.id.empty);
        mLlProgress = rootView.findViewById(android.R.id.progress);

        mSrlRefresh.setColorSchemeColors(getResources().getColor(R.color.secondaryColor));
        mSrlRefresh.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.primaryColor));
        mSrlRefresh.setOnRefreshListener(this);

        mRvList.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvList.setAdapter(new NotificationsListAdapter(
                getContext(), new ArrayList<NotificationModel>(), this));


        (new LoadDataTask(this, mProfileModel)).execute();

        return rootView;
    }

    @Override
    public void onRefresh() {
        (new LoadDataTask(this, mProfileModel)).execute();
    }

    void setData(ArrayList<NotificationModel> notificationModels) {
        NotificationsListAdapter adapter
                = new NotificationsListAdapter(getContext(), notificationModels, this);

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

    @Override
    public void onClick(NotificationModel notificationModel) {
        if (getContext() == null) {
            return;
        }

        ContentResolver contentResolver = getContext().getContentResolver();

        switch (notificationModel.getType()) {
            case NotificationModel.TYPE_NEW_GAME:
                showGame(GameModel.getById(contentResolver, notificationModel.getObjectId()));
                break;
            case NotificationModel.TYPE_GAME_REMOVED:
                break;
            case NotificationModel.TYPE_NEW_ACHIEVEMENT:
                showGame(GameModel.getById(contentResolver, notificationModel.getObjectId()));
                break;
            case NotificationModel.TYPE_ACHIEVEMENT_REMOVED:
                showGame(GameModel.getById(contentResolver, notificationModel.getObjectId()));
                break;
            case NotificationModel.TYPE_ACHIEVEMENT_UNLOCKED:
                showAchievement(AchievementModel.getById(contentResolver, notificationModel.getObjectId()));
                break;
            case NotificationModel.TYPE_GAME_COMPLETE:
                showGame(GameModel.getById(contentResolver, notificationModel.getObjectId()));
                break;
        }
    }

    private void showGame(GameModel gameModel) {
        if (getContext() == null || gameModel == null) {
            return;
        }

        Intent intent = new Intent(getContext(), GameActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(GameActivity.KEY_GAME_ID, gameModel.getId());

        startActivity(intent);
    }

    private void showAchievement(AchievementModel achievementModel) {
        if (getActivity() == null || achievementModel == null) {
            return;
        }

        AchievementPreviewDialogFragment achievementPreviewDialogFragment
                = AchievementPreviewDialogFragment.newInstance(achievementModel);

        achievementPreviewDialogFragment.show(getActivity().getSupportFragmentManager(),
                achievementPreviewDialogFragment.getTag());
    }

    private static class LoadDataTask extends AsyncTask<Void, Void, ArrayList<NotificationModel>> {
        private WeakReference<NotificationsListFragment> mFragmentWeakReference;
        private ProfileModel mProfileModel;
        private ContentResolver mContentResolver;

        LoadDataTask(NotificationsListFragment fragment, ProfileModel profileModel) {
            if (fragment.getContext() == null) {
                return;
            }

            mFragmentWeakReference = new WeakReference<>(fragment);
            mProfileModel = profileModel;
            mContentResolver = fragment.getContext().getContentResolver();
        }

        @Override
        protected ArrayList<NotificationModel> doInBackground(Void... voids) {
            if (mContentResolver == null) {
                return null;
            }

            return NotificationModel.getByProfile(mContentResolver, mProfileModel);
        }

        @Override
        protected void onPostExecute(ArrayList<NotificationModel> notificationModels) {
            if (mFragmentWeakReference == null) {
                return;
            }

            NotificationsListFragment fragment = mFragmentWeakReference.get();

            if (fragment == null) {
                return;
            }

            fragment.setData(notificationModels);
        }
    }
}
