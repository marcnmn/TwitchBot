package twitchvod.tvvod.ui_fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import twitchvod.tvvod.MainActivity;
import twitchvod.tvvod.R;
import twitchvod.tvvod.adapter.GamesAdapter;
import twitchvod.tvvod.data.primitives.Game;

public class GamesRasterFragment extends Fragment
{
    private static final String ARG_SECTION_NUMBER = "section_number";
    private Game mSelectedItem;
    private Animation fadeIn;

    OnGameSelectedListener  mCallback;
    private int mLoadedItems, INT_GRID_UPDATE_VALUE, INT_GRID_UPDATE_THRESHOLD;

    public GamesRasterFragment newInstance(int sectionNumber, String url) {
        GamesRasterFragment fragment = new GamesRasterFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString("url", url);
        fragment.setArguments(args);

        return fragment;
    }

    public interface OnGameSelectedListener {
        public void onGameSelected(Game g);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_games, container, false);
        GridView gridview = (GridView) rootView.findViewById(R.id.gridView);
        String mBaseUrl = getArguments().getString("url");

        mLoadedItems = getResources().getInteger(R.integer.game_grid_start_items);
        INT_GRID_UPDATE_VALUE = getResources().getInteger(R.integer.game_grid_update_items);
        INT_GRID_UPDATE_THRESHOLD = getResources().getInteger(R.integer.game_grid_update_threshold);

        final GamesAdapter mGAdapter2 = new GamesAdapter(getActivity(), mBaseUrl);
        gridview.setAdapter(mGAdapter2);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
                mSelectedItem = mGAdapter2.getItem(position);
                actionBar.setTitle(mSelectedItem.mTitle);
                mCallback.onGameSelected(mSelectedItem);
            }
        });

        gridview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisibleItem = firstVisibleItem + visibleItemCount;
                if (lastVisibleItem >= mLoadedItems - INT_GRID_UPDATE_THRESHOLD) {
                    mGAdapter2.loadTopData(INT_GRID_UPDATE_VALUE, mLoadedItems);
                    mLoadedItems += INT_GRID_UPDATE_VALUE;
                }
            }
        });

        mGAdapter2.loadTopData(mLoadedItems, 0);
        return rootView;
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
        try {
            mCallback = (OnGameSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
}