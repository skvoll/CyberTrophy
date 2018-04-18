package io.github.skvoll.cybertrophy;

import android.content.Context;
import android.os.AsyncTask;

import io.github.skvoll.cybertrophy.data.GamesParser;
import io.github.skvoll.cybertrophy.data.ProfileModel;

public abstract class GamesParserAsyncTask extends AsyncTask<GamesParser.Action, Void, Boolean> {
    private GamesParser mGamesParser;

    public GamesParserAsyncTask(Context context, ProfileModel profileModel) {
        mGamesParser = new GamesParser(context, profileModel);
    }

    public void cancel() {
        if (mGamesParser != null) {
            mGamesParser.cancel();
        }

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
