package io.github.skvoll.cybertrophy;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.skvoll.cybertrophy.data.AchievementModel;

public class AchievementFragment extends Fragment {
    private static final String KEY_ID = "ID";

    private Context mContext;
    private AchievementModel mAchievementModel;

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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_achievement, container, false);

        final ImageView ivIcon = mRootView.findViewById(R.id.iv_icon);
        final TextView tvName = mRootView.findViewById(R.id.tv_name);
        final Button btnShowInfo = mRootView.findViewById(R.id.btn_show_info);

        final LinearLayout mAchievementInfo = mRootView.findViewById(R.id.ll_achievement_info);
        final TextView tvDescription = mRootView.findViewById(R.id.tv_description);

        final String description = mAchievementModel.getDescription() != null
                ? mAchievementModel.getDescription()
                : mContext.getResources().getString(R.string.empty_achievement_description);

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

            mAchievementInfo.setVisibility(View.GONE);
            btnShowInfo.setVisibility(View.VISIBLE);

            btnShowInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GlideApp.with(mContext).load(mAchievementModel.getIconGrayUrl())
                            .placeholder(R.drawable.achievement_icon_empty)
                            .into(ivIcon);
                    tvName.setText(mAchievementModel.getName());
                    tvDescription.setText(description);

                    mAchievementInfo.setVisibility(View.VISIBLE);
                    btnShowInfo.setVisibility(View.GONE);
                }
            });
        }

        return mRootView;
    }
}
