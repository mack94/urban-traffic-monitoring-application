package pl.edu.agh.pp.utils;

import com.google.protobuf.ByteString;
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

        Map<String, ByteString> availableDateRoutesToSend = new HashMap<>();

        for (String key : availableDateRoutes.keySet()) {
            List<Integer> values = availableDateRoutes.get(key);
            Iterator<Integer> iterator = values.iterator();
            byte[] values_bytes = new byte[values.size()];
            while (iterator.hasNext()) {
                Integer i = iterator.next();
                values_bytes[i - 1] = i.byteValue();
            }
            ByteString values_bytes_string = ByteString.copyFrom(values_bytes);

            availableDateRoutesToSend.put(key, values_bytes_string);
        }

        return AvailableHistoricalMessage.newBuilder()
                .putAllAvaiableDateRoutes(availableDateRoutesToSend)
                .build();
    }
}
