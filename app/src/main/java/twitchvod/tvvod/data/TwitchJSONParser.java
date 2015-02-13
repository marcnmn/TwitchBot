package twitchvod.tvvod.data;

import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import twitchvod.tvvod.data.primitives.Channel;
import twitchvod.tvvod.data.primitives.Game;
import twitchvod.tvvod.data.primitives.PastBroadcast;
import twitchvod.tvvod.data.primitives.Stream;

/**
 * Created by marc on 11.02.2015.
 */
public final class TwitchJSONParser {
    private TwitchJSONParser() {
    }

    public static ArrayList<Game> gameJSONtoArrayList(String r) throws JSONException {
        String title, thumb;
        int viewers, channelc, id;
        JSONObject game;

        ArrayList<Game> games = new ArrayList<>();

        JSONObject jObject = new JSONObject(r);
        JSONArray jArray = jObject.getJSONArray("top");

        for (int i=0; i<jArray.length(); i++) {
            viewers = jArray.getJSONObject(i).getInt("viewers");
            channelc = jArray.getJSONObject(i).getInt("channels");

            game = jArray.getJSONObject(i).getJSONObject("game");
            title = game.getString("name");
            id = game.getInt("_id");
            thumb = game.getJSONObject("box").getString("medium");
            Game temp = new Game(title,thumb,viewers,channelc,id,null);
            games.add(temp);
        }
        return games;
    }

    public static ArrayList<Channel> channelJSONtoArrayList(String r) {
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
            if (request_type == "follows") j = j.getJSONObject("channel");
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
            htemp.put("game", j.getString("game"));
            htemp.put("name", j.getString("name"));
            htemp.put("url", j.getString("url"));
            htemp.put("_id", j.getString("_id"));
            channels.add(new Channel((HashMap<String,String>) htemp.clone()));
            htemp.clear();
        }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.v("TwitchBot channelJSONtoArrayList", "no JSON Data");
        }

        return channels;
    }

    public static ArrayList<Stream> streamJSONtoArrayList(String r) throws JSONException {
        String title, logo, preview, curl, stat, game;
        int viewers, id;
        ArrayList<Stream> streams = new ArrayList<>();

        JSONObject jObject;
        jObject = new JSONObject(r);
        JSONArray jArray = jObject.getJSONArray("streams");
        JSONObject channel;

        for (int i=0; i<jArray.length(); i++) {
            id = jArray.getJSONObject(i).getInt("_id");
            game = jArray.getJSONObject(i).getString("game");
            viewers = jArray.getJSONObject(i).getInt("viewers");
            curl = jArray.getJSONObject(i).getJSONObject("_links").getString("self");

            preview = jArray.getJSONObject(i).getJSONObject("preview").getString("large");

            channel = jArray.getJSONObject(i).getJSONObject("channel");
            title = channel.getString("display_name");
            try {
                stat = channel.getString("status");
            } catch (JSONException e) {
                stat = "";
            }
            logo = channel.getString("logo");

            Stream temp = new Stream(title, curl, stat, game, viewers, logo, preview, id);
            streams.add(temp);
        }
        return streams;
    }

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

    public  static String recordedAtToDate(String s) {
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
}
