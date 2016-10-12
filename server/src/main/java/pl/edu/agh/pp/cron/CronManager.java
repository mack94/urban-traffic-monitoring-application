package pl.edu.agh.pp.cron;

import com.google.maps.GeoApiContext;
import com.google.maps.model.TravelMode;
import org.joda.time.Instant;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.cron.utils.ContextLoader;
import pl.edu.agh.pp.cron.utils.RoutesLoader;
import pl.edu.agh.pp.cron.utils.Timer;
import pl.edu.agh.pp.detector.DetectorManager;
import pl.edu.agh.pp.detector.adapters.Server;

public class CronManager {

    private final Logger logger = (Logger) LoggerFactory.getLogger(DetectorManager.class);

    private Server server;

    public CronManager(Server server) {
        this.server = server;
    }

    public void doSomething(String logFile) throws InterruptedException {

        try {
            GeoApiContext context;
            ContextLoader contextLoader = new ContextLoader();
            RoutesLoader routesLoader = RoutesLoader.getInstance();
            JSONArray loadedRoutes = routesLoader.loadJSON();
            context = contextLoader.geoApiContextLoader();
            DetectorManager detectorManager = new DetectorManager(server, logFile);
            Timer timer = Timer.getInstance();
            RequestsExecutor requestsExecutor = new RequestsExecutor(detectorManager);
            AnomalyFinder anomalyFinder = new AnomalyFinder(requestsExecutor, loadedRoutes, context);
            anomalyFinder.start();

            while (true) {
                int loadedRoutesAmount = loadedRoutes.length();

                for (int i = 0; i < loadedRoutesAmount; i++) {
                    JSONObject route = loadedRoutes.getJSONObject(i);
                    String destinations[] = new String[1];
                    String origins[] = new String[1];
                    String id = route.get("id").toString();
                    destinations[0] = route.get("destination").toString();
                    origins[0] = route.get("origin").toString();

                    TravelMode travelMode = TravelMode.DRIVING;
                    Instant departure = Instant.now();

                    requestsExecutor.execute(id, context, origins, destinations, travelMode, departure);
                }

                long waitingTime = timer.getWaitingTime();
                Thread.sleep(waitingTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}