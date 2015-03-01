package twitchvod.src.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import twitchvod.src.R;
import twitchvod.src.data.TwitchNetworkTasks;
import twitchvod.src.data.primitives.Stream;
import twitchvod.src.ui_fragments.StreamListFragment;

public class StreamListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<Stream> mStreams;
    Animation mAlpha;
    private int mWidth = 0;
    private RelativeLayout.LayoutParams mRelativeLayout;
    private Context mContext;

    public StreamListAdapter(StreamListFragment c) {
        if (mStreams == null) {
            mStreams = new ArrayList<>();
        }
        mContext = c.getActivity();
        mInflater = LayoutInflater.from(c.getActivity());
        mAlpha = new AlphaAnimation(0,1);
        mAlpha.setDuration(500);
    }

    public void update(ArrayList<Stream> l) {
        mStreams.addAll(l);
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.streams_row_layout, parent, false);
            holder = new ViewHolder();
            holder.firstLine = (TextView) convertView.findViewById(R.id.firstLine);
            holder.secondLine = (TextView) convertView.findViewById(R.id.secondLine);
            holder.secondLineViewers = (TextView) convertView.findViewById(R.id.secondLineViewers);
            holder.streamStatus = (TextView) convertView.findViewById(R.id.text_stream_status);
            holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        //if (((GridView) parent).getColumnWidth() != mWidth && holder.imageView.getDrawable() != null) {
        //    mWidth = ((GridView) parent).getColumnWidth();
        //    float scale = 1.0f * holder.imageView.getDrawable().getIntrinsicHeight()/holder.imageView.getDrawable().getIntrinsicWidth();
        //    int imageWidth = Math.round(mWidth);
        //    int imageHeight = Math.round(imageWidth * scale);
        //    mRelativeLayout = new RelativeLayout.LayoutParams(imageWidth, imageHeight);
        //}

        //holder.imageView.setTag(mStreams.get(position).mPreviewLink);
        Picasso.with(mContext)
                .load(mStreams.get(position).mPreviewLink)
                .placeholder(R.drawable.stream_offline_preview)
                .error(R.drawable.no_livestream)
                .config(Bitmap.Config.RGB_565)
                .into(holder.imageView);
//        if (mStreams.get(position).mPreview == null) {
//            new DownloadImageTask(holder.imageView, position).execute(mStreams.get(position).mPreviewLink);
//        } else {
//            holder.imageView.setImageBitmap(mStreams.get(position).mPreview);
//        }

        //if (mRelativeLayout != null)
            //holder.imageView.setLayoutParams(mRelativeLayout);

        holder.firstLine.setText(mStreams.get(position).mTitle);
        holder.secondLine.setText(mStreams.get(position).printGame());
        holder.secondLineViewers.setText(String.valueOf(mStreams.get(position).mViewers));
        holder.streamStatus.setText(String.valueOf(mStreams.get(position).mStatus));

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

    public void clearData() {
        mStreams.clear();
    }

    public class ViewHolder {
        public ImageView imageView;
        public TextView secondLine;
        public TextView firstLine;
        public TextView secondLineViewers;
        public TextView streamStatus;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;
        private final int pos;

        public DownloadImageTask(ImageView imageView, int pos) {
            this.imageView = imageView;
            this.pos = pos;
        }

        protected Bitmap doInBackground(String... urls) {
                return TwitchNetworkTasks.downloadBitmap(urls[0]);
        }

        protected void onPostExecute(Bitmap result) {
            if (imageView.getTag().equals(mStreams.get(pos).mPreviewLink))
                imageView.setImageBitmap(result);
            mStreams.get(pos).mPreview = result;
        }
    }
}