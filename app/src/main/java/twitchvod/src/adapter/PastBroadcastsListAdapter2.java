package twitchvod.src.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import twitchvod.src.R;
import twitchvod.src.data.TwitchNetworkTasks;
import twitchvod.src.data.primitives.TwitchVideo;
import twitchvod.src.ui_fragments.ChannelDetailFragment;

public class PastBroadcastsListAdapter2 extends BaseAdapter {
    private final static int IS_HEADER = 0;
    private final static int IS_HIGHLIGHT_HEADER = 1;
    private final static int IS_HIGHLIGHT = 2;
    private final static int IS_BROADCAST_HEADER = 3;
    private final static int IS_BROADCAST = 4;

    private boolean IS_PAUSED = false;

    private String mHighlightHeader = "Highlights", mBroadcastHeader = "Broadcasts";

    private Activity mActivity;
    private ChannelDetailFragment mFragment;
    private ArrayList<TwitchVideo> mHighlights;
    private ArrayList<TwitchVideo> mBroadcasts;
    private LayoutInflater mInflater;
    private int mWidth;
    private ViewGroup.LayoutParams mParams;

    public PastBroadcastsListAdapter2(ChannelDetailFragment c) {
        mActivity = c.getActivity();
        mFragment = c;
        mHighlights = new ArrayList<>();
        mBroadcasts = new ArrayList<>();
        mInflater = LayoutInflater.from(c.getActivity());
    }

    public void updateHighlights(ArrayList <TwitchVideo> c) {
        if (c == null) return;
        if (mHighlights == null) mHighlights = new ArrayList<>();
        mHighlights.addAll(c);
        notifyDataSetChanged();
    }

    public void updateBroadcasts(ArrayList <TwitchVideo> c) {
        if (c == null) return;
        if (mHighlights == null) mBroadcasts = new ArrayList<>();
        mBroadcasts.addAll(c);
        notifyDataSetChanged();
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.broadcast_row_layout, parent, false);
            holder = new ViewHolder();

            holder.firstLine = (TextView) convertView.findViewById(R.id.firstLine);
            holder.secondLine = (TextView) convertView.findViewById(R.id.secondLine);
            holder.secondLineViewers = (TextView) convertView.findViewById(R.id.secondLineViewers);
            holder.imageView = (ImageView) convertView.findViewById(R.id.icon);

            if (mWidth == 0) {
                mWidth = getWindowWidth();
                mParams = holder.imageView.getLayoutParams();
                mParams.width = (int) (mWidth*0.35);
            }

            if (mParams.width > 0) {
                holder.imageView.setLayoutParams(mParams);
            }
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        int j = getGroup(position+1);
        if (j == IS_HIGHLIGHT_HEADER) {
            View highlights = mInflater.inflate(R.layout.channel_video_footer, null);
            ((TextView)highlights.findViewById(R.id.textView)).setText(mHighlightHeader);
            if (mHighlights.isEmpty()) highlights.setVisibility(View.INVISIBLE);
            return highlights;
        } else if (j == IS_HIGHLIGHT) {
            int index = getChildPosition(position, j)+1;
            holder.firstLine.setText(mHighlights.get(index).mTitle);
            holder.secondLine.setText(mHighlights.get(index).timeAgo());
            holder.secondLineViewers.setText(mHighlights.get(index).mViews);

            Picasso.with(mActivity)
                    .load(mHighlights.get(index).mPreviewLink)
                    .placeholder(R.drawable.broadcast_preview)
                    .error(R.drawable.broadcast_preview)
                    .config(Bitmap.Config.RGB_565)
                    .into(holder.imageView);
            //holder.imageView.setTag(mHighlights.get(index).mPreviewLink);
            //if (mHighlights.get(index).mPreview == null)
            //    new DownloadImageTask(j, index, holder.imageView).execute(mHighlights.get(index).mPreviewLink);
                //loadImage(j, index, mHighlights.get(index).mPreviewLink, holder.imageView);
            //else
            //    holder.imageView.setImageBitmap(mHighlights.get(index).mPreview);

        } else if (j == IS_BROADCAST_HEADER) {
            View broadcasts = mInflater.inflate(R.layout.channel_video_footer, null);
            ((TextView)broadcasts.findViewById(R.id.textView)).setText(mBroadcastHeader);
            if (mBroadcasts.isEmpty()) broadcasts.setVisibility(View.INVISIBLE);
            return broadcasts;

        } else if (j == IS_BROADCAST) {
            int index = getChildPosition(position+1, j);
            holder.firstLine.setText(mBroadcasts.get(index).mTitle);
            holder.secondLine.setText(mBroadcasts.get(index).timeAgo());
            holder.secondLineViewers.setText(mBroadcasts.get(index).mViews);

            Picasso.with(mActivity)
                    .load(mBroadcasts.get(index).mPreviewLink)
                    .placeholder(R.drawable.broadcast_preview)
                    .error(R.drawable.broadcast_preview)
                    .config(Bitmap.Config.RGB_565)
                    .into(holder.imageView);
            //holder.imageView.setTag(mBroadcasts.get(index).mPreviewLink);
            //if (mBroadcasts.get(index).mPreview == null)
            //    new DownloadImageTask(j, index, holder.imageView).execute(mBroadcasts.get(index).mPreviewLink);
                //loadImage(j, index, mBroadcasts.get(index).mPreviewLink, holder.imageView);
            //else
            //    holder.imageView.setImageBitmap(mBroadcasts.get(index).mPreview);
        }
        //preloadImages(position + 1);

