package twitchvod.tvvod;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import twitchvod.tvvod.data.primitives.Channel;
import twitchvod.tvvod.data.primitives.Game;
import twitchvod.tvvod.ui_fragments.ChannelDetailFragment;
import twitchvod.tvvod.ui_fragments.ChannelListFragment;
import twitchvod.tvvod.ui_fragments.GamesRasterFragment;
import twitchvod.tvvod.ui_fragments.NavigationDrawerFragment;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        GamesRasterFragment.OnGameSelectedListener, ChannelListFragment.onChannelSelectedListener,
        ChannelDetailFragment.onStreamSelectedListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private String mUrls[];

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    public CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUrls = getResources().getStringArray(R.array.drawer_urls);

        setContentView(R.layout.activity_main);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        mTitle = getTitle();
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getFragmentManager();
        switch (position){
            case 0:
                GamesRasterFragment g = new GamesRasterFragment();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.animator.enter_anim, R.animator.exit_anim);
                transaction.replace(R.id.container, g.newInstance(position, mUrls[position]));
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case 1:
                ChannelListFragment f = new ChannelListFragment();
                FragmentTransaction t = fragmentManager.beginTransaction();
                t.replace(R.id.container, f.newInstance(position, mUrls[position]));
                t.addToBackStack(null);
                t.commit();
                break;
            case 2:
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
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        //actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
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
        ChannelListFragment cFragment = new ChannelListFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, cFragment.newInstance(0, url));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onChannelSelected(Channel g) {
        ChannelDetailFragment cFragment = new ChannelDetailFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, cFragment.newInstance(g, g.toHashMap()));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onStreamSelected(String s) {
    }
}
