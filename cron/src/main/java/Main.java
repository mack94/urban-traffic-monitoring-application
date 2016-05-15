import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.*;
import org.joda.time.ReadableInstant;
import org.json.JSONArray;

public class Main {

    public static void main(String args[]){

        GeoApiContext context;
        ContextLoader contextLoader = new ContextLoader();
        RoutesLoader routesLoader = new RoutesLoader();

        try {
            JSONArray loadedRoutes = routesLoader.loadJSON();
            context = contextLoader.geoApiContextLoader();

            int loadedRoutesAmount = loadedRoutes.length();

            for (int i = 0; i < loadedRoutesAmount; i++) {
                String destinations[] = new String[1];
                String origins[] = new String[1];
                String id = loadedRoutes.getJSONObject(i).get("id").toString();
                destinations[0] = loadedRoutes.getJSONObject(i).get("destination").toString();
                origins[0] = loadedRoutes.getJSONObject(i).get("origin").toString();

//                DistanceMatrixApiRequest distanceMatrixApiRequest = new DistanceMatrixApiRequest(context);
                TravelMode travelMode = TravelMode.DRIVING;

                DistanceMatrix distanceMatrix = DistanceMatrixApi.getDistanceMatrix(context, destinations, origins).mode(travelMode).await();
                DirectionsApiRequest directionsApi = DirectionsApi.getDirections(context, destinations[0], origins[0]).alternatives(true);
                Route route = new Route(id, distanceMatrix, directionsApi);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}