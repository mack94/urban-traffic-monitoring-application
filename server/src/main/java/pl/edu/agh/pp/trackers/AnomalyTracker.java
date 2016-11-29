package pl.edu.agh.pp.trackers;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.adapters.AnomaliesServer;
import pl.edu.agh.pp.expiration.AnomalyExpirationListener;
import pl.edu.agh.pp.utils.JodaTimeHelper;
import pl.edu.agh.pp.settings.IOptions;
import pl.edu.agh.pp.settings.Options;
import pl.edu.agh.pp.exceptions.IllegalPreferenceObjectExpected;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Maciej on 05.10.2016.
 *
 * @author Maciej Makówka
 *         11:12
 *         Project: server.
 */
public final class AnomalyTracker implements IAnomalyTracker {

    private final Logger logger = (Logger) LoggerFactory.getLogger(AnomalyTracker.class);
    // In the future, it can be replaced by the structure in which objects terminates - to make it more memory efficiently.
    private ConcurrentHashMap<Integer, DateTime> anomalyTime = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, String> anomalyID = new ConcurrentHashMap<>();
    private IOptions options = Options.getInstance();
    private Seconds liveTime;
    private Random random = new Random();
    private AnomaliesServer anomaliesServer;
    private AnomalyExpirationListener anomalyExpirationListener;

    public AnomalyTracker() {
        try {
            liveTime = Seconds.seconds((Integer) options.getPreference("AnomalyLiveTime", Integer.class));
        } catch (IllegalPreferenceObjectExpected illegalPreferenceObjectExpected) {
            logger.error("AnomalyTracker LiveTime ilegal preference object expected: " + illegalPreferenceObjectExpected);
        }
        this.anomalyExpirationListener = new AnomalyExpirationListener(anomalyID, anomalyTime);
        anomalyExpirationListener.start();
    }

    public static AnomalyTracker getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public List<Integer> getCurrentAnomaliesRoutesIds() {
        List<Integer> result = anomalyTime.entrySet().stream()
                .filter(entry -> entry.getValue().isAfter(JodaTimeHelper.MINIMUM_ANOMALY_DATE))
                .map(Map.Entry::getKey).collect(Collectors.toList());
        return result;
    }

    @Override
    public synchronized String put(int routeID, DateTime dateTime) {

        DateTime lastAnomalyOnThisRoute = anomalyTime.get(routeID);

        if (lastAnomalyOnThisRoute == null)
            lastAnomalyOnThisRoute = JodaTimeHelper.MINIMUM_ANOMALY_DATE;

        Seconds diff = Seconds.secondsBetween(dateTime, lastAnomalyOnThisRoute);
        if (Math.abs(diff.getSeconds()) > liveTime.getSeconds()) {
            String newAnomalyID = String.format("%04d", routeID) +"_" + dateTime.toLocalDate() + "_" + dateTime.getHourOfDay()+"-"+dateTime.getMinuteOfHour();
            anomalyID.put(routeID, newAnomalyID);
        }
        anomalyTime.put(routeID, dateTime);

        return anomalyID.get(routeID);
    }

    @Override
    public String get(int routeID) {

        String anomalyId = anomalyID.get(routeID);

        if (anomalyId != null)
            return anomalyId;
        return null;
    }

    @Override
    public boolean has(int routeID) {
        return anomalyTime.containsKey(routeID);
    }

    @Override
    public void remove(int routeID) {
        anomalyTime.put(routeID, JodaTimeHelper.MINIMUM_ANOMALY_DATE);
    }

    @Override
    public DateTime getLastUpdate(int routeID) {

        DateTime anomaly = anomalyTime.get(routeID);

        if (anomaly != null)
            return anomaly;
        return JodaTimeHelper.MINIMUM_ANOMALY_DATE;
    }

    public void setAnomaliesServer(AnomaliesServer anomaliesServer) {
        this.anomaliesServer = anomaliesServer;
        this.anomalyExpirationListener.setAnomaliesServer(anomaliesServer);
    }

    public static class Holder {
        static final AnomalyTracker INSTANCE = new AnomalyTracker();
    }
}
