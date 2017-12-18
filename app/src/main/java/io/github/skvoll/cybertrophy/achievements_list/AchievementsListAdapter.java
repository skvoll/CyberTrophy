package io.github.skvoll.cybertrophy.achievements_list;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.github.skvoll.cybertrophy.GlideApp;
import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.AchievementModel;

final class AchievementsListAdapter extends CursorAdapter {
    private static final String TAG = AchievementsListAdapter.class.getSimpleName();

    AchievementsListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
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

        String icon = achievementModel.isUnlocked() ?
                achievementModel.getIconUrl() : achievementModel.getIconGrayUrl();

        String description = achievementModel.getDescription() != null
                ? achievementModel.getDescription()
                : mContext.getResources().getString(R.string.empty_achievement_description);

        GlideApp.with(context).load(icon)
                .placeholder(R.drawable.no_achievement_icon)
                .into(viewHolder.icon);

        if (achievementModel.isUnlocked() || !achievementModel.isHidden()) {
            viewHolder.name.setText(achievementModel.getName());
            viewHolder.description.setText(description);
        } else {
            viewHolder.name.setText("Hidden");
            viewHolder.description.setText(mContext.getResources().getString(R.string.empty));
        }

        if (achievementModel.isUnlocked()) {
            viewHolder.info.setText(
                    DateUtils.getRelativeTimeSpanString(achievementModel.getUnlockTime() * 1000L));
        } else {
            viewHolder.info.setText(achievementModel.getPercent() + "%");
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        TextView description;
        TextView info;

        ViewHolder(View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.iv_icon);
            name = itemView.findViewById(R.id.tv_name);
            description = itemView.findViewById(R.id.tv_description);
            info = itemView.findViewById(R.id.tv_info);
        }
    }
}
