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

    private static RoutesLoader instance = null;
    private static String fileName = "/routes.json";

    public static RoutesLoader getInstance() {
        if (instance == null)
            instance = new RoutesLoader();

        return instance;
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
