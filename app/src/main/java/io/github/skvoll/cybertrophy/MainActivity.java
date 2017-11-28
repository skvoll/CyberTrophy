package io.github.skvoll.cybertrophy;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import io.github.skvoll.cybertrophy.dashboard.DashboardFragment;
import io.github.skvoll.cybertrophy.data.ProfileModel;

public class MainActivity extends AppCompatActivity {
    public static final String PARAMS_FRAGMENT = "PARAMS_FRAGMENT";
    public static final int FRAGMENT_DASHBOARD = R.id.menu_dashboard;
    public static final int FRAGMENT_GAMES = R.id.menu_games;
    public static final int FRAGMENT_PROFILE = R.id.menu_profile;

    private static final String TAG = MainActivity.class.getSimpleName();

    private FragmentManager mFragmentManager;
    private Fragment mDashboardFragment;
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

        ProfileModel profileModel = ProfileModel.getActive(getContentResolver());

        mFragmentManager = getSupportFragmentManager();
        mDashboardFragment = new DashboardFragment();
        mProfileFragment = new ProfileFragment();

        int selectedItem = FRAGMENT_DASHBOARD;

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            selectedItem = bundle.getInt(PARAMS_FRAGMENT, FRAGMENT_DASHBOARD);
        }

        BottomNavigationView navigation = findViewById(R.id.bnv_navigation);
        navigation.setSelectedItemId(selectedItem);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        setFragment(selectedItem);
    }

    private Boolean setFragment(int menuId) {
        Fragment fragment;

        switch (menuId) {
            case R.id.menu_dashboard:
                fragment = mDashboardFragment;
                break;
            case R.id.menu_games:
                fragment = mDashboardFragment;
                break;
            case R.id.menu_profile:
                fragment = mProfileFragment;
                break;
            default:
                return false;
        }

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fl_container, fragment).commit();

        return true;
    }
}
