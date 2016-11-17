package pl.edu.agh.pp.cron.utils;

import com.google.maps.DirectionsApi;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.TravelMode;
import org.joda.time.Instant;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import static org.junit.Assert.*;

/**
 * Created by Krzysztof Węgrzyński on 2016-11-12.
 */
public class RouteTest
{
    private String defaultRoutesLoaderFileName;
    private Field routesLoaderFileNameField;
    private DistanceMatrix distanceMatrix;
    private DirectionsResult directionsApi;
    private String defaultWaypoints;
    private String id;

    @Before
    public void setUp() throws Exception
    {
        ContextLoader contextLoader = new ContextLoader();
        Field contexLoaderFileNameField = contextLoader.getClass().getDeclaredField("propertiesFileName");
        contexLoaderFileNameField.setAccessible(true);
        contexLoaderFileNameField.set(contextLoader, "/test/test_config.properties");

        RoutesLoader routesLoaderInstance = RoutesLoader.getInstance();
        routesLoaderFileNameField = RoutesLoader.class.getDeclaredField("fileName");
        routesLoaderFileNameField.setAccessible(true);
        defaultRoutesLoaderFileName = routesLoaderInstance.getDefaultJSONFileName();
        routesLoaderFileNameField.set(RoutesLoader.getInstance(), "/test/test_routes.json");

        JSONArray loadedRoutes = routesLoaderInstance.loadJSON();

        JSONObject JSONroute = loadedRoutes.getJSONObject(0);
        GeoApiContext context = contextLoader.geoApiContextLoader();
        String[] origins = new String[1];
        String[] destinations = new String[1];
        id = JSONroute.get("id").toString();
        destinations[0] = JSONroute.get("destination").toString();
        origins[0] = JSONroute.get("origin").toString();
        defaultWaypoints = JSONroute.getString("coords");

        TravelMode travelMode = TravelMode.DRIVING;
        Instant departure = Instant.now();

        distanceMatrix = DistanceMatrixApi
                .getDistanceMatrix(context, origins, destinations)
                .mode(travelMode)
                .language("pl")
                .departureTime(departure)
                .await();

        directionsApi = DirectionsApi
                .getDirections(context, origins[0], destinations[0])
                .alternatives(false)
                .language("pl")
                .departureTime(departure)
                .await();
    }

    @After
    public void tearDown() throws Exception
    {
        routesLoaderFileNameField.set(RoutesLoader.getInstance(), defaultRoutesLoaderFileName);
    }

    @Test
    public void Route() throws Exception
    {

        Route route = new Route(id, distanceMatrix, directionsApi, defaultWaypoints);
        String record = route.toString();

        assertTrue(record.length() > 0);

        assertFalse(record.contains("\"Status\": \"NOT_FOUND\""));

        // sprawdzanie czy rekord zawiera odpowiednie pola
        assertTrue(record.contains("timeStamp"));
        assertTrue(record.contains("duration"));
        assertTrue(record.contains("durationInTraffic"));
        assertTrue(record.contains("distance"));
        assertTrue(record.contains("id"));
        assertTrue(record.contains("waypoints"));
        assertTrue(record.contains("anomalyId"));
    }

}
