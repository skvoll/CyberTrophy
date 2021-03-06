package app.cybertrophy.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import java.util.ArrayList;

import app.cybertrophy.GameActivity;
import app.cybertrophy.MainActivity;
import app.cybertrophy.R;
import app.cybertrophy.data.GameModel;
import app.cybertrophy.data.NotificationModel;

public final class GameCompleteNotification extends BaseNotification {
    public static final int ID = 3022;

    private final Context mContext;
    private final ArrayList<String> mGames = new ArrayList<>();

    public GameCompleteNotification(Context context) {
        super(context);

        mContext = context;

        getBuilder().setContentTitle(getResources().getQuantityString(R.plurals.notification_games_complete, 1))
                .setContentText(getResources().getString(R.string.empty));
    }

    public GameCompleteNotification addGame(GameModel gameModel) {
        NotificationModel.gameComplete(gameModel).save(mContext.getContentResolver());

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
                R.plurals.notification_games_complete, mGames.size(), mGames.size()));

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
