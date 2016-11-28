package pl.edu.agh.pp.charts.data.server;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maciej on 05.11.2016.
 * 16:09
 * Project: charts.
 */
public class ServerRoutesInfo {

    private static JSONObject getRouteInfo(int routeID) {
        String jsonTxt;
        StringBuffer result = new StringBuffer("");
        File file = new File("./routes.json");
        JSONObject foundRoute = null;

        try {
            if (file.length() == 0) {
                // Loads the default structure of the file, if it's empty or not exists.
                jsonTxt = "{\"routes\": []}";
            } else {
                // Loads the content if the file is not empty and exists.
                jsonTxt = IOUtils.toString(new FileInputStream(file));
            }
            JSONObject jsonObject = new JSONObject(jsonTxt);
            JSONArray loadedRoutes = jsonObject.getJSONArray("routes");
            int loadedRoutesAmount = loadedRoutes.length();

            int i = 0;
            while (i < loadedRoutesAmount && foundRoute == null) {
                JSONObject route = loadedRoutes.getJSONObject(i);
                String ID = route.get("id").toString();
                if (ID.compareTo(String.valueOf(routeID)) == 0) {
                    foundRoute = route;
                }
                i++;
            }

//            FileReader fileReader = new FileReader(file);
//
//            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (foundRoute == null) {
            System.out.println("############## Error : Route not found!"); // FIXME
            // new exception (?)
        }

        return foundRoute;
    }

    public static String getRouteCoordsStart(int routeID) {
        JSONObject route = getRouteInfo(routeID);
        if (route != null) {
            String coordsRaw = route.get("coords").toString();
            String[] coordsArray = coordsRaw.split(";");
            return coordsArray[0];
        }
        return null;
    }

    public static String getRouteCoordsEnd(int routeID) {
        int coordsAmount = 0;
        JSONObject route = getRouteInfo(routeID);
        if (route != null) {
            String coordsRaw = route.get("coords").toString();
            String[] coordsArray = coordsRaw.split(";");
            coordsAmount = coordsArray.length;
            return coordsArray[coordsAmount - 1];
        }
        return null;
    }

    public static List<String> getRoutes() {
        String jsonTxt;
        StringBuffer result = new StringBuffer("");
        File file = new File("./routes.json");
        List<String> list = new ArrayList<>();
        try {
            if (file.length() == 0) {
                jsonTxt = "{\"routes\": []}";
            } else {
                jsonTxt = IOUtils.toString(new FileInputStream(file));
            }
            JSONObject jsonObject = new JSONObject(jsonTxt);
            JSONArray loadedRoutes = jsonObject.getJSONArray("routes");
            int loadedRoutesAmount = loadedRoutes.length();

            int i = 0;
            while (i < loadedRoutesAmount) {
                JSONObject route = loadedRoutes.getJSONObject(i);
                list.add(route.get("id").toString() + " " + route.get("name"));
                i++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return list;
    }

    public static String getId(String screenId) {
        System.out.println("-----------------");
        return screenId.replaceAll("([\\d]+).*", "$1");
    }
}
