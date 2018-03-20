package io.github.skvoll.cybertrophy.notifications.list;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.github.skvoll.cybertrophy.GlideApp;
import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.NotificationModel;

public final class NotificationsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<NotificationModel> mItems;
    private OnItemClickListener mOnItemClickListener;
    private OnItemRenderListener mOnItemRenderListener;

    NotificationsListAdapter(Context context, ArrayList<NotificationModel> notificationModels,
                             OnItemClickListener onItemClickListener,
                             OnItemRenderListener onItemRenderListener) {
        mContext = context;
        mItems = notificationModels;
        mOnItemClickListener = onItemClickListener;
        mOnItemRenderListener = onItemRenderListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case NotificationModel.TYPE_CATEGORY_SEPARATOR:
                return new SeparatorViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_notifications_list_separator, parent, false));
            default:
                return new NotificationViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_notifications_list_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final NotificationModel notificationModel = mItems.get(position);
        Date date = new Date();
        date.setTime(notificationModel.getTime() * 1000L);
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(
                DateFormat.MEDIUM, DateFormat.SHORT, Resources.getSystem().getConfiguration().locale);

        NotificationViewHolder notificationViewHolder = null;

        switch (notificationModel.getType()) {
            case NotificationModel.TYPE_CATEGORY_SEPARATOR:
                SeparatorViewHolder separatorViewHolder = (SeparatorViewHolder) viewHolder;
                separatorViewHolder.tvTitle.setText(notificationModel.getTitle());
                return;
            case NotificationModel.TYPE_DEBUG:
                notificationViewHolder = (NotificationViewHolder) viewHolder;

                if (notificationModel.isViewed()) {
                    notificationViewHolder.cvItem.setAlpha(0.75f);
                    notificationViewHolder.ivNewIndicator.setVisibility(View.GONE);
                } else {
                    notificationViewHolder.cvItem.setAlpha(1);
                    notificationViewHolder.ivNewIndicator.setVisibility(View.VISIBLE);
                }

                notificationViewHolder.ivImage.setVisibility(View.GONE);
                notificationViewHolder.ivIcon.setVisibility(View.VISIBLE);

                notificationViewHolder.ivIcon.setImageDrawable(
                        mContext.getResources().getDrawable(R.drawable.ic_notifications_black_24dp));

                notificationViewHolder.ivIcon.getDrawable().setTint(
                        mContext.getResources().getColor(R.color.secondaryColor));
                notificationViewHolder.tvTitle.setText(notificationModel.getTitle());
                notificationViewHolder.tvMessage.setText(notificationModel.getMessage());
                notificationViewHolder.tvTime.setText(dateFormat.format(date));
                break;
            case NotificationModel.TYPE_NEW_GAME:
            case NotificationModel.TYPE_GAME_REMOVED:
            case NotificationModel.TYPE_NEW_ACHIEVEMENT:
            case NotificationModel.TYPE_ACHIEVEMENT_REMOVED:
            case NotificationModel.TYPE_GAME_COMPLETE:
                notificationViewHolder = (NotificationViewHolder) viewHolder;

                if (notificationModel.isViewed()) {
                    notificationViewHolder.cvItem.setAlpha(0.75f);
                    notificationViewHolder.ivNewIndicator.setVisibility(View.GONE);
                } else {
                    notificationViewHolder.cvItem.setAlpha(1);
                    notificationViewHolder.ivNewIndicator.setVisibility(View.VISIBLE);
                }

                notificationViewHolder.ivImage.setVisibility(View.GONE);
                notificationViewHolder.ivIcon.setVisibility(View.VISIBLE);

                GlideApp.with(mContext).load(notificationModel.getImageUrl())
                        .placeholder(R.drawable.ic_notifications_black_24dp)
                        .into(notificationViewHolder.ivIcon);

                notificationViewHolder.tvTitle.setText(notificationModel.getTitle());
                notificationViewHolder.tvMessage.setText(
                        getMessage(notificationModel.getType(), notificationModel.getObjectsCount()));
                notificationViewHolder.tvTime.setText(dateFormat.format(date));
                break;
            case NotificationModel.TYPE_ACHIEVEMENT_UNLOCKED:
                notificationViewHolder = (NotificationViewHolder) viewHolder;

                if (notificationModel.isViewed()) {
                    notificationViewHolder.cvItem.setAlpha(0.75f);
                    notificationViewHolder.ivNewIndicator.setVisibility(View.GONE);
                } else {
                    notificationViewHolder.cvItem.setAlpha(1);
                    notificationViewHolder.ivNewIndicator.setVisibility(View.VISIBLE);
                }

                notificationViewHolder.ivImage.setVisibility(View.VISIBLE);
                notificationViewHolder.ivIcon.setVisibility(View.GONE);

                GlideApp.with(mContext).load(notificationModel.getImageUrl())
                        .placeholder(R.drawable.achievement_icon_empty)
                        .into(notificationViewHolder.ivImage);

                notificationViewHolder.tvTitle.setText(notificationModel.getTitle());
                notificationViewHolder.tvMessage.setText(mContext.getResources()
                        .getQuantityText(R.plurals.notification_achievements_unlocked, 1));
                notificationViewHolder.tvTime.setText(dateFormat.format(date));
                break;
        }

        if (notificationViewHolder != null) {
            notificationViewHolder.cvItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onClick(notificationModel);
                }
            });
        }

        mOnItemRenderListener.onRender(notificationModel);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private String getMessage(int type, int quantity) {
        switch (type) {
            case NotificationModel.TYPE_NEW_GAME:
                return mContext.getResources()
                        .getQuantityString(R.plurals.notification_new_games_in_library, quantity);
            case NotificationModel.TYPE_GAME_REMOVED:
                return mContext.getResources()
                        .getQuantityString(R.plurals.notification_games_removed_from_library, quantity);
            case NotificationModel.TYPE_NEW_ACHIEVEMENT:
                return mContext.getResources()
                        .getQuantityString(R.plurals.notification_new_achievements_w_count, quantity, quantity);
            case NotificationModel.TYPE_ACHIEVEMENT_REMOVED:
                return mContext.getResources().getQuantityString(
                        R.plurals.notification_achievements_removed_w_count, quantity, quantity);
            case NotificationModel.TYPE_GAME_COMPLETE:
                return mContext.getResources()
                        .getQuantityString(R.plurals.notification_games_complete, quantity);
            default:
                return mContext.getResources().getString(R.string.empty);
        }
    }

    public interface OnItemClickListener {
        void onClick(NotificationModel notificationModel);
    }

    public interface OnItemRenderListener {
        void onRender(NotificationModel notificationModel);
    }

    private static final class SeparatorViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        SeparatorViewHolder(View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tv_title);
        }
    }

    private static final class NotificationViewHolder extends RecyclerView.ViewHolder {
        CardView cvItem;
        ImageView ivImage;
        ImageView ivIcon;
        ImageView ivNewIndicator;
        TextView tvTitle;
        TextView tvMessage;
        TextView tvTime;

        NotificationViewHolder(View itemView) {
            super(itemView);

            cvItem = (CardView) itemView;

            ivImage = itemView.findViewById(R.id.iv_image);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            ivNewIndicator = itemView.findViewById(R.id.iv_new_indicator);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
