package twitchvod.tvvod.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;

import java.lang.reflect.Array;
import java.util.ArrayList;

import twitchvod.tvvod.R;
import twitchvod.tvvod.data.TwitchJSONParser;
import twitchvod.tvvod.data.async_tasks.TwitchBitmapData;
import twitchvod.tvvod.data.async_tasks.TwitchChannelData;
import twitchvod.tvvod.data.primitives.Channel;
import twitchvod.tvvod.ui_fragments.ChannelListFragment;

public class ChannelListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<Channel> mChannels;
    private String mBaseUrl;
    private onFirstResultsListener  mCallback;
    private RelativeLayout.LayoutParams mRelativeLayout;
    private int mHeight = 0;
    private int mWidth = 0;

    public ChannelListAdapter(ChannelListFragment c, String url) {
        mChannels = new ArrayList<>();
        mBaseUrl = url;
        mInflater = LayoutInflater.from(c.getActivity());
        mCallback = (onFirstResultsListener) c;
    }

    public void updateChannelList(String r) {
        int start = mChannels.size();
        ArrayList<Channel> ne = TwitchJSONParser.channelJSONtoArrayList(r);
        mChannels.addAll(ne);
        notifyDataSetChanged();
        mCallback.onFirstResults();
        int stop = mChannels.size();
        loadThumbnails(start, stop);
    }

    public interface onFirstResultsListener {
        public void onFirstResults();
    }

    public void loadTopData(int limit, int offset) {
        String request = mBaseUrl;
        request += "limit=" + limit + "&offset=" + offset;
        TwitchChannelData t = new TwitchChannelData(this);
        t.execute(request);
    }

    public void update(Channel c) {
        if (getCount() == 0) {
            mCallback.onFirstResults();
        }
        mChannels.add(c);
        notifyDataSetChanged();
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if(convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.channel_row_layout, parent, false);
            holder = new ViewHolder();
            holder.firstLine = (TextView) convertView.findViewById(R.id.firstLine);
            holder.secondLine = (TextView) convertView.findViewById(R.id.secondLine);
            holder.secondLineViewers = (TextView) convertView.findViewById(R.id.secondLineViewers);
            holder.imageView = (ImageView) convertView.findViewById(R.id.icon);

            if (parent.getMeasuredWidth() > 0 && mWidth == 0) {
                mWidth = Math.round(parent.getMeasuredWidth() * 0.4f);
                mRelativeLayout = new RelativeLayout.LayoutParams(mWidth, mWidth);
            }
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mChannels.get(position).mLogoBitmap != null)
            holder.imageView.setImageBitmap(mChannels.get(position).mLogoBitmap);

        holder.firstLine.setText(mChannels.get(position).getDisplayName());
        holder.secondLine.setText(mChannels.get(position).getGame());
        holder.secondLineViewers.setText(String.valueOf(mChannels.get(position).getViews()));
        holder.imageView.setLayoutParams(mRelativeLayout);

        return convertView;
    }

    public void loadThumbnails(int start, int stop) {
        String urls[] = new String[(stop-start)];
        for (int i = 0; i < stop-start; i++) {
            urls[i] = mChannels.get(start + i).getLogoLink();
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
        return Long.valueOf(mChannels.get(position).getId());
    }

    public ArrayList<Channel> getChannels() {
        return mChannels;
    }

    public void updateThumbnail(Bitmap bmp, int item, int offset) {
        mChannels.get(item+offset).mLogoBitmap = bmp;
        notifyDataSetChanged();
    }

    public class ViewHolder {
        public ImageView imageView;
        public TextView secondLine;
        public TextView firstLine;
        public TextView secondLineViewers;
    }

}