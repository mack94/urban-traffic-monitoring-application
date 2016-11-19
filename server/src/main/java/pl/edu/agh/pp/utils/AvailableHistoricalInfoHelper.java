package pl.edu.agh.pp.utils;

import pl.edu.agh.pp.operations.AnomalyOperationProtos.AvailableHistoricalMessage;

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
        System.out.println("I receive add Available DateRoutes");
        System.out.println(availableDateRoutes);
        AvailableHistoricalInfoHelper.availableDateRoutes.putAll(availableDateRoutes);
    }

    public static AvailableHistoricalMessage getAvailableHistoricalMessage() {
        System.out.println("I receive ask about Available DateRoutes message");
        return AvailableHistoricalMessage.newBuilder()
                .putAllAvaiableDateRoutes(availableDateRoutes)
                .build();
    }
}
