package io.github.skvoll.cybertrophy;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import io.github.skvoll.cybertrophy.data.ProfileModel;

public final class ProfileFragment extends Fragment {
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
        Toolbar toolbar = rootView.findViewById(R.id.tb_toolbar);

        GlideApp.with(getContext()).load(profileModel.getAvatarFull())
                .placeholder(R.drawable.profile_avatar_empty).into(ivProfileAvatar);
        GlideApp.with(getContext()).load(profileModel.getBackgroundImage())
                .placeholder(R.drawable.profile_background_empty).into(ivProfileBackground);
        toolbar.setTitle(profileModel.getName());

        /*rootView.findViewById(R.id.btn_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null) {
                    return;
                }

                Intent intent = new Intent(getContext(), SettingsActivity.class);
                getActivity().startActivity(intent);
            }
        });*/

        return rootView;
    }
}
