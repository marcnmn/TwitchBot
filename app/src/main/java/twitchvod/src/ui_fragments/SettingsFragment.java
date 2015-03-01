package twitchvod.src.ui_fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import twitchvod.src.R;


/**
 * Created by marc on 27.01.2015. Gridview of available games
 */
public class SettingsFragment extends Fragment {

    private static final String PREF_USER_COMPLETED_SETUP = "user_completed_setup";
    private static String USER_AUTH_TOKEN = "user_auth_token";
    private static String USER_IS_AUTHENTICATED = "user_is_authenticated";
    private static String SCOPES_OF_USER = "scopes_of_user";
    private static String TWITCH_USERNAME = "twitch_username";

    private int mItemSelected, mQualitySelected, mPreferredQualitySelected;

    public SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        // Video Settings
        LinearLayout qualityLayout = (LinearLayout) rootView.findViewById(R.id.qualitySetting);
        TextView qualityText = (TextView) qualityLayout.findViewById(R.id.textQualitySetting);
        LinearLayout preferredQualityLayout = (LinearLayout) rootView.findViewById(R.id.preferredQualitySetting);
        TextView preferredQualityText = (TextView) qualityLayout.findViewById(R.id.textPreferredQualitySetting);

        // Twitch Settings
        LinearLayout usernameLayout = (LinearLayout) rootView.findViewById(R.id.usernameSetting);
        TextView usernameText = (TextView) qualityLayout.findViewById(R.id.textUsernameSetting);
        LinearLayout twitchLoginLayout = (LinearLayout) rootView.findViewById(R.id.twitchLoginSetting);
        LinearLayout refreshTokenLayout = (LinearLayout) rootView.findViewById(R.id.refreshTokenSetting);

        // Set Listeners

        qualityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQualityDialog(0);
            }
        });

        preferredQualityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPreferredQualityDialog(0);
            }
        });

        usernameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUsernameDialog();
            }
        });

        twitchLoginLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetupFragment s = new SetupFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, s);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        refreshTokenLayout.setOnClickListener(new View.OnClickListener() {
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

    private void showQualityDialog(int best) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String qualities[] = getActivity().getResources().getStringArray(R.array.settings_stream_quality_type);

        builder.setTitle("Select Play Behaviour")
                .setSingleChoiceItems(qualities, best, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mQualitySelected = which;
                        Toast.makeText(getActivity(), "" + which, Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getActivity(),""+ mQualitySelected,Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getActivity(),""+ mQualitySelected,Toast.LENGTH_SHORT).show();
                    }
                });
        builder.create();
        builder.show();
    }

    private void showPreferredQualityDialog(int best) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String qualities[] = getActivity().getResources().getStringArray(R.array.livestream_qualities);

        builder.setTitle("Select Preferred Quality")
                .setSingleChoiceItems(qualities, best, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPreferredQualitySelected = which;
                        Toast.makeText(getActivity(), "" + which, Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getActivity(),""+ mPreferredQualitySelected,Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getActivity(),""+ mPreferredQualitySelected,Toast.LENGTH_SHORT).show();
                    }
                });
        builder.create();
        builder.show();
    }

    private void showUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.setting_username_dialog, null);
        final EditText i = (EditText) v.findViewById(R.id.usernameEditText);
        //i.setPadding(10,10,10,10);
        //final EditText input = i;

        builder.setTitle("Please Enter Username")
                .setView(v)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getActivity(), i.getText(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getActivity(),""+ mQualitySelected,Toast.LENGTH_SHORT).show();
                    }
                });
        builder.create();
        builder.show();
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