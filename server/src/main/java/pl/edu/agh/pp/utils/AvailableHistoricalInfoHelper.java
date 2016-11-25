package pl.edu.agh.pp.utils;

import pl.edu.agh.pp.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.operations.AnomalyOperationProtos.AvailableHistoricalMessage;

import java.util.*;

/**
 * Created by Maciej on 16.11.2016.
 * 16:49
 * Project: server.
 */
public class AvailableHistoricalInfoHelper {

    private static Map<String, List<Integer>> availableDateRoutes = new HashMap<>();

    public static Map<String, List<Integer>> getAvailableDateRoutes() {
        return availableDateRoutes;
    }

    public static void addAvailableDateRoute(String date, Integer routeID) {
        if (availableDateRoutes.containsKey(date)) {
            List<Integer> routes = availableDateRoutes.get(date);
            if (!routes.contains(routeID)) {
                routes.add(routeID);
                availableDateRoutes.put(date, routes);
            }
        } else {
            List<Integer> routes = new LinkedList<>();
            routes.add(routeID);
            availableDateRoutes.put(date, routes);
        }
    }

    public static void addAvailableDateRoutes(Map<String, List<Integer>> availableDateRoutes) {
        System.out.println("I receive add Available DateRoutes");
        System.out.println(availableDateRoutes);
        AvailableHistoricalInfoHelper.availableDateRoutes.putAll(availableDateRoutes);
    }

    public static AvailableHistoricalMessage getAvailableHistoricalMessage() {
        System.out.println("I receive ask about Available DateRoutes message");

        Map<String, AnomalyOperationProtos.AvailableRoutes> availableDateRoutesToSend = new HashMap<>();

        for (String key : availableDateRoutes.keySet()) {
            Map<Integer, Integer> allRoutes = new HashMap<>();
            List<Integer> values = availableDateRoutes.get(key);
            Iterator<Integer> iterator = values.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                Integer route = iterator.next();
                allRoutes.put(i, route);
                i++;
            }

            AnomalyOperationProtos.AvailableRoutes availableRoutes = AnomalyOperationProtos.AvailableRoutes.newBuilder()
                    .putAllRoutes(allRoutes)
                    .build();

            availableDateRoutesToSend.put(key, availableRoutes);
        }

        return AvailableHistoricalMessage.newBuilder()
                .putAllAvaiableDateRoutes(availableDateRoutesToSend)
                .build();
    }
}
