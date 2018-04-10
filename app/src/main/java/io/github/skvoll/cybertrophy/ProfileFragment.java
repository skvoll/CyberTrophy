package io.github.skvoll.cybertrophy;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import io.github.skvoll.cybertrophy.data.ProfileModel;

import static java.lang.Math.abs;

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

        AppBarLayout appBar = rootView.findViewById(R.id.ab_appbar);
        final ConstraintLayout clProfileBackgroundWrapper = rootView.findViewById(R.id.cl_profile_background_wrapper);
        ImageView ivProfileAvatar = rootView.findViewById(R.id.iv_profile_avatar);
        ImageView ivProfileBackground = rootView.findViewById(R.id.iv_profile_background);
        final Toolbar toolbar = rootView.findViewById(R.id.tb_toolbar);

        appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float max = appBarLayout.getHeight() - toolbar.getHeight();
                float current = abs(verticalOffset);

                clProfileBackgroundWrapper.setAlpha(1f - current / max);
            }
        });

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
