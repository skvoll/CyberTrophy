package app.cybertrophy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import java.lang.reflect.Field;

import app.cybertrophy.data.DataContract;
import app.cybertrophy.data.NotificationModel;
import app.cybertrophy.data.ProfileModel;
import app.cybertrophy.notifications.BaseNotification;
import app.cybertrophy.notifications.list.NotificationsListFragment;
import app.cybertrophy.services.AllGamesParserJob;
import app.cybertrophy.services.FirstGamesParserService;
import app.cybertrophy.services.RecentGamesParserJob;

public final class MainActivity extends AppCompatActivity {
    public static final String KEY_FRAGMENT = "FRAGMENT";
    public static final int FRAGMENT_DASHBOARD = R.id.menu_dashboard;
    public static final int FRAGMENT_GAMES = R.id.menu_games;
    public static final int FRAGMENT_NOTIFICATIONS_LIST = R.id.menu_notifications_list;
    public static final int FRAGMENT_PROFILE = R.id.menu_profile;

    private static final String TAG = MainActivity.class.getSimpleName();

    private int mDevToolsCounter = 0;

    private ProfileModel mProfileModel;
    private NotificationObserver mNotificationObserver;

    private BottomNavigationView mBnvNavigation;
    private FragmentManager mFragmentManager;
    private Fragment mDashboardFragment;
    private Fragment mGamesFragment;
    private Fragment mNotificationsListFragment;
    private Fragment mProfileFragment;
    private Fragment mCurrentFragment;

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
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

        mNotificationObserver = new NotificationObserver(new Handler());

        getContentResolver().registerContentObserver(
                DataContract.NotificationEntry.URI, true, mNotificationObserver);

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

        disableShiftMode(mBnvNavigation);
        mBnvNavigation.setSelectedItemId(selectedItem);
        mBnvNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        setFragment(selectedItem);

        mNotificationObserver.updateNotificationIcon(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_FRAGMENT, mBnvNavigation.getSelectedItemId());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getContentResolver().unregisterContentObserver(mNotificationObserver);
    }

    @NonNull
    private Boolean setFragment(int menuId) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        switch (menuId) {
            case FRAGMENT_DASHBOARD:
                mDevToolsCounter = 0;
                if (mCurrentFragment != mDashboardFragment) {
                    fragmentTransaction.hide(mCurrentFragment).show(mDashboardFragment).commit();

                    mCurrentFragment = mDashboardFragment;
                }

                break;
            case FRAGMENT_GAMES:
                mDevToolsCounter = 0;
                if (mCurrentFragment != mGamesFragment) {
                    fragmentTransaction.hide(mCurrentFragment).show(mGamesFragment).commit();

                    mCurrentFragment = mGamesFragment;
                }

                break;
            case FRAGMENT_NOTIFICATIONS_LIST:
                mDevToolsCounter = 0;
                if (mCurrentFragment != mNotificationsListFragment) {
                    fragmentTransaction.hide(mCurrentFragment).show(mNotificationsListFragment).commit();

                    mCurrentFragment = mNotificationsListFragment;
                }

                break;
            case FRAGMENT_PROFILE:
                mDevToolsCounter++;
                if (mCurrentFragment != mProfileFragment) {
                    fragmentTransaction.hide(mCurrentFragment).show(mProfileFragment).commit();

                    mCurrentFragment = mProfileFragment;
                }

                break;
            default:
                return false;
        }

        mNotificationObserver.updateNotificationIcon(false);

        if (mDevToolsCounter > 3) {
            mDevToolsCounter = 0;

            startActivity(new Intent(this, DevToolsActivity.class));
        }

        return true;
    }

    private void setup() {
        BaseNotification.createChannels(this);
        BaseNotification.cancelNotifications(this);

        Log.d(TAG, AllGamesParserJob.class.getSimpleName() + " has "
                + (AllGamesParserJob.setup(getApplicationContext()) == 1 ? "been set." : "not been set."));
        Log.d(TAG, RecentGamesParserJob.class.getSimpleName() + " has "
                + (RecentGamesParserJob.setup(getApplicationContext()) == 1 ? "been set." : "not been set."));
    }

    @SuppressLint("RestrictedApi")
    // TODO: review and remove code from proguard-rules.pro
    private void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                item.setShiftingMode(false);
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class NotificationObserver extends ContentObserver {
        private int mUnviewedCount = 0;

        NotificationObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            updateNotificationIcon(true);
        }

        void updateNotificationIcon(boolean needUpdate) {
            MenuItem menuItem = mBnvNavigation.getMenu().findItem(FRAGMENT_NOTIFICATIONS_LIST);

            if (menuItem == null) {
                return;
            }

            if (needUpdate) {
                mUnviewedCount = NotificationModel.getUnviewedCountByProfile(
                        getContentResolver(), mProfileModel);
            }

            if (mUnviewedCount > 0) {
                if (mCurrentFragment == mNotificationsListFragment) {
                    menuItem.setIcon(getDrawable(R.drawable.ic_notifications_black_24dp));
                } else {
                    menuItem.setIcon(getDrawable(R.drawable.ic_notifications_active_black_24dp));
                }
            } else {
                menuItem.setIcon(getDrawable(R.drawable.ic_notifications_none_black_24dp));
            }
        }
    }
}