        //if (mParams != null) holder.imageView.setLayoutParams(mParams);
        return convertView;
    }

    private void preloadImages(int position) {
        int j;
        int k;
        for (int i = position+1; i < position + 5; i++) {
            if (i >= getCount()) return;
            j = getGroup(i);
            k = getChildPosition(i, j);
            if (j == IS_HIGHLIGHT && mHighlights.get(k).mPreview == null) loadImage(j, k, mHighlights.get(k).mPreviewLink, null);
            if (j == IS_BROADCAST && mBroadcasts.get(k).mPreview == null) loadImage(j, k, mBroadcasts.get(k).mPreviewLink, null);
        }
    }

    public int getCount() {
        int count = 0;
        if (!mHighlights.isEmpty()) count += mHighlights.size() + 1;
        if (!mBroadcasts.isEmpty()) count += mBroadcasts.size() + 1;
        return count;
    }

    public TwitchVideo getHighlight(int position) {
        return mHighlights.get(position);
    }

    public TwitchVideo getBroadcast(int position) {
        return mBroadcasts.get(position);
    }

    public TwitchVideo getItem(int position) {
        if (position < mHighlights.size() && position != 0) return mHighlights.get(position - 1);
        if (position > mHighlights.size() && position != 0) return mBroadcasts.get(position - mHighlights.size());
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public int getGroup(int position) {
        if (position == 0) return IS_HEADER;
        if (!mHighlights.isEmpty() && position == 1) return IS_HIGHLIGHT_HEADER;
        if (!mHighlights.isEmpty() && position <= mHighlights.size() + 1) return IS_HIGHLIGHT;
        if (!mHighlights.isEmpty() && position == mHighlights.size() + 2) return IS_BROADCAST_HEADER;
        if (!mHighlights.isEmpty() && position <= mHighlights.size() + mBroadcasts.size() + 2) return IS_BROADCAST;
        if (mHighlights.isEmpty() && position == 1) return IS_BROADCAST_HEADER;
        if (mHighlights.isEmpty() && position <= mBroadcasts.size()+2) return IS_BROADCAST;
        return -1;
    }

    public int getChildPosition(int position, int group) {
        if (group == IS_HEADER) return 0;
        if (!mHighlights.isEmpty() && group == IS_HIGHLIGHT_HEADER) return 0;
        if (!mHighlights.isEmpty() && group == IS_HIGHLIGHT) return position - 2;
        if (!mHighlights.isEmpty() && group == IS_BROADCAST_HEADER) return 0;
        if (!mHighlights.isEmpty() && group == IS_BROADCAST) return position - mHighlights.size() - 3;
        if (mHighlights.isEmpty() && group == IS_BROADCAST_HEADER) return 0;
        if (mHighlights.isEmpty() && group == IS_BROADCAST) return position - 2;
        return -1;
    }

    private void loadImage(final int group, final int child, final String url, final ImageView imageView) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                if (IS_PAUSED) return;
                final Bitmap bitmap = TwitchNetworkTasks.downloadBitmap(url);
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        if (imageView != null)imageView.setImageBitmap(bitmap);
                        if (group == IS_HIGHLIGHT) mHighlights.get(child).mPreview = bitmap;
                        if (group == IS_BROADCAST) mBroadcasts.get(child).mPreview = bitmap;
                    }
                });
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    public void setHighlightHeader(String h) {
        mHighlightHeader = h;
    }

    public void setBroadcastHeader(String h) {
        mBroadcastHeader = h;
    }

    public ArrayList<TwitchVideo> getHighlights() {
        return mHighlights;
    }

    public ArrayList<TwitchVideo> getBroadcasts() {
        return mBroadcasts;
    }

    public void clearAllData() {
        mHighlights.clear();
        mBroadcasts.clear();
    }

    public void clearHighlightData() {
        mHighlights.clear();
        notifyDataSetChanged();
    }

    public void clearBroadcastData() {
        mBroadcasts.clear();
        notifyDataSetChanged();
    }


    private int getWindowHeight() {
        int height;
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        height = size.y;

        return height;
    }

    private int getWindowWidth() {
        int width;
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;

        return width;
    }

    public class ViewHolder {
        public ImageView imageView;
        public TextView secondLine;
        public TextView firstLine;
        public TextView secondLineViewers;
        public LinearLayout rightPart;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;
        private final int group, child;

        public DownloadImageTask(final int group, final int child, final ImageView imageView) {
            this.imageView = imageView;
            this.group = group;
            this.child = child;
        }

        protected Bitmap doInBackground(String... urls) {
            return TwitchNetworkTasks.downloadBitmap(urls[0]);
        }

        protected void onPostExecute(Bitmap result) {
            if (group == IS_HIGHLIGHT) {
                if (imageView.getTag().equals(mHighlights.get(child).mPreviewLink))
                    imageView.setImageBitmap(result);
                mHighlights.get(child).mPreview = result;
            }
            if (group == IS_BROADCAST) {
                if (imageView.getTag().equals(mBroadcasts.get(child).mPreviewLink))
                    imageView.setImageBitmap(result);
                mBroadcasts.get(child).mPreview = result;
            }
        }
    }
}