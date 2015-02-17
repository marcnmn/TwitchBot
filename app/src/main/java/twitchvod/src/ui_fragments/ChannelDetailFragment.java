package twitchvod.src.ui_fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

import twitchvod.src.R;
import twitchvod.src.adapter.PastBroadcastsListAdapter;
import twitchvod.src.data.TwitchJSONParser;
import twitchvod.src.data.TwitchNetworkTasks;
import twitchvod.src.data.async_tasks.TwitchBitmapData;
import twitchvod.src.data.async_tasks.TwitchBitmapThread;
import twitchvod.src.data.async_tasks.TwitchJSONDataThread;
import twitchvod.src.data.async_tasks.TwitchLiveStream;
import twitchvod.src.data.async_tasks.TwitchLiveStreamThread;
import twitchvod.src.data.async_tasks.TwitchPastBroadcastNew;
import twitchvod.src.data.async_tasks.TwitchToken;
import twitchvod.src.data.primitives.Channel;
import twitchvod.src.data.primitives.Stream;


/**
 * Created by marc on 27.01.2015. Gridview of available games
 */
public class ChannelDetailFragment extends Fragment {
    HashMap<String, String> mAvailableQualities;
    HashMap<String, String> mData;
    private int mLoadedItems, INT_LIST_UPDATE_VALUE, INT_LIST_UPDATE_THRESHOLD;
    onStreamSelectedListener mCallback;
    PastBroadcastsListAdapter mPastAdapter;
    private Spinner mSpinner;
    private ImageView mPlayOverlay;
    private ProgressBar mProgressBar;
    private ImageView mVideo;
    private View.OnTouchListener mTouchListener;

    private ViewGroup mContainer;
    private RelativeLayout mStreamView;
    private Channel mChannel;
    private Stream mStream;

    private boolean mIsStreaming;
    private ImageView mThumbnail;
    private TextView mStreamTitle, mStreamGameTitle, mStreamViewers, mStreamStatus;
    private String mToken, mSig;

    public ChannelDetailFragment newInstance(HashMap<String,String> h) {
        ChannelDetailFragment fragment = new ChannelDetailFragment();
        Bundle args = new Bundle();
        args.putString("title", h.get("display_name"));
        args.putSerializable("data", h);
        fragment.setArguments(args);
        return fragment;
    }

    public ChannelDetailFragment newInstance(String c) {
        ChannelDetailFragment fragment = new ChannelDetailFragment();
        Bundle args = new Bundle();
        args.putString("channel_name", c);
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
        mContainer = container;
        //mSpinner = (Spinner) rootView.findViewById(R.id.quality_spinner);
        mStreamView = (RelativeLayout) rootView.findViewById(R.id.stream_layout_top);
        mThumbnail = (ImageView) rootView.findViewById(R.id.videoFeed);

        mPlayOverlay = (ImageView) rootView.findViewById(R.id.imageOverlay);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.channel_detail_progress);
        ((ActionBarActivity) getActivity()).getSupportActionBar().hide();

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rootView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

//        String mBroadcastsBaseUrl = getString(R.string.channel_past_broadcasts_url) + mData.get("name") + "/videos?broadcasts=true&";

//        mLoadedItems = getResources().getInteger(R.integer.channel_list_start_items);
//        INT_LIST_UPDATE_VALUE = getResources().getInteger(R.integer.channel_list_update_items);
//        INT_LIST_UPDATE_THRESHOLD = getResources().getInteger(R.integer.channel_list_update_threshold);


//        GridView listView = (GridView) rootView.findViewById(R.id.list_past_broadcasts);
//        mPastAdapter = new PastBroadcastsListAdapter(getActivity(), mBroadcastsBaseUrl);
//        listView.setAdapter(mPastAdapter);
//
//        text_title.setText(mData.get("display_name"));
//        text_game.setText("playing " + mData.get("game"));
//        text_viewers.setText(mData.get("views"));
//        text_status.setText("Status: \n" + mData.get("status"));

