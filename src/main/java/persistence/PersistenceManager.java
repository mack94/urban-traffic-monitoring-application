package main.java.persistence;

import main.java.input.Record;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Jakub Janusz on 21.05.2016.
 * 16:58
 * charts
 */
public class PersistenceManager {

    private Map<Integer, String> values;
    private String path;

    public PersistenceManager() {
        values = new HashMap<>();
        values.put(1, "SUN");
        values.put(2, "MON");
        values.put(3, "TUE");
        values.put(4, "WED");
        values.put(5, "THU");
        values.put(6, "FRI");
        values.put(7, "SAT");
        this.path = setPath();
    }

    private String setPath() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String date = dateFormat.format(new Date());
        String path = "resources_" + date + "/";
        new File(path).mkdir();
        return path;
    }

    public void saveToFiles(List<Record> records) {
        try {
            Map<String, BufferedWriter> files = new ConcurrentHashMap<>();
            for (Record record : records) {
                String date = getDate(record);
                String day = getDay(record);
                String route = getRoute(record);
                String filename = path + route + "_" + day + "_" + date + ".txt.";
                if (!new File(filename).isFile()) {
                    FileWriter fileWriter = new FileWriter(filename);
                    BufferedWriter writer = new BufferedWriter(fileWriter);
                    files.put(filename, writer);
                }
                String row = getTime(record) + " " + record.getDurationInTraffic() + "\n";
                files.get(filename).write(row);
            }
            for(String filename : files.keySet()) {
                files.get(filename).close();
                files.remove(filename);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getTime(Record record) {
        String time = record.getDate().substring(12, 17);
        double hour = Double.parseDouble(time.substring(0, 2));
        double mins = new BigDecimal(Double.parseDouble(time.substring(3, 5)) / 60)
                .setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        return String.valueOf(hour + mins);
    }

    private String getRoute(Record record) {
        return record.getId();
    }

    private String getDay(Record record) {
        try {
            Calendar calendar = Calendar.getInstance();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dateFormat.parse(getDate(record));
            calendar.setTime(date);

            return values.get(calendar.get(Calendar.DAY_OF_WEEK));
        } catch (ParseException e) {
            System.out.println("Wrong date.");
            return "";
        }

    }

    private String getDate(Record record) {
        return record.getDate().substring(1, 11);
    }

}
