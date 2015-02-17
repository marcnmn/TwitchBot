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
import twitchvod.src.data.primitives.Game;
import twitchvod.src.ui_fragments.GamesRasterFragment;

public class GamesAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<Game> mGames;
    private double aspectRatio;
    private RelativeLayout.LayoutParams mRelativeLayout;
    private Activity mActivity;


    public GamesAdapter(GamesRasterFragment c) {
        if (mGames == null) mGames = new ArrayList<>();
        mActivity = c.getActivity();
        mInflater = LayoutInflater.from(c.getActivity());
        mGames = new ArrayList<>();
    }

    public void update(ArrayList<Game> g) {
        mGames.addAll(g);
        notifyDataSetChanged();
    }

    public void cleanData() {
        mGames.clear();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.game_item_layout, parent, false);
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

        Game tempGame = mGames.get(position);

        if (tempGame.mBitmapThumb != null) {
            holder.thumbImage.setImageBitmap(tempGame.mBitmapThumb);
        } else {
            holder.thumbImage.setImageResource(R.drawable.game_offline);
            loadImage(position, holder.thumbImage);
        }

        if (mRelativeLayout != null)
            holder.thumbImage.setLayoutParams(mRelativeLayout);

        holder.title.setText(tempGame.mTitle);
        holder.viewers.setText(Integer.toString(tempGame.mViewers));

        return convertView;
    }

    public int getCount() {
        return mGames.size();
    }

    public Game getItem(int position) {
        return mGames.get(position);
    }

    public long getItemId(int position) {
        return Long.valueOf(mGames.get(position).mId);
    }

    public class ViewHolder {
        ImageView thumbImage;
        TextView title;
        TextView viewers;
    }

    private void loadImage(int pos, ImageView imageView) {
        final int fPos = pos;
        final ImageView fImg = imageView;
        Thread thread = new Thread(new Runnable() {
            public void run() {
                final Bitmap bitmap;
                bitmap = TwitchNetworkTasks.downloadBitmap(mGames.get(fPos).mThumbnail);
//                storeImage(bitmap, mGames.get(fPos).mId);
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                    fImg.setImageBitmap(bitmap);
                    mGames.get(fPos).mBitmapThumb = bitmap;
                    }
                });
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
}