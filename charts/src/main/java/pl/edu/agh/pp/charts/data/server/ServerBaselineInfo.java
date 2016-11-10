package pl.edu.agh.pp.charts.data.server;

import pl.edu.agh.pp.charts.adapters.Connector;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;

import java.util.Map;

/**
 * Created by Maciej on 05.11.2016.
 * 18:01
 * Project: charts.
 */
public class ServerBaselineInfo {

    private static Integer routeID = -1;
    private static AnomalyOperationProtos.BaselineMessage.Day day;
    private static Map<Integer, Integer> baseline;

    public synchronized static void addBaselineInfo(int _routeID, AnomalyOperationProtos.BaselineMessage.Day _day, Map<Integer, Integer> _baseline) {
        synchronized (ServerBaselineInfo.class) {
            routeID = _routeID;
            day = _day;
            baseline = _baseline;
            informControllerAboutBaseline(routeID, day, baseline);
        }
    }

    private static void informControllerAboutBaseline(Integer routeID, AnomalyOperationProtos.BaselineMessage.Day day, Map<Integer, Integer> baseline) {
        Connector.updateBaseline(routeID, day, baseline);
    }
}
