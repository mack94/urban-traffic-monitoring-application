package pl.edu.agh.pp.cron.utils;

import com.google.maps.model.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

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

    // private String[] origins;
    // private String[] destinations;

    private String id;
    private DistanceMatrix distanceMatrix;
    private DirectionsResult directionsApi;
    private JSONObject jsonRoute;

    public Route(String id, DistanceMatrix distanceMatrix, DirectionsResult directionsApi, String defaultWaypoints) throws Exception
    {
        this.id = id;
        this.distanceMatrix = distanceMatrix;
        this.directionsApi = directionsApi;
        jsonRoute = loadRouteInfo(defaultWaypoints);
    }

    private JSONObject loadRouteInfo(String defaultWaypoints) throws Exception
    {
        DirectionsRoute[] routes = directionsApi.routes;

        return buildResult(distanceMatrix.rows[0].elements[0], routes, defaultWaypoints);
    }

    private JSONObject buildResult(DistanceMatrixElement me_element, DirectionsRoute[] routes, String defaultWaypoints)
    {

        if ("OK".equals(me_element.status.toString()))
        {
            String duration = String.valueOf(me_element.duration.inSeconds);
            String durationInTraffic = String.valueOf(me_element.durationInTraffic.inSeconds);
            String waypoints = getWaypoints(routes);
            if (waypoints.contains(defaultWaypoints))
            {
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
                    .put("isAnomaly", false);
        }
        return new JSONObject("");
    }

    private String getWaypoints(DirectionsRoute[] routes)
    {
        String result = "";

        for (DirectionsRoute route : routes)
        {
            EncodedPolyline polyline = route.overviewPolyline;
            String decodedPath = decodePolylinePath(polyline);

            result = result.concat("[" + route.summary + "], " + " [" + decodedPath + "]");
        }

        return result;
    }

    private String decodePolylinePath(EncodedPolyline polyline)
    {
        String result = "";
        List<LatLng> polylineDecodedPath = polyline.decodePath();

        for (LatLng polyPoint : polylineDecodedPath)
        {
            result = result.concat(polyPoint.toString() + "; ");
        }

        return result;
    }

    public void setAnomalyMarker()
    {
        jsonRoute.put("isAnomaly", true);
    }

    @Override
    public String toString()
    {
        return jsonRoute.toString();
    }
}
