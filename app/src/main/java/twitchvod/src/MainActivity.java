package twitchvod.src;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import twitchvod.src.data.TwitchJSONParser;
import twitchvod.src.data.primitives.Channel;
import twitchvod.src.data.primitives.Stream;
import twitchvod.src.data.primitives.Game;
import twitchvod.src.ui_fragments.ChannelDetailFragment;
import twitchvod.src.ui_fragments.ChannelListFragment;
import twitchvod.src.ui_fragments.SearchFragment;
import twitchvod.src.ui_fragments.SettingsFragment;
import twitchvod.src.ui_fragments.SetupFragment;
import twitchvod.src.ui_fragments.StreamListFragment;
import twitchvod.src.ui_fragments.GamesRasterFragment;
import twitchvod.src.ui_fragments.NavigationDrawerFragment;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        GamesRasterFragment.OnGameSelectedListener, StreamListFragment.onStreamSelectedListener,
        ChannelListFragment.onChannelSelectedListener {

    private static final String PREF_USER_COMPLETED_SETUP = "user_completed_setup";
    private static String USER_AUTH_TOKEN = "user_auth_token";
    private static String USER_IS_AUTHENTICATED = "user_is_authenticated";
    private static String SCOPES_OF_USER = "scopes_of_user";
    private static String USER_HAS_TWITCH_USERNAME = "user_has_twitch_username";
    private static String TWITCH_USERNAME = "twitch_username";
    private static String TWITCH_DISPLAY_USERNAME = "twitch_display_username";
    private static String TWITCH_STREAM_QUALITY_TYPE = "settings_stream_quality_type";
    private static String TWITCH_PREFERRED_VIDEO_QUALITY = "settings_preferred_video_quality";
    private static String TWITCH_BITMAP_QUALITY = "settings_bitmap_quality";

    private static final String ARG_ACTIONBAR_TITLE = "action_bar";
    private String mUrls[];
    private AdView mAdView;
    private boolean mIsInSetup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUrls = getResources().getStringArray(R.array.drawer_urls);
        setBitmapQuality();
        setContentView(R.layout.activity_main);

        //Picasso.with(this).setIndicatorsEnabled(true);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        //mAdView.loadAd(adRequest);

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
                transaction.addToBackStack("GamesRaster");
                transaction.commit();
                break;
            case 1:
                StreamListFragment mStreamListFragment = new StreamListFragment();
                transaction = getFragmentManager().beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(R.id.container, mStreamListFragment.newInstance(mUrls[position], null));
                transaction.addToBackStack("StreamTop");
                transaction.commit();
                break;
            case 2:
//                ChannelListFragment searchFragment = new ChannelListFragment();
                SearchFragment searchFragment = new SearchFragment();
                transaction = getFragmentManager().beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(R.id.container, searchFragment);
                transaction.addToBackStack("Search");
                transaction.commit();
                break;
            case 3:
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                String req = sp.getString(TWITCH_USERNAME, "");
                if (sp.getBoolean(USER_HAS_TWITCH_USERNAME, false) && !req.isEmpty()) {
                    req = getString(R.string.twitch_user_url) + req + getString(R.string.twitch_user_following_suffix);
                    ChannelListFragment favoritesFragment = new ChannelListFragment();
                    transaction = getFragmentManager().beginTransaction();
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.replace(R.id.container, favoritesFragment.newInstance(req));
                    transaction.addToBackStack("Favorites");
                    transaction.commit();
                }
                break;
            case 4:
                //divider
                break;
            case 5:
                //gopro
                break;
            case 6:
                transaction = getFragmentManager().beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(R.id.container, new SettingsFragment());
                transaction.addToBackStack("Settings");
                transaction.commit();
                break;
            case 100:
                setDefaultSettings();
                mIsInSetup = true;
                SetupFragment s = new SetupFragment();
                transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, s);
                transaction.commit();
                break;
        }
    }

    public void setBitmapQuality() {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(this);
        String qArray[] = getResources().getStringArray(R.array.settings_bitmap_qualities);
        String q = sp.getString(TWITCH_BITMAP_QUALITY, "");

        if (q.contains(qArray[0])) TwitchJSONParser.setHighQuality();
        if (q.contains(qArray[1])) TwitchJSONParser.setMediumQuality();
        if (q.contains(qArray[2])) TwitchJSONParser.setSmallQuality();
    }

    private void setDefaultSettings() {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(this);
        sp.edit().putString(TWITCH_STREAM_QUALITY_TYPE, getString(R.string.default_stream_quality_type)).apply();
        sp.edit().putString(TWITCH_PREFERRED_VIDEO_QUALITY, getString(R.string.default_preferred_video_quality)).apply();
        String defaultBitmap = getResources().getStringArray(R.array.settings_bitmap_qualities)[0];
        sp.edit().putString(TWITCH_BITMAP_QUALITY, defaultBitmap).apply();
    }

    @Override
    public void onPause() {
        mAdView.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdView.resume();
    }

    @Override
    public void onDestroy() {
        mAdView.destroy();
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 1) {
            fm.popBackStack();
        } else if (mIsInSetup) {
            mIsInSetup = false;
            startApp();
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
        transaction.addToBackStack(g.mId);
        transaction.commit();
    }

    @Override
    public void onStreamSelected(Stream g) {
        ChannelDetailFragment mChannelDetailFragment = new ChannelDetailFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.container, mChannelDetailFragment.newInstance(g.mName));
        transaction.addToBackStack(String.valueOf(g.mId));
        transaction.commit();
    }

    @Override
    public void onChannelSelected(Channel c) {
        ChannelDetailFragment mChannelDetailFragment = new ChannelDetailFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.container, mChannelDetailFragment.newInstance(c.getName()));
        transaction.addToBackStack(c.getId());
        transaction.commit();
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.v("ASDFasdf", intent.toString());
    }

    public void startApp() {
        mIsInSetup = false;
        GamesRasterFragment mGamesRasterFragment = new GamesRasterFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.container, mGamesRasterFragment.newInstance(mUrls[0]));
        transaction.addToBackStack("GamesRaster");
        transaction.commit();
    }
}
