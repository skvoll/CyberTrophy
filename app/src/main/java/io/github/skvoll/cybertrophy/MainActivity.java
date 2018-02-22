package io.github.skvoll.cybertrophy;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import java.lang.ref.WeakReference;

import io.github.skvoll.cybertrophy.data.NotificationModel;
import io.github.skvoll.cybertrophy.data.ProfileModel;
import io.github.skvoll.cybertrophy.notifications.BaseNotification;
import io.github.skvoll.cybertrophy.notifications.list.NotificationsListFragment;
import io.github.skvoll.cybertrophy.services.AllGamesParserJob;
import io.github.skvoll.cybertrophy.services.FirstGamesParserService;
import io.github.skvoll.cybertrophy.services.RecentGamesParserJob;

public class MainActivity extends AppCompatActivity {
    public static final String KEY_FRAGMENT = "FRAGMENT";
    public static final int FRAGMENT_DASHBOARD = R.id.menu_dashboard;
    public static final int FRAGMENT_GAMES = R.id.menu_games;
    public static final int FRAGMENT_NOTIFICATIONS_LIST = R.id.menu_notifications_list;
    public static final int FRAGMENT_PROFILE = R.id.menu_profile;

    private static final String TAG = MainActivity.class.getSimpleName();

    private int mDevToolsCounter = 0;

    private ProfileModel mProfileModel;

    private BottomNavigationView mBnvNavigation;
    private FragmentManager mFragmentManager;
    private Fragment mDashboardFragment;
    private Fragment mGamesFragment;
    private Fragment mNotificationsListFragment;
    private Fragment mProfileFragment;
    private Fragment mCurrentFragment;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            return setFragment(item.getItemId());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setup();

        mProfileModel = ProfileModel.getActive(getContentResolver());

        if (mProfileModel == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();

            return;
        }

        if (!mProfileModel.isInitialized()) {
            startService(new Intent(this, FirstGamesParserService.class));
        }

        mFragmentManager = getSupportFragmentManager();
        mBnvNavigation = findViewById(R.id.bnv_navigation);
        mDashboardFragment = new DashboardFragment();
        mGamesFragment = new GamesFragment();
        mNotificationsListFragment = new NotificationsListFragment();
        mProfileFragment = new ProfileFragment();
        mCurrentFragment = mDashboardFragment;

        for (Fragment fragment : mFragmentManager.getFragments()) {
            getSupportFragmentManager().beginTransaction().hide(fragment).commit();
        }

        mFragmentManager.beginTransaction().add(R.id.fl_container,
                mProfileFragment, String.valueOf(FRAGMENT_PROFILE)).commit();
        mFragmentManager.beginTransaction().hide(mProfileFragment).add(R.id.fl_container,
                mNotificationsListFragment, String.valueOf(FRAGMENT_GAMES)).commit();
        mFragmentManager.beginTransaction().hide(mNotificationsListFragment).add(R.id.fl_container,
                mGamesFragment, String.valueOf(FRAGMENT_GAMES)).commit();
        mFragmentManager.beginTransaction().hide(mGamesFragment).add(R.id.fl_container,
                mDashboardFragment, String.valueOf(FRAGMENT_DASHBOARD)).commit();

        int selectedItem = FRAGMENT_DASHBOARD;

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            bundle = savedInstanceState;
        }

        if (bundle != null) {
            selectedItem = bundle.getInt(KEY_FRAGMENT, FRAGMENT_DASHBOARD);
        }

        mBnvNavigation.setSelectedItemId(selectedItem);
        mBnvNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        setFragment(selectedItem);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_FRAGMENT, mBnvNavigation.getSelectedItemId());
    }

    @NonNull
    private Boolean setFragment(int menuId) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        switch (menuId) {
            case FRAGMENT_DASHBOARD:
                mDevToolsCounter = 0;
                if (mCurrentFragment != mDashboardFragment) {
                    fragmentTransaction.hide(mCurrentFragment).show(mDashboardFragment).commit();
                }

                mCurrentFragment = mDashboardFragment;

                break;
            case FRAGMENT_GAMES:
                mDevToolsCounter = 0;
                if (mCurrentFragment != mGamesFragment) {
                    fragmentTransaction.hide(mCurrentFragment).show(mGamesFragment).commit();
                }

                mCurrentFragment = mGamesFragment;

                break;
            case FRAGMENT_NOTIFICATIONS_LIST:
                CheckNotificationsTask.isOpened = true;
                mDevToolsCounter = 0;
                if (mCurrentFragment != mNotificationsListFragment) {
                    fragmentTransaction.hide(mCurrentFragment).show(mNotificationsListFragment).commit();
                }

                mCurrentFragment = mNotificationsListFragment;

                break;
            case FRAGMENT_PROFILE:
                mDevToolsCounter++;
                if (mCurrentFragment != mProfileFragment) {
                    fragmentTransaction.hide(mCurrentFragment).show(mProfileFragment).commit();
                }

                mCurrentFragment = mProfileFragment;

                break;
            default:
                return false;
        }

        if (mDevToolsCounter > 3) {
            mDevToolsCounter = 0;

            startActivity(new Intent(this, DevToolsActivity.class));
        }

        (new CheckNotificationsTask(this, mProfileModel)).execute();

        return true;
    }

    private void setNotificationIcon(@DrawableRes int iconResource) {
        MenuItem menuItem = mBnvNavigation.getMenu().findItem(FRAGMENT_NOTIFICATIONS_LIST);

        if (menuItem == null) {
            return;
        }

        menuItem.setIcon(getDrawable(iconResource));
    }

    private void setup() {
        BaseNotification.createChannels(this);
        BaseNotification.cancelNotifications(this);

        Log.d(TAG, AllGamesParserJob.class.getSimpleName() + " has "
                + (AllGamesParserJob.setup(getApplicationContext()) == 1 ? "been set" : "not been set"));
        Log.d(TAG, RecentGamesParserJob.class.getSimpleName() + " has "
                + (RecentGamesParserJob.setup(getApplicationContext()) == 1 ? "been set" : "not been set"));
    }

    private static class CheckNotificationsTask extends AsyncTask<Void, Void, Integer> {
        static boolean isOpened = false;

        private WeakReference<MainActivity> mMainActivityWeakReference;
        private ProfileModel mProfileModel;

        CheckNotificationsTask(MainActivity mainActivity, ProfileModel profileModel) {
            mMainActivityWeakReference = new WeakReference<>(mainActivity);
            mProfileModel = profileModel;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            MainActivity mainActivity = mMainActivityWeakReference.get();

            if (mainActivity == null) {
                return null;
            }

            ContentResolver contentResolver = mainActivity.getContentResolver();

            return NotificationModel.getUnviewedCountByProfile(contentResolver, mProfileModel);
        }

        @Override
        protected void onPostExecute(Integer unviewedNotificationsCount) {
            MainActivity mainActivity = mMainActivityWeakReference.get();

            if (mainActivity == null) {
                return;
            }

            if (unviewedNotificationsCount > 0) {
                if (isOpened) {
                    mainActivity.setNotificationIcon(R.drawable.ic_notifications_black_24dp);
                } else {
                    mainActivity.setNotificationIcon(R.drawable.ic_notifications_active_black_24dp);
                }
            } else {
                isOpened = false;
                mainActivity.setNotificationIcon(R.drawable.ic_notifications_none_black_24dp);
            }
        }
    }
}
