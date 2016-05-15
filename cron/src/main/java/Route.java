import ch.qos.logback.classic.Logger;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import org.slf4j.LoggerFactory;

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
    private DirectionsApiRequest directionsApi;
    private Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

    public Route(String id, DistanceMatrix distanceMatrix, DirectionsApiRequest directionsApi) throws Exception {
        this.id = id;
        this.distanceMatrix = distanceMatrix;
        this.directionsApi = directionsApi;
        loadRouteInfo();
        logger.error(this.toString());
    }

    private void loadRouteInfo() throws Exception {

//        DirectionsResult directionsResult = directionsApi.await();
        DirectionsRoute[] routes = null;
//
//
//        for (DirectionsRoute route : routes) {
//            // waypoints = System.out.println(route.summary);
//            // ...
//            //System.out.println(route.legs[0].steps[0].startLocation);
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
        String me_result = String.format("\"me_result\": {\"Distance\": \"%s\", \"Duration\": \"%s\", \"DurationInTraffic\": \"%s\", \"Fare\": \"%s\", \"Status\": \"%s\"}",
                me_element.distance, me_element.duration.inSeconds, me_element.durationInTraffic.inSeconds, me_element.fare, me_element.status);
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

    @Override
    public String toString() {
        return "{\"" + id + "\": \"ID\" ," + result + "}";
    }
}
