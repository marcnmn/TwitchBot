package twitchvod.tvvod;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import twitchvod.tvvod.data.primitives.Channel;
import twitchvod.tvvod.data.primitives.Stream;
import twitchvod.tvvod.data.primitives.Game;
import twitchvod.tvvod.ui_fragments.AuthFragment;
import twitchvod.tvvod.ui_fragments.ChannelDetailFragment;
import twitchvod.tvvod.ui_fragments.ChannelListFragment;
import twitchvod.tvvod.ui_fragments.StreamListFragment;
import twitchvod.tvvod.ui_fragments.GamesRasterFragment;
import twitchvod.tvvod.ui_fragments.NavigationDrawerFragment;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        GamesRasterFragment.OnGameSelectedListener, StreamListFragment.onStreamSelectedListener,
        ChannelDetailFragment.onStreamSelectedListener, ChannelListFragment.onChannelSelectedListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private GamesRasterFragment mGamesRasterFragment;
    private StreamListFragment mStreamListFragment;
    private ChannelListFragment mChannelListFragment;
    private ChannelDetailFragment mChannelDetailFragment;

    FragmentManager mFragmentManager;
    private String mUrls[];

    public static final String TAG = "MainActivity";
    private static final String TAG_STREAM_LIST_FRAGMENT = "stream_list_fragment";

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    public CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mFragmentManager = getFragmentManager();
        mUrls = getResources().getStringArray(R.array.drawer_urls);

        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        mStreamListFragment = (StreamListFragment) mFragmentManager.findFragmentByTag(TAG_STREAM_LIST_FRAGMENT);



        Intent auth = new Intent(Intent.ACTION_VIEW);
        auth.setDataAndType(Uri.parse("http://google.de/access_token=naz81royyx1xdwmi7vb6kyqnutqgt1&scope=chat_login"), "http/*");
        //startActivity(auth);

        mTitle = getTitle();
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentTransaction transaction;
        switch (position){
            case 0:
                mGamesRasterFragment = new GamesRasterFragment();
                transaction = mFragmentManager.beginTransaction();
                transaction.setTransition(transaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(R.id.container, mGamesRasterFragment.newInstance(position, mUrls[position]));
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case 1:
                mStreamListFragment = new StreamListFragment();
                transaction = mFragmentManager.beginTransaction();
                transaction.setTransition(transaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(R.id.container, mStreamListFragment.newInstance(position, mUrls[position]));
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case 2:
                mChannelListFragment = new ChannelListFragment();
                transaction = mFragmentManager.beginTransaction();
                transaction.setTransition(transaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(R.id.container, mChannelListFragment.newInstance(position, mUrls[position]));
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case 3:
                mChannelListFragment = new ChannelListFragment();
                transaction = mFragmentManager.beginTransaction();
                transaction.setTransition(transaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(R.id.container, mChannelListFragment.newInstance(position, mUrls[position]));
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case 4:
                AuthFragment a = new AuthFragment();
                transaction = mFragmentManager.beginTransaction();
                transaction.setTransition(transaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(R.id.container, a.newInstance());
                transaction.addToBackStack(null);
                transaction.commit();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
            restoreActionBar();
        } else {
            super.onBackPressed();
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 0:
                mTitle = getString(R.string.title_section1);
                break;
            case 1:
                mTitle = getString(R.string.title_section2);
                break;
            case 2:
                mTitle = getString(R.string.title_section3);
                break;
            case 3:
                mTitle = getString(R.string.title_section4);
                break;
            case 4:
                mTitle = "Auth";
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        //actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

        @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onGameSelected(Game g) {
        String url = getString(R.string.game_channels_url);
        url += g.toURL() + "&";
        mStreamListFragment = new StreamListFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, mStreamListFragment.newInstance(0, url), TAG_STREAM_LIST_FRAGMENT);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onStreamSelected(Stream g) {
        mChannelDetailFragment = new ChannelDetailFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, mChannelDetailFragment.newInstance(g, g.toHashMap()));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onStreamSelected(String s) {
    }

    @Override
    public void onChannelSelected(Channel c) {
        mChannelDetailFragment = new ChannelDetailFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, mChannelDetailFragment.newInstance(c.mData));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.v("ASDFasdf", intent.toString());
    }
}
