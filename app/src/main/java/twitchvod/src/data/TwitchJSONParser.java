package twitchvod.src.data;

import android.util.Log;
import android.util.MalformedJsonException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import twitchvod.src.data.primitives.Channel;
import twitchvod.src.data.primitives.Game;
import twitchvod.src.data.primitives.PastBroadcast;
import twitchvod.src.data.primitives.Stream;
import twitchvod.src.data.primitives.TwitchUser;
import twitchvod.src.data.primitives.TwitchVideo;

public final class TwitchJSONParser {
    private static String BITMAP_QUALITY = "large";

    private TwitchJSONParser() {
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static ArrayList<Game> gameJSONtoArrayList(String r) {
        String title, thumb, id;
        int viewers, channelc;
        JSONObject game;

        ArrayList<Game> games = new ArrayList<>();

        try {
            JSONObject jObject = new JSONObject(r);
            JSONArray jArray = jObject.getJSONArray("top");

            for (int i=0; i<jArray.length(); i++) {
                viewers = jArray.getJSONObject(i).getInt("viewers");
                channelc = jArray.getJSONObject(i).getInt("channels");

                game = jArray.getJSONObject(i).getJSONObject("game");
                title = game.getString("name");
                id = game.getString("_id");
                thumb = game.getJSONObject("box").getString(BITMAP_QUALITY);
                Game temp = new Game(title,thumb,viewers,channelc,id,null);
                games.add(temp);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.v("TwitchBot channelsJSONtoArrayList", "no JSON Data");
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.v("gameJSONtoArrayList", "Nothing to parse. String is empty");
        }
        return games;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static ArrayList<Channel> channelsJSONtoArrayList(String r) {
        ArrayList<Channel> channels = new ArrayList<>();
        HashMap<String, String> htemp = new HashMap<>();
        String request_type;

        if (r.contains("\"channels\":["))
            request_type = "channels";
        else if (r.contains("{\"follows\":["))
            request_type = "follows";
        else
            return channels;

        JSONObject jObject = null;
        try {

            jObject = new JSONObject(r);

        JSONArray jArray = jObject.getJSONArray(request_type);
        JSONObject j;
        for (int i=0; i<jArray.length(); i++) {
            j = jArray.getJSONObject(i);
            if (request_type.equals("follows")) j = j.getJSONObject("channel");
            try {
                htemp.put("updated_at", j.getString("updated_at"));
            } catch (JSONException e) {
                htemp.put("updated_at", "");
            }

            htemp.put("video_banner", j.getString("video_banner"));
            htemp.put("logo", j.getString("logo"));
            htemp.put("display_name", j.getString("display_name"));
            htemp.put("followers", j.getString("followers"));
            htemp.put("status", j.getString("status"));
            htemp.put("views", j.getString("views"));
            htemp.put("followers", j.getString("followers"));
            htemp.put("game", j.getString("game"));
            htemp.put("name", j.getString("name"));
            htemp.put("url", j.getString("url"));
            htemp.put("_id", j.getString("_id"));
            channels.add(new Channel((HashMap<String,String>) htemp.clone()));
            htemp.clear();
        }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.v("TwitchBot channelsJSONtoArrayList", "no JSON Data");
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.v("gameJSONtoArrayList", "Nothing to parse. String is empty");
        }

        return channels;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static Channel channelJSONtoChannel(String r) {
        Channel channel = null;
        HashMap<String, String> hTemp = new HashMap<>();

        JSONObject jObject = null;
        try {
            jObject = new JSONObject(r);
            hTemp.put("status", jObject.getString("status"));
            hTemp.put("display_name", jObject.getString("display_name"));
            hTemp.put("game", jObject.getString("game"));
            hTemp.put("_id", jObject.getString("_id"));
            hTemp.put("name", jObject.getString("name"));
            try {
                hTemp.put("updated_at", jObject.getString("updated_at"));
            } catch (JSONException e) {
                hTemp.put("updated_at", "");
            }
            hTemp.put("logo", jObject.getString("logo"));
            hTemp.put("video_banner", jObject.getString("video_banner"));
            hTemp.put("views", jObject.getString("views"));
            hTemp.put("followers", jObject.getString("followers"));
            hTemp.put("url", jObject.getString("url"));
            hTemp.put("followers", jObject.getString("followers"));

            channel = new Channel(hTemp);
            return  channel;

        } catch (JSONException e) {
            e.printStackTrace();
            Log.v("TwitchBot channelsJSONtoArrayList", "no JSON Data");
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.v("channelsJSONtoArrayList", "Nothing to parse. String is empty");
        }
        return channel;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static Stream streamJSONtoStream(String s) {
        String preview, curl, game;
        HashMap<String, String> hTemp = new HashMap<>();
        int viewers, id;
        Stream stream = null;

        JSONObject jStream;
        JSONObject jChannel;
        try {
            jStream = new JSONObject(s).getJSONObject("stream");
            id = jStream.getInt("_id");
            game = jStream.getString("game");
            viewers = jStream.getInt("viewers");
            curl = jStream.getJSONObject("_links").getString("self");
            preview = jStream.getJSONObject("preview").getString(BITMAP_QUALITY);

            jChannel = jStream.getJSONObject("channel");
            hTemp.put("status", jChannel.getString("status"));
            hTemp.put("display_name", jChannel.getString("display_name"));
            hTemp.put("game", jChannel.getString("game"));
            hTemp.put("_id", jChannel.getString("_id"));
            hTemp.put("name", jChannel.getString("name"));
            try {
                hTemp.put("updated_at", jChannel.getString("updated_at"));
            } catch (JSONException e) {
                hTemp.put("updated_at", "");
            }
            hTemp.put("logo", jChannel.getString("logo"));
            hTemp.put("video_banner", jChannel.getString("video_banner"));
            hTemp.put("views", jChannel.getString("views"));
            hTemp.put("url", jChannel.getString("url"));
            hTemp.put("followers", jChannel.getString("followers"));

            Channel chTemp = new Channel(hTemp);
            stream = new Stream(curl, game, viewers, preview, id, chTemp);
            return stream;

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.v("gameJSONtoArrayList", "Nothing to parse. String is empty");
        }

        return stream;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static ArrayList<Stream> streamJSONtoArrayList(String r) {
        String title, logo, preview, curl, stat, game, name;
        int viewers, id;
        ArrayList<Stream> streams = new ArrayList<>();

        JSONObject jObject;
        try {
            jObject = new JSONObject(r);
            JSONArray jArray = jObject.getJSONArray("streams");
            JSONObject channel;

            for (int i=0; i<jArray.length(); i++) {
                id = jArray.getJSONObject(i).getInt("_id");
                game = jArray.getJSONObject(i).getString("game");
                viewers = jArray.getJSONObject(i).getInt("viewers");
                curl = jArray.getJSONObject(i).getJSONObject("_links").getString("self");
                preview = jArray.getJSONObject(i).getJSONObject("preview").getString(BITMAP_QUALITY);

                channel = jArray.getJSONObject(i).getJSONObject("channel");
                name = channel.getString("name");
                title = channel.getString("display_name");
                try {
                    stat = channel.getString("status");
                } catch (JSONException e) {
                    stat = "";
                }
                logo = channel.getString("logo");

                Stream temp = new Stream(title, curl, stat, game, viewers, logo, preview, id, name);
                streams.add(temp);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.v("gameJSONtoArrayList", "Nothing to parse. String is empty");
        }

        return streams;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static ArrayList<PastBroadcast> broadcastJSONtoArrayList(String r) throws JSONException {
        String title, description, recorded_at, preview, status, game, id, length, views, broadcast_type, name, display_name;
        ArrayList<PastBroadcast> broadcasts = new ArrayList<>();
        JSONObject jObject;

        jObject = new JSONObject(r);
        JSONArray jArray = jObject.getJSONArray("videos");
        JSONObject video, channel;

        for (int i=0; i<jArray.length(); i++) {
            video = jArray.getJSONObject(i);

            title = video.getString("title");
            description = video.getString("description");
            status = video.getString("status");
            id = video.getString("_id");
            recorded_at = video.getString("recorded_at");
            game = video.getString("game");
            length = video.getString("length");
            preview = video.getString("preview");
            views = video.getString("views");
            broadcast_type = video.getString("broadcast_type");

            channel = jArray.getJSONObject(i).getJSONObject("channel");
            name = channel.getString("name");
            display_name = channel.getString("display_name");

            PastBroadcast temp = new PastBroadcast(title, description, status, id, recorded_at, game,
                    length, preview, views, broadcast_type, name, display_name);
            broadcasts.add(temp);
        }
        return broadcasts;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static String recordedAtToDate(String s) {
        int year = Integer.valueOf(s.substring(0, 4));
        int month = Integer.valueOf(s.substring(5, 7));
        int day = Integer.valueOf(s.substring(8, 10));

        int hours = Integer.valueOf(s.substring(11, 13));
        int minutes = Integer.valueOf(s.substring(14, 16));
        int seconds = Integer.valueOf(s.substring(17, 19));


        Calendar now = Calendar.getInstance();
        Calendar recorded = new GregorianCalendar(year, month-1, day, hours, minutes, seconds);

        long minDiff = (now.getTimeInMillis() - recorded.getTimeInMillis())/60000;
        long hourDiff = minDiff/60;
        long dayDiff = hourDiff/24;
        long monthDiff = dayDiff/31;

        if (minDiff < 2) return "recorded " + minDiff + " minute ago";
        if (minDiff < 60) return "recorded " + minDiff + " minutes ago";
        if (hourDiff < 2) return "recorded " + hourDiff + " hour ago";
        if (hourDiff < 24) return "recorded " + hourDiff + " hours ago";
        if (dayDiff < 2) return "recorded " + dayDiff + " day ago";
        if (dayDiff < 31) return "recorded " + dayDiff + " days ago";
        if (monthDiff < 2) return "recorded " + monthDiff + " month ago";
        if (monthDiff < 4) return "recorded " + monthDiff + " months ago";
        return "recorded on " + day + "." + month + "." + year;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static ArrayList<TwitchVideo> dataToVideoList(String s) {
        ArrayList<TwitchVideo> videos = new ArrayList<>();
        String title, description, recorded_at, preview, status, game, id, length, views;

        JSONObject jObject;
        try {
            jObject = new JSONObject(s);
            JSONArray jArray = jObject.getJSONArray("videos");
            JSONObject video;

            for (int i=0; i<jArray.length(); i++) {
                video = jArray.getJSONObject(i);

                title = video.getString("title");
                description = video.getString("description");
                status = video.getString("status");
                id = video.getString("_id");
                recorded_at = video.getString("recorded_at");
                game = video.getString("game");
                length = video.getString("length");
                preview = video.getString("preview");
                views = video.getString("views");

                TwitchVideo temp = new TwitchVideo(title, description, status, id, recorded_at, game,
                        length, preview, views);
                videos.add(temp);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return videos;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static TwitchUser userDataToUser(String s) {
        String display_name, name, bio, updated_at, type, created_at, logo;
        TwitchUser user = null;

        JSONObject jUser;
        try {
            jUser = new JSONObject(s);
            display_name = jUser.getString("display_name");
            name = jUser.getString("name");
            bio = jUser.getString("bio");
            updated_at = jUser.getString("updated_at");
            type = jUser.getString("type");
            created_at = jUser.getString("created_at");
            logo = jUser.getString("logo");

            user = new TwitchUser(display_name, name, bio, updated_at, type, created_at, logo);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.v("userDataToUser", "Nothing to parse. String is empty");
        }
        return user;
    }
}
