package io.github.skvoll.cybertrophy.dashboard;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.DatabaseHelper;
import io.github.skvoll.cybertrophy.data.LogModel;

public class DashboardFragment extends Fragment {
    private static final String TAG = DashboardFragment.class.getSimpleName();

    private ViewGroup mRootView;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public DashboardFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_dashboard, container, false);

        mRecyclerView = mRootView.findViewById(R.id.rv_list);

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

        ArrayList<DashboardItem> dashboardItems = DashboardItem.getItems(
                databaseHelper.getReadableDatabase(), getItemsTypes(), -1, 0);

        mAdapter = new DashboardAdapter(getContext(), dashboardItems);
        mRecyclerView.setAdapter(mAdapter);

        return mRootView;
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
