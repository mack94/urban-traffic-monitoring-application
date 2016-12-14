package pl.edu.agh.pp.charts.data.server;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Dawid on 2016-11-05.
 */
public class BaselineManager {
    private static ArrayList<Baseline> baselines = new ArrayList<>();

    public static void addBaseline(Integer routeID, DayOfWeek day, Map<Integer, Integer> baseline, String type) {
        baselines.add(new Baseline(routeID, day, baseline, type));
    }

    public static Baseline getBaseline(Integer routeID, DayOfWeek dayOfWeek, String type) {
        for (Baseline baseline : baselines) {
            if (baseline.getRouteID().equals(routeID) && dayOfWeek.equals(baseline.getDay()) && type.equalsIgnoreCase((baseline.getType()))) {
                return baseline;
            }
        }
        return null;
    }
}
