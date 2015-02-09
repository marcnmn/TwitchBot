package twitchvod.tvvod.data.async_tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import twitchvod.tvvod.ui_fragments.ChannelDetailFragment;

public class TwitchLiveStream extends AsyncTask<String, Void, Void> {
    HashMap<String, String> hmap= new HashMap<>();
    ChannelDetailFragment mChannelDetailFragment;

    public TwitchLiveStream(ChannelDetailFragment cf) {
        mChannelDetailFragment = cf;
    }

    @Override
    protected Void doInBackground(String... urls) {
        try {
            fetchLivePlaylist(urls[0]);
        } catch (IOException | JSONException ignored) {
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        mChannelDetailFragment.updateStreamData(hmap);
        Log.v("twitch stream url", hmap.get("low"));
    }

    private String fetchLivePlaylist(String myurl) throws IOException, JSONException {
        InputStream is = null;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();
            is = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);

            String q, u;
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (line.contains("http")) {
                    u = line;
                    q = getQuality(line);
                    hmap.put(q, u);
                }
            }
            return null;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private String getQuality(String s) {
        String q = "none";
        if (s.contains("chunked")) return "source";
        if (s.contains("high")) return "high";
        if (s.contains("medium")) return "medium";
        if (s.contains("low")) return "low";
        if (s.contains("mobile")) return "mobile";
        if (s.contains("audio_only")) return "audio_only";
        return q;
    }
}
