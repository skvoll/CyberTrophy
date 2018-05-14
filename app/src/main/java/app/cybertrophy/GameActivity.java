package app.cybertrophy;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
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

import app.cybertrophy.achievements.list.AchievementsListAdapter;
import app.cybertrophy.achievements.list.AchievementsListFragment;
import app.cybertrophy.data.AchievementModel;
import app.cybertrophy.data.GameModel;
import app.cybertrophy.notifications.BaseNotification;

import static java.lang.Math.abs;

public final class GameActivity extends AppCompatActivity implements
        AchievementsListAdapter.OnItemClickListener {
    public static final String KEY_GAME_ID = "GAME_ID";

    private static final String TAG = GameActivity.class.getSimpleName();

    private GameModel mGameModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        BaseNotification.cancelNotifications(this);

        getParams(savedInstanceState);

        AppBarLayout appBar = findViewById(R.id.ab_appbar);
        final FrameLayout headerBackgroundWrapper = findViewById(R.id.fl_header_background_wrapper);
        ImageView headerBackground = findViewById(R.id.iv_header_background);
        final Toolbar toolbar = findViewById(R.id.tb_toolbar);
        TabLayout tabLayout = findViewById(R.id.tl_tabs);
        ViewPager viewPager = findViewById(R.id.vp_container);
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());

        appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float max = appBarLayout.getHeight() - toolbar.getHeight();
                float current = abs(verticalOffset);

                headerBackgroundWrapper.setAlpha(1f - current / max);
            }
        });

        GlideApp.with(this).load(mGameModel.getLogoUrl())
                .placeholder(R.drawable.game_logo_empty)
                .into(headerBackground);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(mGameModel.getName());
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        switch (mGameModel.getStatus()) {
            case GameModel.STATUS_INCOMPLETE:
                pagerAdapter.addFragment(
                        AchievementsListFragment.newInstance(
                                mGameModel.getId(),
                                AchievementModel.STATUS_LOCKED,
                                this
                        ),
                        null
                );

                tabLayout.setVisibility(View.GONE);
                break;
            case GameModel.STATUS_IN_PROGRESS:
                pagerAdapter.addFragment(
                        AchievementsListFragment.newInstance(
                                mGameModel.getId(),
                                AchievementModel.STATUS_LOCKED,
                                this
                        ),
                        getString(R.string.achievements_list_locked)
                );
                pagerAdapter.addFragment(
                        AchievementsListFragment.newInstance(
                                mGameModel.getId(),
                                AchievementModel.STATUS_UNLOCKED,
                                this
                        ),
                        getString(R.string.achievements_list_unlocked)
                );
                break;
            case GameModel.STATUS_COMPLETE:
                pagerAdapter.addFragment(
                        AchievementsListFragment.newInstance(
                                mGameModel.getId(),
                                AchievementModel.STATUS_UNLOCKED,
                                this
                        ),
                        null
                );

                tabLayout.setVisibility(View.GONE);
                break;
        }

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                if (intent == null) {
                    return super.onOptionsItemSelected(item);
                }

                if (NavUtils.shouldUpRecreateTask(this, intent) || isTaskRoot()) {
                    TaskStackBuilder.create(this)
                            .addNextIntentWithParentStack(intent)
                            .startActivities();
                } else {
                    NavUtils.navigateUpTo(this, intent);
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getParams(Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            bundle = savedInstanceState;
        }

        if (bundle == null) {
            throw new IllegalArgumentException("Params are missing.");
        }

        Long gameId = bundle.getLong(KEY_GAME_ID);
        mGameModel = GameModel.getById(getContentResolver(), gameId);

        if (mGameModel == null) {
            throw new IllegalArgumentException("Game id is missing.");
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
