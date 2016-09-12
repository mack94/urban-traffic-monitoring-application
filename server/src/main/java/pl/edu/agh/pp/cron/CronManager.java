package pl.edu.agh.pp.cron;

import com.google.maps.DirectionsApi;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.TravelMode;
import org.joda.time.Instant;
import org.json.JSONArray;
import org.omg.CORBA.TIMEOUT;
import pl.edu.agh.pp.cron.utils.ContextLoader;
import pl.edu.agh.pp.cron.utils.Route;
import pl.edu.agh.pp.cron.utils.RoutesLoader;
import pl.edu.agh.pp.cron.utils.Timer;
import pl.edu.agh.pp.detector.DetectorManager;
import pl.edu.agh.pp.detector.adapters.Server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CronManager {

    private Server server;

    public CronManager(Server server) {
        this.server = server;
    }

    public void doSomething(String logFile) throws InterruptedException {

        GeoApiContext context;
        ContextLoader contextLoader = new ContextLoader();
        RoutesLoader routesLoader = RoutesLoader.getInstance();
        DetectorManager detectorManager = new DetectorManager(server, logFile);
        Timer timer = Timer.getInstance();

        while(true) {
            try {
                long waitingTime = timer.getWaitingTime();
                JSONArray loadedRoutes = routesLoader.loadJSON();
                context = contextLoader.geoApiContextLoader();

                int loadedRoutesAmount = loadedRoutes.length();

                for (int i = 0; i < loadedRoutesAmount; i++) {
                    String destinations[] = new String[1];
                    String origins[] = new String[1];
                    String id = loadedRoutes.getJSONObject(i).get("id").toString();
                    destinations[0] = loadedRoutes.getJSONObject(i).get("destination").toString();
                    origins[0] = loadedRoutes.getJSONObject(i).get("origin").toString();

//              DistanceMatrixApiRequest distanceMatrixApiRequest = new DistanceMatrixApiRequest(context);
                    TravelMode travelMode = TravelMode.DRIVING;
                    Instant departure = Instant.now();

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
//                "\"2016-09-07 18:26:29,314\": ";
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
                    String date = df.format(new Date());
                    System.out.println(date);
                    detectorManager.doSomething("\"" + date + "\": " + new Route(id, distanceMatrix, directionsApi).toString());
                }

                Thread.sleep(waitingTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(2000);
        }
    }
}