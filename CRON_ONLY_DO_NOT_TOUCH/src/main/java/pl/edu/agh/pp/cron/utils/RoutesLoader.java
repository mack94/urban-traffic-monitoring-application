package pl.edu.agh.pp.cron.utils;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Maciej on 15.05.2016.
 * 00:14
 * Project: 1.
 */
public class RoutesLoader {

    /**
     * Thread-safe singleton.
     * But not allow multiple singleton instances.
     */
    private final static RoutesLoader INSTANCE = new RoutesLoader();
    private static String fileName = "/routes.json";

    private RoutesLoader() {
    }

    public static RoutesLoader getInstance() {
        return INSTANCE;
    }

    public String getDefaultJSONFileName() {
        return fileName;
    }

    public JSONArray loadJSON() throws IOException {

        InputStream inputStream = System.class.getResourceAsStream(fileName);

        String jsonTxt = IOUtils.toString(inputStream);
        JSONObject jsonObject = new JSONObject(jsonTxt);

        return jsonObject.getJSONArray("routes");
    }

}
