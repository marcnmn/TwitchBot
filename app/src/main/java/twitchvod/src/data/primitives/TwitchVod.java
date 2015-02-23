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

    public ArrayList<String> getAvailableQualities() {
        ArrayList <String> q = new ArrayList<>();
        for (TwitchVodFileOld t: video) {
           q.add(t.getQuality());
        }
        return q;
    }

    public LinkedHashMap<String, String> toHashmap() {
        LinkedHashMap<String, String> q = new LinkedHashMap<>();
        String quality;
        for (TwitchVodFileOld t: video) {
            quality = t.getQuality();
            for (int i = 0; i < t.getVideo().size(); i++) {
                q.put(t.getQuality()+i, t.getVideo().get(i).getUrl());
            }
        }
        return q;
    }

    public ArrayList<String> getLengths() {
        ArrayList<String> q = new ArrayList<>();
        TwitchVodFileOld  t = video.get(0);
        TwitchVodFile tf;
        for (int i = 0; i < t.getVideo().size(); i++) {
            tf = t.getVideo().get(i);
            q.add(tf.getLength());
        }
        return q;
    }

    public ArrayList<String> getQualities() {
        ArrayList<String> q = new ArrayList<>();
        TwitchVodFileOld  t = video.get(0);
        TwitchVodFile tf;
        for (int i = 0; i < t.getVideo().size(); i++) {
            tf = t.getVideo().get(i);
            q.add(tf.getLength());
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
