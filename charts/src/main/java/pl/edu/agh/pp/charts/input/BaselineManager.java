package pl.edu.agh.pp.charts.input;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Dawid on 2016-11-05.
 */
public class BaselineManager {
    private static ArrayList<Baseline> baselines = new ArrayList<>();

    public static void addBaseline(Integer routeID, DayOfWeek day, Map<Integer, Integer> baseline){
        baselines.add(new Baseline(routeID, day, baseline));
    }

    public static Baseline getBaseline(Integer routeID, DayOfWeek dayOfWeek){
        System.out.println("args - route: " + routeID + " day: " + dayOfWeek);
        for(Baseline baseline :baselines){
            System.out.println("route: " + baseline.getRouteID() + "day: " + baseline.getDay());
            System.out.println("");
            if(baseline.getRouteID().equals(routeID) && dayOfWeek.equals(baseline.getDay())){
                return baseline;
            }
        }
        System.out.println("returning null because route: " + routeID + " and day: " + dayOfWeek);
        return null;
    }
}
