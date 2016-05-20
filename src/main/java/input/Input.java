package main.java.input;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dawid on 2016-05-20.
 */
public class Input {
    private List<Record> list = new ArrayList<>();
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
        record.setDuration(buffer.substring(0,buffer.indexOf(',')));
        buffer = buffer.substring(buffer.indexOf(','));
        buffer = buffer.substring(buffer.indexOf(':'));
        record.setDurationInTraffic(buffer.substring(0,buffer.indexOf(',')));
        System.out.println(record.toString());
        list.add(record);
    }
}
