package twitchvod.src.ui_fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
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
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import twitchvod.src.R;


/**
 * Created by marc on 27.01.2015. Gridview of available games
 */
public class TestFragment extends Fragment {

    private int mItemSelected;

    public TestFragment newInstance() {
        TestFragment fragment = new TestFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_auth, container, false);

        String qualities[] = {"source", "high", "medium", "low", "mobile", "audio only"};

        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle("test");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("testtesttest")
                .setSingleChoiceItems(qualities, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mItemSelected = which;
                       Toast.makeText(getActivity(),""+ which,Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("Play", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getActivity(),""+ mItemSelected,Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getActivity(),""+ mItemSelected,Toast.LENGTH_SHORT).show();
                    }
                });
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();


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