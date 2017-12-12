package io.github.skvoll.cybertrophy;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.github.skvoll.cybertrophy.data.ProfileModel;
import io.github.skvoll.cybertrophy.games_list.GamesListFragment;

public class GamesFragment extends Fragment {
    public static final String PARAMS_TAB = "PARAMS_TAB";

    private static final String TAG = GamesFragment.class.getSimpleName();

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

        ProfileModel profileModel = ProfileModel.getActive(getContext().getContentResolver());

        if (profileModel == null) {
            return null;
        }

        ViewGroup mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_games, container, false);

        mTabLayout = mRootView.findViewById(R.id.tl_tabs);
        mViewPager = mRootView.findViewById(R.id.vp_container);

        mPagerAdapter = new PagerAdapter(getChildFragmentManager());
        mPagerAdapter.addFragment(
                GamesListFragment.newInstance(profileModel.getSteamId(), GamesListFragment.TYPE_IN_PROGRESS),
                getString(R.string.games_list_tab_in_progress)
        );
        mPagerAdapter.addFragment(
                GamesListFragment.newInstance(profileModel.getSteamId(), GamesListFragment.TYPE_INCOMPLETE),
                getString(R.string.games_list_tab_incomplete)
        );
        mPagerAdapter.addFragment(
                GamesListFragment.newInstance(profileModel.getSteamId(), GamesListFragment.TYPE_COMPLETE),
                getString(R.string.games_list_tab_complete)
        );

        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int currentItem = 0;

        if (savedInstanceState != null) {
            currentItem = savedInstanceState.getInt(PARAMS_TAB, 0);
        }

        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setCurrentItem(currentItem);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(PARAMS_TAB, mViewPager.getCurrentItem());
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
