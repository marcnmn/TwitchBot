package twitchvod.src.ui_fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Display;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import twitchvod.src.MainActivity;
import twitchvod.src.R;
import twitchvod.src.data.TwitchJSONParser;
import twitchvod.src.data.TwitchNetworkTasks;
import twitchvod.src.data.async_tasks.TwitchJSONDataThread;
import twitchvod.src.data.primitives.Channel;
import twitchvod.src.data.primitives.Game;
import twitchvod.src.data.primitives.Stream;


/**
 * Created by marc on 27.01.2015. Gridview of available games
 */
public class SearchFragment extends Fragment {
    private ProgressBar mProgressBar;
    private ArrayList<Channel> mChannels;
    private ArrayList<Stream> mStreams;
    private ArrayList<Game> mGames;
    private int resultsIn = 0;
    private GridLayout mGridLayout;
    private LinearLayout mResultLinearLayout;

    public SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.search_progress);
        mProgressBar.setVisibility(View.INVISIBLE);

        mGridLayout = (GridLayout) rootView.findViewById(R.id.game_results);
        mResultLinearLayout = (LinearLayout) rootView.findViewById(R.id.channel_results);

        ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        View v = inflater.inflate(R.layout.search_layout, null);
        SearchView searchView = (SearchView) v.findViewById(R.id.searchfield);
        styleSearchView(searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                answerQuery(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        actionBar.setCustomView(v);
        actionBar.setDisplayShowCustomEnabled(true);

        return rootView;
    }

    private void answerQuery(String s) {
        String channel_request = getActivity().getString(R.string.twitch_search_channels);
        String stream_request = getActivity().getString(R.string.twitch_search_streams);
        String game_request = getActivity().getString(R.string.twitch_search_games);

        try {
            s = URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }

        String queryLimit = "&limit=10";

        TwitchJSONDataThread channel_thread = new TwitchJSONDataThread(this, 0);
        channel_thread.downloadJSONInBackground(channel_request + "q=" + s + queryLimit, Thread.NORM_PRIORITY);
        TwitchJSONDataThread stream_thread = new TwitchJSONDataThread(this, 1);
        stream_thread.downloadJSONInBackground(stream_request + "q=" + s + queryLimit, Thread.NORM_PRIORITY);
        TwitchJSONDataThread game_thread = new TwitchJSONDataThread(this, 2);
        game_thread.downloadJSONInBackground(game_request + "q=" + s + queryLimit + "&type=suggest", Thread.NORM_PRIORITY);
    }

    public void channelDataReceived(String s) {
        mChannels = TwitchJSONParser.channelsJSONtoArrayList(s);
        updateList();
    }

    public void streamDataReceived(String s) {
        mStreams = TwitchJSONParser.streamJSONtoArrayList(s);
        updateList();
    }

    public void gameDataReceived(String s) {
        mGames = TwitchJSONParser.gameJSONtoArrayList(s);
        updateList();
    }

    public void updateList() {
        resultsIn++;
        if (resultsIn == 3) {
            updateLayout();
            resultsIn = 0;
        }
    }

    private void updateLayout() {
        mGridLayout.removeAllViews();
        mResultLinearLayout.removeAllViews();
        mResultLinearLayout.addView(mGridLayout);

        updateGamesGrid();
        updateChannelList();
    }

    private void updateGamesGrid() {
        if (mGames == null || mGames.size() == 0) return;

        int width = mGridLayout.getMeasuredWidth();

        ImageView img;
        TextView title;
        View game_item;
        for (int i = 0; i < 6; i++) {
            game_item = getActivity().getLayoutInflater().inflate(R.layout.game_item_layout, null);

            ((TextView)game_item.findViewById(R.id.game_desc)).setText(mGames.get(i).mTitle);
            game_item.findViewById(R.id.game_viewers).setVisibility(View.INVISIBLE);
            game_item.findViewById(R.id.viewers_icon).setVisibility(View.INVISIBLE);

            img = (ImageView)game_item.findViewById(R.id.game_thumbnail);
            loadImage(mGames.get(i).mThumbnail, img);
            game_item.setLayoutParams(new RelativeLayout.LayoutParams(width/3, width/2));
            mGridLayout.addView(game_item);
        }
    }

    private void updateChannelList() {
        if (mChannels == null) return;

        int width, height;
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        View channel_item;
        for (int i = 0; i < mChannels.size(); i++) {
            final int finalI = i;
            channel_item = getActivity().getLayoutInflater().inflate(R.layout.channel_item_layout, null);
            channel_item.setClickable(true);
            channel_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mChannels.get(finalI);
                    Log.v("asdf", mChannels.get(finalI).getDisplayName());
                }
            });

            ((TextView)channel_item.findViewById(R.id.firstLine)).setText(mChannels.get(i).getDisplayName());
            ((TextView)channel_item.findViewById(R.id.secondLineViewers)).setText(mChannels.get(i).getFollowers());
            ((TextView)channel_item.findViewById(R.id.secondLine)).setText(mChannels.get(i).getStatus());

            ImageView img = (ImageView) channel_item.findViewById(R.id.icon);
            loadImage(mChannels.get(i).getLogoLink(), img);
            img.setLayoutParams(new RelativeLayout.LayoutParams(width/3, width/3));
            mResultLinearLayout.addView(channel_item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowCustomEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
        super.onPause();
    }

    private void styleSearchView(SearchView searchView) {
        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchView.findViewById(searchPlateId);
        int searchTextId = searchPlate.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView searchText = (TextView) searchPlate.findViewById(searchTextId);
        searchText.setTextColor(Color.WHITE);
        searchText.setHintTextColor(Color.LTGRAY);
        searchPlate.setBackgroundResource(R.drawable.abc_textfield_search_activated_mtrl_alpha);
    }

    private void loadImage(final String url, final ImageView imageView) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                final Bitmap bitmap = TwitchNetworkTasks.downloadBitmap(url);
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (bitmap == null) return;
                        imageView.setImageBitmap(bitmap);
                    }
                });
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }


}