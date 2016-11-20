package pl.edu.agh.pp.charts.data.server;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Dawid on 2016-11-20.
 */
public class HistoricalAnomalyManager {
    private static ArrayList<HistoricalAnomaly> historicalAnomalie = new ArrayList<>();

    public static void addHistoricalAnomalies(Integer routeID, DateTime date, Map<String,Map<Integer,Integer>> anomalies) {
        historicalAnomalie.add(new HistoricalAnomaly(routeID, date, anomalies));
    }

    public static HistoricalAnomaly getHistoricalAnomalies(Integer routeID, DateTime date) {
        for (HistoricalAnomaly historicalAnomaly : historicalAnomalie) {
            if (historicalAnomaly.getRouteID().equals(routeID) && date.isEqual(historicalAnomaly.getDate())) {
                return historicalAnomaly;
            }
        }
        return null;
    }
}
