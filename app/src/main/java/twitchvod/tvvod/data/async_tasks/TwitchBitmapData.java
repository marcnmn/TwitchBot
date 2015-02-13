package twitchvod.tvvod.data.async_tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import twitchvod.tvvod.adapter.ChannelListAdapter;
import twitchvod.tvvod.adapter.StreamListAdapter;
import twitchvod.tvvod.adapter.GamesAdapter;
import twitchvod.tvvod.adapter.PastBroadcastsListAdapter;
import twitchvod.tvvod.ui_fragments.ChannelDetailFragment;


public class TwitchBitmapData extends AsyncTask<String, Integer, Void> {
    private Bitmap mBitmaps[];
    private int mOffset;
    private StreamListAdapter mStreamAdapter;
    private GamesAdapter mGameAdapter;
    private ChannelListAdapter mChannelAdapter;
    private PastBroadcastsListAdapter mBroadcastAdapter;
    private ChannelDetailFragment mChannelDetailFragment;
    private Context mContext;

    public TwitchBitmapData(StreamListAdapter c, int offset) {
        mOffset = offset;
        mStreamAdapter = c;
    }

    public TwitchBitmapData(GamesAdapter gamesAdapter, int offset) {
        mGameAdapter = gamesAdapter;
        mOffset = offset;
    }

    public TwitchBitmapData(PastBroadcastsListAdapter a, int offset) {
        mBroadcastAdapter = a;
        mOffset = offset;
    }

    public TwitchBitmapData(ChannelListAdapter c, int offset) {
        mChannelAdapter = c;
        mOffset = offset;
    }

    public TwitchBitmapData(ChannelDetailFragment f) {
        mChannelDetailFragment = f;
    }


    @Override
    protected Void doInBackground(String... urls) {
        try {
            mBitmaps = new Bitmap[urls.length];
            for (int i = 0; i < mBitmaps.length; i++) {
                downloadBitmap(urls[i], i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        int i = progress[0];
        if (mStreamAdapter != null)
            mStreamAdapter.updateThumbnail(mBitmaps[i], i, mOffset);
        else if (mChannelAdapter != null)
            mChannelAdapter.updateThumbnail(mBitmaps[i], i, mOffset);
        else if (mGameAdapter != null)
            mGameAdapter.updateThumbnail(mBitmaps[i], i, mOffset);
        else if (mBroadcastAdapter != null)
            mBroadcastAdapter.updateThumbnail(mBitmaps[i], i, mOffset);
        else if (mChannelDetailFragment != null)
            mChannelDetailFragment.updateThumbnail(mBitmaps[i]);
    }

    private Bitmap downloadBitmap(String myurl, int index) throws IOException {
        InputStream is;
        Bitmap bitmap;
        URL url;

        try {
            url = new URL(myurl);
        } catch (MalformedURLException e) {
            return null;
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        is = conn.getInputStream();
        bitmap = BitmapFactory.decodeStream(is);

        mBitmaps[index] = bitmap;
        publishProgress(index);
        return bitmap;
    }
}
