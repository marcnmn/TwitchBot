package twitchvod.tvvod.data.async_tasks;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import twitchvod.tvvod.ui_fragments.ChannelDetailFragment;

public class TwitchToken extends AsyncTask<String, Void, Void> {
    private String mToken, mSig, mBroadcastId;
    ChannelDetailFragment mCF2;
    private int mTokenType;

    public TwitchToken(ChannelDetailFragment cf) {
        mCF2 = cf;
        mTokenType = 0;
    }

    public TwitchToken(ChannelDetailFragment cf, String id) {
        mCF2 = cf;
        mBroadcastId = id;
        mTokenType = 1;
    }

    @Override
    protected Void doInBackground(String... urls) {
        try {
            fetchLiveToken(urls[0]);
        } catch (IOException | JSONException ignored) {
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (mTokenType == 0) {
            mCF2.fetchStreamM3U8Playlists(mToken, mSig);
        }
        if (mTokenType == 1) {
            mCF2.fetchBroadcastM3U8PlaylistsNew(mToken, mSig, mBroadcastId);
        }
    }

    private String fetchLiveToken(String myurl) throws IOException, JSONException {
        InputStream is = null;
        String result;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();
            is = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line).append("\n");
            }
            result = sb.toString();

            JSONObject jObject = new JSONObject(result);

            mToken = jObject.getString("token");
            mSig = jObject.getString("sig");
            return result;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
