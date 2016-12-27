package pl.edu.agh.pp.cron;

import com.google.maps.GeoApiContext;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

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
    private final static int MANAGMENT_DELAY_SECONDS = 10;

    private AnomaliesServer anomaliesServer;
    private RoutesLoader routesLoader;
    private JSONArray loadedRoutes;
    private int loadedRoutesAmount;
    private List<String> missingRoutesID = new LinkedList<>();
    private DetectorManager detectorManager;

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
            detectorManager = new DetectorManager(anomaliesServer, logFile);
            context = contextLoader.geoApiContextLoader();
            routesLoader = RoutesLoader.getInstance();
            reloadRoutes();
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

                        if (missingRoutesID.contains(route.get("id").toString()))
                        {
                            requestParams.withMissingHistoricalData(true);
                        }
                        else
                        {
                            requestParams.withMissingHistoricalData(false);
                        }
                        requestsExecutor.execute(context, requestParams);
                    }

                    long waitingTime = timer.getWaitingTime(Calendar.getInstance());
                    Thread.sleep(waitingTime * 1000);
                    reloadRoutes();
                }
                catch (Exception e)
                {
                    // TODO: Catch this.
                    logger.error("CronManager: Some Exception occured while executing requestsExecutor: " + e, e);
                    System.out.println("Please, check your API KEY. It's very likely that new key is required.");
                    Thread.sleep(MANAGMENT_DELAY_SECONDS * 1000);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void reloadRoutes() throws IOException, InterruptedException
    {
        boolean routesChanged = false;
        while (true)
        {
            if (loadedRoutes != null)
            {
                JSONArray temp = routesLoader.loadJSON();
                if (temp.length() != loadedRoutes.length())
                {
                    routesChanged = true;
                }
                else
                {
                    for (int i = 0; i < temp.length(); i++)
                    {
                        if (!temp.getJSONObject(i).similar(loadedRoutes.getJSONObject(i)))
                        {
                            routesChanged = true;
                            break;
                        }
                    }
                }
                for (int i = 0; i < loadedRoutesAmount; i++)
                {
                    loadedRoutes.remove(0);
                }
                temp.forEach(object -> loadedRoutes.put(object));
            }
            else
            {
                loadedRoutes = routesLoader.loadJSON();
            }

            loadedRoutesAmount = loadedRoutes.length();
            if (loadedRoutesAmount == 0)
            {
                logger.error("File routes.json doesn't contain any routes, please fill the file with appropriate values");
                Thread.sleep(MANAGMENT_DELAY_SECONDS * 1000);
                continue;
            }
            if (!missingRoutesID.isEmpty())
                DetectorManager.refreshBaselineFilesLoader();
            missingRoutesID = detectorManager.areAllRoutesIncluded(loadedRoutes);
            if (!missingRoutesID.isEmpty())
            {
                logger.error("Supplied historical data does not coincide with chosen routes. Check your Routes.json " +
                        "file and data in data directory");
                logger.error("Routes with missing hitorical data won't be checked for anomalies. Those include(by id): " +
                        Arrays.toString(missingRoutesID.toArray()));
            }

            if (routesChanged)
            {
                DetectorManager.computeBaselineFromDefaultLogsLocation();
            }
            return;
        }
    }
}
