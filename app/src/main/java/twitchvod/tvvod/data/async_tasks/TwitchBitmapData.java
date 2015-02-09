package twitchvod.tvvod.data.async_tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import twitchvod.tvvod.adapter.ChannelListAdapter;
import twitchvod.tvvod.adapter.GamesAdapter;


public class TwitchBitmapData extends AsyncTask<String, Integer, Void> {
    private Bitmap mBitmaps[];
    private int mOffset;
    private ChannelListAdapter mChannelAdapter;
    private GamesAdapter mGameAdapter;

    public TwitchBitmapData(ChannelListAdapter c, int offset) {
        mOffset = offset;
        mChannelAdapter = c;
    }

    public TwitchBitmapData(GamesAdapter gamesAdapter, int offset) {
        mGameAdapter = gamesAdapter;
        mOffset = offset;

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
        if (mChannelAdapter != null)
            mChannelAdapter.updateThumbnail(mBitmaps[i], i, mOffset);
        if (mGameAdapter != null)
            mGameAdapter.updateThumbnail(mBitmaps[i], i, mOffset);
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
