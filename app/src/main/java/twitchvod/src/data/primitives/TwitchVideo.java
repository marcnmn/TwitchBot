package twitchvod.src.data.primitives;

import android.graphics.Bitmap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

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

    public String timeAgo() {
        return TwitchJSONParser.recordedAtToDate(mRecordedAt);
    }
}