package io.github.skvoll.cybertrophy;

import android.content.Context;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;

public final class ChromeCustomTab {
    private Context mContext;
    private Uri mUri;

    private CustomTabsIntent.Builder mBuilder;

    private ChromeCustomTab(Context context, Uri uri) {
        mContext = context;
        mUri = uri;

        mBuilder = new CustomTabsIntent.Builder();

        mBuilder.setToolbarColor(mContext.getResources().getColor(R.color.primaryColor));
        mBuilder.setSecondaryToolbarColor(mContext.getResources().getColor(R.color.primaryDarkColor));

        mBuilder.setStartAnimations(mContext, R.anim.slide_in_right, R.anim.slide_out_right);
        mBuilder.setExitAnimations(mContext, R.anim.slide_in_left, R.anim.slide_out_left);
    }

    public static void show(Context context, Uri uri) {
        ChromeCustomTab chromeCustomTab = new ChromeCustomTab(context, uri);

        chromeCustomTab.show();
    }

    public static void show(Context context, String uri) {
        show(context, Uri.parse(uri));
    }

    private void show() {
        mBuilder.build().launchUrl(mContext, mUri);
    }
}
