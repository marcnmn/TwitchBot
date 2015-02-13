package twitchvod.tvvod.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import twitchvod.tvvod.R;
import twitchvod.tvvod.data.TwitchJSONParser;
import twitchvod.tvvod.data.async_tasks.TwitchBitmapData;
import twitchvod.tvvod.data.async_tasks.TwitchPastBroadcastsData;
import twitchvod.tvvod.data.primitives.PastBroadcast;

public class PastBroadcastsListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<PastBroadcast> mBroadcasts;
    private String mBaseUrl;

    public PastBroadcastsListAdapter(Context c, String url) {
        mBroadcasts = new ArrayList<>();
        mBaseUrl = url;
        mInflater = LayoutInflater.from(c);
    }

    public void loadTopData(int limit, int offset) {
        String request = mBaseUrl;
        request += "limit=" + limit + "&offset=" + offset;
        TwitchPastBroadcastsData t = new TwitchPastBroadcastsData(this);
        t.execute(request);
    }

    public void update(PastBroadcast c) {
        mBroadcasts.add(c);
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
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mBroadcasts.get(position).previewBitmap != null) {
            holder.imageView.setImageBitmap(mBroadcasts.get(position).previewBitmap);
        }

        holder.firstLine.setText(mBroadcasts.get(position).title);
        holder.secondLine.setText(mBroadcasts.get(position).timeAgo());
        holder.secondLineViewers.setText(String.valueOf(mBroadcasts.get(position).views));

        return convertView;
    }

    public void loadThumbnails(int start, int stop) {
        String urls[] = new String[(stop-start)];

        for (int i = 0; i < stop-start; i++) {
            urls[i] = mBroadcasts.get(start + i).preview;
        }
        TwitchBitmapData tb = new TwitchBitmapData(this, start);
        tb.execute(urls);
    }

    public int getCount() {
        return mBroadcasts.size();
    }

    public PastBroadcast getItem(int position) {
        return mBroadcasts.get(position);
    }

    public long getItemId(int position) {
        String id = mBroadcasts.get(position).id;
        return Long.valueOf(id.substring(1,id.length()-1));
    }

    public String getItemID(int position) {
        return mBroadcasts.get(position).id;
    }

    public ArrayList<PastBroadcast> getChannels() {
        return mBroadcasts;
    }

    public void updateThumbnail(Bitmap bmp, int item, int offset) {
        mBroadcasts.get(item+offset).previewBitmap = bmp;
        notifyDataSetChanged();
    }

    public class ViewHolder {
        public ImageView imageView;
        public TextView secondLine;
        public TextView firstLine;
        public TextView secondLineViewers;
    }
}