package pl.edu.agh.pp.cron;

import com.google.maps.DirectionsApi;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.TravelMode;
import org.joda.time.Instant;
import org.json.JSONArray;
import org.json.JSONObject;
import pl.edu.agh.pp.cron.utils.ContextLoader;
import pl.edu.agh.pp.cron.utils.Route;
import pl.edu.agh.pp.cron.utils.RoutesLoader;

public class Main
{

    public static void main(String args[]) throws InterruptedException
    {

        GeoApiContext context;
        ContextLoader contextLoader = new ContextLoader();
        RoutesLoader routesLoader = RoutesLoader.getInstance();

        try
        {
            JSONArray loadedRoutes = routesLoader.loadJSON();
            context = contextLoader.geoApiContextLoader();

            int loadedRoutesAmount = loadedRoutes.length();

            for (int i = 0; i < loadedRoutesAmount; i++)
            {
                JSONObject object = loadedRoutes.getJSONObject(i);
                String destinations[] = new String[1];
                String origins[] = new String[1];
                String id = object.get("id").toString();
                destinations[0] = object.get("destination").toString();
                origins[0] = object.get("origin").toString();
                String waypoints = object.get("coords").toString();

                // DistanceMatrixApiRequest distanceMatrixApiRequest = new DistanceMatrixApiRequest(context);
                TravelMode travelMode = TravelMode.DRIVING;
                Instant departure = Instant.now();

                DistanceMatrix distanceMatrix = DistanceMatrixApi
                        .getDistanceMatrix(context, origins, destinations)
                        .mode(travelMode)
                        .language("pl")
                        .departureTime(departure)
                        .await();

                DirectionsResult directionsApi = DirectionsApi
                        .getDirections(context, origins[0], destinations[0])
                        .alternatives(false)
                        .language("pl")
                        .departureTime(departure)
                        .await();

                new Route(id, distanceMatrix, directionsApi, waypoints);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
