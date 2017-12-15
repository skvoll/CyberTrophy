package io.github.skvoll.cybertrophy;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.github.skvoll.cybertrophy.data.AchievementModel;

public class AchievementFragment extends Fragment {
    private static final String KEY_ID = "ID";

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

        if (getContext() == null) {
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
        mAchievementModel = AchievementModel.getById(getContext().getContentResolver(), id);

        if (mAchievementModel == null) {
            throw new IllegalArgumentException("Achievement id is missing");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_achievement, container, false);

        ImageView ivIcon = mRootView.findViewById(R.id.iv_icon);
        TextView tvName = mRootView.findViewById(R.id.tv_name);
        TextView tvDescription = mRootView.findViewById(R.id.tv_description);

        String icon = mAchievementModel.isUnlocked() ?
                mAchievementModel.getIconUrl() : mAchievementModel.getIconGrayUrl();

        GlideApp.with(getContext()).load(icon)
                .placeholder(R.drawable.no_achievement_icon)
                .into(ivIcon);

        tvName.setText(mAchievementModel.getName());
        if (mAchievementModel.getDescription() != null) {
            tvDescription.setText(mAchievementModel.getDescription());
        }

        return mRootView;
    }
}
