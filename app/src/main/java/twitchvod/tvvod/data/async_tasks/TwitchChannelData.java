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
import twitchvod.tvvod.data.primitives.Channel;

public class TwitchChannelData extends AsyncTask<String, Channel, ArrayList<Channel>> {
    private ArrayList<Channel> mChannels;
    private ChannelListAdapter mAdapter;
    private int offset;

    public TwitchChannelData(ChannelListAdapter c) {
        offset = c.getChannels().size();
        mChannels = new ArrayList<>();
        mAdapter = c;

    }

    @Override
    protected ArrayList<Channel> doInBackground(String... urls) {
        try {
            downloadChannelData(urls[0]);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return mChannels;
    }

    @Override
    protected void onPostExecute(ArrayList<Channel> result) {
        mAdapter.loadThumbnails(offset, offset + mChannels.size());
    }

    @Override
    protected void onProgressUpdate(Channel... progress) {
        mAdapter.update(progress[0]);
    }

    private String downloadChannelData(String myurl) throws IOException, JSONException {
        InputStream is = null;
        String result;
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
            result = sb.toString();
            parseChannelJSON(result);
            return result;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private void parseChannelJSON(String r) throws JSONException {
        String title, logo, preview, curl, stat, game;
        int viewers, id;

        JSONObject jObject;

        jObject = new JSONObject(r);
        JSONArray jArray = jObject.getJSONArray("streams");
        JSONObject channel;

        for (int i=0; i<jArray.length(); i++) {
            id = jArray.getJSONObject(i).getInt("_id");
            game = jArray.getJSONObject(i).getString("game");
            viewers = jArray.getJSONObject(i).getInt("viewers");
            curl = jArray.getJSONObject(i).getJSONObject("_links").getString("self");

            preview = jArray.getJSONObject(i).getJSONObject("preview").getString("medium");

            channel = jArray.getJSONObject(i).getJSONObject("channel");
            title = channel.getString("display_name");
            try {
                stat = channel.getString("status");
            } catch (JSONException e) {
                stat = "";
            }
            logo = channel.getString("logo");

            Channel temp = new Channel(title, curl, stat, game, viewers, logo, preview, id);
            mChannels.add(temp);
            publishProgress(temp);
        }
    }
}
