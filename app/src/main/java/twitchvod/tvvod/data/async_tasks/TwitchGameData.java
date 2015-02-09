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

import twitchvod.tvvod.adapter.GamesAdapter;
import twitchvod.tvvod.data.primitives.Game;


public class TwitchGameData extends AsyncTask<String, Game, ArrayList<Game>> {
    private ArrayList<Game> mGames;
    private GamesAdapter mAdapter;
    private int offset;

    public TwitchGameData(GamesAdapter c) {
        offset = c.getCount();
        mGames = new ArrayList<>();
        mAdapter = c;
    }

    @Override
    protected ArrayList<Game> doInBackground(String... urls) {
        try {
            downloadGameData(urls[0]);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return mGames;
    }

    @Override
    protected void onPostExecute(ArrayList<Game> result) {
        mAdapter.loadThumbnails(offset, offset + mGames.size());
    }

    @Override
    protected void onProgressUpdate(Game... progress) {
        mAdapter.update(progress[0]);
    }

    private String downloadGameData(String myurl) throws IOException, JSONException {
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
            parseGameJSON(result);
            return result;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private void parseGameJSON(String r) throws JSONException {
        String title, thumb;
        int viewers, channelc, id;
        JSONObject game;

        JSONObject jObject = new JSONObject(r);
        JSONArray jArray = jObject.getJSONArray("top");

        for (int i=0; i<jArray.length(); i++) {
            viewers = jArray.getJSONObject(i).getInt("viewers");
            channelc = jArray.getJSONObject(i).getInt("channels");

            game = jArray.getJSONObject(i).getJSONObject("game");
            title = game.getString("name");
            id = game.getInt("_id");
            thumb = game.getJSONObject("box").getString("medium");
            Game temp = new Game(title,thumb,viewers,channelc,id,null);
            mGames.add(temp);
            publishProgress(temp);
        }
    }
}
