package io.github.skvoll.cybertrophy;

import android.content.Intent;
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

        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        ImageView ivProfileAvatar = rootView.findViewById(R.id.iv_profile_avatar);
        ImageView ivProfileBackground = rootView.findViewById(R.id.iv_profile_background);
        TextView tvProfileName = rootView.findViewById(R.id.tv_profile_name);

        GlideApp.with(getContext()).load(profileModel.getAvatarFull())
                .fitCenter()
                .placeholder(R.drawable.profile_avatar_empty).into(ivProfileAvatar);
        GlideApp.with(getContext()).load(profileModel.getBackgroundImage())
                .centerCrop()
                .placeholder(R.color.primary).into(ivProfileBackground);
        tvProfileName.setText(profileModel.getName());

        rootView.findViewById(R.id.btn_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null) {
                    return;
                }

                Intent intent = new Intent(getContext(), SettingsActivity.class);
                getActivity().startActivity(intent);
            }
        });

        return rootView;
    }
}
