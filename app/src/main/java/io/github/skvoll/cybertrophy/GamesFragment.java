package io.github.skvoll.cybertrophy;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.github.skvoll.cybertrophy.data.ProfileModel;
import io.github.skvoll.cybertrophy.games_list.GamesListFragment;

public class GamesFragment extends Fragment {
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

        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setAdapter(mPagerAdapter);

        return mRootView;
    }

    private class PagerAdapter extends FragmentPagerAdapter {
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
