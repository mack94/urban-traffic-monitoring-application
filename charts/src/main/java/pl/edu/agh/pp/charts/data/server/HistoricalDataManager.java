package pl.edu.agh.pp.charts.data.server;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Dawid on 2016-11-12.
 */
public class HistoricalDataManager {
    private static ArrayList<HistoricalData> historicalDataList = new ArrayList<>();

    public static void addHistoricalData(Integer routeID, DateTime date, Map<Integer, Integer> duration) {
        historicalDataList.add(new HistoricalData(routeID, date, duration));
    }

    public static HistoricalData getHistoricalData(Integer routeID, DateTime date) {
        for (HistoricalData historicalData : historicalDataList) {
            if (historicalData.getRouteID().equals(routeID) && date.isEqual(historicalData.getDate())) {
                return historicalData;
            }
        }
        return null;
    }
}
