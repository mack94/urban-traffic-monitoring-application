package pl.edu.agh.pp.detector.loaders;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.detector.records.Record;

/**
 * Created by Maciej on 24.08.2016.
 * 20:08
 * Project: detector.
 */
public class InputParser
{
    public Record parse(String buffer)
    {
        JSONObject json = new JSONObject(buffer);
        // TODO: change after adding isAnomaly field to historical data
        try
        {
            if (Boolean.valueOf(json.getString("isAnomaly")))
            {
                return null;
            }
        }
        catch (Exception ignored)
        {
        }
        Record record = new Record();
        record.setRouteID(Integer.valueOf(json.getString("id")));
        record.setDistance(json.getString("distance"));
        record.setDuration(Integer.valueOf(json.getString("duration")));
        record.setDurationInTraffic(Integer.valueOf(json.getString("durationInTraffic")));
        record.setDateTime(convertStringDateToDateTime(json.getString("timeStamp")));
        return record;
    }

    private DateTime convertStringDateToDateTime(String Date)
    {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss,SSS");
        return formatter.parseDateTime(Date);
    }
}
