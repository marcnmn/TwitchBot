package twitchvod.tvvod.ui_fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.HashMap;

import twitchvod.tvvod.R;
import twitchvod.tvvod.adapter.PastBroadcastsListAdapter;
import twitchvod.tvvod.data.async_tasks.TwitchLiveStream;
import twitchvod.tvvod.data.async_tasks.TwitchPastBroadcastNew;
import twitchvod.tvvod.data.async_tasks.TwitchToken;
import twitchvod.tvvod.data.primitives.Channel;


/**
 * Created by marc on 27.01.2015. Gridview of available games
 */
public class ChannelDetailFragment extends Fragment {
    HashMap<String, String> mAvailableQualities;
    private int mLoadedItems, INT_LIST_UPDATE_VALUE, INT_LIST_UPDATE_THRESHOLD;
    onStreamSelectedListener mCallback;
    PastBroadcastsListAdapter mPastAdapter;
    private int INT_IS_STREAM = 0;
    private int INT_IS_BROADCAST = 1;
    Channel mChannel;
    Spinner mSpinner;
    private String mBroadcastsBaseUrl;

    public ChannelDetailFragment newInstance(Channel c, HashMap<String,String> h) {
        ChannelDetailFragment fragment = new ChannelDetailFragment();
        Bundle args = new Bundle();
        args.putString("title", c.mTitle);
        args.putSerializable("channel", h);
        fragment.setArguments(args);
        return fragment;
    }

    public interface onStreamSelectedListener {
        public void onStreamSelected(String s);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_channel_detail, container, false);
        mSpinner = (Spinner) rootView.findViewById(R.id.quality_spinner);
        VideoView video = (VideoView) rootView.findViewById(R.id.videoFeed);

        mChannel = new Channel((HashMap<String, String>) getArguments().getSerializable("channel"));
        mBroadcastsBaseUrl = getString(R.string.channel_past_broadcasts_url) + mChannel.titleToURL() + "/videos?broadcasts=true&";

        mLoadedItems = getResources().getInteger(R.integer.channel_list_start_items);
        INT_LIST_UPDATE_VALUE = getResources().getInteger(R.integer.channel_list_update_items);
        INT_LIST_UPDATE_THRESHOLD = getResources().getInteger(R.integer.channel_list_update_threshold);
        
        TextView text_title = (TextView) rootView.findViewById(R.id.channelTitel);
        TextView text_game = (TextView) rootView.findViewById(R.id.channelGame);
        TextView text_viewers = (TextView) rootView.findViewById(R.id.channelViewers);
        TextView text_status = (TextView) rootView.findViewById(R.id.channelStatus);

        ListView listView = (ListView) rootView.findViewById(R.id.list_past_broadcasts);
        mPastAdapter = new PastBroadcastsListAdapter(getActivity(), mBroadcastsBaseUrl);
        listView.setAdapter(mPastAdapter);

        text_title.setText(mChannel.mTitle);
        text_game.setText(mChannel.mGame);
        text_viewers.setText(String.valueOf(mChannel.mViewers));
        text_status.setText(mChannel.mStatus);

        //video.setVideoPath("http://video59.ams01.hls.twitch.tv/hls95/imaqtpie_13065546016_202388579/medium/py-index-live.m3u8?token=id=7279848897392817578,bid=13065546016,exp=1423576850,node=video59-1.ams01.hls.justin.tv,nname=video59.ams01,fmt=medium&sig=d92ded9f6bc81cc8cc4e6932c140a0657aef1443");
        //video.start();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(getActivity(), "" + position, Toast.LENGTH_SHORT).show();
                startBroadcast(mPastAdapter.getItemID(position));
            }
        });

        video.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                playStream(mAvailableQualities.get(mSpinner.getSelectedItem()));
                return false;
            }
        });

        mPastAdapter.loadTopData(10, 0);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisibleItem = firstVisibleItem + visibleItemCount;
                if (lastVisibleItem >= mLoadedItems - INT_LIST_UPDATE_THRESHOLD) {
                    mPastAdapter.loadTopData(INT_LIST_UPDATE_VALUE, mLoadedItems);
                    mLoadedItems += INT_LIST_UPDATE_VALUE;
                }
            }
        });

        fetchStreamToken(mChannel.titleToURL());
        return rootView;
    }

    private void fetchStreamToken(String s) {
       String tokenUrl = getString(R.string.stream_token_url) + s + "/access_token";
       new TwitchToken(this).execute(tokenUrl);
    }

    private void startBroadcast(String s) {
        String prefix = s.substring(0,1);
        String suffix = s.substring(1, s.length());
        switch (prefix) {
            case "v": fetchBroadcastTokenNew(suffix); break;
        }
    }

    private void fetchBroadcastTokenNew(String s) {
        String tokenUrl = getString(R.string.past_broadcast_token_url_new) + s + "/access_token";
        new TwitchToken(this, s).execute(tokenUrl);
    }

    public void fetchStreamM3U8Playlists(String tok, String sig) {
        String m3u8Url = "http://usher.twitch.tv/api/channel/hls/";
        m3u8Url += mChannel.titleToURL() + ".m3u8?player=twitchweb&token=";
        m3u8Url += tok + "&sig=" + sig;
        m3u8Url += "&allow_audio_only=true&allow_source=true&type=any&p=8732417";
        Log.v("asdfa", m3u8Url);
        new TwitchLiveStream(this).execute(m3u8Url);
    }

    public void fetchBroadcastM3U8PlaylistsNew(String token, String sig, String id) {
        String m3u8Url = "http://usher.twitch.tv/vod/" + id + "?nauth=";
        m3u8Url += token + "&nauthsig=" + sig;
        Log.v("tvvod", m3u8Url);
        new TwitchPastBroadcastNew(this).execute(m3u8Url);
    }


    public void updateStreamData(HashMap<String, String> hmap) {
        mAvailableQualities = hmap;

        ArrayList<String> q = qualHashmapToArrayList(hmap);
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, q);
        itemsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(itemsAdapter);
    }

    public void playStream(String s) {
        Intent stream = new Intent(Intent.ACTION_VIEW);
        stream.setDataAndType(Uri.parse(s), "video/*");
        startActivity(stream);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (onStreamSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    private ArrayList<String> qualHashmapToArrayList(HashMap<String, String> h) {
        ArrayList<String> l = new ArrayList<>();
        String [] keys = getResources().getStringArray(R.array.livestream_qualities);
        for (int i = 0; i < keys.length; i++) {
            if (h.containsKey(keys[i].replace(" ","_"))) l.add(keys[i]);
        }
        return l;
    }
}