package io.github.skvoll.cybertrophy;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.github.skvoll.cybertrophy.data.GameModel;
import io.github.skvoll.cybertrophy.data.ProfileModel;
import io.github.skvoll.cybertrophy.games.list.GamesListFragment;

public class GamesFragment extends Fragment implements
        GamesListFragment.OnItemClickListener {
    public static final String KEY_TAB = "TAB";

    private static final String TAG = GamesFragment.class.getSimpleName();

    private ProfileModel mProfileModel;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;

    public GamesFragment() {
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

        return inflater.inflate(R.layout.fragment_games, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int currentItem = 0;

        if (savedInstanceState != null) {
            currentItem = savedInstanceState.getInt(KEY_TAB, currentItem);
        }

        mTabLayout = view.findViewById(R.id.tl_tabs);
        mViewPager = view.findViewById(R.id.vp_container);

        mPagerAdapter = new PagerAdapter(getChildFragmentManager());
        mPagerAdapter.addFragment(
                GamesListFragment.newInstance(mProfileModel.getId(), GameModel.IN_PROGRESS, this),
                getString(R.string.games_list_tab_in_progress));
        mPagerAdapter.addFragment(
                GamesListFragment.newInstance(mProfileModel.getId(), GameModel.INCOMPLETE, this),
                getString(R.string.games_list_tab_incomplete));
        mPagerAdapter.addFragment(
                GamesListFragment.newInstance(mProfileModel.getId(), GameModel.COMPLETE, this),
                getString(R.string.games_list_tab_complete));

        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setCurrentItem(currentItem);

        mTabLayout.setupWithViewPager(mViewPager);

        view.findViewById(android.R.id.progress).setVisibility(View.GONE);
    }

    @Override
    public void onClick(GameModel gameModel) {
        if (gameModel == null) {
            return;
        }

        Intent intent = new Intent(getContext(), GameActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(GameActivity.KEY_GAME_ID, gameModel.getId());

        startActivity(intent);
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentsList = new ArrayList<>();
        private final List<String> mFragmentTitlesList = new ArrayList<>();

        PagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentsList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitlesList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentsList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentsList.add(fragment);
            mFragmentTitlesList.add(title);
        }
    }
}
