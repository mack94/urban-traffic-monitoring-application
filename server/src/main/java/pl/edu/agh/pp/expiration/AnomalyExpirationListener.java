package pl.edu.agh.pp.expiration;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import pl.edu.agh.pp.adapters.AnomaliesServer;
import pl.edu.agh.pp.exceptions.IllegalPreferenceObjectExpected;
import pl.edu.agh.pp.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.settings.Options;
import pl.edu.agh.pp.settings.PreferencesNamesHolder;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AnomalyExpirationListener extends Thread {
    private ConcurrentHashMap<Integer, String> anomalyID;
    private ConcurrentHashMap<Integer, DateTime> anomalyTime;
    private Set<String> expiredAnomalies;
    private AnomaliesServer anomaliesServer;

    public AnomalyExpirationListener(ConcurrentHashMap<Integer, String> anomalyID, ConcurrentHashMap<Integer, DateTime> anomalyTime) {
        this.anomalyID = anomalyID;
        this.anomalyTime = anomalyTime;
        this.expiredAnomalies = new HashSet<>();
    }

    @Override
    public void run() {
        int anomalyLifeTime = 500;
        int expirationBroadcast = 3600;
        int expirationInterval;
        while (true) {
            try {
                anomalyLifeTime = (int) Options.getInstance().getPreference(PreferencesNamesHolder.ANOMALY_LIFE_TIME, Integer.class);
                expirationBroadcast = (int) Options.getInstance().getPreference(PreferencesNamesHolder.ANOMALY_EXPIRATION_BROADCAST, Integer.class);
            } catch (IllegalPreferenceObjectExpected e) {
                e.printStackTrace();
            }
            int finalAnomalyLifeTime = anomalyLifeTime;
            int finalExpirationBroadcast = expirationBroadcast;
            anomalyID.entrySet()
                    .stream()
                    .filter(entry -> !expiredAnomalies.contains(entry.getValue()))
                    .forEach(entry -> {
                        DateTime anomaly = anomalyTime.get(entry.getKey());
                        DateTime now = DateTime.now();
                        int lastUpdateInSeconds = Seconds.secondsBetween(anomaly, now).getSeconds();
                        if (lastUpdateInSeconds > finalAnomalyLifeTime) {
                            sendMessage(entry.getKey(), entry.getValue());
                            if (lastUpdateInSeconds > finalExpirationBroadcast) {
                                expiredAnomalies.add(entry.getValue());
                            }
                        }
                    });
            try {
                expirationInterval = (int) Options.getInstance().getPreference(PreferencesNamesHolder.ANOMALY_EXPIRATION_INTERVAL, Integer.class);
                sleep(expirationInterval * 1000);
            } catch (InterruptedException | IllegalPreferenceObjectExpected e) {
                e.printStackTrace();
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
}
