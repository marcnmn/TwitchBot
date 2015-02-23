package twitchvod.src.data.async_tasks;

import android.app.Activity;
import android.util.Log;

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
import java.util.HashMap;
import java.util.LinkedHashMap;

import twitchvod.src.ui_fragments.ChannelDetailFragment;
import twitchvod.src.ui_fragments.ChannelListFragment;
import twitchvod.src.ui_fragments.GamesRasterFragment;
import twitchvod.src.ui_fragments.StreamListFragment;

public class TwitchLiveStreamThread {
    private StreamListFragment mStreamListFragment;
    private GamesRasterFragment mGamesRasterFragment;
    private ChannelListFragment mChannelListFragment;
    private ChannelDetailFragment mChannelDetailFragment;
    private Thread mThread;
    private LinkedHashMap<String, String> mStreamUrls;
    private boolean mIsAuthenticated;
    private String mUserToken;

    public TwitchLiveStreamThread(ChannelDetailFragment c) {
        mChannelDetailFragment = c;
    }
    public TwitchLiveStreamThread(ChannelDetailFragment c, String token) {
        mChannelDetailFragment = c;
        mIsAuthenticated = true;
        mUserToken = token;
    }


    public void downloadJSONInBackground(final String s, final String name, final int requestType, int priority) {
        mThread = new Thread(new Runnable() {
            public void run() {
                if (requestType == 0) mStreamUrls = liveStreamUrls(s, name);
                if (requestType == 1) mStreamUrls = broadcastUrls(s, name);
                if (mChannelDetailFragment.getActivity() == null) return;
                    mChannelDetailFragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pushResult(mStreamUrls, requestType);
                    }
                });
            }
        });
        if (priority > 0) mThread.setPriority(priority);
        mThread.start();
    }

    private void pushResult(LinkedHashMap<String, String> result, int req) {
         if (mChannelDetailFragment != null && req == 0) mChannelDetailFragment.liveLinksReceived(result);
    }

    private LinkedHashMap<String, String> liveStreamUrls(String s, String name) {
        LinkedHashMap<String, String> streams = null;
        JSONObject jToken = downloadJSONData(s);
        try {
            String token = jToken.getString("token");
            String sig = jToken.getString("sig");

            String m3u8Url = "http://usher.twitch.tv/api/channel/hls/";
            m3u8Url += name + ".m3u8?player=twitchweb&token=";
            m3u8Url += token + "&sig=" + sig;
            m3u8Url += "&allow_audio_only=true&allow_source=true&type=any&p=8732417";
            if (mIsAuthenticated) m3u8Url += "&oauth_token=" + mUserToken;
            Log.v("asdfa", m3u8Url);

            streams = fetchLivePlaylist(m3u8Url);
            return streams;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return streams;
    }

    private LinkedHashMap<String, String> broadcastUrls(String s, String n) {
        return null;
    }

    private JSONObject downloadJSONData(String myurl) {
        InputStream is = null;
        String result;
        JSONObject jObject = null;
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
            jObject = new JSONObject(result);
            return jObject;

        } catch (MalformedURLException | JSONException | UnsupportedEncodingException e) {
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

    private LinkedHashMap<String, String> fetchLivePlaylist(String myurl) {
        InputStream is = null;
        LinkedHashMap<String, String> hmap = new LinkedHashMap<>();
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            try {
                conn.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            return hmap;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return hmap;
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
