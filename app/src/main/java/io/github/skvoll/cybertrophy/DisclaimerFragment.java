package io.github.skvoll.cybertrophy;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DisclaimerFragment extends BottomSheetDialogFragment {
    private static final String TAG = DisclaimerFragment.class.getSimpleName();

    public DisclaimerFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_disclaimer, container, false);
    }
}
