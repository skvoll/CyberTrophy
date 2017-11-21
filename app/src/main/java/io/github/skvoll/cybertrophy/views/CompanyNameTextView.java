package io.github.skvoll.cybertrophy.views;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import io.github.skvoll.cybertrophy.R;

public final class CompanyNameTextView extends AppCompatTextView {
    public CompanyNameTextView(Context context) {
        super(context);

        setup(context);
    }

    public CompanyNameTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setup(context);
    }

    public CompanyNameTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setup(context);
    }

    private void setup(Context context) {
        setText(R.string.company_name);
        setTypeface(ResourcesCompat.getFont(context, R.font.roboto_mono));
    }
}
