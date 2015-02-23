package twitchvod.src.ui_fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.transition.Transition;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import twitchvod.src.R;
import twitchvod.src.adapter.PastBroadcastsListAdapter2;
import twitchvod.src.data.TwitchJSONParser;
import twitchvod.src.data.TwitchNetworkTasks;
import twitchvod.src.data.async_tasks.TwitchBroadcastThread;
import twitchvod.src.data.async_tasks.TwitchJSONDataThread;
import twitchvod.src.data.async_tasks.TwitchLiveStreamThread;
import twitchvod.src.data.async_tasks.TwitchOldBroadcastThread;
import twitchvod.src.data.primitives.Channel;
import twitchvod.src.data.primitives.Stream;
import twitchvod.src.data.primitives.TwitchUser;
import twitchvod.src.data.primitives.TwitchVideo;
import twitchvod.src.data.primitives.TwitchVod;


/**
 * Created by marc on 27.01.2015. Gridview of available games
 */
public class ChannelDetailFragment extends Fragment {
    LinkedHashMap<String, String> mAvailableQualities;
    HashMap<String, String> mData;
    private int mLoadedItems, INT_LIST_UPDATE_VALUE, INT_LIST_UPDATE_THRESHOLD;
    onStreamSelectedListener mCallback;
    private ImageView mPlayOverlay;
    private ProgressBar mProgressBar;
    private ImageView mVideo;
    private View.OnTouchListener mTouchListener;
    private AdapterView.OnItemClickListener mVideoClicked;

    private ViewGroup mContainer;
    private int mQualitySelected;

    private RelativeLayout mStreamView, mOverlay;
    private Channel mChannel;
    private Stream mStream;
    private TwitchUser mUser;

    private static String USER_AUTH_TOKEN = "user_auth_token";
    private static String USER_IS_AUTHENTICATED = "user_is_authenticated";
    private static String SCOPES_OF_USER = "scopes_of_user";
    private String mUserToken, mUserScope;
    private boolean mIsAuthenticated;

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
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        mIsAuthenticated = sp.getBoolean(USER_IS_AUTHENTICATED, false);
        if (mIsAuthenticated) {
            mUserToken = sp.getString(USER_AUTH_TOKEN, "");
            mUserScope = sp.getString(SCOPES_OF_USER, "");
        }

        //mSpinner = (Spinner) rootView.findViewById(R.id.quality_spinner);
        mStreamView = (RelativeLayout) rootView.findViewById(R.id.stream_layout_top);
        mThumbnail = (ImageView) rootView.findViewById(R.id.videoFeed);

