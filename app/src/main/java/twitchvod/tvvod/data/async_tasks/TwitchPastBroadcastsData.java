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

import twitchvod.tvvod.adapter.PastBroadcastsListAdapter;
import twitchvod.tvvod.data.TwitchJSONParser;
import twitchvod.tvvod.data.primitives.PastBroadcast;

public class TwitchPastBroadcastsData extends AsyncTask<String, PastBroadcast, ArrayList<PastBroadcast>> {
    private ArrayList<PastBroadcast> mBroadcasts;
    private PastBroadcastsListAdapter mAdapter;
    private int offset;

    public TwitchPastBroadcastsData(PastBroadcastsListAdapter c) {
        offset = c.getChannels().size();
        mBroadcasts = new ArrayList<>();
        mAdapter = c;
    }

    @Override
    protected ArrayList<PastBroadcast> doInBackground(String... urls) {
        try {
            downloadChannelData(urls[0]);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return mBroadcasts;
    }

    @Override
    protected void onPostExecute(ArrayList<PastBroadcast> result) {
        mAdapter.loadThumbnails(offset, offset + mBroadcasts.size());
    }

    @Override
    protected void onProgressUpdate(PastBroadcast... progress) {
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
            TwitchJSONParser.gameJSONtoArrayList(result);
            return result;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private void parseChannelJSON(String r) throws JSONException {
        String title, description, recorded_at, preview, status, game, id, length, views, broadcast_type, name, display_name;

        JSONObject jObject;

        jObject = new JSONObject(r);
        JSONArray jArray = jObject.getJSONArray("videos");
        JSONObject video, channel;

        for (int i=0; i<jArray.length(); i++) {
            video = jArray.getJSONObject(i);

            title = video.getString("title");
            description = video.getString("description");
            status = video.getString("status");
            id = video.getString("_id");
            recorded_at = video.getString("recorded_at");
            game = video.getString("game");
            length = video.getString("length");
            preview = video.getString("preview");
            views = video.getString("views");
            broadcast_type = video.getString("broadcast_type");

            channel = jArray.getJSONObject(i).getJSONObject("channel");
            name = channel.getString("name");
            display_name = channel.getString("display_name");

            PastBroadcast temp = new PastBroadcast(title, description, status, id, recorded_at, game,
                    length, preview, views, broadcast_type, name, display_name);
            mBroadcasts.add(temp);
            publishProgress(temp);
        }
    }
}
