package twitchvod.src.data.async_tasks;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import twitchvod.src.adapter.StreamListAdapter;
import twitchvod.src.data.TwitchJSONParser;
import twitchvod.src.data.primitives.Stream;
import twitchvod.src.ui_fragments.ChannelDetailFragment;
import twitchvod.src.ui_fragments.ChannelListFragment;
import twitchvod.src.ui_fragments.GamesRasterFragment;
import twitchvod.src.ui_fragments.StreamListFragment;

public class TwitchJSONDataThread {
    private StreamListFragment mStreamListFragment;
    private GamesRasterFragment mGamesRasterFragment;
    private ChannelListFragment mChannelListFragment;
    private ChannelDetailFragment mChannelDetailFragment;
    private Thread mThread;
    private boolean mAbort = false;

    public TwitchJSONDataThread(ChannelDetailFragment c) {
        mChannelDetailFragment = c;
    }

    public TwitchJSONDataThread(ChannelListFragment c) {
        mChannelListFragment = c;
    }

    public TwitchJSONDataThread(GamesRasterFragment g) {
        mGamesRasterFragment = g;
    }

    public TwitchJSONDataThread(StreamListFragment s) {
        mStreamListFragment = s;
    }

    public void downloadJSONInBackground(String s, int priority) {
        final String fUrl = s;
        mThread = new Thread(new Runnable() {
            public void run() {
                final String is = downloadJSONData(fUrl);
                if (mAbort) return;
                getThreadActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pushResult(is);
                    }
                });
            }
        });
        if (priority > 0) mThread.setPriority(priority);
        mThread.start();
    }

    private Activity getThreadActivity() {
        if (mChannelDetailFragment != null) return mChannelDetailFragment.getActivity();
        if (mChannelListFragment != null) return mChannelListFragment.getActivity();
        if (mGamesRasterFragment != null) return mGamesRasterFragment.getActivity();
        if (mStreamListFragment != null) return mStreamListFragment.getActivity();
        return null;
    }

    private void pushResult(String s) {
        //if (mChannelDetailFragment != null) mChannelDetailFragment.js(b);
        //if (mChannelListFragment != null) mChannelListFragment.getActivity();
        //if (mGamesRasterFragment != null) mGamesRasterFragment.getActivity();
        if (mStreamListFragment != null) mStreamListFragment.dataReceived(s);
    }

    private String downloadJSONData(String myurl) {
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
            return result;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public void stopThread() {
        mAbort = true;
    }
}
