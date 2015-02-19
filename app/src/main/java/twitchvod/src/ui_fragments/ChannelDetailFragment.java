package twitchvod.src.ui_fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import twitchvod.src.R;
import twitchvod.src.adapter.PastBroadcastsListAdapter2;
import twitchvod.src.data.TwitchJSONParser;
import twitchvod.src.data.TwitchNetworkTasks;
import twitchvod.src.data.async_tasks.TwitchBroadcastThread;
import twitchvod.src.data.async_tasks.TwitchJSONDataThread;
import twitchvod.src.data.async_tasks.TwitchLiveStreamThread;
import twitchvod.src.data.primitives.Channel;
import twitchvod.src.data.primitives.Stream;
import twitchvod.src.data.primitives.TwitchUser;
import twitchvod.src.data.primitives.TwitchVideo;


/**
 * Created by marc on 27.01.2015. Gridview of available games
 */
public class ChannelDetailFragment extends Fragment {
    HashMap<String, String> mAvailableQualities;
    HashMap<String, String> mData;
    private int mLoadedItems, INT_LIST_UPDATE_VALUE, INT_LIST_UPDATE_THRESHOLD;
    onStreamSelectedListener mCallback;
    private ImageView mPlayOverlay;
    private ProgressBar mProgressBar;
    private ImageView mVideo;
    private View.OnTouchListener mTouchListener;
    private AdapterView.OnItemClickListener mVideoClicked;

    private RelativeLayout mStreamView, mOverlay;
    private Channel mChannel;
    private Stream mStream;
    private TwitchUser mUser;

    private ImageView mThumbnail, mChannelBanner;
    private TextView mStreamTitle, mStreamGameTitle, mStreamViewers, mStreamStatus;
    private String mToken, mSig;
    private ListView mVideoList;
    private PastBroadcastsListAdapter2 mVideoListAdapter2;
    private View mStreamHeader, mFooter;
    private View mChannelHeader;

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
        //mSpinner = (Spinner) rootView.findViewById(R.id.quality_spinner);
        mStreamView = (RelativeLayout) rootView.findViewById(R.id.stream_layout_top);
        mThumbnail = (ImageView) rootView.findViewById(R.id.videoFeed);

