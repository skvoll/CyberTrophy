package io.github.skvoll.cybertrophy.dashboard;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

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
    private RecyclerView mRecyclerView;
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

        loadDashboardItems(0);

        ViewGroup mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_dashboard, container, false);

        mSwipeRefreshLayout = mRootView.findViewById(R.id.srl_refresh);
        mRecyclerView = mRootView.findViewById(R.id.rv_list);

        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.secondaryColor));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new DashboardAdapter(getContext(),
                mRecyclerView, mDashboardItems, this);

        mAdapter.setOnEndReachListener(this);

        mRecyclerView.setAdapter(mAdapter);

        return mRootView;
    }

    @Override
    public void onRefresh() {
        loadDashboardItems(0);
        mAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onEndReached() {
        mRecyclerView.post(new Runnable() {
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
                break;
            case DashboardItem.TYPE_NEW_GAME:
                break;
            case DashboardItem.TYPE_ACHIEVEMENT_UNLOCKED:
                AchievementModel achievementModel = AchievementModel.getByCode(
                        contentResolver, dashboardItem.getAchievementCode());

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
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
