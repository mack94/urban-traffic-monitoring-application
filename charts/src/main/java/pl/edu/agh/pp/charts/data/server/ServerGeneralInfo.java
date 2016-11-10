package pl.edu.agh.pp.charts.data.server;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import pl.edu.agh.pp.charts.adapters.Connector;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Maciej on 04.11.2016.
 * 17:44
 * Project: charts.
 */
public class ServerGeneralInfo {

    private static int anomalyLiveTime;
    private static int baselineWindowSize;
    private static double leverValue;
    private static int port;
    private static String routes;
    private static AnomalyOperationProtos.SystemGeneralMessage.Shift shift;
    private static String systemDate;
    //TODO: Check whether it's everything

    public static void setSystemGeneralMessage(AnomalyOperationProtos.SystemGeneralMessage systemGeneralMessage) {
        setAnomalyLiveTime(systemGeneralMessage.getAnomalyLiveTime());
        setBaselineWindowSize(systemGeneralMessage.getBaselineWindowSize());
        setLeverValue(systemGeneralMessage.getLeverValue());
        setPort(systemGeneralMessage.getPort());
        //setRoutes(systemGeneralMessage.getRoutes());
        setShift(systemGeneralMessage.getShift());
        setSystemDate(systemGeneralMessage.getSystemDate());
        informControllerAboutChanges();
    }

    public static void addRoute(AnomalyOperationProtos.RouteMessage routeMessage) {
        int routeID = routeMessage.getRouteID();
        String origin = routeMessage.getOrigin();
        String destination = routeMessage.getDestination();
        String coords = routeMessage.getCoords();
        setRoutes(String.valueOf(routeID), origin, destination, coords);
    }

    public static void setRoutes(String ID, String origin, String destination, String coords) {
        String jsonTxt;
        StringBuffer result = new StringBuffer("");
        File file = new File("./routes.json");

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

            JSONObject newRoute = new JSONObject();
            newRoute.put("id", ID);
            newRoute.put("origin", origin);
            newRoute.put("destination", destination);
            newRoute.put("coords", coords);

            loadedRoutes.put(loadedRoutesAmount, newRoute);
            jsonObject.remove("routes");
            jsonObject.put("routes", loadedRoutes);

            FileWriter fileWriter = new FileWriter(file, false);
            fileWriter.write(String.valueOf(jsonObject));
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getAnomalyLiveTime() {
        return anomalyLiveTime;
    }

    public static void setAnomalyLiveTime(int anomalyLiveTime) {
        ServerGeneralInfo.anomalyLiveTime = anomalyLiveTime;
        informControllerAboutChanges();
    }

    public static int getBaselineWindowSize() {
        return baselineWindowSize;
    }

    public static void setBaselineWindowSize(int baselineWindowSize) {
        ServerGeneralInfo.baselineWindowSize = baselineWindowSize;
        informControllerAboutChanges();
    }

    public static double getLeverValue() {
        return leverValue;
    }

    public static void setLeverValue(double leverValue) {
        ServerGeneralInfo.leverValue = leverValue;
        informControllerAboutChanges();
    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int port) {
        ServerGeneralInfo.port = port;
        informControllerAboutChanges();
    }

    public static AnomalyOperationProtos.SystemGeneralMessage.Shift getShift() {
        return shift;
    }

    public static void setShift(AnomalyOperationProtos.SystemGeneralMessage.Shift shift) {
        ServerGeneralInfo.shift = shift;
        informControllerAboutChanges();
    }

    public static String getSystemDate() {
        return systemDate;
    }

    public static void setSystemDate(String systemDate) {
        ServerGeneralInfo.systemDate = systemDate;
        informControllerAboutChanges();
    }

    public static String getRoutesJSON() {
        String routesJSON = "";
        StringBuffer result = new StringBuffer("");
        File file = new File("./routes.json");
        try {
            if (file.length() == 0) {
                // Loads the default structure of the file, if it's empty or not exists.
                routesJSON = "{\"routes\": []}";
            } else {
                // Loads the content if the file is not empty and exists.
                routesJSON = IOUtils.toString(new FileInputStream(file));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return routesJSON;
    }

    private static void informControllerAboutChanges() {
        Connector.updateServerInfo(leverValue, anomalyLiveTime, baselineWindowSize, shift, port);
    }

}
