package twitchvod.src.ui_fragments;

import android.app.Activity;
import android.app.Fragment;
import android.net.http.SslError;
import android.os.Bundle;
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

    public AuthFragment newInstance() {
        AuthFragment fragment = new AuthFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_auth, container, false);
        WebView w = (WebView) rootView.findViewById(R.id.webView);

        String url = "https://api.twitch.tv/kraken/oauth2/authorize?response_type=token&client_id=cgkxsqu4n4wwrq4enos0dyhz60bzea4&redirect_uri=http://localhost/oauth&scope=chat_login";
        String url2 = "http://google.com/";

        WebView webview = new WebView(getActivity());
        getActivity().setContentView(webview);
        webview.getSettings().setJavaScriptEnabled(true);
        //webview.getSettings().setUseWideViewPort(true);
        //webview.getSettings().setLoadWithOverviewMode(true);

        final Activity activity = getActivity();
        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
                activity.setProgress(progress * 1000);
            }
        });
        webview.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed(); // Ignore SSL certificate errors
            }
        });

        webview.loadUrl(url);
        //w.loadUrl(url2);
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