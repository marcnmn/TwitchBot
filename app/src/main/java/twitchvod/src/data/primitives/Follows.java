package twitchvod.src.data.primitives;

import android.graphics.Bitmap;

import java.util.HashMap;

public class Follows {
    public HashMap<String, String> mData;
    public Bitmap mLogoBitmap, mBannerBitmap;

    public Follows(HashMap<String, String> h) {
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

    public String getGame() {
        return mData.get("game");
    }
}