        //video.setVideoPath("http://video59.ams01.hls.twitch.tv/hls95/imaqtpie_13065546016_202388579/medium/py-index-live.m3u8?token=id=7279848897392817578,bid=13065546016,exp=1423576850,node=video59-1.ams01.hls.justin.tv,nname=video59.ams01,fmt=medium&sig=d92ded9f6bc81cc8cc4e6932c140a0657aef1443");
        //video.start();

//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                Toast.makeText(getActivity(), "" + position, Toast.LENGTH_SHORT).show();
//                startBroadcast(mPastAdapter.getItemID(position));
//            }
//        });
//
        mTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //String s = (String) mSpinner.getSelectedItem();
                //s = s.replace(" ", "_");
                playStream(mAvailableQualities.get("source"));
                return false;
            }
        };

//        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                int lastVisibleItem = firstVisibleItem + visibleItemCount;
//                if (lastVisibleItem >= mLoadedItems - INT_LIST_UPDATE_THRESHOLD) {
//                    mPastAdapter.loadTopData(INT_LIST_UPDATE_VALUE, mLoadedItems);
//                    mLoadedItems += INT_LIST_UPDATE_VALUE;
//                }
//            }
//        });

        return rootView;
    }


    //------------------Channel Stuff -------------------------///////////////////
    private void downloadChannelData() {
        String request = getActivity().getResources().getString(R.string.channel_url);
        request += getArguments().getString("channel_name");
        TwitchJSONDataThread t = new TwitchJSONDataThread(this, 0);
        t.downloadJSONInBackground(request, Thread.NORM_PRIORITY);
    }

    public void channelDataReceived(String s) {
        mChannel = TwitchJSONParser.channelJSONtoChannel(s);
        if (mChannel == null) errorScreen();
        else
            updateChannelLayout();
    }

    private void updateChannelLayout() {
        mProgressBar.setVisibility(View.INVISIBLE);
        mStreamView.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
    }

    //------------------Stream Stuff -------------------------///////////////////
    private void downloadStreamData(String name) {
        String request = getActivity().getResources().getString(R.string.channel_stream_url);
        request += name;
        TwitchJSONDataThread t = new TwitchJSONDataThread(this, 1);
        t.downloadJSONInBackground(request, Thread.NORM_PRIORITY);
    }

    public void streamDataReceived(String s) {
        mStream = TwitchJSONParser.streamJSONtoStream(s);
        if (mStream == null) {
            downloadChannelData();
        } else {
            mIsStreaming = true;
            updateLiveStreamLayout();
        }
    }

    private void updateLiveStreamLayout() {
        mProgressBar.setVisibility(View.INVISIBLE);

        loadImage(mStream.mPreviewLink, mThumbnail);
        TextView sTitle = (TextView) mStreamView.findViewById(R.id.channelTitel);
        sTitle.setText(mStream.mChannel.getDisplayName());
        TextView sGame = (TextView) mStreamView.findViewById(R.id.channelGame);
        sGame.setText("playing " + mStream.mChannel.getGame());
        TextView sViewers = (TextView) mStreamView.findViewById(R.id.channelViewers);
        sViewers.setText(String.valueOf(mStream.mViewers));
        ImageView viewers_icon = (ImageView)mStreamView.findViewById(R.id.viewers_icon);
        viewers_icon.setVisibility(View.VISIBLE);
    }

    //------------------Livestream Stuff -------------------------///////////////////
    private void fetchStreamToken(String s) {
        String tokenUrl = getString(R.string.stream_token_url) + s + "/access_token";
        TwitchLiveStreamThread t = new TwitchLiveStreamThread(this);
        t.downloadJSONInBackground(tokenUrl, getArguments().getString("channel_name"), 0, Thread.NORM_PRIORITY);
    }

    public void liveLinksReceived(HashMap<String, String> result) {
        mAvailableQualities = result;
        mPlayOverlay.setImageResource(R.drawable.play_logo);
        mThumbnail.setOnTouchListener(mTouchListener);
    }

    public void tokenDataReceived(String s) {
        JSONObject jObject = null;
        try {
            jObject = new JSONObject(s);
            mToken = jObject.getString("token");
            mSig = jObject.getString("sig");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void fetchStreamM3U8Playlists(String tok, String sig) {
        String m3u8Url = "http://usher.twitch.tv/api/channel/hls/";
        m3u8Url += mData.get("name") + ".m3u8?player=twitchweb&token=";
        m3u8Url += tok + "&sig=" + sig;
        m3u8Url += "&allow_audio_only=true&allow_source=true&type=any&p=8732417";
        Log.v("asdfa", m3u8Url);

        TwitchJSONDataThread tokenData = new TwitchJSONDataThread(this, 3);
        tokenData.downloadJSONInBackground(m3u8Url, Thread.NORM_PRIORITY);
    }

    public void livePlaylistReceived(String s) {
        //TwitchJSONDataThread tokenData = new TwitchJSONDataThread(this, 3);
        //tokenData.downloadJSONInBackground(m3u8Url, Thread.NORM_PRIORITY);
    }





    public void broadcastDataReceived(String s) {
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

    public void fetchBroadcastM3U8PlaylistsNew(String token, String sig, String id) {
        String m3u8Url = "http://usher.twitch.tv/vod/" + id + "?nauth=";
        m3u8Url += token + "&nauthsig=" + sig;
        Log.v("tvvod", m3u8Url);
        new TwitchPastBroadcastNew(this).execute(m3u8Url);
    }


    public void updateStreamData(HashMap<String, String> hmap) {
        mProgressBar.setProgress(4);
        mAvailableQualities = hmap;
        ArrayList<String> q = qualHashmapToArrayList(hmap);
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, q);
        itemsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(itemsAdapter);

        if (mAvailableQualities.size() > 0) {
            mPlayOverlay.setImageResource(R.drawable.play_logo);
            mVideo.setOnTouchListener(mTouchListener);
        } else {
            mPlayOverlay.setImageResource(R.drawable.no_livestream);
        }
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    public void playStream(String s) {
        Intent stream = new Intent(Intent.ACTION_VIEW);
        stream.setDataAndType(Uri.parse(s), "video/*");
        startActivity(stream);
    }

    public void playBroadcast(HashMap<String, String> h) {
        Intent stream = new Intent(Intent.ACTION_VIEW);
        String sUrl;

        if (h.containsKey(mSpinner.getSelectedItem())) {
            sUrl = h.get(mSpinner.getSelectedItem());
        }
        else {
            sUrl = h.get("source");
            if (sUrl == null) sUrl = h.get("source");
            if (sUrl == null) sUrl = h.get("high");
            if (sUrl == null) sUrl = h.get("medium");
        }

        stream.setDataAndType(Uri.parse(sUrl), "video/*");
        startActivity(stream);
    }

    public void fetchThumbnail() {
        TwitchBitmapData t = new TwitchBitmapData(this);
        TwitchBitmapThread tt = new TwitchBitmapThread(this);
        if (mData.get("previewLink") != null) {
            tt.downloadImageInBackground(mData.get("previewLink"), Thread.NORM_PRIORITY);
            tt.stopThread();
        } else if (mData.get("video_banner") != null) {
            tt.downloadImageInBackground(mData.get("previewLink"), Thread.NORM_PRIORITY);
            tt.stopThread();
        }
    }

    public void updateThumbnail(Bitmap bitmap) {
        mVideo.setImageBitmap(bitmap);
    }

    @Override
    public void onResume() {
        if (mStream != null) {
            updateLiveStreamLayout();
            if (mAvailableQualities != null) {
                liveLinksReceived(mAvailableQualities);
            }
        } else {
            downloadStreamData(getArguments().getString("channel_name"));
            fetchStreamToken(getArguments().getString("channel_name"));
        }
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("data", mData);
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ((ActionBarActivity) getActivity()).getSupportActionBar().show();
        mCallback = null;
    }

    private ArrayList<String> qualHashmapToArrayList(HashMap<String, String> h) {
        ArrayList<String> l = new ArrayList<>();
        String[] keys = getResources().getStringArray(R.array.livestream_qualities);
        for (int i = 0; i < keys.length; i++) {
            if (h.containsKey(keys[i].replace(" ", "_"))) l.add(keys[i]);
        }
        return l;
    }

    private void loadImage(final String url, final ImageView imageView) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                final Bitmap bitmap = TwitchNetworkTasks.downloadBitmap(url);
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                    }
                });
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    private void errorScreen() {

    }
}