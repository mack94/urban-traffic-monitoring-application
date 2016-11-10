package pl.edu.agh.pp.charts.data.local;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Jakub Janusz on 06.06.2016.
 * 20:32
 * charts
 */
public class ResourcesHolder {

    private static ResourcesHolder instance = new ResourcesHolder();
    private static String path;
    private List<String> logs = new ArrayList<>();
    private Set<String> days = new HashSet<>();

    private ResourcesHolder() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date d = new Date();
        path = "workspace/" + dateFormat.format(d) + "/";
    }

    public static ResourcesHolder getInstance() {
        return instance;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void addLog(String log) {
        logs.add(log);
    }

    public Set<String> getDays() {
        return days;
    }

    public void addDay(String day) {
        days.add(day);
    }

    public String getPath() {
        return path;
    }

}
