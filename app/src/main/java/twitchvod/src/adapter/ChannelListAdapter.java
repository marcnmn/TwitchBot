package twitchvod.src.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import twitchvod.src.R;
import twitchvod.src.data.TwitchNetworkTasks;
import twitchvod.src.data.primitives.Channel;
import twitchvod.src.ui_fragments.ChannelListFragment;

public class ChannelListAdapter extends BaseAdapter {
    private Activity mActivity;
    private ChannelListFragment mFragment;
    private LayoutInflater mInflater;
    private ArrayList<Channel> mChannels;
    private RelativeLayout.LayoutParams mRelativeLayout;
    private int mWidth = 0;

    public ChannelListAdapter(ChannelListFragment c) {
        mActivity = c.getActivity();
        mFragment = c;
        mChannels = new ArrayList<>();
        mInflater = LayoutInflater.from(c.getActivity());
    }

    public void update(ArrayList <Channel> c) {
        mChannels.addAll(c);
        notifyDataSetChanged();
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if(convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.channel_item_layout, parent, false);
            holder = new ViewHolder();
            holder.firstLine = (TextView) convertView.findViewById(R.id.firstLine);
            holder.secondLine = (TextView) convertView.findViewById(R.id.secondLine);
            holder.secondLineViewers = (TextView) convertView.findViewById(R.id.secondLineViewers);
            holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (convertView.getMeasuredWidth() != mWidth && convertView.getMeasuredWidth() != 0) {
            mWidth = Math.round(convertView.getMeasuredWidth() * 0.4f);
            mRelativeLayout = new RelativeLayout.LayoutParams(mWidth, mWidth);
        }

        if (mChannels.get(position).mLogoBitmap == null) {
            loadImage(position, holder.imageView);
        } else {
            holder.imageView.setImageBitmap(mChannels.get(position).mLogoBitmap);
        }

        if (mRelativeLayout != null)
            holder.imageView.setLayoutParams(mRelativeLayout);

        holder.firstLine.setText(mChannels.get(position).getDisplayName());
        holder.secondLine.setText(mChannels.get(position).getGame());
        holder.secondLineViewers.setText(String.valueOf(mChannels.get(position).getViews()));

        return convertView;
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

    public void resetDimensions() {
        mRelativeLayout = null;
        mWidth = 0;
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

    private void loadImage(final int pos, final ImageView imageView) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                final Bitmap bitmap = TwitchNetworkTasks.downloadBitmap(mChannels.get(pos).getLogoLink());
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                        mChannels.get(pos).mLogoBitmap = bitmap;
                        mChannels.get(pos).mLogoBitmap = bitmap;
                    }
                });
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

}