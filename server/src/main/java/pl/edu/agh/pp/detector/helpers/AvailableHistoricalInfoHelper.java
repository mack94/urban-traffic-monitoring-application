package pl.edu.agh.pp.detector.helpers;

import pl.edu.agh.pp.detector.operations.AnomalyOperationProtos.AvailableHistoricalMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Maciej on 16.11.2016.
 * 16:49
 * Project: server.
 */
public class AvailableHistoricalInfoHelper {

    private static Map<String, Integer> availableDateRoutes = new HashMap<>();

    public static Map<String, Integer> getAvailableDateRoutes() {
        return availableDateRoutes;
    }

    public static void addAvailableDateRoutes(Map<String, Integer> availableDateRoutes) {
        AvailableHistoricalInfoHelper.availableDateRoutes.putAll(availableDateRoutes);
    }

    public static AvailableHistoricalMessage getAvailableHistoricalMessage() {
        return AvailableHistoricalMessage.newBuilder()
                .putAllAvaiableDateRoutes(availableDateRoutes)
                .build();
    }
}
