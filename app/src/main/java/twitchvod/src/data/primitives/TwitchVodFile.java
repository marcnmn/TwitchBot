package twitchvod.src.data.primitives;

import java.util.ArrayList;

/**
 * Created by marc on 20.02.2015.
 */
public class TwitchVodFile {
    private String url, length;

    public TwitchVodFile() {
        url = "";
        length = "";
    }

    public TwitchVodFile(String u, String l) {
        url = u;
        length = l;
    }


    public String getUrl() {
        return url;
    }


    public String getLength() {
        return length;
    }
}
