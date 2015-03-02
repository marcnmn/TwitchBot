package twitchvod.src.ui_fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import twitchvod.src.R;
import twitchvod.src.data.TwitchNetworkTasks;


/**
 * Created by marc on 27.01.2015. Gridview of available games
 */
public class AuthFragment extends Fragment {

    private static final String PREF_USER_COMPLETED_SETUP = "user_completed_setup";
    private static String USER_AUTH_TOKEN = "user_auth_token";
    private static String USER_IS_AUTHENTICATED = "user_is_authenticated";
    private static String SCOPES_OF_USER = "scopes_of_user";
    private static String USER_HAS_TWITCH_USERNAME = "user_has_twitch_username";
    private static String TWITCH_USERNAME = "twitch_username";
    private static String TWITCH_DISPLAY_USERNAME = "twitch_display_username";
    private int mNumberOfAttempts = 0;

    private View mRootView;

    public AuthFragment newInstance() {
        AuthFragment fragment = new AuthFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_auth, container, false);

        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle("Authentication");

        loadTwitchAuthenication();

        return mRootView;
    }

    private void loadTwitchAuthenication() {
        final WebView w = (WebView) mRootView.findViewById(R.id.webView);
        final Activity activity = getActivity();
        //final String get_oauth_token = "https://api.twitch.tv/kraken/oauth2/authorize?response_type=token&client_id=cgkxsqu4n4wwrq4enos0dyhz60bzea4&redirect_uri=https://twitchbot&scope=user_subscriptions";
        final String get_oauth_token = getActivity().getResources().getString(R.string.twitch_oauth_get_token_url);
        final String oauth_base = getActivity().getResources().getString(R.string.twitch_oauth_base_url);

        //w.getSettings().setJavaScriptEnabled(true);

        w.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                getActivity().setProgress(progress * 100);
            }
        });

        w.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                //Toast.makeText(getActivity(), "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (mNumberOfAttempts > 4) return;
                if (url.contains("access_token=")) {
                    w.setVisibility(View.GONE);

                    int index_token = url.indexOf("=")+1;
                    int index_middle = url.lastIndexOf("&");
                    int index_scope = url.lastIndexOf("=")+1;
                    String token = url.substring(index_token, index_middle);
                    String scopes = url.substring(index_scope, url.length());
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(USER_IS_AUTHENTICATED, true).apply();
                    sp.edit().putString(USER_AUTH_TOKEN, token).apply();
                    sp.edit().putString(SCOPES_OF_USER, scopes).apply();

                    Log.d("SetupFragment:usertoken", token);
                    Log.d("SetupFragment:scopes", scopes);

                    new DownloadJSONTask(0).execute(oauth_base + token);
                }
            }
        });
        w.loadUrl(get_oauth_token);
    }

    private void usernameConfirmed(String username, String userDisplayName) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        sp.edit().putBoolean(USER_HAS_TWITCH_USERNAME, true).apply();
        sp.edit().putString(TWITCH_USERNAME, username).apply();
        sp.edit().putString(TWITCH_DISPLAY_USERNAME, userDisplayName).apply();
        getActivity().getFragmentManager().popBackStack();
    }

    private void oauthDataReceived(JSONObject loggedIn) {
        String username = "";
        try {
            username = loggedIn.getJSONObject("token").getString("user_name");
            String user_url = getActivity().getResources().getString(R.string.twitch_user_url) + username;
            new DownloadJSONTask(1).execute(user_url);
        } catch (JSONException e) {
            Toast.makeText(getActivity(), "Usertoken not valid", Toast.LENGTH_LONG).show();
            Log.d("SetupFragment:username", "no valid username" + username);
        } catch (NullPointerException e) {
            Toast.makeText(getActivity(), "Usertoken not valid", Toast.LENGTH_LONG).show();
            Log.d("SetupFragment:username", "no valid username" + username + " Nullpointer");
        }
    }

    private void userSearchDataReceived(JSONObject userData) {
        String username = "", userDisplayName = "";
        try {
            username = userData.getString("name");
            userDisplayName = userData.getString("display_name");
            usernameConfirmed(username, userDisplayName);
        } catch (JSONException e) {
        } catch (NullPointerException e) {
            Log.d("SetupFragment:username", "no valid username" + username + " Nullpointer");
        }
    }

    private class DownloadJSONTask extends AsyncTask<String, Void, JSONObject> {

        private final int type;

        public DownloadJSONTask(int type) {
            this.type = type;
        }

        protected JSONObject doInBackground(String... urls) {
            return TwitchNetworkTasks.downloadJSONData(urls[0]);
        }

        protected void onPostExecute(JSONObject result) {
            if (type == 0) oauthDataReceived(result);
            if (type == 1) userSearchDataReceived(result);
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
}