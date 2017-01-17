package pl.edu.agh.pp.cron;

import com.google.maps.DirectionsApi;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DistanceMatrix;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.detectors.DetectorManager;
import pl.edu.agh.pp.halfroute.HalfRouteManager;
import pl.edu.agh.pp.loaders.InputParser;
import pl.edu.agh.pp.utils.Record;
import pl.edu.agh.pp.utils.RequestParams;
import pl.edu.agh.pp.utils.Route;

/**
 * Created by Jakub Janusz on 12.10.2016.
 * 18:15
 * server
 */
public class RequestsExecutor {
    private final Logger trafficLogger = LoggerFactory.getLogger("traffic");
    private final Logger logger = LoggerFactory.getLogger(RequestsExecutor.class);
    private final DetectorManager detectorManager;
    private final InputParser inputParser = new InputParser();

    public RequestsExecutor(DetectorManager detectorManager) {
        this.detectorManager = detectorManager;
    }

    public synchronized void execute(GeoApiContext context, RequestParams requestParams) throws Exception {
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
        String logEntry = route.toString();
        Record record = inputParser.parse(logEntry);

//        String alternativeLogEntry = null;
//        if (!"default".equals(record.getWaypoints())) {
//            HalfRouteManager halfRouteManager = new HalfRouteManager(record, defaultWaypoints);
//            alternativeLogEntry = logEntry;
//            logEntry = halfRouteManager.splitRoute();
//            record = inputParser.parse(logEntry);
//        }

        String anomalyId = "";
        if (!requestParams.isMissingHistoricalData()) {
            anomalyId = detectorManager.isAnomaly(record);
        }
//        if (alternativeLogEntry != null) {
//            addAnomalyIdAndLog(alternativeLogEntry, anomalyId);
//        }
        addAnomalyIdAndLog(logEntry, anomalyId);

    }

    private void addAnomalyIdAndLog(String entry, String anomalyID) {
        JSONObject jsonObject = new JSONObject(entry);
        jsonObject.put("anomalyId", anomalyID);

        trafficLogger.error(jsonObject.toString());
        logger.error(jsonObject.toString());
    }

}
