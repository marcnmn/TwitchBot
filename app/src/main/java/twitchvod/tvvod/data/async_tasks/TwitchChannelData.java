package twitchvod.tvvod.data.async_tasks;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import twitchvod.tvvod.adapter.ChannelListAdapter;
import twitchvod.tvvod.data.TwitchJSONParser;
import twitchvod.tvvod.data.primitives.Channel;
import twitchvod.tvvod.data.primitives.Stream;

public class TwitchChannelData extends AsyncTask<String, Void, String> {
    private ChannelListAdapter mChannelListAdapter;
    private int offset;

    public TwitchChannelData(ChannelListAdapter c) {
        offset = c.getChannels().size();
        mChannelListAdapter = c;
    }

    @Override
    protected String doInBackground(String... urls) {
        String result = "";
        try {
            result = downloadChannelData(urls[0]);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        mChannelListAdapter.updateChannelList(result);
        //mChannelListAdapter.loadThumbnails(offset, offset + result.size());
    }

    @Override
    protected void onProgressUpdate(Void... progress) {
    }

    private String downloadChannelData(String myurl) throws IOException, JSONException {
        InputStream is = null;
        String s;
        ArrayList<Channel> result;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            is = conn.getInputStream();

            // Convert the InputStream into a string
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line).append("\n");
            }
            s = sb.toString();
            return s;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
