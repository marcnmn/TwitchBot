package twitchvod.src.data.primitives;

import java.util.ArrayList;

/**
 * Created by marc on 20.02.2015.
 */
public class TwitchVodFileOld {
    private String quality;
    private ArrayList<TwitchVodFile> video;

    public TwitchVodFileOld() {
        video = new ArrayList<>();
        quality = "";
    }

    public TwitchVodFileOld(ArrayList<TwitchVodFile> v, String q) {
        video = v;
        quality = q;
    }


    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public ArrayList<TwitchVodFile> getVideo() {
        return video;
    }

    public void setVideo(ArrayList<TwitchVodFile> video) {
        this.video = video;
    }
}
