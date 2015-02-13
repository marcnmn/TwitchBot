package twitchvod.src.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import twitchvod.src.R;
import twitchvod.src.data.primitives.Stream;
import twitchvod.src.data.async_tasks.TwitchBitmapData;
import twitchvod.src.data.async_tasks.TwitchStreamData;
import twitchvod.src.ui_fragments.StreamListFragment;

public class StreamListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<Stream> mStreams;
    private String mBaseUrl;
    private onFirstResultsListener  mCallback;
    private int mHeight;
    private int update_counter;

    public StreamListAdapter(StreamListFragment c, String url) {
        mStreams = new ArrayList<>();
        mBaseUrl = url;
        mInflater = LayoutInflater.from(c.getActivity());
        mCallback = (onFirstResultsListener) c;
    }

    public interface onFirstResultsListener {
        public void onFirstResults();
    }

    public void loadTopData(int limit, int offset) {
        String request = mBaseUrl;
        request += "limit=" + limit + "&offset=" + offset;
        TwitchStreamData t = new TwitchStreamData(this);
        t.execute(request);
    }

    public void update(Stream c) {
        if (getCount() == 0) {
            mCallback.onFirstResults();
        }
        mStreams.add(c);
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

        if (mStreams.get(position).mPreview != null)
            holder.imageView.setImageBitmap(mStreams.get(position).mPreview);
        else if (mStreams.get(position).mLogo != null)
           holder.imageView.setImageBitmap(mStreams.get(position).mLogo);

        holder.firstLine.setText(mStreams.get(position).mTitle);
        holder.secondLine.setText(mStreams.get(position).mGame);
        holder.secondLineViewers.setText(String.valueOf(mStreams.get(position).mViewers));

        return convertView;
    }

    public void loadThumbnails(int start, int stop) {
        String urls[] = new String[(stop-start) * 2];

        int i2;
        for (int i = 0; i < stop-start; i++) {
            i2 = i*2;
            urls[i2] = mStreams.get(start + i).mPreviewLink;
            urls[i2+1] = "";
        }
        TwitchBitmapData tb = new TwitchBitmapData(this, start);
        tb.execute(urls);
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

    public ArrayList<Stream> getChannels() {
        return mStreams;
    }

    public void updateThumbnail(Bitmap bmp, int item, int offset) {

        int i;
        if (item%2 == 0) {
            i = item / 2 + offset;
            mStreams.get(i).mPreview = bmp;
        }
        else {
            i = (item-1) / 2 + offset;
            mStreams.get(i).mLogo = bmp;
        }
        update_counter++;
        if (update_counter%3 == 0) {
            notifyDataSetChanged();
        }

    }

    public class ViewHolder {
        public ImageView imageView;
        public TextView secondLine;
        public TextView firstLine;
        public TextView secondLineViewers;
    }

}