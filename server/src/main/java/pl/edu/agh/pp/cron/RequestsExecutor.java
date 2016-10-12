package pl.edu.agh.pp.cron;

import com.google.maps.DirectionsApi;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.TravelMode;
import org.joda.time.Instant;
import pl.edu.agh.pp.cron.utils.Route;
import pl.edu.agh.pp.detector.DetectorManager;

/**
 * Created by Jakub Janusz on 12.10.2016.
 * 18:15
 * server
 */
public class RequestsExecutor {

    private final DetectorManager detectorManager;

    public RequestsExecutor(DetectorManager detectorManager) {
        this.detectorManager = detectorManager;
    }

    public synchronized void execute(String id, GeoApiContext context, String[] origins, String[] destinations, TravelMode travelMode, Instant departure) throws Exception {
        DistanceMatrix distanceMatrix = DistanceMatrixApi
                .getDistanceMatrix(context, origins, destinations)
                .mode(travelMode)
                .language("pl")
                .departureTime(departure)
                .await();

        DirectionsResult directionsApi = DirectionsApi
                .getDirections(context, destinations[0], origins[0])
                .alternatives(false)
                .language("pl")
                .departureTime(departure)
                .await();

        detectorManager.doSomething(new Route(id, distanceMatrix, directionsApi).toString());
    }

}
