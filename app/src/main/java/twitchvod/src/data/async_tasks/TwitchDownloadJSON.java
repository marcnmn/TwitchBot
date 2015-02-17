package twitchvod.src.data.async_tasks;

import android.os.AsyncTask;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import twitchvod.src.adapter.ChannelListAdapter;

public class TwitchDownloadJSON extends AsyncTask<String, String, String> {
    private String mData;
    private ChannelListAdapter mChannelListAdapter;

    public TwitchDownloadJSON(ChannelListAdapter c) {
        mData = "";
        mChannelListAdapter = c;
    }

    @Override
    protected String doInBackground(String... urls) {
        try {
            downloadJSON(urls[0]);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return mData;
    }

    @Override
    protected void onPostExecute(String result) {
        if (mChannelListAdapter != null) {
            //mChannelListAdapter.updateChannelList(result);
        }
    }

    @Override
    protected void onProgressUpdate(String... progress) {
    }

    private String downloadJSON(String myurl) throws IOException, JSONException {
        InputStream is = null;
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
            mData = sb.toString();
            return mData;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
