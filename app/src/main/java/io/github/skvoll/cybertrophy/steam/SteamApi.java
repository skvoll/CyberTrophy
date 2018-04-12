package io.github.skvoll.cybertrophy.steam;

import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;

import io.github.skvoll.cybertrophy.VolleySingleton;

public final class SteamApi {
    public static final Long REQUEST_TIMEOUT = 60000L;
    private static final String TAG = SteamApi.class.getSimpleName();
    private static final String HOST = "http://api.steampowered.com";
    private static final String KEY = "09B2CD771C3E7F9B99D91E12EF384B21";
    private VolleySingleton mVolleySingleton;

    public SteamApi(VolleySingleton volleySingleton) {
        mVolleySingleton = volleySingleton;
    }

    public static String getAuthUrl(String realm) {
        return String.format("https://steamcommunity.com/openid/login?" +
                "openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select&" +
                "openid.identity=http://specs.openid.net/auth/2.0/identifier_select&" +
                "openid.mode=checkid_setup&" +
                "openid.ns=http://specs.openid.net/auth/2.0&" +
                "openid.realm=https://%s&" +
                "openid.return_to=https://%s/signin/", realm, realm);
    }

    public SteamApiResponseListener<HashMap<String, BigDecimal>> getGlobalAchievementPercentagesForApp(
            final Long appId,
            final SteamApiResponseListener<HashMap<String, BigDecimal>> listener) {
        String uri = String.format("/ISteamUserStats/GetGlobalAchievementPercentagesForApp/v0002/?gameid=%s", appId);
        String url = makeUrl(uri);

        SteamApiRequest request = new SteamApiRequest<HashMap<String, BigDecimal>>(
                Request.Method.GET, url, null, listener) {
            @Override
            HashMap<String, BigDecimal> parseResponse(JSONObject response) {
                if (response.optJSONObject("achievementpercentages") == null
                        || response.optJSONObject("achievementpercentages").optJSONArray("achievements") == null) {
                    return new HashMap<>();
                }

                JSONArray items = response.optJSONObject("achievementpercentages").optJSONArray("achievements");

                HashMap<String, BigDecimal> result = new HashMap<>(items.length());

                for (int i = 0; i < items.length(); i++) {
                    result.put(items.optJSONObject(i).optString("name").toUpperCase(), BigDecimal.valueOf(items.optJSONObject(i).optDouble("percent")));
                }

                return result;
            }
        };

        addToRequestQueue(request);

        return listener;
    }

    public SteamApiResponseListener<LongSparseArray<SteamProfile>> getPlayerSummaries(
            final Long[] steamIds,
            final SteamApiResponseListener<LongSparseArray<SteamProfile>> listener) {
        String uri = String.format("/ISteamUser/GetPlayerSummaries/v0002/?steamids=%s", TextUtils.join(",", steamIds));
        String url = makeUrl(uri);

        SteamApiRequest request = new SteamApiRequest<LongSparseArray<SteamProfile>>(
                Request.Method.GET, url, null, listener) {
            @Override
            LongSparseArray<SteamProfile> parseResponse(JSONObject response) {
                if (response.optJSONObject("response") == null
                        || response.optJSONObject("response").optJSONArray("players") == null) {
                    return new LongSparseArray<>();
                }

                JSONArray items = response.optJSONObject("response").optJSONArray("players");

                LongSparseArray<SteamProfile> result = new LongSparseArray<>(items.length());

                for (int i = 0; i < items.length(); i++) {
                    SteamProfile steamProfile = (new Gson()).fromJson(items.optJSONObject(i).toString(), SteamProfile.class);

                    result.append(steamProfile.steamId, steamProfile);
                }

                return result;
            }
        };

        addToRequestQueue(request);

        return listener;
    }

