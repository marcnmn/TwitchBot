package twitchvod.src.data.primitives;

import android.graphics.Bitmap;

import java.util.ArrayList;

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

    public String bestPossibleUrl() {
        int bestQual = -1;
        String result = null;
        for (int i = 0; i < video.size(); i++) {
            if (quality(video.get(i).getQuality()) > bestQual) {
                bestQual = i;
            }
        }
        if (bestQual > -1)
            result =  video.get(bestQual).getVideo().get(0).getUrl();
        return result;
    }

    private int quality(String s) {
        if (s.contains("240")) return 0;
        if (s.contains("360")) return 1;
        if (s.contains("480")) return 2;
        if (s.contains("720")) return 3;
        if (s.contains("live")) return 4;
        return -1;
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
