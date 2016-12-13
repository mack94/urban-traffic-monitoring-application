package pl.edu.agh.pp.cron;

import com.google.maps.GeoApiContext;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.Calendar;

import org.joda.time.Instant;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.adapters.AnomaliesServer;
import pl.edu.agh.pp.repeater.AnomalyRepeater;
import pl.edu.agh.pp.utils.ContextLoader;
import pl.edu.agh.pp.utils.RequestParams;
import pl.edu.agh.pp.utils.RoutesLoader;
import pl.edu.agh.pp.utils.Timer;
import pl.edu.agh.pp.detectors.DetectorManager;

public class CronManager {

    private final Logger logger = (Logger) LoggerFactory.getLogger(DetectorManager.class);
    private final static int MANAGMENT_DELAY_SECONDS = 10;

    private AnomaliesServer anomaliesServer;
    private RoutesLoader routesLoader;
    private JSONArray loadedRoutes;
    private int loadedRoutesAmount;
    private DetectorManager detectorManager;

    public CronManager(AnomaliesServer anomaliesServer) {
        this.anomaliesServer = anomaliesServer;
    }


    public void getAPIData(String logFile) throws InterruptedException {

        try {
            GeoApiContext context;
            ContextLoader contextLoader = new ContextLoader();
            detectorManager = new DetectorManager(anomaliesServer, logFile);
            context = contextLoader.geoApiContextLoader();
            routesLoader = RoutesLoader.getInstance();
            reloadRoutes();
            Timer timer = Timer.getInstance();
            RequestsExecutor requestsExecutor = new RequestsExecutor(detectorManager);
            AnomalyRepeater anomalyRepeater = new AnomalyRepeater(requestsExecutor, loadedRoutes, context);
            anomalyRepeater.start();

            while (true) {
                try {
                    for (int i = 0; i < loadedRoutesAmount; i++) {
                        JSONObject route = loadedRoutes.getJSONObject(i);
                        RequestParams requestParams = new RequestParams()
                                .withId(route.get("id").toString())
                                .withOrigins(new String[]{route.get("origin").toString()})
                                .withDestinations(new String[]{route.get("destination").toString()})
                                .withTravelMode(TravelMode.DRIVING)
                                .withDeparture(Instant.now())
                                .withDefaultWaypoints(route.getString("coords"));

                        requestsExecutor.execute(context, requestParams);
                    }

                    long waitingTime = timer.getWaitingTime(Calendar.getInstance());
                    Thread.sleep(waitingTime);
                    reloadRoutes();
                } catch (Exception e) {
                    // TODO: Catch this.
                    logger.error("CronManager: Some Exception occured while executing requestsExecutor: " + e, e);
                    System.out.println("Please, check your API KEY. It's very likely that new key required.");
                    Thread.sleep(MANAGMENT_DELAY_SECONDS*1000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reloadRoutes() throws IOException, InterruptedException {
        boolean computeBaseline = false;
        boolean routesChanged = false;
        while (true) {
            if(loadedRoutes != null){
                JSONArray temp = routesLoader.loadJSON();
                if(temp.length() != loadedRoutes.length()){
                    routesChanged = true;
                }else{
                    for (int i = 0; i < temp.length(); i++) {
                        if(!temp.getJSONObject(i).similar(loadedRoutes.getJSONObject(i))){
                            routesChanged = true;
                            break;
                        }
                    }
                }
                for (int i = 0; i < loadedRoutesAmount; i++) {
                    loadedRoutes.remove(0);
                }
                temp.forEach(object -> loadedRoutes.put(object));
            }
            else{
                loadedRoutes = routesLoader.loadJSON();
            }

            loadedRoutesAmount = loadedRoutes.length();
            if (loadedRoutesAmount == 0) {
                logger.error("File routes.json doesn't contain any routes, please fill the file with appropriate values");
                Thread.sleep(MANAGMENT_DELAY_SECONDS*1000);
                continue;
            }

            if (!detectorManager.areAllRoutesIncluded(loadedRoutes)) {
                logger.error("Supplied historical data does not coincide with chosen routes. Check your Routes.json " +
                        "file and data in logs directory");
                Thread.sleep(MANAGMENT_DELAY_SECONDS*1000);
                DetectorManager.refreshBaselineFilesLoader();
                computeBaseline = true;
                continue;
            }

            if(computeBaseline || routesChanged) {
                DetectorManager.computeBaselineFromDefaultLogsLocation();
            }
            return;
        }
    }
}
