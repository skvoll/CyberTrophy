package io.github.skvoll.cybertrophy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import io.github.skvoll.cybertrophy.data.ProfileModel;
import io.github.skvoll.cybertrophy.notifications.BaseNotification;
import io.github.skvoll.cybertrophy.services.AllGamesParserJob;
import io.github.skvoll.cybertrophy.services.FirstGamesParserService;
import io.github.skvoll.cybertrophy.services.RecentGamesParserJob;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        setupJobSchedule();

        final ProfileModel profileModel = ProfileModel.getActive(getContentResolver());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (profileModel == null) {
                    startActivity(new Intent(SplashActivity.this, AuthActivity.class));
                } else {
                    if (!profileModel.isInitialized()) {
                        startService(new Intent(SplashActivity.this, FirstGamesParserService.class));
                    }

                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }

                finish();
            }
        }, 1500);
    }

    private void setupJobSchedule() {
        BaseNotification.createChannels(this);

        Log.d(TAG, AllGamesParserJob.class.getSimpleName() + " is setted: "
                + (AllGamesParserJob.setup(getApplicationContext()) == 1 ? "true" : "false"));
        Log.d(TAG, RecentGamesParserJob.class.getSimpleName() + " is setted: "
                + (RecentGamesParserJob.setup(getApplicationContext()) == 1 ? "true" : "false"));
    }
}
