package pl.edu.agh.pp.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.operations.AnomalyOperationProtos;

import java.util.HashMap;

/**
 * Created by Maciej on 14.12.2016.
 * 23:22
 * Project: server.
 */
public class CurrentAnomaliesHelper {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(CurrentAnomaliesHelper.class);
    private static final Object lock = new Object();
    protected HashMap<Integer, AnomalyOperationProtos.AnomalyMessage> lastMessages = new HashMap<>();

    private CurrentAnomaliesHelper() {
    }

    public static CurrentAnomaliesHelper getInstance() {
        return Holder.INSTANCE;
    }

    // It's necessary because if it is not present, anomalies that last occurred on route for instance 7 days ago would be sent as
    // current anomalies.
    public void clearCurrentAnomalies() {
        lastMessages.clear();
    }

    public HashMap<Integer, AnomalyOperationProtos.AnomalyMessage> getCurrentAnomalies() {
        synchronized (lock) {
            return lastMessages;
        }
    }

    public void putLastMessage(AnomalyOperationProtos.AnomalyMessage anomalyMessage) {
        synchronized (lock) {
            lastMessages.put(anomalyMessage.getRouteIdx(), anomalyMessage);
        }
    }

    public static class Holder {
        static final CurrentAnomaliesHelper INSTANCE = new CurrentAnomaliesHelper();
    }

}
