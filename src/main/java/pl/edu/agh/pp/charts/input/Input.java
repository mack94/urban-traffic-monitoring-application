package pl.edu.agh.pp.charts.input;


import pl.edu.agh.pp.charts.persistence.PersistenceManager;

import java.util.*;

/**
 * Created by Dawid on 2016-05-20.
 */
public class Input {
    private List<Record> records = new ArrayList<>();
    private Set<String> ids = new TreeSet<>();
    private Set<String> days = new TreeSet<>();
    private PersistenceManager persistenceManager = new PersistenceManager();

    public void addLine(String buffer){
        Record record = new Record();
        record.setDate(buffer.substring(0,buffer.indexOf('{')-2));
        buffer = buffer.substring(buffer.indexOf('{'));
        record.setId(buffer.substring(buffer.indexOf('"'),buffer.indexOf(':')));
        buffer = buffer.substring(buffer.indexOf(','));
        buffer = buffer.substring(buffer.indexOf('{'));
        buffer = buffer.substring(buffer.indexOf(':'));
        record.setDistance(buffer.substring(0,buffer.indexOf('m')+2));
        buffer = buffer.substring(buffer.indexOf(','));
        buffer = buffer.substring(buffer.indexOf(':'));
        record.setDuration(buffer.substring(2,buffer.indexOf(',')));
        buffer = buffer.substring(buffer.indexOf(','));
        buffer = buffer.substring(buffer.indexOf(':'));
        record.setDurationInTraffic(buffer.substring(2,buffer.indexOf(',')));
        record.setTime();
        ids.add(record.getId());
        days.add(record.getDay());
        records.add(record);
    }

    public Set<String> getIds(){
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

    public Map<Double,Double> getSummary(String day, int begin, int end, boolean traffic, boolean aggregated) {
        List<Map<Double, Double>> maps = new ArrayList<>();
        for(int i = begin; i <= end; i++) {
            maps.add(getData(day, String.valueOf(i), traffic, aggregated));
        }
        Map<Double, Double> result = new HashMap<>();
        for(Map<Double, Double> map : maps) {
            for(Double key : map.keySet()) {
                if(!result.containsKey(key)) {
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
            System.out.println(amount);
            for(Double key : result.keySet()) {
                Double value = result.get(key);
                value = value / amount;
                result.put(key, value);
            }
        }
        */

        return result;
    }
}
