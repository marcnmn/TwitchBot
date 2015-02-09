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
    private String mToken, mSig, mChannel;
    ChannelDetailFragment mCF2;

    public TwitchToken(ChannelDetailFragment cf) {
        mCF2 = cf;
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
        mCF2.fetchStreams(mChannel, mToken, mSig);
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
            int a = mToken.indexOf("channel", 15);
            int b = mToken.indexOf(",", 20);
            mChannel = mToken.substring(a+10, b-1);
            return result;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
