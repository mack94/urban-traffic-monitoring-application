package pl.edu.agh.pp.charts.data.server;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Dawid on 2016-11-05.
 */
public class BaselineManager {
    private static ArrayList<Baseline> baselines = new ArrayList<>();

    public static void addBaseline(Integer routeID, DayOfWeek day, Map<Integer, Integer> baseline) {
        baselines.add(new Baseline(routeID, day, baseline));
    }

    public static Baseline getBaseline(Integer routeID, DayOfWeek dayOfWeek) {
        for (Baseline baseline : baselines) {
            if (baseline.getRouteID().equals(routeID) && dayOfWeek.equals(baseline.getDay())) {
                return baseline;
            }
        }
        return null;
    }
}
