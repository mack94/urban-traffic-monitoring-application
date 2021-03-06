package pl.edu.agh.pp.trackers;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.adapters.AnomaliesServer;
import pl.edu.agh.pp.expiration.AnomalyExpirationListener;
import pl.edu.agh.pp.serializers.FileSerializer;
import pl.edu.agh.pp.serializers.ISerializer;
import pl.edu.agh.pp.utils.AnomalyLifeTimeInfoHelper;
import pl.edu.agh.pp.utils.JodaTimeHelper;

import java.util.List;
import java.util.Map;
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
    private ISerializer serializer = FileSerializer.getInstance();
    private ConcurrentHashMap<Integer, DateTime> anomalyTime = serializer.deserializeAnomalyTime();
    private ConcurrentHashMap<Integer, String> anomalyID = serializer.deserializeAnomalyId();
    private Seconds lifeTime;
    private AnomalyExpirationListener anomalyExpirationListener;

    private AnomalyTracker() {
        lifeTime = Seconds.seconds(AnomalyLifeTimeInfoHelper.getInstance().getAnomalyLifeTimeValue());
        this.anomalyExpirationListener = new AnomalyExpirationListener(anomalyID, anomalyTime);
        anomalyExpirationListener.start();
    }

    public static AnomalyTracker getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public List<Integer> getCurrentAnomaliesRoutesIds() {
        List<Integer> result = anomalyTime.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isAfter(JodaTimeHelper.MINIMUM_ANOMALY_DATE))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return result;
    }

    @Override
    public synchronized String put(int routeID, DateTime dateTime) {

        DateTime lastAnomalyOnThisRoute = anomalyTime.get(routeID);

        if (lastAnomalyOnThisRoute == null)
            lastAnomalyOnThisRoute = JodaTimeHelper.MINIMUM_ANOMALY_DATE;

        Seconds diff = Seconds.secondsBetween(dateTime, lastAnomalyOnThisRoute);
        if (Math.abs(diff.getSeconds()) > lifeTime.getSeconds()) {
            String newAnomalyID = String.format("%04d", routeID) + "_" + dateTime.toLocalDate() + "_" + dateTime.getHourOfDay() + "-" + dateTime.getMinuteOfHour();
            anomalyID.put(routeID, newAnomalyID);
            logger.info("NEW ANOMALY ID = " + newAnomalyID);
        }
        anomalyTime.put(routeID, dateTime);

        serializer.serializeAnomalyId(anomalyID);
        serializer.serializeAnomalyTime(anomalyTime);

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
        serializer.serializeAnomalyTime(anomalyTime);
    }

    @Override
    public DateTime getLastUpdate(int routeID) {

        DateTime anomaly = anomalyTime.get(routeID);

        if (anomaly != null)
            return anomaly;
        return JodaTimeHelper.MINIMUM_ANOMALY_DATE;
    }

    public void setAnomaliesServer(AnomaliesServer anomaliesServer) {
        this.anomalyExpirationListener.setAnomaliesServer(anomaliesServer);
    }

    public static class Holder {
        static final AnomalyTracker INSTANCE = new AnomalyTracker();
    }
}
