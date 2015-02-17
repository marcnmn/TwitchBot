package twitchvod.src.ui_fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import twitchvod.src.R;
import twitchvod.src.adapter.ChannelListAdapter;
import twitchvod.src.data.async_tasks.TwitchJSONDataThread;
import twitchvod.src.data.async_tasks.TwitchJSONParserThread;
import twitchvod.src.data.primitives.Channel;


/**
 * Created by marc on 27.01.2015. Gridview of available games
 */
public class ChannelListFragment extends Fragment {
    private int mLoadedItems, INT_LIST_UPDATE_VALUE, INT_LIST_UPDATE_THRESHOLD;
    private ChannelListAdapter mChannelListAdapter;
    private onChannelSelectedListener mCallback;
    private ProgressBar mProgressBar;
    private String mUrl;
    private String mTitle;

    private ArrayList<Channel> mChannels;

    public ChannelListFragment newInstance(String url) {
        ChannelListFragment fragment = new ChannelListFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        args.putString("bar_title", "Favorites");
        fragment.setArguments(args);
        return fragment;
    }

    public ChannelListFragment newInstance(String url, String barTitle) {
        ChannelListFragment fragment = new ChannelListFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        args.putString("bar_title", barTitle);
        fragment.setArguments(args);
        return fragment;
    }

    public interface onChannelSelectedListener {
        public void onChannelSelected(Channel c);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_channel_list, container, false);
        GridView listView = (GridView) rootView.findViewById(R.id.channelTopList);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.channels_list_progress);

        if (savedInstanceState != null) {
            mUrl = savedInstanceState.getString("url");
            mTitle = savedInstanceState.getString("bar_title");
        } else {
            mUrl = getArguments().getString("url");
            mTitle = getArguments().getString("bar_title");
        }

        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(mTitle);

        mLoadedItems = getResources().getInteger(R.integer.channel_list_start_items);

        mChannelListAdapter = new ChannelListAdapter(this);
        listView.setAdapter(mChannelListAdapter);

        mLoadedItems = getResources().getInteger(R.integer.channel_list_start_items);
        INT_LIST_UPDATE_VALUE = getResources().getInteger(R.integer.channel_list_update_items);
        INT_LIST_UPDATE_THRESHOLD = getResources().getInteger(R.integer.channel_list_update_threshold);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(getActivity(), "" + position, Toast.LENGTH_SHORT).show();
                //fetchToken(mChannelListAdapter.getItem(position));
                mCallback.onChannelSelected(mChannelListAdapter.getItem(position));
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisibleItem = firstVisibleItem + visibleItemCount;
                if (lastVisibleItem >= mLoadedItems - INT_LIST_UPDATE_THRESHOLD) {
                    downloadChannelData(INT_LIST_UPDATE_VALUE, mLoadedItems);
                    mLoadedItems += INT_LIST_UPDATE_VALUE;
                }
            }
        });

        return rootView;
    }

    public void downloadChannelData(int limit, int offset) {
        String request = mUrl;
        request += "limit=" + limit + "&offset=" + offset;
        TwitchJSONDataThread t = new TwitchJSONDataThread(this);
        t.downloadJSONInBackground(request, Thread.NORM_PRIORITY);
    }

    public void dataReceived(String s) {
        TwitchJSONParserThread t = new TwitchJSONParserThread(this);
        t.parseJSONInBackground(s, Thread.NORM_PRIORITY);
    }

    public void dataParsed(ArrayList<Channel> l) {
        if (mChannels == null) {
            mChannels = l;
            mProgressBar.setVisibility(View.INVISIBLE);
        }
        else
            mChannels.addAll(l);

        mChannelListAdapter.update(l);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mChannels != null) {
            mLoadedItems = mChannels.size();
            mChannelListAdapter.resetDimensions();
            mChannelListAdapter.update(mChannels);
            mProgressBar.setVisibility(View.INVISIBLE);
        } else {
            mLoadedItems = getResources().getInteger(R.integer.game_grid_start_items);
            mChannelListAdapter.resetDimensions();
            downloadChannelData(mLoadedItems, 0);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", mUrl);
        outState.putString("bar_title", mTitle);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (onChannelSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnChannelSelectedListener");
        }
    }
}