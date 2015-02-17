package twitchvod.src;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import twitchvod.src.data.primitives.Channel;
import twitchvod.src.data.primitives.Stream;
import twitchvod.src.data.primitives.Game;
import twitchvod.src.ui_fragments.AuthFragment;
import twitchvod.src.ui_fragments.ChannelDetailFragment;
import twitchvod.src.ui_fragments.ChannelListFragment;
import twitchvod.src.ui_fragments.SearchFragment;
import twitchvod.src.ui_fragments.StreamListFragment;
import twitchvod.src.ui_fragments.GamesRasterFragment;
import twitchvod.src.ui_fragments.NavigationDrawerFragment;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        GamesRasterFragment.OnGameSelectedListener, StreamListFragment.onStreamSelectedListener,
        ChannelDetailFragment.onStreamSelectedListener, ChannelListFragment.onChannelSelectedListener {

    private static final String ARG_ACTIONBAR_TITLE = "action_bar";
    private String mUrls[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUrls = getResources().getStringArray(R.array.drawer_urls);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        NavigationDrawerFragment mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentTransaction transaction;
        switch (position){
            case 0:
                GamesRasterFragment mGamesRasterFragment = new GamesRasterFragment();
                transaction = getFragmentManager().beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(R.id.container, mGamesRasterFragment.newInstance(mUrls[position]));
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case 1:
                StreamListFragment mStreamListFragment = new StreamListFragment();
                transaction = getFragmentManager().beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(R.id.container, mStreamListFragment.newInstance(mUrls[position], null));
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case 2:
//                ChannelListFragment searchFragment = new ChannelListFragment();
                SearchFragment searchFragment = new SearchFragment();
                transaction = getFragmentManager().beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(R.id.container, searchFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case 3:
                ChannelListFragment favoritesFragment = new ChannelListFragment();
                transaction = getFragmentManager().beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(R.id.container, favoritesFragment.newInstance(mUrls[position]));
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case 4:
                AuthFragment a = new AuthFragment();
                transaction = getFragmentManager().beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(R.id.container, a.newInstance());
                transaction.addToBackStack(null);
                transaction.commit();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 1) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        savedInstanceState.putString(ARG_ACTIONBAR_TITLE, (String) actionBar.getTitle());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onGameSelected(Game g) {
        String url = getString(R.string.game_streams_url);
        url += g.toURL() + "&";
        StreamListFragment mStreamListFragment = new StreamListFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.container, mStreamListFragment.newInstance(url, g.mTitle));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onStreamSelected(Stream g) {
        ChannelDetailFragment mChannelDetailFragment = new ChannelDetailFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.container, mChannelDetailFragment.newInstance(g.titleToURL()));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onStreamSelected(String s) {
    }

    @Override
    public void onChannelSelected(Channel c) {
        ChannelDetailFragment mChannelDetailFragment = new ChannelDetailFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.container, mChannelDetailFragment.newInstance(c.getName()));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.v("ASDFasdf", intent.toString());
    }

}
