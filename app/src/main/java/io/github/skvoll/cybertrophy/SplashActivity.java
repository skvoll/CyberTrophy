package io.github.skvoll.cybertrophy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import io.github.skvoll.cybertrophy.data.ProfileModel;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final ProfileModel profileModel = ProfileModel.getActive(getContentResolver());

        Intent intent;

        if (profileModel == null) {
            intent = new Intent(SplashActivity.this, AuthActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