    public SteamApiResponseListener<HashMap<String, SteamPlayerAchievement>> getPlayerAchievements(
            final Long steamId,
            final Long appId,
            final String language,
            final SteamApiResponseListener<HashMap<String, SteamPlayerAchievement>> listener) {
        String uri = String.format("/ISteamUserStats/GetPlayerAchievements/v0001/?steamid=%s&appid=%s&l=%s", steamId, appId, language);
        String url = makeUrl(uri);

        SteamApiRequest request = new SteamApiRequest<HashMap<String, SteamPlayerAchievement>>(
                Request.Method.GET, url, null, listener) {
            @Override
            HashMap<String, SteamPlayerAchievement> parseResponse(JSONObject response) {
                if (response.optJSONObject("playerstats") == null
                        || response.optJSONObject("playerstats").optJSONArray("achievements") == null) {
                    return new HashMap<>();
                }

                JSONArray items = response.optJSONObject("playerstats").optJSONArray("achievements");

                HashMap<String, SteamPlayerAchievement> result = new HashMap<>(items.length());

                for (int i = 0; i < items.length(); i++) {
                    SteamPlayerAchievement steamPlayerAchievement = (new Gson()).fromJson(items.optJSONObject(i).toString(), SteamPlayerAchievement.class);

                    steamPlayerAchievement.apiName = steamPlayerAchievement.apiName.toUpperCase();

                    result.put(steamPlayerAchievement.apiName, steamPlayerAchievement);
                }

                return result;
            }
        };

        addToRequestQueue(request);

        return listener;
    }

    public SteamApiResponseListener<LongSparseArray<SteamGame>> getOwnedGames(
            final Long steamId,
            final boolean includeAppInfo,
            final boolean includePlayedFreeGames,
            final Long[] appIdsFilter,
            final SteamApiResponseListener<LongSparseArray<SteamGame>> listener) {
        JSONObject inputJson = new JSONObject();

        try {
            inputJson.put("steamid", steamId);
            inputJson.put("include_appinfo", includeAppInfo ? 1 : 0);
            inputJson.put("include_played_free_games", includePlayedFreeGames ? 1 : 0);
            if (appIdsFilter != null) {
                inputJson.put("appids_filter", new JSONArray(appIdsFilter));
            }
        } catch (JSONException e) {
            listener.setResponse(new LongSparseArray<SteamGame>());

            return listener;
        }

        String uri = String.format("/IPlayerService/GetOwnedGames/v0001/?input_json=%s", inputJson);
        String url = makeUrl(uri);

        SteamApiRequest request = new SteamApiRequest<LongSparseArray<SteamGame>>(
                Request.Method.GET, url, null, listener) {
            @Override
            LongSparseArray<SteamGame> parseResponse(JSONObject response) {
                if (response.optJSONObject("response") == null
                        || response.optJSONObject("response").optJSONArray("games") == null) {
                    return new LongSparseArray<>();
                }

                JSONArray items = response.optJSONObject("response").optJSONArray("games");

                LongSparseArray<SteamGame> result = new LongSparseArray<>(items.length());

                for (int i = 0; i < items.length(); i++) {
                    SteamGame steamGame = (new Gson()).fromJson(items.optJSONObject(i).toString(), SteamGame.class);
                    steamGame.hasCommunityVisibleStats = steamGame.hasCommunityVisibleStats != null;

                    result.append(steamGame.appId, steamGame);
                }

                return result;
            }
        };

        addToRequestQueue(request);

        return listener;
    }

    public SteamApiResponseListener<LongSparseArray<SteamGame>> getRecentlyPlayedGames(
            final Long steamId,
            final int count,
            final SteamApiResponseListener<LongSparseArray<SteamGame>> listener) {
        JSONObject inputJson = new JSONObject();

        try {
            inputJson.put("steamid", steamId);
            inputJson.put("count", count);
        } catch (JSONException e) {
            listener.setResponse(new LongSparseArray<SteamGame>());

            return listener;
        }

        String uri = String.format("/IPlayerService/GetRecentlyPlayedGames/v0001/?input_json=%s", inputJson);
        String url = makeUrl(uri);

        SteamApiRequest request = new SteamApiRequest<LongSparseArray<SteamGame>>(
                Request.Method.GET, url, null, listener) {
            @Override
            LongSparseArray<SteamGame> parseResponse(JSONObject response) {
                if (response.optJSONObject("response") == null
                        || response.optJSONObject("response").optJSONArray("games") == null) {
                    return new LongSparseArray<>();
                }

                JSONArray items = response.optJSONObject("response").optJSONArray("games");

                LongSparseArray<SteamGame> result = new LongSparseArray<>(items.length());

                for (int i = 0; i < items.length(); i++) {
                    SteamGame steamGame = (new Gson()).fromJson(items.optJSONObject(i).toString(), SteamGame.class);
                    steamGame.hasCommunityVisibleStats = steamGame.hasCommunityVisibleStats != null;

                    result.append(steamGame.appId, steamGame);
                }

                return result;
            }
        };

        addToRequestQueue(request);

        return listener;
    }

