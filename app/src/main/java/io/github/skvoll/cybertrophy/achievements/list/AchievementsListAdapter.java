package io.github.skvoll.cybertrophy.achievements.list;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.GlideApp;
import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.AchievementModel;

public final class AchievementsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_FULL = 1;
    public static final int TYPE_SMALL = 2;

    private Context mContext;
    private ArrayList<AchievementModel> mItems;
    private int mType;
    private AchievementsListFragment.OnItemClickListener mOnItemClickListener;
    private int mMaxWidth;

    public AchievementsListAdapter(Context context, ArrayList<AchievementModel> achievementModels,
                                   AchievementsListFragment.OnItemClickListener onItemClickListener, int type) {
        mContext = context;
        mItems = achievementModels;
        mType = type;
        mOnItemClickListener = onItemClickListener;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        mMaxWidth = displayMetrics.widthPixels;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (mType) {
            case TYPE_FULL:
                return new AchievementFullViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_achievements_list_item_full, parent, false));
            case TYPE_SMALL:
                return new AchievementSmallViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_achievements_list_item_small, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final AchievementModel achievementModel = mItems.get(position);

        switch (mType) {
            case TYPE_FULL:
                AchievementFullViewHolder achievementFullViewHolder
                        = (AchievementFullViewHolder) viewHolder;

                String description = achievementModel.getDescription() != null
                        ? achievementModel.getDescription()
                        : mContext.getResources().getString(R.string.empty_achievement_description);

                if (achievementModel.isUnlocked()) {
                    GlideApp.with(mContext).load(achievementModel.getIconUrl())
                            .placeholder(R.drawable.achievement_icon_empty)
                            .into(achievementFullViewHolder.ivIcon);

                    achievementFullViewHolder.ivIconMask.setVisibility(View.GONE);

                    achievementFullViewHolder.tvName.setText(achievementModel.getName());
                    achievementFullViewHolder.tvDescription.setText(description);
                } else {
                    GlideApp.with(mContext).load(achievementModel.getIconGrayUrl())
                            .placeholder(R.drawable.achievement_icon_empty)
                            .into(achievementFullViewHolder.ivIcon);

                    if (achievementModel.isHidden()) {
                        achievementFullViewHolder.ivIconMask.setVisibility(View.VISIBLE);

                        achievementFullViewHolder.tvName.setText(R.string.achievement_title_hidden);
                        achievementFullViewHolder.tvDescription.setText(mContext.getResources().getString(R.string.empty));
                    } else {
                        achievementFullViewHolder.ivIconMask.setVisibility(View.GONE);

                        achievementFullViewHolder.tvName.setText(achievementModel.getName());
                        achievementFullViewHolder.tvDescription.setText(description);
                    }
                }

                if (achievementModel.isUnlocked()) {
                    achievementFullViewHolder.tvInfo.setText(DateUtils.getRelativeTimeSpanString(
                            achievementModel.getUnlockTime() * 1000L));
                    achievementFullViewHolder.vProgress.getLayoutParams().width = 0;
                } else {
                    achievementFullViewHolder.tvInfo.setText(String.format("%s%%", achievementModel.getPercent()));
                    achievementFullViewHolder.vProgress.getLayoutParams().width
                            = Math.round(mMaxWidth * (achievementModel.getPercent().floatValue() / 100f));
                }

                achievementFullViewHolder.vContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnItemClickListener.onClick(achievementModel);
                    }
                });
                break;
            case TYPE_SMALL:
                AchievementSmallViewHolder achievementSmallViewHolder
                        = (AchievementSmallViewHolder) viewHolder;

                if (achievementModel.isUnlocked()) {
                    GlideApp.with(mContext).load(achievementModel.getIconUrl())
                            .placeholder(R.drawable.achievement_icon_empty)
                            .into(achievementSmallViewHolder.ivIcon);
                } else {
                    if (achievementModel.isHidden()) {
                        achievementSmallViewHolder.ivIconMask.setVisibility(View.VISIBLE);
                    }

                    GlideApp.with(mContext).load(achievementModel.getIconGrayUrl())
                            .placeholder(R.drawable.achievement_icon_empty)
                            .into(achievementSmallViewHolder.ivIcon);
                }

                achievementSmallViewHolder.ivIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnItemClickListener.onClick(achievementModel);
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private static final class AchievementFullViewHolder extends RecyclerView.ViewHolder {
        View vContainer;
        View vProgress;
        ImageView ivIcon;
        ImageView ivIconMask;
        TextView tvName;
        TextView tvDescription;
        TextView tvInfo;

        AchievementFullViewHolder(View itemView) {
            super(itemView);

            vContainer = itemView;

            vProgress = itemView.findViewById(R.id.v_progress);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            ivIconMask = itemView.findViewById(R.id.iv_icon_mask);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvInfo = itemView.findViewById(R.id.tv_info);
        }
    }

    private static final class AchievementSmallViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        ImageView ivIconMask;

        AchievementSmallViewHolder(View itemView) {
            super(itemView);

            ivIcon = itemView.findViewById(R.id.iv_icon);
            ivIconMask = itemView.findViewById(R.id.iv_icon_mask);
        }
    }
}
