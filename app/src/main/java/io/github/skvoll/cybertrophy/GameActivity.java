package io.github.skvoll.cybertrophy;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
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
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import io.github.skvoll.cybertrophy.achievements.list.AchievementsListAdapter;
import io.github.skvoll.cybertrophy.achievements.list.AchievementsListFragment;
import io.github.skvoll.cybertrophy.data.AchievementModel;
import io.github.skvoll.cybertrophy.data.GameModel;
import io.github.skvoll.cybertrophy.notifications.BaseNotification;

import static java.lang.Math.abs;

public final class GameActivity extends AppCompatActivity implements
        AchievementsListAdapter.OnItemClickListener {
    public static final String KEY_GAME_ID = "GAME_ID";

    private static final String TAG = GameActivity.class.getSimpleName();

    private GameModel mGameModel;

    private AppBarLayout mAppBar;
    private FrameLayout mHeaderBackgroundWrapper;
    private ImageView mHeaderBackground;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        BaseNotification.cancelNotifications(this);

        getParams(savedInstanceState);

        mAppBar = findViewById(R.id.ab_appbar);
        mHeaderBackgroundWrapper = findViewById(R.id.fl_header_background_wrapper);
        mHeaderBackground = findViewById(R.id.iv_header_background);
        mToolbar = findViewById(R.id.tb_toolbar);
        mTabLayout = findViewById(R.id.tl_tabs);
        mViewPager = findViewById(R.id.vp_container);
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());

        mAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float max = appBarLayout.getHeight() - mToolbar.getHeight();
                float current = abs(verticalOffset);

                mHeaderBackgroundWrapper.setAlpha(1f - current / max);
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
                                AchievementModel.STATUS_LOCKED,
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
                                AchievementModel.STATUS_LOCKED,
                                this
                        ),
                        getString(R.string.achievements_list_locked)
                );
                mPagerAdapter.addFragment(
                        AchievementsListFragment.newInstance(
                                mGameModel.getId(),
                                AchievementModel.STATUS_UNLOCKED,
                                this
                        ),
                        getString(R.string.achievements_list_unlocked)
                );
                break;
            case GameModel.STATUS_COMPLETE:
                mPagerAdapter.addFragment(
                        AchievementsListFragment.newInstance(
                                mGameModel.getId(),
                                AchievementModel.STATUS_UNLOCKED,
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
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    private void getParams(Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            bundle = savedInstanceState;
        }

        if (bundle == null) {
            throw new IllegalArgumentException("Params are missing");
        }

        Long gameId = bundle.getLong(KEY_GAME_ID);
        mGameModel = GameModel.getById(getContentResolver(), gameId);

        if (mGameModel == null) {
            throw new IllegalArgumentException("Game id is missing");
        }
    }

    @Override
    public void onClick(AchievementModel achievementModel) {
        AchievementPreviewDialogFragment achievementPreviewDialogFragment
                = AchievementPreviewDialogFragment.newInstance(achievementModel);

        achievementPreviewDialogFragment.show(getSupportFragmentManager(),
                achievementPreviewDialogFragment.getTag());
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
