package twitchvod.src.data.primitives;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by marc on 20.02.2015.
 */
public class TwitchVod {
    private String duration, channel, previewLink;
    private ArrayList<TwitchVodFileOld> video;
    private Bitmap preview;

    public TwitchVod() {
        video = new ArrayList<>();
    }

    public TwitchVod(ArrayList<TwitchVodFileOld> v, String d, String c, String p){
        duration = d;
        channel = c;
        previewLink = p;
        video = v;
    }

    public Integer bestPossibleUrl() {
        int bestQual = -1;
        int bestIndex = -1;

        for (int i = 0; i < video.size(); i++) {
            if (quality(video.get(i).getQuality()) > bestQual) {
                bestQual = quality(video.get(i).getQuality());
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private int quality(String s) {
        if (s.contains("240")) return 0;
        if (s.contains("360")) return 1;
        if (s.contains("480")) return 2;
        if (s.contains("720")) return 3;
        if (s.contains("live")) return 4;
        if (s.contains("source")) return 4;
        if (s.contains("chunked")) return 4;
        return -1;
    }

    public LinkedHashMap<String, String> getAvailableQualities() {
        LinkedHashMap<String, String> q = new LinkedHashMap<>();
        String qualities[] = new String[video.size()];
        TwitchVodFileOld t;
        for (int i = 0; i < video.size(); i++) {
            t = video.get(i);
            q.put(t.getQuality(), t.getVideo().get(0).getUrl());
            qualities[i] = video.get(i).getQuality();
        }
        return q;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getPreviewLink() {
        return previewLink;
    }

    public void setPreviewLink(String previewLink) {
        this.previewLink = previewLink;
    }

    public ArrayList<TwitchVodFileOld> getVideo() {
        return video;
    }

    public void setVideo(ArrayList<TwitchVodFileOld> video) {
        this.video = video;
    }

    public Bitmap getPreview() {
        return preview;
    }

    public void setPreview(Bitmap preview) {
        this.preview = preview;
    }

}
