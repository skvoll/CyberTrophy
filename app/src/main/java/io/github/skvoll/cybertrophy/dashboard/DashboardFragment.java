package io.github.skvoll.cybertrophy.dashboard;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.DatabaseHelper;
import io.github.skvoll.cybertrophy.data.LogModel;

public class DashboardFragment extends Fragment {
    private static final String TAG = DashboardFragment.class.getSimpleName();

    public DashboardFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        loadData();
    }

    private void loadData() {
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

        ArrayList<DashboardItem> dashboardItems = DashboardItem.getItems(
                databaseHelper.getReadableDatabase(), getItemsTypes(), -1, 0);

        for (DashboardItem dashboardItem : dashboardItems) {
            switch (dashboardItem.getType()) {
                case DashboardItem.TYPE_NEW_GAME:
                    Log.i(TAG, "New game added: " + dashboardItem.getAppName());
                    break;
                case DashboardItem.TYPE_ACHIEVEMENT_UNLOCKED:
                    Log.i(TAG, "Achievement unlocked: " + dashboardItem.getAchievementName());
                    break;
            }
        }
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
