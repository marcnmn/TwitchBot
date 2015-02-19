package twitchvod.src.data.async_tasks;

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
import java.util.HashMap;

import twitchvod.src.data.TwitchNetworkTasks;
import twitchvod.src.data.primitives.TwitchVideo;
import twitchvod.src.ui_fragments.ChannelDetailFragment;
import twitchvod.src.ui_fragments.ChannelListFragment;
import twitchvod.src.ui_fragments.GamesRasterFragment;
import twitchvod.src.ui_fragments.StreamListFragment;

public class TwitchBroadcastThread {
    private StreamListFragment mStreamListFragment;
    private GamesRasterFragment mGamesRasterFragment;
    private ChannelListFragment mChannelListFragment;
    private ChannelDetailFragment mChannelDetailFragment;
    private TwitchVideo mVideo;
    private Thread mThread;
    private HashMap<String, String> mStreamUrls;

    public TwitchBroadcastThread(ChannelDetailFragment c) {
        mChannelDetailFragment = c;
    }

    public TwitchBroadcastThread(TwitchVideo v) {
        mVideo = v;
    }

    public void downloadJSONInBackground(final String tokenUrl, final String videoId, final int requestType, int priority) {
        mThread = new Thread(new Runnable() {
            public void run() {
                if (requestType == 0) mStreamUrls = liveStreamUrls(tokenUrl, videoId);
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

    private void pushResult(HashMap<String, String> result, int req) {
         if (mChannelDetailFragment != null && req == 0) mChannelDetailFragment.videoPlaylistReceived(result);
    }

    private HashMap<String, String> liveStreamUrls(String s, String id) {
        HashMap<String, String> videoPlaylist = null;
        JSONObject jToken = TwitchNetworkTasks.downloadJSONData(s);
        try {
            String token = jToken.getString("token");
            String sig = jToken.getString("sig");

            String m3u8Url = "http://usher.twitch.tv/vod/" + id + "?nauth=";
            m3u8Url += token + "&nauthsig=" + sig;
            Log.v("asdfa", m3u8Url);

            videoPlaylist = TwitchNetworkTasks.fetchTwitchPlaylist(m3u8Url);
            return videoPlaylist;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return videoPlaylist;
    }

}
