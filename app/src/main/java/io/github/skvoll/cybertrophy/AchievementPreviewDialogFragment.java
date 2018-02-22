package io.github.skvoll.cybertrophy;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.github.skvoll.cybertrophy.data.AchievementModel;
import io.github.skvoll.cybertrophy.data.GameModel;

public class AchievementPreviewDialogFragment extends BottomSheetDialogFragment {
    private static final String TAG = AchievementPreviewDialogFragment.class.getSimpleName();
    private static final String KEY_ID = "ID";

    private Context mContext;
    private AchievementModel mAchievementModel;
    private GameModel mGameModel;

    public AchievementPreviewDialogFragment() {
    }

    public static AchievementPreviewDialogFragment newInstance(AchievementModel achievementModel) {
        AchievementPreviewDialogFragment achievementPreviewDialogFragment
                = new AchievementPreviewDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putLong(KEY_ID, achievementModel.getId());
        achievementPreviewDialogFragment.setArguments(bundle);

        return achievementPreviewDialogFragment;
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

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        ViewGroup rootView = (ViewGroup) View.inflate(getContext(),
                R.layout.fragment_achievement_preview_dialog, null);

        final ImageView ivIcon = rootView.findViewById(R.id.iv_icon);
        final ImageView ivIconMask = rootView.findViewById(R.id.iv_icon_mask);
        final TextView tvName = rootView.findViewById(R.id.tv_name);
        final TextView tvUnlockDate = rootView.findViewById(R.id.tv_unlock_date);
        final TextView tvRarity = rootView.findViewById(R.id.tv_rarity);
        final Button btnShowInfo = rootView.findViewById(R.id.btn_show_info);

        final ImageButton btnGuidesSteam = rootView.findViewById(R.id.btn_guides_steam);
        final ImageButton btnGuidesYoutube = rootView.findViewById(R.id.btn_guides_youtube);
        final ImageButton btnGuidesGoogle = rootView.findViewById(R.id.btn_guides_google);

        final LinearLayout llAchievementInfo = rootView.findViewById(R.id.ll_achievement_info);
        final TextView tvDescription = rootView.findViewById(R.id.tv_description);

        final String description = mAchievementModel.getDescription() != null
                ? mAchievementModel.getDescription()
                : mContext.getResources().getString(R.string.empty_achievement_description);

        Date date = new Date();
        date.setTime(mAchievementModel.getUnlockTime() * 1000L);
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT, Resources.getSystem().getConfiguration().locale);

        final String unlockDate = dateFormat.format(date);

        if (mAchievementModel.isUnlocked()) {
            GlideApp.with(mContext).load(mAchievementModel.getIconUrl())
                    .placeholder(R.drawable.achievement_icon_empty)
                    .into(ivIcon);

            tvName.setText(mAchievementModel.getName());
            tvDescription.setText(description);
        } else {
            GlideApp.with(mContext).load(mAchievementModel.getIconGrayUrl())
                    .placeholder(R.drawable.achievement_icon_empty)
                    .into(ivIcon);

            if (mAchievementModel.isHidden()) {
                ivIconMask.setVisibility(View.VISIBLE);

                tvName.setText(R.string.achievement_title_hidden);
                llAchievementInfo.setVisibility(View.INVISIBLE);
                btnShowInfo.setVisibility(View.VISIBLE);

                btnShowInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ivIconMask.setVisibility(View.GONE);
                        tvName.setText(mAchievementModel.getName());
                        tvDescription.setText(description);

                        llAchievementInfo.setVisibility(View.VISIBLE);
                        btnShowInfo.setVisibility(View.GONE);
                    }
                });
            } else {
                tvName.setText(mAchievementModel.getName());
                tvDescription.setText(description);
            }
        }

        if (!mAchievementModel.isUnlocked()) {
            tvUnlockDate.setVisibility(View.GONE);
        }

        tvUnlockDate.setText(unlockDate);
        tvRarity.setText(getResources().getString(R.string.achievement_rarity,
                getRarityString(mAchievementModel.getRatity()), mAchievementModel.getPercent()));

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

        dialog.setContentView(rootView);
    }

    private String getRarityString(int rarity) {
        switch (rarity) {
            case AchievementModel.RARITY_COMMON:
                return getResources().getString(R.string.rarity_common);
            case AchievementModel.RARITY_RARE:
                return getResources().getString(R.string.rarity_rare);
            case AchievementModel.RARITY_EPIC:
                return getResources().getString(R.string.rarity_epic);
            case AchievementModel.RARITY_LEGENDARY:
                return getResources().getString(R.string.rarity_legendary);
            default:
                return getResources().getString(R.string.empty);
        }
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
