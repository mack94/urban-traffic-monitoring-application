package pl.edu.agh.pp.charts.settings;

import ch.qos.logback.classic.Logger;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;


/**
 * Created by Maciej on 08.10.2016.
 * 16:25
 * Project: charts.
 */

public class PreferencesLoader {

    private static PreferencesLoader instance = null;
    private static String fileName = "/default_preferences.json";
    private final Logger logger = (Logger) LoggerFactory.getLogger(Configuration.class);

    public static PreferencesLoader getInstance() {
        if (instance == null)
            instance = new PreferencesLoader();

        return instance;
    }

    public String getDefaultJSONFileName() {
        return fileName;
    }

    public JSONArray loadJSON() throws IOException {

        InputStream inputStream = System.class.getResourceAsStream(fileName);

        String jsonTxt = IOUtils.toString(inputStream);
        JSONObject jsonObject = new JSONObject(jsonTxt);

        logger.info("PreferencesLoader.loadJSON(): Preferences loaded from the JSON which contains default preferences");
        return jsonObject.getJSONArray("preferences");
    }
}

