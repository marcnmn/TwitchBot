package twitchvod.src.data.primitives;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.LinkedHashMap;

import twitchvod.src.data.TwitchJSONParser;

public class TwitchVideo {
    public String mTitle, mDesc, mStatus, mId, mRecordedAt, mGame, mLength, mPreviewLink, mViews;
    public Bitmap mPreview;

    public TwitchVideo(String title, String desc, String status, String id, String recordedAt,
                       String game, String length, String previewLink, String views) {
        mTitle = title;
        mDesc = desc;
        mStatus = status;
        mId = id;
        mRecordedAt = recordedAt;
        mGame = game;
        mLength = length;
        mPreviewLink = previewLink;
        mViews = views;
    }

    public LinkedHashMap<String, String> toHashmap() {
        LinkedHashMap<String, String> data = new LinkedHashMap<>();
        data.put("title", mTitle);
        data.put("description", mDesc);
        data.put("status", mStatus);
        data.put("recordedAt", mRecordedAt);
        data.put("length", mLength);
        data.put("previewLink", mPreviewLink);
        data.put("views", mViews);
        return data;
    }

    public String timeAgo() {
        return TwitchJSONParser.recordedAtToDate(mRecordedAt);
    }
}