package twitchvod.src.ui_fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.net.http.SslError;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import twitchvod.src.R;


/**
 * Created by marc on 27.01.2015. Gridview of available games
 */
public class AuthFragment extends Fragment {

    private String USER_AUTH_TOKEN = "user_auth_token";
    private String USER_IS_AUTHENTICATED = "user_is_authenticated";
    private String SCOPES_OF_USER = "scopes_of_user";
    private int mNumberOfAttempts = 0;

    public AuthFragment newInstance() {
        AuthFragment fragment = new AuthFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_auth, container, false);
        final WebView w = (WebView) rootView.findViewById(R.id.webView);
        final Activity activity = getActivity();

        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle("Authentication");

        final String url = "https://api.twitch.tv/kraken/oauth2/authorize?response_type=token&client_id=cgkxsqu4n4wwrq4enos0dyhz60bzea4&redirect_uri=https://twitchbot&scope=chat_login";
        final String url2 = "http://google.com/";

        w.getSettings().setJavaScriptEnabled(true);

        w.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed(); // Ignore SSL certificate errors
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (mNumberOfAttempts > 4) return;
                if (url.contains("access_token=")) {

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

                    FragmentManager fm = activity.getFragmentManager();
                    if (fm.getBackStackEntryCount() > 1) {
                        fm.popBackStack();
                    }

                    Log.v("Authentication Test", url);
                } else {
                    try {
                        w.wait(1000);
                        w.loadUrl(url2);
                        Toast.makeText(activity, "Could not login. Trying again.", Toast.LENGTH_SHORT).show();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        w.loadUrl(url);
        return rootView;
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