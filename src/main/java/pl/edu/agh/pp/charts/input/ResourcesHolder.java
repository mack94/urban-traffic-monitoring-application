package pl.edu.agh.pp.charts.input;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Jakub Janusz on 06.06.2016.
 * 20:32
 * charts
 */
public class ResourcesHolder {

    private static ResourcesHolder instance = new ResourcesHolder();
    private static String path;

    private ResourcesHolder() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date d = new Date();
        path = "workspace/" + dateFormat.format(d) + "/";
    }

    public static ResourcesHolder getInstance() {
        return instance;
    }

    private Set<String> logs = new HashSet<>();

    public Set<String> getLogs() {
        return logs;
    }

    public void addLog(String log) {
        logs.add(log);
    }

    public String getPath() {
        return path;
    }

}
