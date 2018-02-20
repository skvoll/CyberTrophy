package io.github.skvoll.cybertrophy.notifications.list;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.NotificationModel;

public final class NotificationsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<NotificationModel> mItems;

    public NotificationsListAdapter(Context context, ArrayList<NotificationModel> notificationModels) {
        mContext = context;
        mItems = notificationModels;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new LogViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_notifications_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final NotificationModel notificationModel = mItems.get(position);

        LogViewHolder logViewHolder = (LogViewHolder) viewHolder;

        logViewHolder.tvTitle.setText(notificationModel.getTitle());
        logViewHolder.tvMessage.setText(notificationModel.getMessage());
        logViewHolder.tvTime.setText(notificationModel.getProfileId() + "");
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private static final class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvMessage;
        TextView tvTime;

        LogViewHolder(View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tv_title);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
