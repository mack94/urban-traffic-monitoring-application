package pl.edu.agh.pp.detector.loaders;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import pl.edu.agh.pp.detector.records.Record;

/**
 * Created by Maciej on 24.08.2016.
 * 20:08
 * Project: detector.
 */
public class InputParser {

    private Record record;

    public InputParser() {
    }

    public Record parse(String buffer) {
        record = new Record();
        record.setDateTime(ConvertStringDateToDateTime(buffer.substring(0,buffer.indexOf('{')-2)));
        buffer = buffer.substring(buffer.indexOf('{'));
        record.setRouteID(Integer.parseInt(buffer.substring(buffer.indexOf('"') + 1, buffer.indexOf(':') - 1)));
        buffer = buffer.substring(buffer.indexOf(','));
        buffer = buffer.substring(buffer.indexOf('{'));
        buffer = buffer.substring(buffer.indexOf(':'));
        // FIXME
//            System.out.println(buffer.substring(5, buffer.indexOf('m') - 2));
//            record.setDistance(Integer.parseInt(buffer.substring(5, buffer.indexOf('m') - 2)));
        buffer = buffer.substring(buffer.indexOf(','));
        buffer = buffer.substring(buffer.indexOf(':'));
        record.setDuration(Integer.parseInt(buffer.substring(3, buffer.indexOf(',') - 1)));
        buffer = buffer.substring(buffer.indexOf(','));
        buffer = buffer.substring(buffer.indexOf(':'));
        record.setDurationInTraffic(Integer.parseInt(buffer.substring(3, buffer.indexOf(',') - 1)));
//            record.setTime();
//            ids.add(record.getId());
//            days.add(record.getDay());
//            ResourcesHolder.getInstance().addDay(record.getDay());
        return record;
    }


    private DateTime ConvertStringDateToDateTime(String Date) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("\"yyyy-MM-dd HH:mm:ss,SSS\"");
        return formatter.parseDateTime(Date);
    }
}
