package twitchvod.tvvod.ui_fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import twitchvod.tvvod.MainActivity;
import twitchvod.tvvod.R;
import twitchvod.tvvod.adapter.ChannelListAdapter;
import twitchvod.tvvod.adapter.StreamListAdapter;
import twitchvod.tvvod.data.async_tasks.TwitchChannelData;
import twitchvod.tvvod.data.primitives.Channel;
import twitchvod.tvvod.data.primitives.Stream;


/**
 * Created by marc on 27.01.2015. Gridview of available games
 */
public class ChannelListFragment extends Fragment implements ChannelListAdapter.onFirstResultsListener{
    private static final String ARG_SECTION_NUMBER = "section_number";
    private int mLoadedItems, INT_LIST_UPDATE_VALUE, INT_LIST_UPDATE_THRESHOLD;
    private ChannelListAdapter mAdapter;
    private onChannelSelectedListener mCallback;
    private ProgressBar mProgressBar;

    public ChannelListFragment newInstance(int sectionNumber, String url) {
        ChannelListFragment fragment = new ChannelListFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public interface onChannelSelectedListener {
        public void onChannelSelected(Channel c);
    }

    @Override
    public void onFirstResults() {
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_channel_list, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.channelTopList);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.channels_list_progress);

        mAdapter = new ChannelListAdapter(this, getArguments().getString("url"));
        listView.setAdapter(mAdapter);

        mLoadedItems = getResources().getInteger(R.integer.channel_list_start_items);
        INT_LIST_UPDATE_VALUE = getResources().getInteger(R.integer.channel_list_update_items);
        INT_LIST_UPDATE_THRESHOLD = getResources().getInteger(R.integer.channel_list_update_threshold);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(getActivity(), "" + position, Toast.LENGTH_SHORT).show();
                //fetchToken(mAdapter.getItem(position));
                mCallback.onChannelSelected(mAdapter.getItem(position));
            }
        });

        mAdapter.loadTopData(mLoadedItems, 0);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisibleItem = firstVisibleItem + visibleItemCount;
                if (lastVisibleItem >= mLoadedItems - INT_LIST_UPDATE_THRESHOLD) {
                    mAdapter.loadTopData(INT_LIST_UPDATE_VALUE, mLoadedItems);
                    mLoadedItems += INT_LIST_UPDATE_VALUE;
                }
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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