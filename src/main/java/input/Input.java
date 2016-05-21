package main.java.input;

import main.java.persistence.PersistenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Dawid on 2016-05-20.
 */
public class Input {
    private List<Record> records = new ArrayList<>();
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
        System.out.println(record.toString());
        records.add(record);
    }

    public List<Record> getInput(){
        return records;
    }

    public void persist() {
        persistenceManager.saveToFiles(records);
    }

    public Map<Double, Double> getFromFile(String day, String id, boolean traffic) {
        return persistenceManager.readFromFiles(day, id, traffic);
    }
}
