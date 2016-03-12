package net.chiragaggarwal.android.popflix.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Videos {
    private static final String RESULTS = "results";
    private ArrayList<Video> videos;

    public Videos() {
        this.videos = new ArrayList<>();
    }

    public static Videos fromJson(JSONObject videosJsonObject) throws JSONException {
        JSONArray videosResults = videosJsonObject.getJSONArray(RESULTS);
        Videos videos = new Videos();
        for (Integer videoIndex = 0; videoIndex < videosResults.length(); videoIndex++) {
            JSONObject videoJsonObject = videosResults.getJSONObject(videoIndex);
            Video video = Video.fromJson(videoJsonObject);
            videos.add(video);
        }
        return videos;
    }

    private void add(Video video) {
        this.videos.add(video);
    }
}
