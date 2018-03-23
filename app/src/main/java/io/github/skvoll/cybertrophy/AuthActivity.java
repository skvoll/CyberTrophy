package io.github.skvoll.cybertrophy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.util.LongSparseArray;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;

import java.lang.ref.WeakReference;

import io.github.skvoll.cybertrophy.data.ProfileModel;
import io.github.skvoll.cybertrophy.steam.SteamApi;
import io.github.skvoll.cybertrophy.steam.SteamProfile;

public class AuthActivity extends AppCompatActivity {
    private static final String TAG = AuthActivity.class.getSimpleName();

    private boolean mIsSteam = false;
    private BottomSheetBehavior mBottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showSignIn();
    }

    @Override
    public void onBackPressed() {
        if (mIsSteam) {
            showSignIn();
        } else if (mBottomSheetBehavior != null && mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    private void showSignIn() {
        mIsSteam = false;

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_auth);

        final LinearLayout llContainer = findViewById(R.id.ll_container);
        mBottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bs_disclaimer));
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                llContainer.setAlpha(1.0f - slideOffset);
            }
        });

        findViewById(R.id.btn_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSteam();
            }
        });

        findViewById(R.id.tv_disclaimer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    @SuppressLint("setJavaScriptEnabled")
    private void showSteam() {
        mIsSteam = true;

        getWindow().setStatusBarColor(getResources().getColor(R.color.steamHeader));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.steamBackground));

        final WebView webView = new WebView(this);
        final String realm = getString(R.string.app_name);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setBackgroundColor(getResources().getColor(R.color.steamBackground));
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Uri uri = Uri.parse(url);

                if (uri.getAuthority().equals(realm.toLowerCase())) {
                    webView.stopLoading();
                    showLoading();

                    String steamId = Uri.parse(uri.getQueryParameter("openid.identity")).getLastPathSegment();

                    saveProfile(Long.valueOf(steamId));
                }
            }
        });
        setContentView(webView);

        webView.loadUrl(SteamApi.getAuthUrl(realm));
    }

    private void showLoading() {
        mIsSteam = false;

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        FrameLayout frameLayout = new FrameLayout(this);
        ProgressBar progressBar = new ProgressBar(this);

        FrameLayout.LayoutParams llLayoutParams = new FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        llLayoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(llLayoutParams);

        frameLayout.addView(progressBar);
        setContentView(frameLayout);
    }

    private void saveProfile(final Long steamId) {
        final VolleySingleton volleySingleton = VolleySingleton.getInstance(getApplicationContext());
        SteamApi steamApi = new SteamApi(volleySingleton);

        steamApi.getPlayerSummaries(new Long[]{steamId}, new SteamApi.ResponseListener<LongSparseArray<SteamProfile>>() {
            @Override
            public void onSuccess(LongSparseArray<SteamProfile> response) {
                if (response == null || response.indexOfKey(steamId) < 0) {
                    return;
                }

                (new ProfileAsyncTask(AuthActivity.this)).execute(response.get(steamId));
            }

            @Override
            public void onError(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        });
    }

    private static class ProfileAsyncTask extends AsyncTask<SteamProfile, Void, Void> {
        private WeakReference<Activity> mContextWeakReference;

        ProfileAsyncTask(Activity activity) {
            mContextWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(SteamProfile... steamProfiles) {
            Activity activity = mContextWeakReference.get();

            if (activity == null) {
                return null;
            }

            ProfileModel profileModel = new ProfileModel(steamProfiles[0]);
            profileModel.loadBackgroundImage(activity);
            profileModel.save(activity.getContentResolver());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Activity activity = mContextWeakReference.get();

            if (activity == null) {
                return;
            }

            Intent intent = new Intent(activity, MainActivity.class);
            intent.putExtra(MainActivity.KEY_FRAGMENT, MainActivity.FRAGMENT_PROFILE);

            activity.startActivity(intent);

            activity.finish();
        }
    }
}
