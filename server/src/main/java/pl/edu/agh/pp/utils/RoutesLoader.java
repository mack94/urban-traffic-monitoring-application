package pl.edu.agh.pp.utils;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

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
        createJSON();
        InputStream inputStream = new FileInputStream("." + fileName);

        String jsonTxt = IOUtils.toString(inputStream);
        JSONObject jsonObject = new JSONObject(jsonTxt);

        return jsonObject.getJSONArray("routes");
    }

    private void createJSON() throws IOException {
        File file = new File("." + fileName);
        if (!file.exists()) {
            OutputStream outputStream = new FileOutputStream(file);
            InputStream inputStream = System.class.getResourceAsStream(fileName);
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            inputStream.close();
            outputStream.close();
        }
    }

}
