package io.github.skvoll.cybertrophy.achievements_list;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.BigDecimal;

import io.github.skvoll.cybertrophy.GlideApp;
import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.AchievementModel;

final class AchievementsListAdapter extends CursorAdapter {
    private static final String TAG = AchievementsListAdapter.class.getSimpleName();

    private final BigDecimal mOneHundred = new BigDecimal("100");
    private int mMaxWidth;

    AchievementsListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        mMaxWidth = displayMetrics.widthPixels;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_achievements_list_item, parent, false);

        view.setTag(new ViewHolder(view));

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        AchievementModel achievementModel = new AchievementModel(cursor);

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String description = achievementModel.getDescription() != null
                ? achievementModel.getDescription()
                : mContext.getResources().getString(R.string.empty_achievement_description);

        if (achievementModel.isUnlocked() || !achievementModel.isHidden()) {
            String icon = achievementModel.isUnlocked() ?
                    achievementModel.getIconUrl() : achievementModel.getIconGrayUrl();

            GlideApp.with(context).load(icon)
                    .placeholder(R.drawable.achievement_icon_empty)
                    .into(viewHolder.icon);
            viewHolder.name.setText(achievementModel.getName());
            viewHolder.description.setText(description);
        } else {
            GlideApp.with(context).load(R.drawable.achievement_icon_hidden)
                    .placeholder(R.drawable.achievement_icon_empty)
                    .into(viewHolder.icon);
            viewHolder.name.setText(R.string.achievement_title_hidden);
            viewHolder.description.setText(mContext.getResources().getString(R.string.empty));
        }

        if (achievementModel.isUnlocked()) {
            viewHolder.info.setText(DateUtils.getRelativeTimeSpanString(
                    achievementModel.getUnlockTime() * 1000L));
            viewHolder.progress.getLayoutParams().width = 0;
        } else {
            viewHolder.info.setText(String.format("%s%%", achievementModel.getPercent()));

            viewHolder.progress.getLayoutParams().width = Math.round(mMaxWidth * (achievementModel.getPercent().floatValue() / 100f));
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        View progress;
        ImageView icon;
        TextView name;
        TextView description;
        TextView info;

        ViewHolder(View itemView) {
            super(itemView);

            progress = itemView.findViewById(R.id.v_progress);
            icon = itemView.findViewById(R.id.iv_icon);
            name = itemView.findViewById(R.id.tv_name);
            description = itemView.findViewById(R.id.tv_description);
            info = itemView.findViewById(R.id.tv_info);
        }
    }
}
