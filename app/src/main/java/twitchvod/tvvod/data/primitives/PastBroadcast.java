package twitchvod.tvvod.data.primitives;

import android.graphics.Bitmap;

import java.util.HashMap;

public class PastBroadcast {
    public String title;
    public String description;
    public String status;
    public String id;
    public String recorded_at;
    public String game;
    public String length;
    public String preview;
    public String views;
    public String broadcast_type;
    public String name;
    public String display_name;

    public Bitmap previewBitmap;

    public PastBroadcast(HashMap<String, String> h) {
        this.title = h.get("title");
        this.description = h.get("description");
        this.status = h.get("status");
        this.id = h.get("id");
        this.recorded_at = h.get("recorded_at");
        this.game = h.get("game");
        this.length = h.get("length");
        this.preview = h.get("preview");
        this.views = h.get("views");
        this.broadcast_type = h.get("broadcast_type");
        this.name = h.get("name");
        this.display_name = h.get("display_name");
    }

    public PastBroadcast(String title, String description, String status, String id, String recorded_at,
                         String game, String length, String preview, String views, String broadcast_type,
                         String name, String display_name) {

        this.title = title;
        this.description = description;
        this.status = status;
        this.id = id;
        this.recorded_at = recorded_at;
        this.game = game;
        this.length = length;
        this.preview = preview;
        this.views = views;
        this.broadcast_type = broadcast_type;
        this.name = name;
        this.display_name = display_name;
    }

    public HashMap<String, String> toHashMap() {
        HashMap<String, String> h = new HashMap<>();
        h.put("title", title);
        h.put("description", description);
        h.put("status", status);
        h.put("id", id);
        h.put("recorded_at", recorded_at);
        h.put("game", game);
        h.put("length", length);
        h.put("preview", preview);
        h.put("views", views);
        h.put("broadcast_type", broadcast_type);
        h.put("name", name);
        h.put("display_name", display_name);
        return h;
    }
}