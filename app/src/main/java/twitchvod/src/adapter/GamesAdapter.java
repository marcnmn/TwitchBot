package twitchvod.src.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import twitchvod.src.MainActivity;
import twitchvod.src.R;
import twitchvod.src.data.TwitchNetworkTasks;
import twitchvod.src.data.primitives.Game;
import twitchvod.src.data.async_tasks.TwitchBitmapData;
import twitchvod.src.data.async_tasks.TwitchGameData;
import twitchvod.src.ui_fragments.GamesRasterFragment;

public class GamesAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private String mBaseUrl;
    private Activity mContext;
    private ArrayList<Game> mGames;
    private double aspectRatio;
    private RelativeLayout.LayoutParams mRelativeLayout;
    private Activity mActivity;

    public GamesAdapter(Context c, String url) {
        mContext = (Activity) c;
        mGames = new ArrayList<>();
        mBaseUrl = url;
    }

    public GamesAdapter(GamesRasterFragment c, String url) {
        if (mGames == null) mGames = new ArrayList<>();
        mActivity = c.getActivity();
        mInflater = LayoutInflater.from(c.getActivity());
        mGames = new ArrayList<>();
        mBaseUrl = url;
    }

    public void loadTopData(int limit, int offset) {
        String request = mBaseUrl;
        request += "limit=" + limit + "&offset=" + offset;
        TwitchGameData t = new TwitchGameData(this);
        t.execute(request);
    }

    public void update(Game g) {
        mGames.add(g);
        notifyDataSetChanged();
    }

    public void update2(ArrayList<Game> g) {
        mGames.addAll(g);
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(convertView == null || convertView.getTag() == null) {
            convertView = inflater.inflate(R.layout.game_item_layout, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.game_desc);
            holder.viewers = (TextView) convertView.findViewById(R.id.game_viewers);
            holder.thumbImage = (ImageView) convertView.findViewById(R.id.game_thumbnail);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (convertView.getMeasuredWidth() > 0 && aspectRatio == 0 && holder.thumbImage.getDrawable() != null) {
            double height =  holder.thumbImage.getDrawable().getIntrinsicHeight();
            double width = holder.thumbImage.getDrawable().getIntrinsicWidth();
            aspectRatio = height / width;
            int conWidth = convertView.getMeasuredWidth();
            int conHeight = (int) Math.round(conWidth * aspectRatio);
            mRelativeLayout = new RelativeLayout.LayoutParams(conWidth,conHeight);
        }

        if (mRelativeLayout == null) {
            return convertView;
        }
        if (mRelativeLayout != null) {
            holder.thumbImage.setLayoutParams(mRelativeLayout);
        }

        if (mGames.get(position).mBitmapThumb == null) {
            holder.thumbImage.setImageResource(R.drawable.game_offline);
            loadImage(position, holder.thumbImage);
        } else {
            holder.thumbImage.setImageBitmap(mGames.get(position).mBitmapThumb);
        }


        String s = mGames.get(position).mTitle;
        holder.title.setText(mGames.get(position).mTitle);
        holder.viewers.setText(Integer.toString(mGames.get(position).mViewers));

        return convertView;
    }

    public void loadThumbnails(int start, int stop) {
        String urls[] = new String[ stop - start ];
        for (int i = 0; i < stop-start; i++) {
            urls[i] = mGames.get(start + i).mThumbnail;
        }
        TwitchBitmapData tb = new TwitchBitmapData(this, start);
        tb.execute(urls);
    }

    public void updateThumbnail(Bitmap bmp, int item, int offset) {
        mGames.get(item + offset).mBitmapThumb = bmp;
        notifyDataSetChanged();

    }

    public int getCount() {
        return mGames.size();
    }

    public Game getItem(int position) {
        return mGames.get(position);
    }

    public long getItemId(int position) {
        return mGames.get(position).mId;
    }

    public class ViewHolder {
        ImageView thumbImage;
        TextView title;
        TextView viewers;
    }

    private void loadImage(int pos, ImageView imageView) {
        final int fpos = pos;
        final ImageView fImg = imageView;
        Thread thread = new Thread(new Runnable() {
            public void run() {
                final Bitmap bitmap = TwitchNetworkTasks.downloadBitmap(mGames.get(fpos).mThumbnail);
                mContext.runOnUiThread(new Runnable() {
                    public void run() {
                        fImg.setImageBitmap(bitmap);
                        mGames.get(fpos).mBitmapThumb = bitmap;
                    }
                });
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
}