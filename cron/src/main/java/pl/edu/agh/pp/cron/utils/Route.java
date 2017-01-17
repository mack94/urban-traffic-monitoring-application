package pl.edu.agh.pp.cron.utils;

import ch.qos.logback.classic.Logger;
import com.google.maps.model.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;
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
public class Route
{

    private String id;
    private DistanceMatrix distanceMatrix;
    private DirectionsResult directionsApi;
    private JSONObject jsonRoute;
    private Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

    public Route(String id, DistanceMatrix distanceMatrix, DirectionsResult directionsApi, String waypoints) throws Exception
    {
        this.id = id;
        this.distanceMatrix = distanceMatrix;
        this.directionsApi = directionsApi;
        jsonRoute = loadRouteInfo(waypoints);
        if (jsonRoute != null)
        {
            logger.error(jsonRoute.toString());
        }
    }

    private JSONObject loadRouteInfo(String waypoints) throws Exception
    {

        DirectionsResult directionsResult = directionsApi;
        DirectionsRoute[] routes = directionsResult.routes;

        return buildResult(distanceMatrix.rows[0].elements[0], routes, waypoints);
    }

    private JSONObject buildResult(DistanceMatrixElement me_element, DirectionsRoute[] routes, String defaultWaypoints)
    {

        if ("OK".equals(me_element.status.toString()))
        {
            String duration = String.valueOf(me_element.duration.inSeconds);
            String durationInTraffic = String.valueOf(me_element.durationInTraffic.inSeconds);

            String waypoints = getWaypoints(routes);
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS").format(new Date());

            JSONObject jsonObject = new JSONObject()
                    .put("timeStamp", timeStamp)
                    .put("id", id)
                    .put("distance", me_element.distance)
                    .put("duration", duration)
                    .put("durationInTraffic", durationInTraffic)
                    .put("anomalyId", "");

            if (waypoints.contains(defaultWaypoints))
            {
                jsonObject.put("waypoints", "default");
            }
            else
            {
                jsonObject.put("waypoints", waypoints);
            }

            return jsonObject;
        }
        return null;
    }

    private String getWaypoints(DirectionsRoute[] routes)
    {
        String result = "";

        for (DirectionsRoute route : routes)
        {
            result = result.concat(decodePolylinePath(route.overviewPolyline));
        }

        return result;
    }

    private String decodePolylinePath(EncodedPolyline polyline)
    {
        List<LatLng> polylineDecodedPath = polyline.decodePath();

        return polylineDecodedPath.stream()
                .map(LatLng::toString)
                .collect(Collectors.joining(";"));
    }

    @Override
    public String toString()
    {
        return jsonRoute.toString();
    }
}
