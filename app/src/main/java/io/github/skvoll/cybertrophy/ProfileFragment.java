package io.github.skvoll.cybertrophy;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.github.skvoll.cybertrophy.data.ProfileModel;

public class ProfileFragment extends Fragment {
    private static final String TAG = ProfileFragment.class.getSimpleName();

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getContext() == null) {
            return null;
        }

        ProfileModel profileModel = ProfileModel.getActive(getContext().getContentResolver());

        if (profileModel == null) {
            return null;
        }

        View mRootView = inflater.inflate(R.layout.fragment_profile, container, false);

        ImageView ivProfileAvatar = mRootView.findViewById(R.id.iv_profile_avatar);
        ImageView ivProfileBackground = mRootView.findViewById(R.id.iv_profile_background);
        TextView tvProfileName = mRootView.findViewById(R.id.tv_profile_name);

        GlideApp.with(getContext()).load(profileModel.getAvatarFull())
                .fitCenter()
                .placeholder(R.drawable.profile_avatar_empty).into(ivProfileAvatar);
        GlideApp.with(getContext()).load(profileModel.getBackgroundImage())
                .centerCrop()
                .placeholder(R.color.primary).into(ivProfileBackground);
        tvProfileName.setText(profileModel.getName());

        return mRootView;
    }
}
