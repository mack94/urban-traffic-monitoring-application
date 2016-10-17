package pl.edu.agh.pp.cron;

import com.google.maps.GeoApiContext;
import com.google.maps.model.TravelMode;
import org.joda.time.Instant;
import org.json.JSONArray;
import org.json.JSONObject;
import pl.edu.agh.pp.detector.trackers.AnomalyTracker;
import pl.edu.agh.pp.detector.trackers.IAnomalyTracker;
import pl.edu.agh.pp.settings.exceptions.IllegalPreferenceObjectExpected;

import java.util.List;

/**
 * Created by Jakub Janusz on 12.10.2016.
 * 18:30
 * server
 */
public class AnomalyRepeater extends Thread {

    private final RequestsExecutor requestsExecutor;
    private final JSONArray routes;
    private final GeoApiContext context;
    private final IAnomalyTracker anomalyTracker;

    public AnomalyRepeater(RequestsExecutor requestsExecutor, JSONArray routes, GeoApiContext context) throws IllegalPreferenceObjectExpected {
        this.requestsExecutor = requestsExecutor;
        this.routes = routes;
        this.context = context;
        this.anomalyTracker = AnomalyTracker.getInstance();
    }

    @Override
    public void run() {
        while (true) {
            synchronized (requestsExecutor) {
                List<Integer> routesIds = anomalyTracker.getCurrentAnomaliesRoutesIds();
                for (Integer id : routesIds) {
                    try {
                        JSONObject route = getRoute(id);
                        if (route != null) {
                            String destinations[] = new String[1];
                            String origins[] = new String[1];
                            destinations[0] = route.get("destination").toString();
                            origins[0] = route.get("origin").toString();

                            TravelMode travelMode = TravelMode.DRIVING;
                            Instant departure = Instant.now();
                            requestsExecutor.execute(String.valueOf(id), context, origins, destinations, travelMode, departure);
                        }
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

    private JSONObject getRoute(int id) {
        for (int i = 0; i < routes.length(); i++) {
            JSONObject route = routes.getJSONObject(i);
            if (Integer.valueOf(route.getString("id")) == id) {
                return route;
            }
        }
        return null;
    }
}
