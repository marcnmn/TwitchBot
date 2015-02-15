package twitchvod.src.data.async_tasks;

import android.app.Activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import twitchvod.src.data.TwitchJSONParser;
import twitchvod.src.data.primitives.Stream;
import twitchvod.src.ui_fragments.ChannelDetailFragment;
import twitchvod.src.ui_fragments.ChannelListFragment;
import twitchvod.src.ui_fragments.GamesRasterFragment;
import twitchvod.src.ui_fragments.StreamListFragment;

public class TwitchJSONParserThread {
    private StreamListFragment mStreamListFragment;
    private GamesRasterFragment mGamesRasterFragment;
    private ChannelListFragment mChannelListFragment;
    private ChannelDetailFragment mChannelDetailFragment;
    private Thread mThread;
    private boolean mAbort = false;

    public TwitchJSONParserThread(ChannelDetailFragment c) {
        mChannelDetailFragment = c;
    }

    public TwitchJSONParserThread(ChannelListFragment c) {
        mChannelListFragment = c;
    }

    public TwitchJSONParserThread(GamesRasterFragment g) {
        mGamesRasterFragment = g;
    }

    public TwitchJSONParserThread(StreamListFragment s) {
        mStreamListFragment = s;
    }

    public void parseJSONInBackground(String s, int priority) {
        final String fJSON = s;
        mThread = new Thread(new Runnable() {
            public void run() {
                if (mAbort) return;
                parseAndPush(fJSON);
            }
        });
        if (priority > 0) mThread.setPriority(priority);
        mThread.start();
    }

    private void parseAndPush(String j) {
        //if (mChannelDetailFragment != null) mChannelDetailFragment.js(b);
        //if (mChannelListFragment != null) mChannelListFragment.getActivity();
        //if (mGamesRasterFragment != null) mGamesRasterFragment.getActivity();
        if (mStreamListFragment != null) parseAndPushStream(j);
        }

    private void parseAndPushStream(String j) {
        final ArrayList<Stream> streams = TwitchJSONParser.streamJSONtoArrayList(j);
        mStreamListFragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStreamListFragment.dataParsed(streams);
            }
        });
    }

    public void stopThread() {
        mAbort = true;
    }
}
