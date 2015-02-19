package twitchvod.src.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import twitchvod.src.data.primitives.Channel;
import twitchvod.src.data.primitives.Game;
import twitchvod.src.data.primitives.PastBroadcast;
import twitchvod.src.data.primitives.Stream;

/**
 * Created by marc on 11.02.2015.
 */
public final class TwitchNetworkTasks {
    private TwitchNetworkTasks() {
    }

    public static Bitmap downloadBitmap(String myurl) {
        InputStream is;
        Bitmap bitmap = null;
        URL url;
        try {
            url = new URL(myurl);
            HttpURLConnection conn = null;
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();
            is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static JSONObject downloadJSONData(String myurl) {
        InputStream is = null;
        String result;
        JSONObject jObject = null;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            is = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line).append("\n");
            }
            result = sb.toString();
            jObject = new JSONObject(result);

        } catch (MalformedURLException | JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return jObject;
    }

    public static HashMap<String, String> fetchTwitchPlaylist(String myurl) {
        InputStream is = null;
        HashMap<String, String> hmap = new HashMap<>();
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            try {
                conn.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            is = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);

            String q, u;
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (line.contains("http")) {
                    u = line;
                    q = getQuality(line);
                    hmap.put(q, u);
                }
            }
            return hmap;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return hmap;
    }

    private static String getQuality(String s) {
        String q = "none";
        if (s.contains("chunked")) return "source";
        if (s.contains("high")) return "high";
        if (s.contains("medium")) return "medium";
        if (s.contains("low")) return "low";
        if (s.contains("mobile")) return "mobile";
        if (s.contains("audio_only")) return "audio_only";
        return q;
    }
}
