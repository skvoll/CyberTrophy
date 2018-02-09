package io.github.skvoll.cybertrophy;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import io.github.skvoll.cybertrophy.dashboard.DashboardFragment;
import io.github.skvoll.cybertrophy.data.ProfileModel;
import io.github.skvoll.cybertrophy.notifications.BaseNotification;
import io.github.skvoll.cybertrophy.services.AllGamesParserJob;
import io.github.skvoll.cybertrophy.services.FirstGamesParserService;
import io.github.skvoll.cybertrophy.services.RecentGamesParserJob;

public class MainActivity extends AppCompatActivity {
    public static final String ACTION_CLOSE_NOTIFICATION = "CLOSE_NOTIFICATION";
    public static final String KEY_NOTIFICATION_ID = "NOTIFICATION_ID";
    public static final String KEY_FRAGMENT = "FRAGMENT";
    public static final int FRAGMENT_DASHBOARD = R.id.menu_dashboard;
    public static final int FRAGMENT_GAMES = R.id.menu_games;
    public static final int FRAGMENT_PROFILE = R.id.menu_profile;

    private static final String TAG = MainActivity.class.getSimpleName();

    private int devToolsCounter = 0;

    private BottomNavigationView mBottomNavigationView;
    private FragmentManager mFragmentManager;
    private Fragment mDashboardFragment;
    private Fragment mGamesFragment;
    private Fragment mProfileFragment;

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

        BaseNotification.cancelNotifications(this);

        setup();

        ProfileModel profileModel = ProfileModel.getActive(getContentResolver());

        if (profileModel == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();

            return;
        }

        if (!profileModel.isInitialized()) {
            startService(new Intent(this, FirstGamesParserService.class));
        }

        mFragmentManager = getSupportFragmentManager();
        mBottomNavigationView = findViewById(R.id.bnv_navigation);
        mDashboardFragment = new DashboardFragment();
        mGamesFragment = new GamesFragment();
        mProfileFragment = new ProfileFragment();

        int selectedItem = FRAGMENT_DASHBOARD;

        if (getIntent().getAction() != null) {
            switch (getIntent().getAction()) {
                case ACTION_CLOSE_NOTIFICATION:
                    Bundle actionExtras = getIntent().getExtras();
                    if (actionExtras == null) {
                        break;
                    }

                    NotificationManager notificationManager
                            = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                    if (notificationManager == null) {
                        break;
                    }

                    notificationManager.cancel(actionExtras.getInt(KEY_NOTIFICATION_ID));
                    break;
            }
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            bundle = savedInstanceState;
        }

        if (bundle != null) {
            selectedItem = bundle.getInt(KEY_FRAGMENT, FRAGMENT_DASHBOARD);
        }

        mBottomNavigationView.setSelectedItemId(selectedItem);
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (savedInstanceState == null) {
            setFragment(selectedItem);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_FRAGMENT, mBottomNavigationView.getSelectedItemId());
    }

    @NonNull
    private Boolean setFragment(int menuId) {
        Fragment fragment;

        switch (menuId) {
            case R.id.menu_dashboard:
                devToolsCounter = 0;
                fragment = mDashboardFragment;
                break;
            case R.id.menu_games:
                devToolsCounter = 0;
                fragment = mGamesFragment;
                break;
            case R.id.menu_profile:
                devToolsCounter++;
                fragment = mProfileFragment;
                break;
            default:
                return false;
        }

        if (devToolsCounter > 3) {
            devToolsCounter = 0;

            startActivity(new Intent(this, DevToolsActivity.class));
        }

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fl_container, fragment).commit();

        return true;
    }

    private void setup() {
        BaseNotification.createChannels(this);

        Log.d(TAG, AllGamesParserJob.class.getSimpleName() + " is "
                + (AllGamesParserJob.setup(getApplicationContext()) == 1 ? "setted" : "not setted"));
        Log.d(TAG, RecentGamesParserJob.class.getSimpleName() + " is "
                + (RecentGamesParserJob.setup(getApplicationContext()) == 1 ? "setted" : "not setted"));
    }
}
