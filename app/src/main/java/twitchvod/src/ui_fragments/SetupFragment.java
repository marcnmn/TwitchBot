package twitchvod.src.ui_fragments;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.http.SslError;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import twitchvod.src.MainActivity;
import twitchvod.src.R;

public class SetupFragment extends Fragment
{
    private LinearLayout mScreenContainer;
    private LinearLayout mButtonContainer;
    private int width;
    private RelativeLayout greetings;
    private RelativeLayout authenticate;
    private RelativeLayout twitchLogin;
    private RelativeLayout nameLogin;
    private RelativeLayout setupComplete;
    private RadioGroup loginType;
    private ImageView mIndicator;


    private int page = 0;
    private static final String PREF_USER_COMPLETED_SETUP = "user_completed_setup";
    private static String USER_AUTH_TOKEN = "user_auth_token";
    private static String USER_IS_AUTHENTICATED = "user_is_authenticated";
    private static String SCOPES_OF_USER = "scopes_of_user";
    private int mNumberOfAttempts = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.setup, container, false);

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        sp.edit().putBoolean(PREF_USER_COMPLETED_SETUP, true).apply();

        mScreenContainer = (LinearLayout) rootView.findViewById(R.id.screenContainer);
        mButtonContainer = (LinearLayout) rootView.findViewById(R.id.button_bar);

        greetings = (RelativeLayout) rootView.findViewById(R.id.greetings);
        authenticate = (RelativeLayout) rootView.findViewById(R.id.choose_login);
        twitchLogin = (RelativeLayout) rootView.findViewById(R.id.twitch_login);
        nameLogin = (RelativeLayout) rootView.findViewById(R.id.name_login);
        setupComplete = (RelativeLayout) rootView.findViewById(R.id.setupCompleted);

        loginType = (RadioGroup)rootView.findViewById(R.id.radioLoginMethod);
        loginType.getCheckedRadioButtonId();

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;

        LinearLayout.LayoutParams fullscreen = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);

        greetings.setLayoutParams(fullscreen);
        authenticate.setLayoutParams(fullscreen);
        twitchLogin.setLayoutParams(fullscreen);
        nameLogin.setLayoutParams(fullscreen);
        setupComplete.setLayoutParams(fullscreen);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width /4, 4);
        lp.addRule(RelativeLayout.ABOVE, R.id.button_bar);

        mIndicator = (ImageView) rootView.findViewById(R.id.borderIndicator);
        mIndicator.setLayoutParams(lp);
        TextView skip = (TextView) rootView.findViewById(R.id.skipButton);
        TextView next = (TextView) rootView.findViewById(R.id.nextButton);

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipSetup();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextStep();
            }
        });

        return rootView;
    }

    private void nextStep() {
        if(page >= mScreenContainer.getChildCount()) return;

        switch (page) {
            case 0: makeSteps(1); break;
            case 1:
                if (loginType.getCheckedRadioButtonId() == R.id.radioTwitchLogin) {
                    makeSteps(1);
                    loadTwitchAuthenication();
                    nameLogin.setVisibility(View.GONE);
                    (mButtonContainer.findViewById(R.id.nextButton)).setVisibility(View.GONE);
                }
                else {
                    twitchLogin.setVisibility(View.GONE);
                    makeSteps(1);
                }
                break;
            case 2:
                makeSteps(1);
                break;
            case 3:
                ((MainActivity)getActivity()).startApp();
                break;

        }
    }

    private void makeSteps(int steps) {
        if(page >= mScreenContainer.getChildCount()) return;

        if (page+steps == 3){
            ((TextView)mButtonContainer.findViewById(R.id.nextButton)).setVisibility(View.VISIBLE);
            ((TextView)mButtonContainer.findViewById(R.id.nextButton)).setText("Finish");
            (mButtonContainer.findViewById(R.id.skipButton)).setVisibility(View.GONE);
        }

        ObjectAnimator m1 = ObjectAnimator.ofFloat(greetings, "translationX", -page * width, -(page +steps)* width);
        ObjectAnimator m2 = ObjectAnimator.ofFloat(authenticate, "translationX", -page * width, -(page +steps)* width);
        ObjectAnimator m3 = ObjectAnimator.ofFloat(twitchLogin, "translationX", -page * width, -(page +steps)* width);
        ObjectAnimator m4 = ObjectAnimator.ofFloat(nameLogin, "translationX", -page * width, -(page +steps)* width);
        ObjectAnimator m5 = ObjectAnimator.ofFloat(setupComplete, "translationX", -page * width, -(page +steps)* width);
        m1.start();
        m2.start();
        m3.start();
        m4.start();
        m5.start();

        ObjectAnimator progress = ObjectAnimator.ofFloat(mIndicator, "translationX", page *(width /4), (page +steps)*(width /4));
        progress.start();
        page += steps;
    }

    private void skipSetup() {
        switch (page) {
            case 0: ((MainActivity)getActivity()).startApp(); break;
            case 1:
                twitchLogin.setVisibility(View.GONE);
                makeSteps(2);
                break;
            case 2:
                makeSteps(1);
        }
    }

    private void loadTwitchAuthenication() {
        final WebView w = (WebView) twitchLogin.findViewById(R.id.webView);
        final ProgressBar p = (ProgressBar) twitchLogin.findViewById(R.id.twitchProgress);
        final String url = "https://api.twitch.tv/kraken/oauth2/authorize?response_type=token&client_id=cgkxsqu4n4wwrq4enos0dyhz60bzea4&redirect_uri=https://twitchbot&scope=user_subscriptions";
        w.getSettings().setJavaScriptEnabled(true);

        w.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                getActivity().setProgress(progress * 100);
            }
        });

        w.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(getActivity(), "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                p.setVisibility(View.GONE);
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

                    Log.v("usertoken", token);
                    Log.v("usertoken", scopes);

                    makeSteps(1);
                }
            }
        });
        w.loadUrl(url);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity) getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((ActionBarActivity) getActivity()).getSupportActionBar().show();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}