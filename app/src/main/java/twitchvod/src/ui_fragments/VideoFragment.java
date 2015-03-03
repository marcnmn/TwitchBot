package twitchvod.src.ui_fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import twitchvod.src.MainActivity;
import twitchvod.src.R;
import twitchvod.src.adapter.OldVideoListAdapter;
import twitchvod.src.data.TwitchNetworkTasks;
import twitchvod.src.data.primitives.TwitchVideo;
import twitchvod.src.data.primitives.TwitchVod;


/**
 * Created by marc on 27.01.2015. Gridview of available games
 */
public class VideoFragment extends Fragment {

    private ArrayList<String> qualities;
    private LinkedHashMap <String,String> mData, mVideoInfo;
    private TwitchVod mVideo;
    private ImageView mPlayOverlay;
    private ProgressBar mProgressBar;
    private View.OnTouchListener mTouchListener;
    private AdapterView.OnItemClickListener mVideoClicked;


    private static String TWITCH_STREAM_QUALITY_TYPE = "settings_stream_quality_type";
    private static String TWITCH_PREFERRED_VIDEO_QUALITY = "settings_preferred_video_quality";

    private SharedPreferences mPreferences;

    private ImageView mThumbnail, mChannelBanner;
    private ListView mVideoList;
    private int mQualitySelected;

    public VideoFragment newInstance(TwitchVod h, TwitchVideo twitchVideo) {
        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("lengths", h.getLengths());
        args.putStringArrayList("qualities", h.getAvailableQualities());
        args.putSerializable("data", h.toHashmap());
        args.putSerializable("video_info", twitchVideo.toHashmap());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_video, container, false);
        RelativeLayout header = (RelativeLayout) rootView.findViewById(R.id.videoHeader);
        ListView videos = (ListView) rootView.findViewById(R.id.fullVideoList);

        ((MainActivity)getActivity()).getSupportActionBar().hide();

        ArrayList<String> lengths = getArguments().getStringArrayList("lengths");
        qualities = getArguments().getStringArrayList("qualities");
        mData = (LinkedHashMap) getArguments().getSerializable("data");
        mVideoInfo = (LinkedHashMap) getArguments().getSerializable("video_info");

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        OldVideoListAdapter adapter = new OldVideoListAdapter(this, lengths);

        ImageView thumb = (ImageView) rootView.findViewById(R.id.videoThumb);
        loadLogo(mVideoInfo.get("previewLink"), thumb);

        setHeaderHeight(thumb);

        ((TextView)header.findViewById(R.id.videoTitle)).setText(mVideoInfo.get("title"));
        ((TextView)header.findViewById(R.id.viewsAndRecorded)).setText(mVideoInfo.get("description"));
        ((TextView)header.findViewById(R.id.videoViews)).setText(mVideoInfo.get("views"));

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.channel_detail_progress);

        videos.setAdapter(adapter);

        videos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playVideo(getHash(position));
            }
        });

        return rootView;
    }


    // ------------------------VideoPlayer-----------------/////////////////////////////////
    private void playVideo(LinkedHashMap<String, String> q) {
        if (bestPossibleQuality2(q) >= 0) {
            switch (mPreferences.getString("settings_stream_quality_type", "")) {
                case "always ask": showPlayDialog(q, preferredQualityOrBest(q)); break;
                case "auto select best": playStream(q.get(bestPossibleQuality(q))); break;
                case "set maximum":
                    if(preferredQualityOrWorse(q) == null) {
                        showPlayDialog(q, preferredQualityOrBest(q));
                        Toast.makeText(getActivity(), "Sorry. No video below the maximum quality.", Toast.LENGTH_SHORT).show();
                    } else {
                        playStream(q.get(preferredQualityOrWorse(q)));
                    }
                    break;
            }
        } else {
            Toast.makeText(getActivity(), "Could not load Video, You may need to subscribe to the channel.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setHeaderHeight(ImageView header) {
        int width, height;
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        if (isInLandscape())
            header.getLayoutParams().width = width/4;
        else
            header.getLayoutParams().width = width/3;
    }

    private LinkedHashMap<String, String> getHash(int p) {
        LinkedHashMap<String, String> qurls = new LinkedHashMap<>();
        for (String q: qualities) {
            qurls.put(q, mData.get(q+p));
        }
        return qurls;
    }

    private void showPlayDialog(final LinkedHashMap<String, String> q, int best) {
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
        if (s.contains("audio_only")) return 0;
        if (s.contains("240")) return 1;
        if (s.contains("mobile")) return 1;
        if (s.contains("360")) return 2;
        if (s.contains("low")) return 2;
        if (s.contains("480")) return 3;
        if (s.contains("medium")) return 3;
        if (s.contains("720")) return 4;
        if (s.contains("high")) return 4;
        if (s.contains("live")) return 5;
        if (s.contains("source")) return 5;
        if (s.contains("chunked")) return 5;
        return -1;
    }

    private void loadLogo(final String url, final ImageView imageView) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                final Bitmap bitmap = TwitchNetworkTasks.downloadBitmap(url);
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (imageView != null)
                            imageView.setImageBitmap(bitmap);
                    }
                });
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    public String bestPossibleQuality(HashMap<String, String> qualities) {
        if (qualities.containsKey("source")) return "source";
        if (qualities.containsKey("high")) return "high";
        if (qualities.containsKey("medium")) return "medium";
        if (qualities.containsKey("low")) return "low";
        if (qualities.containsKey("mobile")) return "mobile";
        if (qualities.containsKey("audio_only")) return "audio_only";
        return null;
    }

    public int bestPossibleQuality2(HashMap<String, String> q) {
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

    public int preferredQualityOrBest(HashMap<String, String> q) {
        final String qa[] = q.keySet().toArray(new String[q.size()]);
        String pref = mPreferences.getString(TWITCH_PREFERRED_VIDEO_QUALITY,"");
        int iPref = qualityValue(pref);

        for (int i = 0; i < qa.length; i++) {
            if (qualityValue(qa[i]) == iPref) {
                return i;
            }
        }
        return bestPossibleQuality2(q);
    }

    public String preferredQualityOrWorse(HashMap<String, String> q) {
        final String qa[] = q.keySet().toArray(new String[q.size()]);
        String pref = mPreferences.getString(TWITCH_PREFERRED_VIDEO_QUALITY,"");
        int iPref = qualityValue(pref);

        int bestQ = -1;
        int bestI = -1;

        for (int i = 0; i < qa.length; i++) {
            if (qualityValue(qa[i]) <= iPref && qualityValue(qa[i]) > bestQ) {
                bestQ = qualityValue(qa[i]);
                bestI = i;
            }
        }
        if (bestI < 0) return null;
        return qa[bestI];
    }


    private boolean isInLandscape() {
        return getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
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