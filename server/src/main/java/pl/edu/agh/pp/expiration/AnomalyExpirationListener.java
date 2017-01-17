package pl.edu.agh.pp.expiration;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.adapters.AnomaliesServer;
import pl.edu.agh.pp.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.utils.AnomalyLifeTimeInfoHelper;
import pl.edu.agh.pp.utils.CurrentAnomaliesHelper;
import pl.edu.agh.pp.utils.ExpirationBroadcastInfoHelper;
import pl.edu.agh.pp.utils.ExpirationIntervalInfoHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AnomalyExpirationListener extends Thread {
    private final Logger logger = LoggerFactory.getLogger(AnomalyExpirationListener.class);
    private ConcurrentHashMap<Integer, String> anomalyID;
    private ConcurrentHashMap<Integer, DateTime> anomalyTime;
    private AnomaliesServer anomaliesServer;
    private final Set<Anomaly> currentAnomalies = new HashSet<>();

    public AnomalyExpirationListener(ConcurrentHashMap<Integer, String> anomalyID, ConcurrentHashMap<Integer, DateTime> anomalyTime) {
        this.anomalyID = anomalyID;
        this.anomalyTime = anomalyTime;
    }

    @Override
    public void run() {

        while (anomaliesServer == null) {
            try {
                sleep(10_000);
            } catch (InterruptedException ignored) {
            }
        }

        int anomalyLifeTime;
        int expirationBroadcast;
        int expirationInterval;

        while (true) {
            try {
                logger.info("Checking for expiration started");
                anomalyLifeTime = AnomalyLifeTimeInfoHelper.getInstance().getAnomalyLifeTimeValue();
                expirationBroadcast = ExpirationBroadcastInfoHelper.getInstance().getExpirationBroadcastValue();

                List<Anomaly> newAnomalies = anomalyID.entrySet()
                        .stream()
                        .map(entry -> new Anomaly(entry.getKey(), entry.getValue(), anomalyTime.get(entry.getKey())))
                        .collect(Collectors.toList());
                currentAnomalies.removeAll(newAnomalies);
                currentAnomalies.addAll(newAnomalies);

                Set<Anomaly> anomaliesThatExpire = new HashSet<>();
                for (Anomaly anomaly : currentAnomalies) {
                    int lastUpdateInSeconds = Seconds.secondsBetween(anomaly.getLastUpdate(), DateTime.now()).getSeconds();
                    if (lastUpdateInSeconds > anomalyLifeTime) {
                        if (lastUpdateInSeconds > expirationBroadcast) {
                            anomaliesThatExpire.add(anomaly);
                        } else {
                            sendMessage(anomaly.routeId, anomaly.getId());
                            CurrentAnomaliesHelper.getInstance().removeAnomaly(anomaly.getId());
                        }
                    }
                }
                currentAnomalies.removeAll(anomaliesThatExpire);

                expirationInterval = ExpirationIntervalInfoHelper.getInstance().getExpirationIntervalValue();
                sleep(expirationInterval * 1000);
            } catch (Exception e) {
                logger.error("Error occurred while checking expiration of anomalies", e);
            }
        }
    }

    private void sendMessage(int routeId, String anomalyId) {
        AnomalyOperationProtos.AnomalyMessage message = AnomalyOperationProtos.AnomalyMessage.newBuilder()
                .setAnomalyID(anomalyId)
                .setRouteIdx(routeId)
                .setDate(DateTime.now().toString("yyyy-MM-dd HH:mm:ss"))
                .setIsActive(false)
                .build();
        anomaliesServer.send(message.toByteArray());
    }

    public void setAnomaliesServer(AnomaliesServer anomaliesServer) {
        this.anomaliesServer = anomaliesServer;
    }

    private class Anomaly {
        private Integer routeId;
        private String id;
        private DateTime lastUpdate;

        private Anomaly(Integer routeId, String id, DateTime lastUpdate) {
            this.routeId = routeId;
            this.id = id;
            this.lastUpdate = lastUpdate;
        }

        private Integer getRouteId() {
            return routeId;
        }

        private String getId() {
            return id;
        }

        private DateTime getLastUpdate() {
            return lastUpdate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Anomaly anomaly = (Anomaly) o;

            if (routeId != null ? !routeId.equals(anomaly.routeId) : anomaly.routeId != null)
                return false;
            return id != null ? id.equals(anomaly.id) : anomaly.id == null;

        }

        @Override
        public int hashCode() {
            int result = routeId != null ? routeId.hashCode() : 0;
            result = 31 * result + (id != null ? id.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return String.format("Anomaly %s at route %d, last update on %s", id, routeId, lastUpdate);
        }
    }
}
