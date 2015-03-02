package twitchvod.src.ui_fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import twitchvod.src.R;
import twitchvod.src.data.TwitchNetworkTasks;


/**
 * Created by marc on 27.01.2015. Gridview of available games
 */
public class SettingsFragment extends Fragment {

    private static final String PREF_USER_COMPLETED_SETUP = "user_completed_setup";
    private static String USER_AUTH_TOKEN = "user_auth_token";
    private static String USER_IS_AUTHENTICATED = "user_is_authenticated";
    private static String SCOPES_OF_USER = "scopes_of_user";
    private static String USER_HAS_TWITCH_USERNAME = "user_has_twitch_username";
    private static String TWITCH_USERNAME = "twitch_username";
    private static String TWITCH_DISPLAY_USERNAME = "twitch_display_username";
    private static String TWITCH_STREAM_QUALITY_TYPE = "settings_stream_quality_type";
    private static String TWITCH_PREFERRED_VIDEO_QUALITY = "settings_preferred_video_quality";

    private LinearLayout mQualityLayout, mPreferredQualityLayout, mUsernameLayout, mTwitchLoginLayout, mRefreshTokenLayout;
    private TextView mQualityText, mPreferredQualityText, mUsernameText;
    private EditText mUsernameEditText;
    private SharedPreferences mPreferences;
    private int mItemSelected, mQualityTypeSelected, mPreferredQualitySelected;
    private View mUsernameDialogView;

    public SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        // Video Settings
        mQualityLayout = (LinearLayout) rootView.findViewById(R.id.qualitySetting);
        mQualityText = (TextView) mQualityLayout.findViewById(R.id.textQualitySetting);
        mPreferredQualityLayout = (LinearLayout) rootView.findViewById(R.id.preferredQualitySetting);
        mPreferredQualityText = (TextView) mPreferredQualityLayout.findViewById(R.id.textPreferredQualitySetting);

        // Twitch Settings
        mUsernameLayout = (LinearLayout) rootView.findViewById(R.id.usernameSetting);
        mUsernameText = (TextView) mUsernameLayout.findViewById(R.id.textUsernameSetting);
        mTwitchLoginLayout = (LinearLayout) rootView.findViewById(R.id.twitchLoginSetting);
        mRefreshTokenLayout = (LinearLayout) rootView.findViewById(R.id.refreshTokenSetting);


        // Set initial values
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mQualityTypeSelected = getTypeIndex(mPreferences.getString(TWITCH_STREAM_QUALITY_TYPE, ""));
        mPreferredQualitySelected = getPrefQualityIndex(mPreferences.getString(TWITCH_PREFERRED_VIDEO_QUALITY, ""));

        mQualityText.setText(mPreferences.getString(TWITCH_STREAM_QUALITY_TYPE, ""));
        mPreferredQualityText.setText(mPreferences.getString(TWITCH_PREFERRED_VIDEO_QUALITY, ""));
        mUsernameText.setText(mPreferences.getString(TWITCH_DISPLAY_USERNAME, ""));

        if (mPreferences.getBoolean(USER_HAS_TWITCH_USERNAME, false))
            ((ImageView)mUsernameLayout.findViewById(R.id.usernameStatusIcon)).setImageResource(R.drawable.username_check);
        else
            ((ImageView)mUsernameLayout.findViewById(R.id.usernameStatusIcon)).setImageResource(R.drawable.username_fail);