        mPlayOverlay = (ImageView) rootView.findViewById(R.id.imageOverlay);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.channel_detail_progress);

        //mLoadedItems = getResources().getInteger(R.integer.channel_list_start_items);
        mLoadedItems = 8;
        INT_LIST_UPDATE_VALUE = getResources().getInteger(R.integer.channel_list_update_items);
        INT_LIST_UPDATE_THRESHOLD = getResources().getInteger(R.integer.channel_list_update_threshold);

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mVideoList = (ListView) rootView.findViewById(R.id.expandableVideoList);
            mVideoListAdapter2 = new PastBroadcastsListAdapter2(this);

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
                if (bestPossibleQuality2(mAvailableQualities) >= 0) {
                    showPlayDialog(mAvailableQualities, bestPossibleQuality2(mAvailableQualities));
                }
                return false;
            }
        };

        return rootView;
    }


    //------------------ Channel Stuff -------------------------///////////////////
    private void downloadChannelData() {
        String request = getActivity().getResources().getString(R.string.channel_url);
        request += getArguments().getString("channel_name");
        if (mIsAuthenticated) request += "?oauth_token=" + mUserToken;
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

        mProgressBar.setVisibility(View.GONE);
        mStreamView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 0));

        if (mVideoList != null) {
            mVideoList.setOnItemClickListener(mVideoClicked);
            if (mVideoList.getHeaderViewsCount() == 0) {
                mVideoList.addHeaderView(mChannelHeader);
                mVideoList.setAdapter(mVideoListAdapter2);
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
        if (mIsAuthenticated) request += "?oauth=" + mUserToken;
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
        mProgressBar.setVisibility(View.GONE);
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
                mVideoList.setAdapter(mVideoListAdapter2);
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
        TwitchLiveStreamThread t;
        if (mIsAuthenticated) {
            t = new TwitchLiveStreamThread(this, mUserToken);
        } else {
            t = new TwitchLiveStreamThread(this);
        }
        t.downloadJSONInBackground(tokenUrl, getArguments().getString("channel_name"), 0, Thread.NORM_PRIORITY);
    }

    public void liveLinksReceived(LinkedHashMap<String, String> result) {
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
        if (v == null) {
            Toast.makeText(getActivity(), "Could not load Video", Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);
        String prefix = v.mId.substring(0,1);
        String suffix = v.mId.substring(1, v.mId.length());
        String request;
        TwitchBroadcastThread t = new TwitchBroadcastThread(this);
        TwitchOldBroadcastThread to = new TwitchOldBroadcastThread(this);
        if (mIsAuthenticated)
            t = new TwitchBroadcastThread(this, mUserToken);
        switch (prefix) {
            case "v":
                request = getString(R.string.twitch_video_token_url) + suffix + "/access_token";
                t.downloadJSONInBackground(request, suffix, 0, Thread.NORM_PRIORITY);
                break;
            case "a":
                request = "https://api.twitch.tv/api/videos/" + v.mId + "?as3=t";
                if (mIsAuthenticated)
                    request += "&oauth_token=" + mUserToken;
                to.downloadJSONInBackground(request, Thread.NORM_PRIORITY);
                Log.v("request: ", request);
                break;
            case "c":
                request = "https://api.twitch.tv/api/videos/" + v.mId + "?as3=t";
                if (mIsAuthenticated)
                    request += "&oauth_token=" + mUserToken;
                to.downloadJSONInBackground(request, Thread.NORM_PRIORITY);
                Log.v("request: ", request);
                break;
        }
    }

    public void videoPlaylistReceived(LinkedHashMap<String, String> result) {
        mProgressBar.setVisibility(View.INVISIBLE);
        if (bestPossibleQuality2(result) >= 0) {
            showPlayDialog(result, bestPossibleQuality2(result));
        } else {
            Toast.makeText(getActivity(), "Could not load Video, You need to subscribe to the channel.", Toast.LENGTH_SHORT).show();
        }
    }

    public void oldVideoPlaylistReceived(TwitchVod t) {
        mProgressBar.setVisibility(View.INVISIBLE);
        if (t.bestPossibleUrl() >= 0) {
            showOldVodFragment(t);
        } else {
            Toast.makeText(getActivity(), "Could not load Video, You need to subscribe to the channel.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showOldVodFragment(TwitchVod t) {
        VideoFragment videoFragment = new VideoFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.container, videoFragment.newInstance(t));
        transaction.addToBackStack(null);
        transaction.commit();
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
        if (mVideoListAdapter2 != null) mVideoListAdapter2.updateHighlights(mChannel.mHighlights);
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
        if (mVideoListAdapter2 != null) mVideoListAdapter2.updateBroadcasts(mChannel.mBroadcasts);
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
        super.onResume();
        ((ActionBarActivity) getActivity()).getSupportActionBar().hide();
        if(isInLandscape()) fullscreen();
        if (mStream != null && mUser != null) {
            updateLiveStreamLayout();
            if (mVideoListAdapter2 != null) {
                mVideoListAdapter2.clearAllData();
                mVideoListAdapter2.updateHighlights(mChannel.mHighlights);
                mVideoListAdapter2.updateBroadcasts(mChannel.mBroadcasts);
            }
            if (mAvailableQualities != null) {
                liveLinksReceived(mAvailableQualities);
            }
        } else if (mChannel != null && mUser != null) {
            if (mVideoListAdapter2 != null) {
                mVideoListAdapter2.clearAllData();
                mVideoListAdapter2.updateHighlights(mChannel.mHighlights);
                mVideoListAdapter2.updateBroadcasts(mChannel.mBroadcasts);
            }
            updateChannelLayout();
        } else {
            if (mVideoListAdapter2 != null) mVideoListAdapter2.clearAllData();
            downloadStreamData(getArguments().getString("channel_name"));
            downloadUserData(getArguments().getString("channel_name"));
            fetchStreamToken(getArguments().getString("channel_name"));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
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

    @Override
    public void onStop() {
        super.onStop();
        ((ActionBarActivity) getActivity()).getSupportActionBar().show();
        if(isInLandscape()) showUi();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (imageView != null)
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

    public int bestPossibleQuality2(HashMap<String, String> q) {
        final String qa[] = q.keySet().toArray(new String[q.size()]);
        int bestQ = -1;
        int bestI = -1;

        for (int i = 0; i < qa.length; i++) {
            if (qualityValue(qa[i]) > bestQ) {
                bestQ = qualityValue(qa[i]);
                bestI = i;
            }
        }
        return bestI;
    }

    private boolean isInLandscape() {
        return getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private void fullscreen() {
        int i = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                ;

        getActivity().getWindow().getDecorView().setSystemUiVisibility(i);
    }

    private void showUi() {
        View decorView = getActivity().getWindow().getDecorView();
        int i = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                ;

        if(decorView.getSystemUiVisibility() != i) {return;
        }
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    private void showPlayDialog(final LinkedHashMap<String, String> q, int best) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String qualities[] = q.keySet().toArray(new String[q.size()]);
        String cleanQualities[] = getCleanQualities(qualities);

        builder.setTitle("Select Quality")
                .setSingleChoiceItems(cleanQualities, best, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mQualitySelected = which;
                        Toast.makeText(getActivity(), "" + which, Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("Play", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getActivity(),""+ mQualitySelected,Toast.LENGTH_SHORT).show();
                        playStream(q.get(qualities[mQualitySelected]));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getActivity(),""+ mQualitySelected,Toast.LENGTH_SHORT).show();
                    }
                });
        builder.create();
        builder.show();
    }

    private void errorScreen() {

    }

    private int qualityValue(String s) {
        if (s.contains("240")) return 0;
        if (s.contains("360")) return 1;
        if (s.contains("480")) return 2;
        if (s.contains("720")) return 3;
        if (s.contains("live")) return 4;
        if (s.contains("source")) return 4;
        if (s.contains("chunked")) return 4;
        return -1;
    }

    private String[] getCleanQualities(String[] s) {
        String q[] = new String[s.length];

        for (int i = 0; i < s.length; i++) {
            if (s[i].contains("live")) {
                q[i] = "source";
                continue;
            }
            if (s[i].contains("chunked")){
                q[i] = "source";
                continue;
            }
            if (s[i].contains("audio_only")) {
                q[i] = "audio only";
                continue;
            }
            q[i] = s[i];
        }
        return q;
    }
}