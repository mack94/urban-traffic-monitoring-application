package pl.edu.agh.pp.charts.data.local;

import pl.edu.agh.pp.charts.data.server.Anomaly;
import pl.edu.agh.pp.charts.data.server.ServerRoutesInfo;

/**
 * Created by Krzysztof on 2016-11-19.
 */
public class MapRoute {
    private String startLat;
    private String startLng;
    private String endLat;
    private String endLng;
    private String routeId;

    public MapRoute(String startLat, String startLng, String endLat, String endLng) {
        this.startLat = startLat;
        this.startLng = startLng;
        this.endLat = endLat;
        this.endLng = endLng;
    }

    public MapRoute(String startLat, String startLng, String endLat, String endLng, String routeId) {
        this(startLat, startLng, endLat, endLng);
        this.routeId = routeId;
    }

    public MapRoute(Anomaly anomaly) {
        String startCoord = ServerRoutesInfo.getRouteCoordsStart(Integer.parseInt(anomaly.getRouteId()));
        String endCoord = ServerRoutesInfo.getRouteCoordsEnd(Integer.parseInt(anomaly.getRouteId()));

        this.startLat = startCoord.split(",")[0];
        this.startLng = startCoord.split(",")[1];
        this.endLat = endCoord.split(",")[0];
        this.endLng = endCoord.split(",")[1];
        this.routeId = anomaly.getRouteId();
    }

    public String getStartLat() {
        return startLat;
    }

    public void setStartLat(String startLat) {
        this.startLat = startLat;
    }

    public String getStartLng() {
        return startLng;
    }

    public void setStartLng(String startLng) {
        this.startLng = startLng;
    }

    public String getEndLat() {
        return endLat;
    }

    public void setEndLat(String endLat) {
        this.endLat = endLat;
    }

    public String getEndLng() {
        return endLng;
    }

    public void setEndLng(String endLng) {
        this.endLng = endLng;
    }

    public String getRouteJavaScriptInstruction() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("calculateAndDisplayRoute(");
        stringBuilder.append(" directionsService");
        stringBuilder.append(",");
        stringBuilder.append(" {lat: " + startLat + "," + "lng: " + startLng + "}");
        stringBuilder.append(",");
        stringBuilder.append(" {lat: " + endLat + "," + "lng: " + endLng + "}");
        stringBuilder.append(",");
        stringBuilder.append(" \"" + routeId + "\"");
        stringBuilder.append(",");
        stringBuilder.append(" \'" + "#" +Colors.getNextColor().toString().substring(2,8).toUpperCase()+"'");
        stringBuilder.append(",");
        stringBuilder.append(" map");
        stringBuilder.append(");");

        return stringBuilder.toString();
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }
}
