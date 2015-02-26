package twitchvod.src.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import twitchvod.src.R;
import twitchvod.src.data.primitives.Channel;
import twitchvod.src.data.primitives.TwitchVod;
import twitchvod.src.ui_fragments.VideoFragment;

public class OldVideoListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<String> mLengths;

    public OldVideoListAdapter(VideoFragment v, ArrayList<String> l) {
        mLengths = l;
        mInflater = LayoutInflater.from(v.getActivity());
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if(convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.old_video_row, parent, false);
            holder = new ViewHolder();
            holder.part = (TextView) convertView.findViewById(R.id.part);
            holder.length = (TextView) convertView.findViewById(R.id.length);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.part.setText("Part " + (position+1) + " of " + mLengths.size());
        holder.length.setText(secondsInMinutes(mLengths.get(position)));

        return convertView;
    }

    public int getCount() {
        return mLengths.size();
    }

    public Integer getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public class ViewHolder {
        public TextView part;
        public TextView length;
    }

    private String secondsInMinutes(String s) {
        int sec = Integer.valueOf(s);

        int min = (int) ((1.0 * sec / 60) % 60);
        sec = sec % 60;

        return "Length " + min + ": " + sec + " min";
    }
}