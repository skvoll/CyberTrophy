package io.github.skvoll.cybertrophy.dashboard;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.GameActivity;
import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.AchievementModel;
import io.github.skvoll.cybertrophy.data.LogModel;
import io.github.skvoll.cybertrophy.data.ProfileModel;

public class DashboardFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        DashboardAdapter.DashboardOnItemClickListener,
        DashboardAdapter.DashboardOnEndReachListener {
    private static final String TAG = DashboardFragment.class.getSimpleName();

    private static final int ITEMS_LIMIT = 100;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mList;
    private View mListEmpty;
    private DashboardAdapter mAdapter;

    private ProfileModel mProfileModel;
    private ArrayList<DashboardItem> mDashboardItems = new ArrayList<>(0);

    public DashboardFragment() {
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

        ViewGroup mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_dashboard, container, false);

        mSwipeRefreshLayout = mRootView.findViewById(R.id.srl_refresh);
        mList = mRootView.findViewById(android.R.id.list);
        mListEmpty = mRootView.findViewById(android.R.id.empty);

        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.secondaryColor));
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.primaryColor));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new DashboardAdapter(getContext(),
                mList, mDashboardItems, this);

        mAdapter.setOnEndReachListener(this);

        mList.setAdapter(mAdapter);

        loadDashboardItems(0);

        return mRootView;
    }

    @Override
    public void onRefresh() {
        mList.post(new Runnable() {
            @Override
            public void run() {
                loadDashboardItems(0);
                mAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onEndReached() {
        mList.post(new Runnable() {
            @Override
            public void run() {
                loadDashboardItems(mDashboardItems.size());
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
            }
        });
    }

    @Override
    public void onClick(DashboardItem dashboardItem) {
        if (getContext() == null) {
            return;
        }

        ContentResolver contentResolver = getContext().getContentResolver();

        switch (dashboardItem.getType()) {
            case DashboardItem.TYPE_CURRENT_GAME:
            case DashboardItem.TYPE_NEW_GAME:
            case DashboardItem.TYPE_GAME_COMPLETE:
                Intent intent = new Intent(getContext(), GameActivity.class);
                intent.putExtra(GameActivity.KEY_PROFILE_ID, dashboardItem.getProfileId());
                intent.putExtra(GameActivity.KEY_GAME_ID, dashboardItem.getGameId());

                startActivity(intent);
                break;
            case DashboardItem.TYPE_ACHIEVEMENT_UNLOCKED:
                AchievementModel achievementModel = AchievementModel.getById(
                        contentResolver, dashboardItem.getAchievementId());

                if (achievementModel == null) {
                    return;
                }

                showAchievementDialog(achievementModel);
                break;
        }
    }

    private void loadDashboardItems(int offset) {
        if (offset == 0) {
            mDashboardItems.clear();
            DashboardItem currentGame = DashboardItem.currentGame(getContext(), mProfileModel);
            if (currentGame != null) {
                mDashboardItems.add(currentGame);
            }
        }

        mDashboardItems.addAll(DashboardItem.getItems(getContext(), mProfileModel, getItemsTypes(), ITEMS_LIMIT, offset));

        if (mDashboardItems.size() > 0) {
            mList.setVisibility(View.VISIBLE);
            mListEmpty.setVisibility(View.GONE);
        } else {
            mList.setVisibility(View.GONE);
            mListEmpty.setVisibility(View.VISIBLE);
        }
    }

    private Integer[] getItemsTypes() {
        return new Integer[]{
                LogModel.TYPE_MESSAGE,
                LogModel.TYPE_NEW_GAME,
                LogModel.TYPE_NEW_ACHIEVEMENT,
                LogModel.TYPE_ACHIEVEMENT_UNLOCKED,
                LogModel.TYPE_GAME_COMPLETE
        };
    }

    private void showAchievementDialog(AchievementModel achievementModel) {
        if (getContext() == null) {
            return;
        }

        String description = achievementModel.getDescription();
        if (description == null || description.equals("")) {
            description = getContext().getResources().getString(R.string.empty_achievement_description);
        }

        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle(achievementModel.getName());
        alertDialog.setMessage(description);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