    public SteamApiResponseListener<HashMap<String, SteamAchievement>> getSchemaForGame(
            final Long appId,
            final String language,
            final SteamApiResponseListener<HashMap<String, SteamAchievement>> listener) {
        String uri = String.format("/ISteamUserStats/GetSchemaForGame/v2/?appid=%s&l=%s", appId, language);
        String url = makeUrl(uri);

        SteamApiRequest request = new SteamApiRequest<HashMap<String, SteamAchievement>>(
                Request.Method.GET, url, null, listener) {
            @Override
            HashMap<String, SteamAchievement> parseResponse(JSONObject response) {
                if (response.optJSONObject("game") == null
                        || response.optJSONObject("game").optJSONObject("availableGameStats") == null
                        || response.optJSONObject("game").optJSONObject("availableGameStats").optJSONArray("achievements") == null) {
                    return new HashMap<>();
                }

                JSONArray items = response.optJSONObject("game").optJSONObject("availableGameStats").optJSONArray("achievements");

                HashMap<String, SteamAchievement> result = new HashMap<>(items.length());

                for (int i = 0; i < items.length(); i++) {
                    SteamAchievement steamAchievement = (new Gson()).fromJson(items.optJSONObject(i).toString(), SteamAchievement.class);

                    steamAchievement.name = steamAchievement.name.toUpperCase();

                    result.put(steamAchievement.name, steamAchievement);
                }

                return result;
            }
        };

        addToRequestQueue(request);

        return listener;
    }

    private String makeUrl(String uri) {
        return String.format("%s%s&format=json&key=%s&t=%s", HOST, uri, KEY, System.currentTimeMillis());
    }

    private void addToRequestQueue(SteamApiRequest request) {
        Log.d(TAG, "Requesting: " + request.getUrl());

        mVolleySingleton.addToRequestQueue(request);
    }

    private interface SteamApiResponseListener<T> {
        void setResponse(T response);

        void setError(VolleyError error);
    }

    public static abstract class ResponseListener<T> implements SteamApiResponseListener<T> {
        public final void setResponse(T response) {
            onSuccess(response);
        }

        public final void setError(VolleyError error) {
            onError(error);
        }

        public abstract void onSuccess(T response);

        public abstract void onError(VolleyError error);
    }

    public static class FutureResponseListener<T> implements SteamApiResponseListener<T> {
        private boolean mResponseReceived = false;
        private T mResponse;
        private VolleyError mVolleyError;

        @Override
        public synchronized void setResponse(T response) {
            mResponse = response;
            mResponseReceived = true;

            notify();
        }

        @Override
        public synchronized void setError(VolleyError error) {
            mVolleyError = error;

            notify();
        }

        public final synchronized T get(long timeout) throws InterruptedException, VolleyError {
            if (mVolleyError != null) {
                throw new Error(mVolleyError.toString());
            }

            if (mResponseReceived) {
                return mResponse;
            }

            if (timeout != 0) {
                wait(timeout);
            } else {
                wait(0);
            }

            if (mVolleyError != null) {
                throw mVolleyError;
            }

            if (!mResponseReceived) {
                throw new TimeoutError();
            }

            return mResponse;
        }
    }

    private static abstract class SteamApiRequest<T> extends Request<T> {
        private Response.Listener<T> mListener;
        private JSONObject mParams;

        SteamApiRequest(int method, String url, JSONObject params, final SteamApiResponseListener<T> listener) {
            super(method, url, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    listener.setError(error);
                }
            });

            mParams = params;
            mListener = new Response.Listener<T>() {
                @Override
                public void onResponse(T response) {
                    listener.setResponse(response);
                }
            };
        }

        @Override
        protected void deliverResponse(T response) {
            mListener.onResponse(response);
        }

        @Override
        protected Response<T> parseNetworkResponse(NetworkResponse response) {
            String json = new String(response.data);

            try {
                return Response.success(parseResponse(new JSONObject(json)),
                        HttpHeaderParser.parseCacheHeaders(response));
            } catch (JSONException e) {
                return Response.error(new ParseError(e));
            }
        }

        abstract T parseResponse(JSONObject response);
    }
}
