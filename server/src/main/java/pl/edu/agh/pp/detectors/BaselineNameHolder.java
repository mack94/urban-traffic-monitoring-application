package pl.edu.agh.pp.detectors;

import pl.edu.agh.pp.utils.enums.DayOfWeek;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jakub Janusz on 13.12.2016.
 * 21:42
 * server
 */
public class BaselineNameHolder {
    private static Map<DayOfWeek, Map<Integer, String>> BASELINE_MAP = new HashMap<>();

    public static void addBaseline(DayOfWeek day, Integer route, String baseline) {
        if (!BASELINE_MAP.containsKey(day)) {
            BASELINE_MAP.put(day, new HashMap<>());
        }
        Map<Integer, String> map = BASELINE_MAP.get(day);
        map.put(route, baseline);
    }

    public static String getBaseline(DayOfWeek day, Integer route) {
        if (BASELINE_MAP.containsKey(day)) {
            if (BASELINE_MAP.get(day).containsKey(route)) {
                return BASELINE_MAP.get(day).get(route);
            }
        }
        return "NONE";
    }

    public static void clear() {
        BASELINE_MAP = new HashMap<>();
    }
}