        mPlayOverlay = (ImageView) rootView.findViewById(R.id.imageOverlay);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.channel_detail_progress);

        //mLoadedItems = getResources().getInteger(R.integer.channel_list_start_items);
        mLoadedItems = 8;
        INT_LIST_UPDATE_VALUE = getResources().getInteger(R.integer.channel_list_update_items);
        INT_LIST_UPDATE_THRESHOLD = getResources().getInteger(R.integer.channel_list_update_threshold);

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

            rootView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mVideoList = (ListView) rootView.findViewById(R.id.expandableVideoList);
            mVideoListAdapter2 = new PastBroadcastsListAdapter2(this);
            mVideoList.setAdapter(mVideoListAdapter2);

            mChannelHeader = getActivity().getLayoutInflater().inflate(R.layout.channel_video_header, null);
            mStreamHeader = getActivity().getLayoutInflater().inflate(R.layout.stream_video_header, null);
            mFooter = getActivity().getLayoutInflater().inflate(R.layout.channel_video_footer, null);
        }

        mVideoClicked = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playSelectedVideo(mVideoListAdapter2.getItem(position));
            }
        };


        mTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //String s = (String) mSpinner.getSelectedItem();
                //s = s.replace(" ", "_");
                playStream(mAvailableQualities.get("source"));
                return false;
            }
        };

        return rootView;
    }


    //------------------ Channel Stuff -------------------------///////////////////
    private void downloadChannelData() {
        String request = getActivity().getResources().getString(R.string.channel_url);
        request += getArguments().getString("channel_name");
        TwitchJSONDataThread t = new TwitchJSONDataThread(this, 0);
        t.downloadJSONInBackground(request, Thread.NORM_PRIORITY);
    }

    public void channelDataReceived(String s) {
        mChannel = TwitchJSONParser.channelJSONtoChannel(s);
        if (mChannel == null) errorScreen();
        else {
            updateChannelLayout();
            downloadHighlightData(mLoadedItems, 0);
            downloadBroadcastData(mLoadedItems, 0);
        }
    }

    private void updateChannelLayout() {
        if (mChannel == null || mUser == null) return;

        mProgressBar.setVisibility(View.INVISIBLE);
        mStreamView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 0));

        if (mVideoList != null) {
            mVideoList.setOnItemClickListener(mVideoClicked);
            if (mVideoList.getHeaderViewsCount() == 0) {
                mVideoList.addHeaderView(mChannelHeader);
            }

            mChannelBanner = (ImageView) mChannelHeader.findViewById(R.id.channel_banner);
            loadLogo(mChannel.getLogoLink(), mChannelBanner);

            ((TextView) mChannelHeader.findViewById(R.id.textTitleView)).setText(mChannel.getDisplayName());
            ((TextView) mChannelHeader.findViewById(R.id.textBioView)).setText(mUser.getBio());
            ((TextView) mChannelHeader.findViewById(R.id.textViewsView)).setText(mChannel.getFollowers() + " Followers");
            mVideoList.setVisibility(View.VISIBLE);
        }
    }

    //------------------ Stream Stuff -------------------------///////////////////
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
            mChannel = mStream.mChannel;
            updateLiveStreamLayout();
            downloadHighlightData(mLoadedItems, 0);
            downloadBroadcastData(mLoadedItems, 0);
        }
    }

    private void updateLiveStreamLayout() {
        if (mStream == null || mUser == null) return;
        mProgressBar.setVisibility(View.INVISIBLE);
        mStreamView.setVisibility(View.VISIBLE);

        loadLogo(mStream.mPreviewLink, mThumbnail);
        TextView sTitle = (TextView) mStreamView.findViewById(R.id.channelTitel);
        sTitle.setText(mStream.mChannel.getDisplayName());
        TextView sGame = (TextView) mStreamView.findViewById(R.id.channelGame);
        sGame.setText("playing " + mStream.mChannel.getGame());
        TextView sViewers = (TextView) mStreamView.findViewById(R.id.channelViewers);
        sViewers.setText(String.valueOf(mStream.mViewers));
        ImageView viewers_icon = (ImageView)mStreamView.findViewById(R.id.viewers_icon);
        viewers_icon.setVisibility(View.VISIBLE);

        if (mVideoList != null) {
            mVideoList.setOnItemClickListener(mVideoClicked);
            if (mVideoList.getHeaderViewsCount() == 0) {
                mVideoList.addHeaderView(mStreamHeader);
            }

            mChannelBanner = (ImageView) mStreamHeader.findViewById(R.id.channel_banner);
            loadLogo(mChannel.getLogoLink(), mChannelBanner);

            ((TextView) mStreamHeader.findViewById(R.id.textTitleView)).setText(mChannel.getStatus());
            ((TextView) mStreamHeader.findViewById(R.id.textViewsView)).setText(mStream.mViewers + " Viewers");
            mVideoList.setVisibility(View.VISIBLE);
        }
    }

    //------------------ Livestream Stuff -------------------------/////////////////////////////////////////////
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

    //------------------ User Stuff -------------------------///////////////////
    private void downloadUserData(String name) {
        String request = getActivity().getResources().getString(R.string.twitch_user_url);
        request += name;
        TwitchJSONDataThread t = new TwitchJSONDataThread(this, 4);
        t.downloadJSONInBackground(request, Thread.NORM_PRIORITY);
    }

    public void userDataReceived(String s) {
        mUser = TwitchJSONParser.userDataToUser(s);
        if (mUser == null) {
            return;
        } else {
            updateLiveStreamLayout();
        }
    }

    //------------------ Video Stuff -------------------------///////////////////////////////////////////

    public void playSelectedVideo(TwitchVideo v) {
        String prefix = v.mId.substring(0,1);
        String suffix = v.mId.substring(1, v.mId.length());
        String request;
        TwitchBroadcastThread t = new TwitchBroadcastThread(this);
        switch (prefix) {
            case "v":
                request = getString(R.string.twitch_video_token_url) + suffix + "/access_token";
                t.downloadJSONInBackground(request, suffix, 0, Thread.NORM_PRIORITY);
                break;
        }
    }

    public void videoPlaylistReceived(HashMap<String, String> result) {
        if (bestPossibleQuality(result) != null) {
             playStream(result.get(bestPossibleQuality(result)));
        }
    }

    //------------------ Highlight Stuff -------------------------///////////////////////////////////////////

    public void downloadHighlightData(int limit, int offset) {
        String request = getString(R.string.channel_videos_url);
        request += getArguments().getString("channel_name") + "/videos?";
        request += "limit=" + limit + "&offset=" + offset;
        TwitchJSONDataThread t = new TwitchJSONDataThread(this, 2);
        t.downloadJSONInBackground(request, Thread.NORM_PRIORITY);
    }

    public void highlightDataReceived(String s) {
        mChannel.mHighlights = TwitchJSONParser.dataToVideoList(s);
        mVideoListAdapter2.updateHighlights(mChannel.mHighlights);
    }

    //------------------ PastBroadcast Stuff -------------------------/////////////////////////////////////////////

    public void downloadBroadcastData(int limit, int offset) {
        String request = getString(R.string.channel_videos_url);
        request += getArguments().getString("channel_name") + "/videos?";
        request += getString(R.string.channel_broadcasts_url_appendix);
        request += "limit=" + limit + "&offset=" + offset;
        TwitchJSONDataThread t = new TwitchJSONDataThread(this, 3);
        t.downloadJSONInBackground(request, Thread.NORM_PRIORITY);
    }

    public void broadcastDataReceived(String s) {
        mChannel.mBroadcasts = TwitchJSONParser.dataToVideoList(s);
        mVideoListAdapter2.updateBroadcasts(mChannel.mBroadcasts);
    }

    public void playStream(String s) {
        Intent stream = new Intent(Intent.ACTION_VIEW);
        stream.setDataAndType(Uri.parse(s), "video/*");
        startActivity(stream);
    }

    public void updateThumbnail(Bitmap bitmap) {
        mVideo.setImageBitmap(bitmap);
    }

    @Override
    public void onResume() {
       // ((ActionBarActivity) getActivity()).getSupportActionBar().hide();
        if (mStream != null && mUser != null) {
            updateLiveStreamLayout();
            mVideoListAdapter2.clearAllData();
            mVideoListAdapter2.updateHighlights(mChannel.mHighlights);
            mVideoListAdapter2.updateBroadcasts(mChannel.mBroadcasts);
            if (mAvailableQualities != null) {
                liveLinksReceived(mAvailableQualities);
            }
        } else if (mChannel != null && mUser != null) {
            updateChannelLayout();
            mVideoListAdapter2.clearAllData();
            mVideoListAdapter2.updateHighlights(mChannel.mHighlights);
            mVideoListAdapter2.updateBroadcasts(mChannel.mBroadcasts);
        } else {
            mVideoListAdapter2.clearAllData();
            downloadStreamData(getArguments().getString("channel_name"));
            downloadUserData(getArguments().getString("channel_name"));
            fetchStreamToken(getArguments().getString("channel_name"));
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //((ActionBarActivity) getActivity()).getSupportActionBar().show();
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

    private void loadLogo(final String url, final ImageView imageView) {
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

    public String bestPossibleQuality(HashMap<String, String> qualities) {
        if (qualities.containsKey("source")) return "source";
        if (qualities.containsKey("high")) return "high";
        if (qualities.containsKey("medium")) return "medium";
        if (qualities.containsKey("low")) return "low";
        if (qualities.containsKey("mobile")) return "mobile";
        if (qualities.containsKey("audio_only")) return "audio_only";
        return null;
    }

    private void errorScreen() {

    }
}