package io.github.skvoll.cybertrophy;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import io.github.skvoll.cybertrophy.achievements_list.AchievementsListFragment;
import io.github.skvoll.cybertrophy.data.GameModel;
import io.github.skvoll.cybertrophy.data.ProfileModel;

public class GameActivity extends AppCompatActivity {
    public static final String KEY_APP_ID = "APP_ID";
    public static final String KEY_STEAM_ID = "STEAM_ID";

    private static final String TAG = GameActivity.class.getSimpleName();

    private ProfileModel mProfileModel;
    private GameModel mGameModel;

    private ImageView mHeaderBackground;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        getParams(savedInstanceState);

        mHeaderBackground = findViewById(R.id.iv_header_background);
        mToolbar = findViewById(R.id.tb_toolbar);
        mTabLayout = findViewById(R.id.tl_tabs);
        mViewPager = findViewById(R.id.vp_container);
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());

        GlideApp.with(this).load(mGameModel.getLogoUrl())
                .placeholder(R.drawable.no_game_logo)
                .into(mHeaderBackground);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(mGameModel.getName());
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (mGameModel.getStatus() == GameModel.STATUS_IN_PROGRESS) {
            mPagerAdapter.addFragment(
                    AchievementsListFragment.newInstance(
                            mProfileModel.getSteamId(), mGameModel.getAppId(), AchievementsListFragment.TYPE_LOCKED),
                    getString(R.string.achievements_list_locked)
            );
            mPagerAdapter.addFragment(
                    AchievementsListFragment.newInstance(
                            mProfileModel.getSteamId(), mGameModel.getAppId(), AchievementsListFragment.TYPE_UNLOCKED),
                    getString(R.string.achievements_list_unlocked)
            );
        } else {
            mPagerAdapter.addFragment(
                    AchievementsListFragment.newInstance(
                            mProfileModel.getSteamId(), mGameModel.getAppId(), AchievementsListFragment.TYPE_ALL),
                    null
            );

            mTabLayout.setVisibility(View.GONE);
        }

        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return false;
    }

    private void getParams(Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            bundle = savedInstanceState;
        }

        if (bundle == null) {
            throw new IllegalArgumentException("Params are missing");
        }

        Long steamId = bundle.getLong(KEY_STEAM_ID);
        mProfileModel = ProfileModel.getBySteamId(getContentResolver(), steamId);

        if (mProfileModel == null) {
            throw new IllegalArgumentException("Steam id is missing");
        }

        Long appId = bundle.getLong(KEY_APP_ID);
        mGameModel = GameModel.getByAppId(getContentResolver(), appId);

        if (mGameModel == null) {
            throw new IllegalArgumentException("App id is missing");
        }
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
