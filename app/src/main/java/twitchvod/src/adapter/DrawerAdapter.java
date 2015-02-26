package twitchvod.src.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import twitchvod.src.R;
import twitchvod.src.data.TwitchNetworkTasks;
import twitchvod.src.data.primitives.Game;
import twitchvod.src.ui_fragments.GamesRasterFragment;
import twitchvod.src.ui_fragments.NavigationDrawerFragment;

public class DrawerAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<Game> mGames;
    private double aspectRatio;
    private RelativeLayout.LayoutParams mRelativeLayout;
    private Activity mActivity;
    private Context c;
    private AlphaAnimation mAlpha;
    private boolean heightSet = false;

    private String[] titles;
    private int[] drawables;
    private String[] footer;

    private ArrayList<Thread> mThreads = new ArrayList<>();


    public DrawerAdapter(Context c, String[] titles, int[] drawables, String[] footer) {
        this.titles = titles;
        this.drawables = drawables;
        this.footer = footer;
        mInflater = LayoutInflater.from(c);
        this.c = c;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.drawer_row_layout, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.drawerText);
            holder.thumbImage = (ImageView) convertView.findViewById(R.id.drawerIcon);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position < titles.length) {
            holder.title.setText(titles[position]);
            holder.thumbImage.setImageResource(drawables[position]);
        }

        if (position == titles.length){
            convertView = mInflater.inflate(R.layout.drawer_item, parent, false);
            return convertView;
        }

        if (position > titles.length){
            convertView = mInflater.inflate(R.layout.drawer_row_footer, parent, false);
            ((TextView) convertView.findViewById(R.id.drawerFooter)).setText(footer[position - titles.length - 1]);
            return convertView;
        }

        return convertView;
    }

    public int getCount() {
        return titles.length + 1 + footer.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 4;
    }

    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        ImageView thumbImage;
        TextView title;
    }
}