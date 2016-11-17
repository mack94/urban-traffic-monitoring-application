package pl.edu.agh.pp.cron;

import com.google.maps.DirectionsApi;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DistanceMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.cron.utils.RequestParams;
import pl.edu.agh.pp.cron.utils.Route;
import pl.edu.agh.pp.detector.DetectorManager;

/**
 * Created by Jakub Janusz on 12.10.2016.
 * 18:15
 * server
 */
public class RequestsExecutor
{
    private final Logger logger = LoggerFactory.getLogger(RequestsExecutor.class);
    private final DetectorManager detectorManager;

    public RequestsExecutor(DetectorManager detectorManager)
    {
        this.detectorManager = detectorManager;
    }

    public synchronized void execute(GeoApiContext context, RequestParams requestParams) throws Exception
    {
        DistanceMatrix distanceMatrix = DistanceMatrixApi
                .getDistanceMatrix(context, requestParams.getOrigins(), requestParams.getDestinations())
                .mode(requestParams.getTravelMode())
                .language("pl")
                .departureTime(requestParams.getDeparture())
                .await();

        DirectionsResult directionsApi = DirectionsApi
                .getDirections(context, requestParams.getOrigins()[0], requestParams.getDestinations()[0])
                .alternatives(false)
                .language("pl")
                .mode(requestParams.getTravelMode())
                .departureTime(requestParams.getDeparture())
                .await();

        String defaultWaypoints = requestParams.getDefaultWaypoints();
        Route route = new Route(requestParams.getId(), distanceMatrix, directionsApi, defaultWaypoints);
        route.setAnomalyId(detectorManager.isAnomaly(route.toString(), defaultWaypoints));
        logger.error(route.toString());
    }

}
