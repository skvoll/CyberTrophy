package io.github.skvoll.cybertrophy;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import io.github.skvoll.cybertrophy.achievements_list.AchievementsListFragment;
import io.github.skvoll.cybertrophy.data.AchievementModel;
import io.github.skvoll.cybertrophy.data.GameModel;
import io.github.skvoll.cybertrophy.data.ProfileModel;

public class GameActivity extends AppCompatActivity implements AchievementsListFragment.OnItemClickListener {
    public static final String KEY_PROFILE_ID = "PROFILE_ID";
    public static final String KEY_GAME_ID = "GAME_ID";

    private static final String TAG = GameActivity.class.getSimpleName();

    private ProfileModel mProfileModel;
    private GameModel mGameModel;

    private DrawerLayout mDrawerLayout;
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

        mDrawerLayout = findViewById(R.id.dl_drawer);
        mHeaderBackground = findViewById(R.id.iv_header_background);
        mToolbar = findViewById(R.id.tb_toolbar);
        mTabLayout = findViewById(R.id.tl_tabs);
        mViewPager = findViewById(R.id.vp_container);
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerLayout.addDrawerListener(new ActionBarDrawerToggle(
                this, mDrawerLayout, null, R.string.empty, R.string.empty) {

            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        });

        GlideApp.with(this).load(mGameModel.getLogoUrl())
                .placeholder(R.drawable.game_logo_empty)
                .into(mHeaderBackground);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(mGameModel.getName());
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        switch (mGameModel.getStatus()) {
            case GameModel.STATUS_INCOMPLETE:
                mPagerAdapter.addFragment(
                        AchievementsListFragment.newInstance(
                                mGameModel.getId(),
                                AchievementsListFragment.TYPE_LOCKED,
                                this
                        ),
                        null
                );

                mTabLayout.setVisibility(View.GONE);
                break;
            case GameModel.STATUS_IN_PROGRESS:
                mPagerAdapter.addFragment(
                        AchievementsListFragment.newInstance(
                                mGameModel.getId(),
                                AchievementsListFragment.TYPE_LOCKED,
                                this
                        ),
                        getString(R.string.achievements_list_locked)
                );
                mPagerAdapter.addFragment(
                        AchievementsListFragment.newInstance(
                                mGameModel.getId(),
                                AchievementsListFragment.TYPE_UNLOCKED,
                                this
                        ),
                        getString(R.string.achievements_list_unlocked)
                );
                break;
            case GameModel.STATUS_COMPLETE:
                mPagerAdapter.addFragment(
                        AchievementsListFragment.newInstance(
                                mGameModel.getId(),
                                AchievementsListFragment.TYPE_UNLOCKED,
                                this
                        ),
                        null
                );

                mTabLayout.setVisibility(View.GONE);
                break;
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

        Long profileId = bundle.getLong(KEY_PROFILE_ID);
        mProfileModel = ProfileModel.getById(getContentResolver(), profileId);

        if (mProfileModel == null) {
            throw new IllegalArgumentException("Profile id is missing");
        }

        Long gameId = bundle.getLong(KEY_GAME_ID);
        mGameModel = GameModel.getById(getContentResolver(), gameId);

        if (mGameModel == null) {
            throw new IllegalArgumentException("Game id is missing");
        }
    }

    @Override
    public void onClick(AchievementModel achievementModel) {
        FrameLayout frameLayout = findViewById(R.id.fl_drawer);
        Fragment fragment = AchievementFragment.newInstance(achievementModel);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_drawer, fragment).commit();

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mDrawerLayout.openDrawer(Gravity.END);
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
