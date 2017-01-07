package pl.edu.agh.pp.utils;

import com.google.maps.model.*;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Maciej on 14.05.2016.
 * 23:46
 * Project: 1.
 * <p>
 * Please, note that the version 1.0 of this application supports only point to point routes.
 * You can only enter route with two points - start and end.
 */
public class Route {

    private String id;
    private DistanceMatrix distanceMatrix;
    private DirectionsResult directionsApi;
    private JSONObject jsonRoute;

    public Route(String id, DistanceMatrix distanceMatrix, DirectionsResult directionsApi, String defaultWaypoints) throws Exception {
        this.id = id;
        this.distanceMatrix = distanceMatrix;
        this.directionsApi = directionsApi;
        jsonRoute = loadRouteInfo(defaultWaypoints);
    }

    private JSONObject loadRouteInfo(String defaultWaypoints) throws Exception {
        DirectionsRoute[] routes = directionsApi.routes;

        return buildResult(distanceMatrix.rows[0].elements[0], routes, defaultWaypoints);
    }

    private JSONObject buildResult(DistanceMatrixElement me_element, DirectionsRoute[] routes, String defaultWaypoints) {
        if ("OK".equals(me_element.status.toString())) {
            String duration = String.valueOf(me_element.duration.inSeconds);
            String durationInTraffic = String.valueOf(me_element.durationInTraffic.inSeconds);
            String waypoints = getWaypoints(routes);
            if (waypoints.contains(defaultWaypoints)) {
                waypoints = "default";
            }
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS").format(new Date());

            return new JSONObject()
                    .put("timeStamp", timeStamp)
                    .put("id", id)
                    .put("distance", me_element.distance)
                    .put("duration", duration)
                    .put("durationInTraffic", durationInTraffic)
                    .put("waypoints", waypoints)
                    .put("anomalyId", "");
        }
        return new JSONObject("");
    }

    private String getWaypoints(DirectionsRoute[] routes) {
        String result = "";

        for (DirectionsRoute route : routes) {
            result = result.concat(decodePolylinePath(route.overviewPolyline));
        }

        return result;
    }

    private String decodePolylinePath(EncodedPolyline polyline) {
        List<LatLng> polylineDecodedPath = polyline.decodePath();

        return polylineDecodedPath.stream()
                .map(LatLng::toString)
                .collect(Collectors.joining(";"));
    }

    public void setAnomalyId(String anomalyId) {
        jsonRoute.put("anomalyId", anomalyId);
    }

    @Override
    public String toString() {
        return jsonRoute.toString();
    }
}
