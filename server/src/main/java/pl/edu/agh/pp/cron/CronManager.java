package pl.edu.agh.pp.cron;

import com.google.maps.GeoApiContext;
import com.google.maps.model.TravelMode;

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

public class CronManager
{

    private final Logger logger = (Logger) LoggerFactory.getLogger(DetectorManager.class);

    private AnomaliesServer anomaliesServer;

    public CronManager(AnomaliesServer anomaliesServer)
    {
        this.anomaliesServer = anomaliesServer;
    }

    public void getAPIData(String logFile) throws InterruptedException
    {

        try
        {
            GeoApiContext context;
            ContextLoader contextLoader = new ContextLoader();
            RoutesLoader routesLoader = RoutesLoader.getInstance();
            JSONArray loadedRoutes = routesLoader.loadJSON();
            int loadedRoutesAmount = loadedRoutes.length();
            if (loadedRoutesAmount == 0)
            {
                logger.error("File routes.json doesn't contain any routes, please fill the file with appropriate values");
                return;
            }
            context = contextLoader.geoApiContextLoader();
            DetectorManager detectorManager = new DetectorManager(anomaliesServer, logFile);
            if (!detectorManager.areAllRoutesIncluded(loadedRoutes))
            {
                logger.error("Supplied historical data does not coincide with chosen routes. Check your Routes.json " +
                        "file and data in logs directory");
                return;
            }
            Timer timer = Timer.getInstance();
            RequestsExecutor requestsExecutor = new RequestsExecutor(detectorManager);
            AnomalyRepeater anomalyRepeater = new AnomalyRepeater(requestsExecutor, loadedRoutes, context);
            anomalyRepeater.start();

            while (true)
            {
                try
                {
                    for (int i = 0; i < loadedRoutesAmount; i++)
                    {
                        JSONObject route = loadedRoutes.getJSONObject(i);
                        RequestParams requestParams = new RequestParams()
                                .withId(route.get("id").toString())
                                .withOrigins(new String[] { route.get("origin").toString() })
                                .withDestinations(new String[] { route.get("destination").toString() })
                                .withTravelMode(TravelMode.DRIVING)
                                .withDeparture(Instant.now())
                                .withDefaultWaypoints(route.getString("coords"));

                        requestsExecutor.execute(context, requestParams);
                    }

                    long waitingTime = timer.getWaitingTime(Calendar.getInstance());
                    Thread.sleep(waitingTime);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
