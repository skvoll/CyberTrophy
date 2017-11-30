package io.github.skvoll.cybertrophy.dashboard;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.LogModel;

public class DashboardFragment extends Fragment {
    private static final String TAG = DashboardFragment.class.getSimpleName();

    private static final int ITEMS_LIMIT = 100;

    private ViewGroup mRootView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private DashboardAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<DashboardItem> mDashboardItems = new ArrayList<>(0);

    public DashboardFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_dashboard, container, false);

        mSwipeRefreshLayout = mRootView.findViewById(R.id.srl_refresh);
        mRecyclerView = mRootView.findViewById(R.id.rv_list);

        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.secondaryColor));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mDashboardItems.clear();
                mDashboardItems.addAll(DashboardItem.getItems(getContext(), getItemsTypes(), ITEMS_LIMIT, 0));
                mAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new DashboardAdapter(getContext(), mRecyclerView, mDashboardItems);

        mAdapter.setOnEndReachListener(new DashboardAdapter.onEndReachListener() {
            @Override
            public void onEndReached() {
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        mDashboardItems.addAll(
                                DashboardItem.getItems(getContext(), getItemsTypes(), ITEMS_LIMIT, mDashboardItems.size()));
                        mAdapter.notifyDataSetChanged();
                        mAdapter.setLoaded();
                    }
                });
            }
        });

        mRecyclerView.setAdapter(mAdapter);

        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDashboardItems.addAll(DashboardItem.getItems(getContext(), getItemsTypes(), ITEMS_LIMIT, 0));
    }

    private Integer[] getItemsTypes() {
        return new Integer[]{
                LogModel.TYPE_MESSAGE,
                LogModel.TYPE_NEW_GAME,
                LogModel.TYPE_NEW_ACHIEVEMENT,
                LogModel.TYPE_ACHIEVEMENT_UNLOCKED
        };
    }
}
