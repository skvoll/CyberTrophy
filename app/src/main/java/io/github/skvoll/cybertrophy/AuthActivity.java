package io.github.skvoll.cybertrophy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.volley.VolleyError;

import io.github.skvoll.cybertrophy.data.ProfileModel;
import io.github.skvoll.cybertrophy.services.FirstGamesParserService;
import io.github.skvoll.cybertrophy.steam.SteamApi;
import io.github.skvoll.cybertrophy.steam.SteamProfile;

public class AuthActivity extends AppCompatActivity {
    private static final String TAG = AuthActivity.class.getSimpleName();

    private boolean mIsAuthShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showDisclaimer();
    }

    @Override
    public void onBackPressed() {
        if (mIsAuthShown) {
            showDisclaimer();
        } else {
            super.onBackPressed();
        }
    }

    private void showDisclaimer() {
        mIsAuthShown = false;

        setContentView(R.layout.activity_auth);

        findViewById(R.id.btn_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAuth();
            }
        });
    }

    @SuppressLint("setJavaScriptEnabled")
    private void showAuth() {
        mIsAuthShown = true;

        final WebView webView = new WebView(this);
        final String realm = getString(R.string.app_name);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setBackgroundColor(getResources().getColor(R.color.primaryColor));
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                setTitle(url);
                Uri uri = Uri.parse(url);

                if (uri.getAuthority().equals(realm.toLowerCase())) {
                    webView.stopLoading();
                    String steamId = Uri.parse(uri.getQueryParameter("openid.identity")).getLastPathSegment();

                    saveProfile(Long.valueOf(steamId));
                }
            }
        });

        setContentView(webView);

        webView.loadUrl(SteamApi.getAuthUrl(realm));
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
        private Activity mActivity;

        ProfileAsyncTask(Activity activity) {
            mActivity = activity;
        }

        @Override
        protected Void doInBackground(SteamProfile... steamProfiles) {
            ProfileModel profileModel = new ProfileModel(steamProfiles[0]);
            profileModel.loadBackgroundImage(mActivity);
            profileModel.save(mActivity.getContentResolver());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Intent intent = new Intent(mActivity, MainActivity.class);
            intent.putExtra(MainActivity.KEY_FRAGMENT, MainActivity.FRAGMENT_PROFILE);

            mActivity.startActivity(intent);

            mActivity.finish();
        }
    }
}
