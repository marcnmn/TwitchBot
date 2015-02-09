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
import twitchvod.tvvod.data.primitives.Channel;
import twitchvod.tvvod.data.async_tasks.TwitchBitmapData;
import twitchvod.tvvod.data.async_tasks.TwitchChannelData;

public class ChannelListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<Channel> mChannels;
    private String mBaseUrl;

    public ChannelListAdapter(Context c, String url) {
        mChannels = new ArrayList<>();
        mBaseUrl = url;
        mInflater = LayoutInflater.from(c);
    }

    public void loadTopData(int limit, int offset) {
        String request = mBaseUrl;
        request += "limit=" + limit + "&offset=" + offset;
        TwitchChannelData t = new TwitchChannelData(this);
        t.execute(request);
    }

    public void update(Channel c) {
        mChannels.add(c);
        notifyDataSetChanged();
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if(convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.channels_row_layout, parent, false);
            holder = new ViewHolder();
            holder.firstLine = (TextView) convertView.findViewById(R.id.firstLine);
            holder.secondLine = (TextView) convertView.findViewById(R.id.secondLine);
            holder.secondLineViewers = (TextView) convertView.findViewById(R.id.secondLineViewers);
            holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mChannels.get(position).mPreview != null)
            holder.imageView.setImageBitmap(mChannels.get(position).mPreview);
        else if (mChannels.get(position).mLogo != null)
           holder.imageView.setImageBitmap(mChannels.get(position).mLogo);

        holder.firstLine.setText(mChannels.get(position).mTitle);
        holder.secondLine.setText(mChannels.get(position).mGame);
        holder.secondLineViewers.setText(String.valueOf(mChannels.get(position).mViewers));

        return convertView;
    }

    public void loadThumbnails(int start, int stop) {
        String urls[] = new String[(stop-start) * 2];

        int i2;
        for (int i = 0; i < stop-start; i++) {
            i2 = i*2;
            urls[i2] = mChannels.get(start + i).mPreviewLink;
            urls[i2+1] = "";
        }
        TwitchBitmapData tb = new TwitchBitmapData(this, start);
        tb.execute(urls);
    }

    public int getCount() {
        return mChannels.size();
    }

    public Channel getItem(int position) {
        return mChannels.get(position);
    }

    public long getItemId(int position) {
        return mChannels.get(position).mId;
    }

    public ArrayList<Channel> getChannels() {
        return mChannels;
    }

    public void updateThumbnail(Bitmap bmp, int item, int offset) {

        int i;
        if (item%2 == 0) {
            i = item / 2 + offset;
            mChannels.get(i).mPreview = bmp;
        }
        else {
            i = (item-1) / 2 + offset;
            mChannels.get(i).mLogo = bmp;
        }
        notifyDataSetChanged();

    }

    public class ViewHolder {
        public ImageView imageView;
        public TextView secondLine;
        public TextView firstLine;
        public TextView secondLineViewers;
    }
}