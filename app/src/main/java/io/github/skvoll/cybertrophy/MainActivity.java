package io.github.skvoll.cybertrophy;

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

import io.github.skvoll.cybertrophy.data.ProfileModel;
import io.github.skvoll.cybertrophy.notifications.BaseNotification;
import io.github.skvoll.cybertrophy.services.AllGamesParserJob;
import io.github.skvoll.cybertrophy.services.FirstGamesParserService;
import io.github.skvoll.cybertrophy.services.RecentGamesParserJob;

public class MainActivity extends AppCompatActivity {
    public static final String KEY_FRAGMENT = "FRAGMENT";
    public static final int FRAGMENT_DASHBOARD = R.id.menu_dashboard;
    public static final int FRAGMENT_GAMES = R.id.menu_games;
    public static final int FRAGMENT_PROFILE = R.id.menu_profile;

    private static final String TAG = MainActivity.class.getSimpleName();

    private int devToolsCounter = 0;

    private BottomNavigationView mBtnNavigation;
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
        mBtnNavigation = findViewById(R.id.bnv_navigation);
        mDashboardFragment = new DashboardFragment();
        mGamesFragment = new GamesFragment();
        mProfileFragment = new ProfileFragment();

        int selectedItem = FRAGMENT_DASHBOARD;

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            bundle = savedInstanceState;
        }

        if (bundle != null) {
            selectedItem = bundle.getInt(KEY_FRAGMENT, FRAGMENT_DASHBOARD);
        }

        mBtnNavigation.setSelectedItemId(selectedItem);
        mBtnNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (savedInstanceState == null) {
            setFragment(selectedItem);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_FRAGMENT, mBtnNavigation.getSelectedItemId());
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
        BaseNotification.cancelNotifications(this);

        Log.d(TAG, AllGamesParserJob.class.getSimpleName() + " has "
                + (AllGamesParserJob.setup(getApplicationContext()) == 1 ? "been set" : "not been set"));
        Log.d(TAG, RecentGamesParserJob.class.getSimpleName() + " has "
                + (RecentGamesParserJob.setup(getApplicationContext()) == 1 ? "been set" : "not been set"));
    }
}
