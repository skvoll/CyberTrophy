package app.cybertrophy;

import android.content.Context;
import android.os.AsyncTask;

import app.cybertrophy.data.GamesParser;
import app.cybertrophy.data.ProfileModel;

public abstract class GamesParserAsyncTask extends AsyncTask<GamesParser.Action, Void, Boolean> {
    private final GamesParser mGamesParser;

    public GamesParserAsyncTask(Context context, ProfileModel profileModel) {
        mGamesParser = new GamesParser(context, profileModel);
    }

    public void cancel() {
        mGamesParser.cancel();

        cancel(true);
    }

    protected void setProgressListener(GamesParser.GamesParserProgressListener progressListener) {
        mGamesParser.setProgressListener(progressListener);
    }

    @Override
    protected Boolean doInBackground(GamesParser.Action... actions) {
        GamesParser.Action action = actions[0];

        if (action == null) {
            throw new IllegalArgumentException("Action is missing.");
        }

        try {
            mGamesParser.run(action);
        } catch (GamesParser.GamesParserException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }
}
