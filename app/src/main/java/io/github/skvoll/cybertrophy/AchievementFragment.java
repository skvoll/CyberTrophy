package io.github.skvoll.cybertrophy;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.github.skvoll.cybertrophy.data.AchievementModel;
import io.github.skvoll.cybertrophy.data.GameModel;

public class AchievementFragment extends Fragment {
    private static final String TAG = AchievementFragment.class.getSimpleName();
    private static final String KEY_ID = "ID";

    private Context mContext;
    private AchievementModel mAchievementModel;
    private GameModel mGameModel;

    private ViewGroup mRootView;

    public AchievementFragment() {
    }

    public static AchievementFragment newInstance(AchievementModel achievementModel) {
        AchievementFragment achievementFragment = new AchievementFragment();

        Bundle bundle = new Bundle();
        bundle.putLong(KEY_ID, achievementModel.getId());
        achievementFragment.setArguments(bundle);

        return achievementFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getContext();

        if (mContext == null) {
            return;
        }

        Bundle bundle = getArguments();
        if (bundle == null) {
            bundle = savedInstanceState;
        }

        if (bundle == null) {
            throw new IllegalArgumentException("Params are missing");
        }

        Long id = bundle.getLong(KEY_ID);
        mAchievementModel = AchievementModel.getById(mContext.getContentResolver(), id);

        if (mAchievementModel == null) {
            throw new IllegalArgumentException("Achievement id is missing");
        }

        mGameModel = GameModel.getById(mContext.getContentResolver(), mAchievementModel.getGameId());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_achievement, container, false);

        final ImageView ivIcon = mRootView.findViewById(R.id.iv_icon);
        final TextView tvName = mRootView.findViewById(R.id.tv_name);
        final LinearLayout llUnlockDate = mRootView.findViewById(R.id.ll_unlock_date_wrapper);
        final TextView tvUnlockDate = mRootView.findViewById(R.id.tv_unlock_date);
        final TextView tvPercentage = mRootView.findViewById(R.id.tv_percentage);
        final Button btnShowInfo = mRootView.findViewById(R.id.btn_show_info);

        final ImageButton btnGuidesSteam = mRootView.findViewById(R.id.btn_guides_steam);
        final ImageButton btnGuidesYoutube = mRootView.findViewById(R.id.btn_guides_youtube);
        final ImageButton btnGuidesGoogle = mRootView.findViewById(R.id.btn_guides_google);

        final LinearLayout llAchievementInfo = mRootView.findViewById(R.id.ll_achievement_info);
        final TextView tvDescription = mRootView.findViewById(R.id.tv_description);

        final String description = mAchievementModel.getDescription() != null
                ? mAchievementModel.getDescription()
                : mContext.getResources().getString(R.string.empty_achievement_description);

        Date date = new Date();
        date.setTime(mAchievementModel.getUnlockTime() * 1000L);
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT, getResources().getConfiguration().locale);

        final String unlockDate = dateFormat.format(date);

        if (mAchievementModel.isUnlocked() || !mAchievementModel.isHidden()) {
            String icon = mAchievementModel.isUnlocked() ?
                    mAchievementModel.getIconUrl() : mAchievementModel.getIconGrayUrl();

            GlideApp.with(mContext).load(icon)
                    .placeholder(R.drawable.achievement_icon_empty)
                    .into(ivIcon);
            tvName.setText(mAchievementModel.getName());
            tvDescription.setText(description);
        } else {
            GlideApp.with(mContext).load(R.drawable.achievement_icon_hidden)
                    .placeholder(R.drawable.achievement_icon_empty)
                    .into(ivIcon);
            tvName.setText(R.string.achievement_title_hidden);

            llAchievementInfo.setVisibility(View.GONE);
            btnShowInfo.setVisibility(View.VISIBLE);

            btnShowInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GlideApp.with(mContext).load(mAchievementModel.getIconGrayUrl())
                            .placeholder(R.drawable.achievement_icon_empty)
                            .into(ivIcon);
                    tvName.setText(mAchievementModel.getName());
                    tvDescription.setText(description);

                    llAchievementInfo.setVisibility(View.VISIBLE);
                    btnShowInfo.setVisibility(View.GONE);
                }
            });
        }

        if (!mAchievementModel.isUnlocked()) {
            llUnlockDate.setVisibility(View.GONE);
        }

        tvUnlockDate.setText(unlockDate);
        tvPercentage.setText(String.format("%s%%", mAchievementModel.getPercent()));

        btnGuidesSteam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guidesSearchSteam(mGameModel.getAppId(), mAchievementModel.getName());
            }
        });

        btnGuidesYoutube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guidesSearchYoutube(mGameModel.getName(), mAchievementModel.getName());
            }
        });

        btnGuidesGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guidesSearchGoogle(mGameModel.getName(), mAchievementModel.getName());
            }
        });

        return mRootView;
    }

    private void guidesSearchSteam(Long appId, String achievementName) {
        openChromeCustomTab(Uri.parse(
                "http://steamcommunity.com/app/" + appId + "/guides/?searchText=" + achievementName + "&browsefilter=trend&browsesort=creationorder&requiredtags%5B%5D=Achievements"
        ));
    }

    private void guidesSearchYoutube(String gameName, String achievementName) {
        String query = gameName + " " + achievementName + " achievement";

        try {
            Intent intent = new Intent(Intent.ACTION_SEARCH);
            intent.setPackage("com.google.android.youtube");
            intent.putExtra("query", query);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            openChromeCustomTab(Uri.parse(
                    "https://www.youtube.com/results?search_query=" + query
            ));
        }
    }

    private void guidesSearchGoogle(String gameName, String achievementName) {
        String query = gameName + " " + achievementName + " achievement";

        openChromeCustomTab(Uri.parse(
                "https://www.google.ru/search?q=" + query
        ));
    }

    private void openChromeCustomTab(Uri uri) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(mContext, uri);
    }
}