        // Set Listeners
        mQualityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQualityDialog();
            }
        });

        mPreferredQualityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPreferredQualityDialog();
            }
        });
        if (mQualityTypeSelected == 1) mPreferredQualityLayout.setClickable(false);

        mUsernameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUsernameDialog();
            }
        });

        mTwitchLoginLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPreferences.getBoolean(USER_HAS_TWITCH_USERNAME, false)) {
                    newLoginDialog();
                }
                else {
                    SetupFragment s = new SetupFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, s);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }
        });

        mRefreshTokenLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthFragment a = new AuthFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, a);
                transaction.commit();
                Toast.makeText(getActivity(), "refresh token", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }



    private void showQualityDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String types[] = getActivity().getResources().getStringArray(R.array.settings_stream_quality_type);
        final int disabled = getActivity().getResources().getColor(R.color.primary_text_disabled_material_light);
        final int enabled = getActivity().getResources().getColor(R.color.secondary_text_default_material_light);


        builder.setTitle("Select Play Behaviour")
                .setSingleChoiceItems(types, mQualityTypeSelected, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mQualityTypeSelected = which;
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mQualityText.setText(types[mQualityTypeSelected]);
                        mPreferences.edit().putString(TWITCH_STREAM_QUALITY_TYPE, types[mQualityTypeSelected]).apply();
                        if (types[mQualityTypeSelected].equals("auto select best")) {
                            mPreferredQualityLayout.setClickable(false);
                            mPreferredQualityText.setTextColor(disabled);
                        }
                        else {
                            mPreferredQualityLayout.setClickable(true);
                            mPreferredQualityText.setTextColor(enabled);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        builder.create();
        builder.show();
    }

    private void showPreferredQualityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String qualities[] = getActivity().getResources().getStringArray(R.array.livestream_qualities);

        builder.setTitle("Select Preferred Quality")
                .setSingleChoiceItems(qualities, mPreferredQualitySelected, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPreferredQualitySelected = which;
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mPreferredQualityText.setText(qualities[mPreferredQualitySelected]);
                        mPreferences.edit().putString(TWITCH_PREFERRED_VIDEO_QUALITY, qualities[mPreferredQualitySelected]).apply();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        builder.create();
        builder.show();
    }

    private void showUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        mUsernameDialogView = inflater.inflate(R.layout.setting_username_dialog, null);
        mUsernameEditText = (EditText) mUsernameDialogView.findViewById(R.id.usernameEditText);

        builder.setTitle("Please Enter Username")
                .setView(mUsernameDialogView)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String request = getActivity().getResources().getString(R.string.twitch_user_url)
                                + mUsernameEditText.getText().toString();
                        mUsernameText.setText(mUsernameEditText.getText());
                        new DownloadJSONTask().execute(request);
                        mUsernameLayout.findViewById(R.id.usernameUpdateProgress).setVisibility(View.VISIBLE);
                        mUsernameLayout.findViewById(R.id.usernameStatusIcon).setVisibility(View.GONE);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getActivity(),""+ mQualityTypeSelected,Toast.LENGTH_SHORT).show();
                    }
                });
        builder.create();
        builder.show();
    }

    private void newLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String qualities[] = getActivity().getResources().getStringArray(R.array.livestream_qualities);

        builder.setTitle("New Twitch Account")
                .setMessage("Do your really want to log in with a new Account?")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CookieManager cookieManager = CookieManager.getInstance();
                        cookieManager.removeAllCookie();

                        SetupFragment s = new SetupFragment();
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.container, s);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        builder.create();
        builder.show();
    }

    private void newUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String qualities[] = getActivity().getResources().getStringArray(R.array.livestream_qualities);

        builder.setTitle("Recognized new Twitch Account")
                .setMessage("Do you want to log into Twitch for restricted streams?")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AuthFragment a = new AuthFragment();
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.container, a);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        builder.create();
        builder.show();
    }

    private void usernameDataReceived(JSONObject userData) {
        mUsernameLayout.findViewById(R.id.usernameUpdateProgress).setVisibility(View.GONE);
        String username = "", userDisplayName = "";
        try {
            username = userData.getString("name");
            userDisplayName = userData.getString("display_name");
            usernameConfirmed(username, userDisplayName);
        } catch (JSONException e) {
            mUsernameLayout.findViewById(R.id.usernameStatusIcon).setVisibility(View.VISIBLE);
            ((ImageView)mUsernameLayout.findViewById(R.id.usernameStatusIcon)).setImageResource(R.drawable.username_fail);
            mPreferences.edit().putBoolean(USER_HAS_TWITCH_USERNAME, false).apply();
            mPreferences.edit().putBoolean(USER_IS_AUTHENTICATED, false).apply();
            Log.d("SetupFragment:username", "no valid username" + username);
        } catch (NullPointerException e) {
            mUsernameLayout.findViewById(R.id.usernameStatusIcon).setVisibility(View.VISIBLE);
            ((ImageView)mUsernameLayout.findViewById(R.id.usernameStatusIcon)).setImageResource(R.drawable.username_fail);
            mPreferences.edit().putBoolean(USER_HAS_TWITCH_USERNAME, false).apply();
            mPreferences.edit().putBoolean(USER_IS_AUTHENTICATED, false).apply();
            Log.d("SetupFragment:username", "no valid username" + username + " Nullpointer");
        }
    }

    private void usernameConfirmed(String username, String userDisplayName) {
        mUsernameLayout.findViewById(R.id.usernameStatusIcon).setVisibility(View.VISIBLE);
        ((ImageView)mUsernameLayout.findViewById(R.id.usernameStatusIcon)).setImageResource(R.drawable.username_check);

        mPreferences.edit().putBoolean(USER_HAS_TWITCH_USERNAME, true).apply();
        mPreferences.edit().putString(TWITCH_USERNAME, username).apply();
        mPreferences.edit().putString(TWITCH_DISPLAY_USERNAME, userDisplayName).apply();

        if (username.equals(mPreferences.getString(TWITCH_USERNAME, ""))) {
            mPreferences.edit().putBoolean(USER_IS_AUTHENTICATED, false).apply();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            newUserDialog();
        }

        mUsernameText.setText(userDisplayName);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private int getTypeIndex(String s) {
        String types[] = getActivity().getResources().getStringArray(R.array.settings_stream_quality_type);
        for (int i = 0; i < types.length; i++) {
            if (s.equals(types[i])) return i;
        }
        return 0;
    }

    private int getPrefQualityIndex(String s) {
        String types[] = getActivity().getResources().getStringArray(R.array.livestream_qualities);
        for (int i = 0; i < types.length; i++) {
            if (s.equals(types[i])) return i;
        }
        return 0;
    }

    private class DownloadJSONTask extends AsyncTask<String, Void, JSONObject> {

        public DownloadJSONTask(){}

        protected JSONObject doInBackground(String... urls) {
            return TwitchNetworkTasks.downloadJSONData(urls[0]);
        }

        protected void onPostExecute(JSONObject result) {
            usernameDataReceived(result);
        }
    }
}