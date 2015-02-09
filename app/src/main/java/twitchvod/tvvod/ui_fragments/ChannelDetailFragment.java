package twitchvod.tvvod.ui_fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.VideoView;

import java.util.HashMap;

import twitchvod.tvvod.R;
import twitchvod.tvvod.data.async_tasks.TwitchLiveStream;
import twitchvod.tvvod.data.async_tasks.TwitchToken;
import twitchvod.tvvod.data.primitives.Channel;


/**
 * Created by marc on 27.01.2015. Gridview of available games
 */
public class ChannelDetailFragment extends Fragment {
    HashMap<String, String> mAvailableQualities;
    onStreamSelectedListener mCallback;
    Channel mChannel;

    public ChannelDetailFragment newInstance(Channel c, HashMap<String,String> h) {
        ChannelDetailFragment fragment = new ChannelDetailFragment();
        Bundle args = new Bundle();
        args.putString("title", c.mTitle);
        args.putSerializable("channel", h);
        fragment.setArguments(args);
        return fragment;
    }

    public interface onStreamSelectedListener {
        public void onStreamSelected(String s);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_channel_detail, container, false);
        Spinner spinner = (Spinner) rootView.findViewById(R.id.quality_spinner);
        VideoView video = (VideoView) rootView.findViewById(R.id.videoFeed);

        video.setVideoPath("http://video59.ams01.hls.twitch.tv/hls95/imaqtpie_13065546016_202388579/medium/py-index-live.m3u8?token=id=7279848897392817578,bid=13065546016,exp=1423576850,node=video59-1.ams01.hls.justin.tv,nname=video59.ams01,fmt=medium&sig=d92ded9f6bc81cc8cc4e6932c140a0657aef1443");
        //video.start();
        video.setClickable(true);

        mChannel = new Channel((HashMap<String,String>) getArguments().getSerializable("channel"));

        fetchToken(getArguments().getString("title"));
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.livestream_qualities, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        video.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        return rootView;
    }

    private void fetchToken(String s) {
        String tokenUrl = "http://api.twitch.tv/api/channels/" + s + "/access_token";
        new TwitchToken(this).execute(tokenUrl);
    }

    public void fetchStreams(String cha, String tok, String sig) {
        String m3u8Url = "http://usher.twitch.tv/api/channel/hls/";
        m3u8Url += cha + ".m3u8?player=twitchweb&token=";
        m3u8Url += tok + "&sig=" + sig;
        m3u8Url += "&allow_audio_only=true&allow_source=true&type=any&p=8732417|VIDEO=\"audio_only\"";

        Log.v("tvvod", m3u8Url);
        new TwitchLiveStream(this).execute(m3u8Url);
    }


    public void updateStreamData(HashMap<String, String> hmap) {
        mAvailableQualities = hmap;
        playStream(hmap.get("low"));
    }

    public void playStream(String s) {
        Intent stream = new Intent(Intent.ACTION_VIEW);
        stream.setDataAndType(Uri.parse(s), "video/*");
        startActivity(stream);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (onStreamSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
}