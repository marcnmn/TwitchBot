package twitchvod.src.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.AnimatorRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import twitchvod.src.MainActivity;
import twitchvod.src.R;
import twitchvod.src.data.TwitchNetworkTasks;
import twitchvod.src.data.primitives.Stream;
import twitchvod.src.data.async_tasks.TwitchBitmapData;
import twitchvod.src.data.async_tasks.TwitchStreamData;
import twitchvod.src.ui_fragments.StreamListFragment;

public class StreamListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<Stream> mStreams;
    private MainActivity mActivity;
    Animation mAlpha;

    public StreamListAdapter(StreamListFragment c, String url) {
        if (mStreams == null) mStreams = new ArrayList<>();
        mActivity = (MainActivity) c.getActivity();
        mInflater = LayoutInflater.from(c.getActivity());
        mAlpha = new AlphaAnimation(0,1);
        mAlpha.setDuration(500);
    }

    public void update(ArrayList<Stream> l) {
        if (getCount() == 0) {
        }
        mStreams.addAll(l);
        notifyDataSetChanged();
    }

    public void setStream(int index, Stream s) {
        if (getCount() == 0)
            return;
        mStreams.set(index, s);
        notifyDataSetChanged();
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.streams_row_layout, parent, false);
            holder = new ViewHolder();
            holder.firstLine = (TextView) convertView.findViewById(R.id.firstLine);
            holder.secondLine = (TextView) convertView.findViewById(R.id.secondLine);
            holder.secondLineViewers = (TextView) convertView.findViewById(R.id.secondLineViewers);
            holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mStreams.get(position).mPreview == null) {
            loadImage(position, holder.imageView);
        } else {
            holder.imageView.setImageBitmap(mStreams.get(position).mPreview);
        }

        holder.firstLine.setText(mStreams.get(position).mTitle);
        holder.secondLine.setText(mStreams.get(position).mGame);
        holder.secondLineViewers.setText(String.valueOf(mStreams.get(position).mViewers));

        return convertView;
    }

    public int getCount() {
        return mStreams.size();
    }

    public Stream getItem(int position) {
        return mStreams.get(position);
    }

    public long getItemId(int position) {
        return mStreams.get(position).mId;
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
                    final Bitmap bitmap = TwitchNetworkTasks.downloadBitmap(mStreams.get(pos).mPreviewLink);
                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                            mStreams.get(pos).mPreview = bitmap;
                        }
                    });
                }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
}