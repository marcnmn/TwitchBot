package twitchvod.tvvod.data.primitives;

import android.graphics.Bitmap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class Channel {
    public String mTitle, mLogoLink, mPreviewLink, mUrl, mStatus, mGame;
    public int mViewers, mId;
    public Bitmap mLogo, mPreview;

    public Channel(String title, String url, String status, String game, int viewers,
                   String logoLink, String previewLink, int id) {
        mTitle = title;
        mViewers = viewers;
        mUrl = url;
        mGame = game;
        mId = id;
        mStatus = status;
        mLogoLink = logoLink;
        mPreviewLink = previewLink;
    }

    public Channel (HashMap<String, String> h) {
        mTitle = h.get("title");
        mViewers = Integer.valueOf(h.get("viewers"));
        mUrl = h.get("url");
        mGame = h.get("game");
        mId = Integer.valueOf(h.get("id"));
        mStatus = h.get("status");
        mLogoLink = h.get("logoLink");
        mPreviewLink = h.get("previewLink");
    }

    public HashMap<String, String> toHashMap() {
        HashMap<String, String> h = new HashMap<>();
        h.put("title", mTitle);
        h.put("viewers", String.valueOf(mViewers));
        h.put("url", mUrl);
        h.put("id", String.valueOf(mId));
        h.put("game", mGame);
        h.put("status", mStatus);
        h.put("logoLink", mLogoLink);
        h.put("previewLink", mPreviewLink);
        return h;
    }

    public String titleToURL() {
        String url = "";
        try {
            url = URLEncoder.encode(mTitle, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url.toLowerCase();
    }

    @Override
    public String toString() {
        return mTitle+ " spielt " + mGame + " mit " + mViewers + " Zuschauern";
    }

    public String channelInfo() {
        return "Playing " + mGame + "\n" +  mViewers + " Viewers";
    }
}