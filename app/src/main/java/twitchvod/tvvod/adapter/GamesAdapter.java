package twitchvod.tvvod.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import twitchvod.tvvod.R;
import twitchvod.tvvod.data.primitives.Game;
import twitchvod.tvvod.data.async_tasks.TwitchBitmapData;
import twitchvod.tvvod.data.async_tasks.TwitchGameData;

public class GamesAdapter extends BaseAdapter {
    private String mBaseUrl;
    private Context mContext;
    private ArrayList<Game> mGames;
    private Animation fadeIn;
    private int mHeight = 0;
    private int mWidth = 0;
    private RelativeLayout.LayoutParams mRelativeLayout;
    private boolean mHeightOk;

    public GamesAdapter(Context c, String url) {
        mContext = c;
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
    }

    // create a new ImageView for each item referenced by the Adapter
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
            convertView.setTag(holder); // set the View holder
        }
        else
            holder = (ViewHolder) convertView.getTag();

        holder.title.setText(mGames.get(position).mTitle);
        holder.viewers.setText(Integer.toString(mGames.get(position).mViewers));

        if (mGames.get(position).mBitmapThumb == null) {
            holder.thumbImage.setImageResource(R.drawable.game_offline);
        } else {
            holder.thumbImage.setImageBitmap(mGames.get(position).mBitmapThumb);
        }

        if (convertView.getMeasuredWidth() > 0 && mHeight == 0) {
            double scale = 1.0 * mGames.get(position).mBitmapThumb.getHeight() / mGames.get(position).mBitmapThumb.getWidth();
            mWidth = convertView.getMeasuredWidth();
            mHeight = (int) Math.round(mWidth * scale);
            mRelativeLayout = new RelativeLayout.LayoutParams(mWidth,mHeight);
        }

        if (mRelativeLayout != null) {
            convertView.setLayoutParams(mRelativeLayout);
        }

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
}