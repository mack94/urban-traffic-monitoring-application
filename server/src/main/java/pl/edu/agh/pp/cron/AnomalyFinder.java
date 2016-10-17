package pl.edu.agh.pp.cron;

import com.google.maps.GeoApiContext;
import com.google.maps.model.TravelMode;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.json.JSONArray;
import org.json.JSONObject;
import pl.edu.agh.pp.detector.trackers.AnomalyTracker;
import pl.edu.agh.pp.detector.trackers.IAnomalyTracker;
import pl.edu.agh.pp.settings.Options;
import pl.edu.agh.pp.settings.exceptions.IllegalPreferenceObjectExpected;

import java.util.List;

/**
 * Created by Jakub Janusz on 12.10.2016.
 * 18:30
 * server
 */
public class AnomalyFinder extends Thread {

    private final RequestsExecutor requestsExecutor;
    private final JSONArray routes;
    private final GeoApiContext context;
    private final IAnomalyTracker anomalyTracker;
    private final int anomalyLiveTime;

    public AnomalyFinder(RequestsExecutor requestsExecutor, JSONArray routes, GeoApiContext context) throws IllegalPreferenceObjectExpected {
        this.requestsExecutor = requestsExecutor;
        this.routes = routes;
        this.context = context;
        this.anomalyTracker = AnomalyTracker.getInstance();
        this.anomalyLiveTime = (Integer) Options.getInstance().getPreference("AnomalyLiveTime", Integer.class);
    }

    @Override
    public void run() {
        while (true) {
            synchronized (requestsExecutor) {
                List<Integer> routesIds = anomalyTracker.getCurrentAnomaliesRoutesIds();
                DateTime now = DateTime.now();
                for (Integer id : routesIds) {
                    // TODO: for now it't "plus 1", needs to be changed after detector bugfix
                    id++;
                    int index = id;
                    JSONObject route = routes.getJSONObject(index);
                    String destinations[] = new String[1];
                    String origins[] = new String[1];
                    destinations[0] = route.get("destination").toString();
                    origins[0] = route.get("origin").toString();

                    TravelMode travelMode = TravelMode.DRIVING;
                    Instant departure = Instant.now();
                    try {
                        requestsExecutor.execute(String.valueOf(id), context, origins, destinations, travelMode, departure);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                Thread.sleep(1000 * 60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
