package pl.edu.agh.pp.charts.data.local;


import org.json.JSONObject;
import pl.edu.agh.pp.charts.persistence.PersistenceManager;

import java.io.IOException;
import java.util.*;

/**
 * Created by Dawid on 2016-05-20.
 */
public class Input {
    private List<Record> records = new ArrayList<>();
    private Set<String> ids = new TreeSet<>();
    private Set<String> days = new TreeSet<>();
    private PersistenceManager persistenceManager = new PersistenceManager();
    private Map<String, Route> routes;
    private RoutesLoader routesLoader;

    public void addLine(String buffer) {
        JSONObject json = new JSONObject(buffer);
        Record record = new Record();
        record.setDate(json.getString("timeStamp"));
        record.setId(json.getString("id"));
        record.setDistance(json.getString("distance"));
        record.setDuration(json.getString("duration"));
        record.setDurationInTraffic(json.getString("durationInTraffic"));
        record.setTime();
        ids.add(record.getId());
        days.add(record.getDay());
        ResourcesHolder.getInstance().addDay(record.getDay());
        records.add(record);
    }

    public Set<String> getIds() {
        return ids;
    }

    public Set<String> getDays() {
        return days;
    }

    public void persist() {
        persistenceManager.saveToFiles(records);
        records = new ArrayList<>();
    }

    public Map<Double, Double> getData(String day, String id, boolean traffic, boolean aggregated) {
        return aggregated ?
                persistenceManager.readFromFiles(day, id, traffic) :
                persistenceManager.readFromFile(day, id, traffic);
    }

    public Map<Double, Double> getSummary(String day, int begin, int end, boolean traffic, boolean aggregated) {
        List<Map<Double, Double>> maps = new ArrayList<>();
        for (int i = begin; i <= end; i++) {
            maps.add(getData(day, String.valueOf(i), traffic, aggregated));
        }
        Map<Double, Double> result = new HashMap<>();
        for (Map<Double, Double> map : maps) {
            for (Double key : map.keySet()) {
                if (!result.containsKey(key)) {
                    result.put(key, 0d);
                }
                Double currentValue = result.get(key);
                Double newValue = map.get(key);
                result.put(key, currentValue + newValue);
            }
        }

        /*
        if(aggregated) {
            double amount = end - begin + 1;
            for(Double key : result.keySet()) {
                Double value = result.get(key);
                value = value / amount;
                result.put(key, value);
            }
        }
        */

        return result;
    }

    public void getRoutes() {
        routesLoader = new RoutesLoader();
        routes = new HashMap<>();
        try {
            routesLoader.loadRoutes(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void addRoute(String id, String origin, String destination) {
        routes.put(id, new Route(id, origin, destination));
    }

    public String getRoute(String id) {
        return routes.get(id).toString();
    }

    public String getId(String route) {
        return route.split("-")[0].trim();
    }

    public String getReverse(String Id) {
        Route route = routes.get(Id);
        for (String routeId : routes.keySet()) {
            if (routes.get(routeId).getOrigin().equals(route.getDestination())) {
                if (routes.get(routeId).getDestination().equals(route.getOrigin())) {
                    return routes.get(routeId).toString();
                }
            }
        }
        return null;
    }

    public void cleanUp() {
        persistenceManager.removeFiles();
    }
}
