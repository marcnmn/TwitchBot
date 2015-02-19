package twitchvod.src.data.primitives;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;

public class Channel {
    public HashMap<String, String> mData;
    public Bitmap mLogoBitmap, mBannerBitmap;
    public ArrayList<TwitchVideo> mHighlights, mBroadcasts;

    public Channel(String name) {
        mData = new HashMap<>();
        mData.put("name", name);
    }

    public Channel(HashMap<String, String> h) {
        mData = h;
    }

    public String getLogoLink() {
        return mData.get("logo");
    }

    public String getBannerLink() {
        return mData.get("video_banner");
    }

    public String getStatus() {
        return mData.get("status");
    }

    public String getDisplayName() {
        return mData.get("display_name");
    }

    public String getId() {
        return mData.get("_id");
    }

    public String getViews() {
        return mData.get("views");
    }

    public String getFollowers() {
        return mData.get("followers");
    }

    public String getGame() {
        return mData.get("game");
    }

    public String getName() {
        return mData.get("name");
    }
}