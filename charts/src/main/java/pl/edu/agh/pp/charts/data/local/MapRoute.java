package pl.edu.agh.pp.charts.data.local;

/**
 * Created by Krzysztof on 2016-11-19.
 */
public class MapRoute {
    private String startLat;
    private String startLng;
    private String endLat;
    private String endLng;

    public MapRoute(String startLat, String startLng, String endLat, String endLng) {
        this.startLat = startLat;
        this.startLng = startLng;
        this.endLat = endLat;
        this.endLng = endLng;
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
        stringBuilder.append(" new google.maps.DirectionsRenderer({map: map})");
        stringBuilder.append(",");
        stringBuilder.append(" directionsService");
        stringBuilder.append(",");
        stringBuilder.append(" {lat: " + startLat + "," + "lng: " + startLng + "}");
        stringBuilder.append(",");
        stringBuilder.append(" {lat: " + endLat + "," + "lng: " + endLng + "}");
        stringBuilder.append(",");
        stringBuilder.append(" map");
        stringBuilder.append(");");

        return stringBuilder.toString();
    }
}
