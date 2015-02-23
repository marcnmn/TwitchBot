package twitchvod.src.ui_fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import twitchvod.src.R;
import twitchvod.src.adapter.OldVideoListAdapter;
import twitchvod.src.data.primitives.TwitchVod;
import twitchvod.src.data.primitives.TwitchVodFileOld;


/**
 * Created by marc on 27.01.2015. Gridview of available games
 */
public class VideoFragment extends Fragment {

    private ArrayList<String> qualities;
    private LinkedHashMap <String,String> mData;
    private TwitchVod mVideo;
    onStreamSelectedListener mCallback;
    private ImageView mPlayOverlay;
    private ProgressBar mProgressBar;
    private View.OnTouchListener mTouchListener;
    private AdapterView.OnItemClickListener mVideoClicked;


    private ImageView mThumbnail, mChannelBanner;
    private ListView mVideoList;
    private int mQualitySelected;

    public VideoFragment newInstance(TwitchVod h) {
        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("lengths", h.getLengths());
        args.putStringArrayList("qualities", h.getAvailableQualities());
        args.putSerializable("data", h.toHashmap());
        fragment.setArguments(args);
        return fragment;
    }

    public interface onStreamSelectedListener {
        public void onStreamSelected(String s);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_video, container, false);
        ListView videos = (ListView) rootView.findViewById(R.id.videoList);

        ArrayList<String> lengths = getArguments().getStringArrayList("lengths");
        qualities = getArguments().getStringArrayList("qualities");
        mData = (LinkedHashMap) getArguments().getSerializable("data");

        OldVideoListAdapter adapter = new OldVideoListAdapter(this, lengths);

        mThumbnail = (ImageView) rootView.findViewById(R.id.videoFeed);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.channel_detail_progress);

        videos.setAdapter(adapter);

        videos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showPlayDialog(getHash(position));
            }
        });

        return rootView;
    }

    private LinkedHashMap<String, String> getHash(int p) {
        LinkedHashMap<String, String> qurls = new LinkedHashMap<>();
        for (String q: qualities) {
            qurls.put(q, mData.get(q+p));
        }
        return qurls;
    }

    private void showPlayDialog(final LinkedHashMap<String, String> q) {
        int best = bestPossibleQuality(q);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String qualities[] = q.keySet().toArray(new String[q.size()]);
        String cleanQualities[] = getCleanQualities(qualities);

        builder.setTitle("Select Quality")
                .setSingleChoiceItems(cleanQualities, best, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mQualitySelected = which;
                        Toast.makeText(getActivity(), "" + which, Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("Play", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getActivity(),""+ mQualitySelected,Toast.LENGTH_SHORT).show();
                        playStream(q.get(qualities[mQualitySelected]));
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

    public void playStream(String s) {
        Intent stream = new Intent(Intent.ACTION_VIEW);
        stream.setDataAndType(Uri.parse(s), "video/*");
        startActivity(stream);
    }

    private String[] getCleanQualities(String[] s) {
        String q[] = new String[s.length];

        for (int i = 0; i < s.length; i++) {
            if (s[i].contains("live")) {
                q[i] = "source";
                continue;
            }
            if (s[i].contains("chunked")){
                q[i] = "source";
                continue;
            }
            if (s[i].contains("audio_only")) {
                q[i] = "audio only";
                continue;
            }
            q[i] = s[i];
        }
        return q;
    }

    public int bestPossibleQuality(LinkedHashMap<String, String> q) {
        final String qa[] = q.keySet().toArray(new String[q.size()]);
        int bestQ = -1;
        int bestI = -1;

        for (int i = 0; i < qa.length; i++) {
            if (qualityValue(qa[i]) > bestQ) {
                bestQ = qualityValue(qa[i]);
                bestI = i;
            }
        }
        return bestI;
    }

    private int qualityValue(String s) {
        if (s.contains("240")) return 0;
        if (s.contains("360")) return 1;
        if (s.contains("480")) return 2;
        if (s.contains("720")) return 3;
        if (s.contains("live")) return 4;
        if (s.contains("source")) return 4;
        if (s.contains("chunked")) return 4;
        return -1;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}