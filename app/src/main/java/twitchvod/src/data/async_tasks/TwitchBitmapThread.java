package twitchvod.src.data.async_tasks;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import twitchvod.src.ui_fragments.ChannelDetailFragment;
import twitchvod.src.ui_fragments.ChannelListFragment;
import twitchvod.src.ui_fragments.GamesRasterFragment;
import twitchvod.src.ui_fragments.StreamListFragment;


public class TwitchBitmapThread {
    private StreamListFragment mStreamListFragment;
    private GamesRasterFragment mGamesRasterFragment;
    private ChannelListFragment mChannelListFragment;
    private ChannelDetailFragment mChannelDetailFragment;
    private Thread mThread;
    private boolean mAbort;

    public TwitchBitmapThread(ChannelDetailFragment c) {
        mChannelDetailFragment = c;
    }

    public TwitchBitmapThread(ChannelListFragment c) {
        mChannelListFragment = c;
    }

    public TwitchBitmapThread(GamesRasterFragment g) {
        mGamesRasterFragment = g;
    }

    public TwitchBitmapThread(StreamListFragment s) {
        mStreamListFragment = s;
    }

    public void downloadImageInBackground(String s, int priority) {
        final String fUrl = s;
        mThread = new Thread(new Runnable() {
            public void run() {
                final Bitmap bitmap = downloadBitmap(fUrl);
                getThreadActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        pushResult(bitmap);
                    }
                });
            }
        });
        if (priority > 0) mThread.setPriority(priority);
        mThread.start();
    }

    public void downloadImagesInBackground(ArrayList<String> s, int priority, int offset) {
        final ArrayList<String> fUrls = s;
        final int fOffset = offset;
        mThread = new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < fUrls.size(); i++){
                    final int index = i;
                    final Bitmap bitmap = downloadBitmap(fUrls.get(i));
                    if (mAbort) return;
                    getThreadActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pushResults(bitmap, fOffset + index);
                        }
                    });
                }
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

    private void pushResult(Bitmap b) {
        if (mChannelDetailFragment != null) mChannelDetailFragment.updateThumbnail(b);
        if (mChannelListFragment != null) mChannelListFragment.getActivity();
        if (mGamesRasterFragment != null) mGamesRasterFragment.getActivity();
        if (mStreamListFragment != null) mStreamListFragment.getActivity();
    }

    private void pushResults(Bitmap b, int i) {
        if (mChannelDetailFragment != null) mChannelDetailFragment.updateThumbnail(b);
        if (mChannelListFragment != null) mChannelListFragment.getActivity();
        if (mGamesRasterFragment != null) mGamesRasterFragment.getActivity();
    }

    private Bitmap downloadBitmap(String myurl) {
        InputStream is;
        Bitmap bitmap = null;
        URL url;

        try {
            url = new URL(myurl);
            HttpURLConnection conn = null;
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();
            is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public void stopThread() {
        mAbort = true;
    }
}
