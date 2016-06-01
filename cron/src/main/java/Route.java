import ch.qos.logback.classic.Logger;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.model.*;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

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

    private String result;

    private String id;
    private DistanceMatrix distanceMatrix;
    private DirectionsResult directionsApi;
    private Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

    public Route(String id, DistanceMatrix distanceMatrix, DirectionsResult directionsApi) throws Exception {
        this.id = id;
        this.distanceMatrix = distanceMatrix;
        this.directionsApi = directionsApi;
        System.out.println("Directions API: " + directionsApi);
        loadRouteInfo();
        logger.error(this.toString());
    }

    private void loadRouteInfo() throws Exception {

        DirectionsResult directionsResult = directionsApi;
        DirectionsRoute[] routes = directionsResult.routes;


//        for (DirectionsRoute route : routes) {
////            waypoints = System.out.println(route.summary);
//            System.out.println(route.summary);
//            // ...
//            System.out.println(route.legs[0].steps[0].startLocation);
//
//        }

        result = buildResult(distanceMatrix.rows[0].elements[0], routes);
//        for (DistanceMatrixRow row: distanceMatrix.rows) {
//            for (DistanceMatrixElement element: row.elements) {
//                result = buildResult(element, routes);
//
//            }
//        }
    }

    private String buildResult(DistanceMatrixElement me_element, DirectionsRoute[] routes) {

        String me_result;

        if (me_element.status.toString().compareTo("OK") == 0) {
            String duration = String.valueOf(me_element.duration.inSeconds);
            String durationInTraffic = String.valueOf(me_element.durationInTraffic.inSeconds);

            String waypoints = getWaypoints(routes);

            me_result = String.format("\"me_result\": {\"Distance\": \"%s\", \"Duration\": \"%s\", \"DurationInTraffic\": \"%s\", \"Status\": \"%s\", \"Waypoints\": [\"%s\"]}",
                    me_element.distance, duration, durationInTraffic, me_element.status, waypoints);
        } else {
            me_result = String.format("\"me_result\": {\"Distance\": \"%s\", \"Status\": \"%s\"}",
                    me_element.distance, me_element.status);
        }

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
        return me_result;
    }

    private String getWaypoints(DirectionsRoute[] routes) {
        String result = "";

        for (DirectionsRoute route : routes) {
            result = result.concat(route.summary);
        }

        return result;
    }

    @Override
    public String toString() {
        return "{\"" + id + "\": \"ID\" ," + result + "}";
    }
}
