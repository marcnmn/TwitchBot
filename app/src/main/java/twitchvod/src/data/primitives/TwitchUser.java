package twitchvod.src.data.primitives;

import android.graphics.Bitmap;

import twitchvod.src.data.TwitchJSONParser;

public class TwitchUser {
    private String mDisplay_Name, mName, mBio, mUpdatedAt, mType, mCreatedAt, mLogoLink;
    private Bitmap mLogo;

    public TwitchUser(String display_name, String name, String bio, String updated_at, String type, String created_at, String logoLink) {
        mDisplay_Name = display_name;
        mName = name;
        mBio = bio;
        mUpdatedAt = updated_at;
        mType = type;
        mCreatedAt = created_at;
        mLogoLink = logoLink;
    }

    public String timeAgoUpdated() {
        return TwitchJSONParser.recordedAtToDate(mUpdatedAt);
    }

    public String getDisplay_Name() {
        return mDisplay_Name;
    }

    public String getBio() {
        return mBio;
    }

    public String getType() {
        return mType;
    }

    public String getUpdatedAtAt() {
        return mUpdatedAt;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public String getmName() {
        return mName;
    }

    public String getmLogoLink() {
        return mLogoLink;
    }
}