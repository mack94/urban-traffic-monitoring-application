package pl.edu.agh.pp.detector.helpers;

import pl.edu.agh.pp.detector.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.detector.records.Record;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Maciej on 16.11.2016.
 * 17:27
 * Project: server.
 */
public class HistoricalInfoHelper {
    private static List<Record> records = new LinkedList<>();

    public static void addRecord(Record record) {
        HistoricalInfoHelper.records.add(record);
    }

    public static void addRecords(List<Record> records) {
        HistoricalInfoHelper.records.addAll(records);
    }

    public static Map<Integer, Integer> getRecords(String date, int routeID) {
        Map<Integer, Integer> result;
        result = records.stream().filter(x -> x.getDateTime().toString("yyyy-MM-dd").compareTo(date) == 0)
                .filter(x -> x.getRouteID() == routeID)
                .collect(Collectors.toMap(Record::getTimeInSeconds, Record::getDurationInTraffic));
        return result;
    }

    public static AnomalyOperationProtos.HistoricalMessage getHistoricalMessage(String date, int routeID) {

        Map<Integer, Integer> measures = getRecords(date, routeID);

        return AnomalyOperationProtos.HistoricalMessage.newBuilder()
                .setRouteID(routeID)
                .setDate(date)
                .putAllMeasures(measures)
                .build();
    }
}
