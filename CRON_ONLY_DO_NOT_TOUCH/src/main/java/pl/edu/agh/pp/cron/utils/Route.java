package pl.edu.agh.pp.cron.utils;

import ch.qos.logback.classic.Logger;
import com.google.maps.model.*;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Maciej on 14.05.2016.
 * 23:46
 * Project: 1.
 * <p>
 * TODO: Below could be improved.
 * Please, note that the version 1.0 of this application supports only point to point routes.
 * You can only enter route with two points - start and end.
 */
public class Route {

//    private String[] origins;
//    private String[] destinations;

    private String id;
    private DistanceMatrix distanceMatrix;
    private DirectionsResult directionsApi;
    private JSONObject jsonRoute;
    private Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

    public Route(String id, DistanceMatrix distanceMatrix, DirectionsResult directionsApi, String waypoints) throws Exception {
        this.id = id;
        this.distanceMatrix = distanceMatrix;
        this.directionsApi = directionsApi;
        System.out.println("Directions API: " + directionsApi);
        jsonRoute = loadRouteInfo(waypoints);
        if (jsonRoute != null) {
            logger.error(jsonRoute.toString());
        }
    }

    private JSONObject loadRouteInfo(String waypoints) throws Exception {

        DirectionsResult directionsResult = directionsApi;
        DirectionsRoute[] routes = directionsResult.routes;


//        for (DirectionsRoute route : routes) {
////            waypoints = System.out.println(route.summary);
//            System.out.println(route.summary);
//            // ...
//            System.out.println(route.legs[0].steps[0].startLocation);
//
//        }
        return buildResult(distanceMatrix.rows[0].elements[0], routes, waypoints);
//        for (DistanceMatrixRow row: distanceMatrix.rows) {
//            for (DistanceMatrixElement element: row.elements) {
//                result = buildResult(element, routes);
//
//            }
//        }
    }

    private JSONObject buildResult(DistanceMatrixElement me_element, DirectionsRoute[] routes, String defaultWaypoints) {

        if ("OK".equals(me_element.status.toString())) {
            String duration = String.valueOf(me_element.duration.inSeconds);
            String durationInTraffic = String.valueOf(me_element.durationInTraffic.inSeconds);

            String waypoints = getWaypoints(routes);
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS").format(new Date());
            System.out.println("---------------------------" + timeStamp);

            JSONObject jsonObject = new JSONObject()
                    .put("timeStamp", timeStamp)
                    .put("id", id)
                    .put("distance", me_element.distance)
                    .put("duration", duration)
                    .put("durationInTraffic", durationInTraffic)
                    .put("isAnomaly", false);

            if(waypoints.contains(defaultWaypoints)) {
                jsonObject.put("waypoints", "default");
            } else {
                jsonObject.put("waypoints", waypoints);
            }

            return jsonObject;

//            me_result = String.format("\"me_result\": {\"Distance\": \"%s\", \"Duration\": \"%s\", \"DurationInTraffic\": \"%s\", \"Status\": \"%s\", \"Waypoints\": [\"%s\"]}",
//                    me_element.distance, duration, durationInTraffic, me_element.status, waypoints);
        }
//        else {
//            me_result = String.format("\"me_result\": {\"Distance\": \"%s\", \"Status\": \"%s\"}",
//                    me_element.distance, me_element.status);
//        }

//        String r_result = "";
//        int route_id = 0;
//        for (DirectionsRoute route : routes){
//
//            String route_result = "[summary=" + route.summary + ", legs="
//                    + Arrays.toString(route.legs) + ", waypoint_order="
//                    + Arrays.toString(route.waypointOrder) + ", overview_polyline="
//                    + route.overviewPolyline + ", bounds=" + route.bounds
//                    + ", warnings=" + Arrays.toString(route.warnings) + "]";
//
//            r_result = r_result.concat("\"id\": \"" + route_result + "\",");
//            route_id++;
//        }

//        return me_result.concat(" \"r_result\": {" + r_result + "}");
        return null;
    }

    private String getWaypoints(DirectionsRoute[] routes) {
        String result = "";

        for (DirectionsRoute route : routes) {
            EncodedPolyline polyline = route.overviewPolyline;
            String decodedPath = decodePolylinePath(polyline);

            result = result.concat("[" + route.summary + "], " + " [" + decodedPath + "]");
        }

        return result;
    }

    private String decodePolylinePath(EncodedPolyline polyline) {
        String result = "";
        List<LatLng> polylineDecodedPath = polyline.decodePath();

        for (LatLng polyPoint : polylineDecodedPath) {
            result = result.concat(polyPoint.toString() + "; ");
        }

        return result;
    }

    @Override
    public String toString() {
        return jsonRoute.toString();
    }
}
