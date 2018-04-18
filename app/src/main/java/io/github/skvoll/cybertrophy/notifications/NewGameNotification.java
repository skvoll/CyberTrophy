package io.github.skvoll.cybertrophy.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import java.util.ArrayList;

import io.github.skvoll.cybertrophy.GameActivity;
import io.github.skvoll.cybertrophy.MainActivity;
import io.github.skvoll.cybertrophy.R;
import io.github.skvoll.cybertrophy.data.GameModel;
import io.github.skvoll.cybertrophy.data.NotificationModel;

public final class NewGameNotification extends BaseNotification {
    public static final int ID = 3001;

    private Context mContext;
    private ArrayList<String> mGames = new ArrayList<>();

    public NewGameNotification(Context context) {
        super(context);

        mContext = context;

        getBuilder().setContentTitle(getResources().getQuantityString(R.plurals.notification_new_games_in_library, 1))
                .setContentText(getResources().getString(R.string.empty));
    }

    public NewGameNotification addGame(GameModel gameModel) {
        NotificationModel.newGame(gameModel).save(mContext.getContentResolver());

        mGames.add(gameModel.getName());

        Intent intent;
        PendingIntent pendingIntent;

        if (mGames.size() > 1) {
            intent = new Intent(mContext, MainActivity.class);
            intent.putExtra(MainActivity.KEY_FRAGMENT, MainActivity.FRAGMENT_GAMES);
            pendingIntent = PendingIntent.getActivity(
                    mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            intent = new Intent(mContext, GameActivity.class);
            intent.putExtra(GameActivity.KEY_GAME_ID, gameModel.getId());
            pendingIntent = PendingIntent.getActivity(
                    mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        getBuilder().setContentIntent(pendingIntent);

        getBuilder().setContentTitle(getResources().getQuantityString(
                R.plurals.notification_new_games_in_library, mGames.size(), mGames.size()));

        String contentText = TextUtils.join("\n", mGames);
        getBuilder().setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
        getBuilder().setContentText(contentText);

        return this;
    }

    @Override
    public int getId() {
        return ID;
    }
}
