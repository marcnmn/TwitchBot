package twitchvod.src.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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
}
