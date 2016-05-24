package pl.edu.agh.pp.charts.persistence;

import pl.edu.agh.pp.charts.input.Record;

import java.io.*;
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
        Date d = new Date();
        String date = dateFormat.format(d);
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
                String row = record.getTimeForChart() + " " + record.getDurationInTraffic() + " " + record.getDuration() + "\n";
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

    public Map<Double, Double> readFromFile(String day, String id, boolean traffic) {
        try {
            File dir = new File(path);
            File[] files = dir.listFiles();
            Map<Double, Double> results = new HashMap<>();

            for(File file : files) {
                String filename = file.getName();
                if(file.isFile() && filename.contains(day) && filename.startsWith(id + "_")) {
                    FileReader fileReader = new FileReader(path + filename);
                    BufferedReader br = new BufferedReader(fileReader);

                    String line = br.readLine();
                    while(isLineCorrect(line)) {
                        String[] values = line.split(" ");
                        Double time = Double.parseDouble(values[0]);

                        int index = traffic ? 1 : 2;
                        results.put(time, Double.valueOf(values[index]));

                        line = br.readLine();
                    }

                    br.close();
                    fileReader.close();
                }
            }

            return results;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Map<Double, Double> readFromFiles(String day, String id, boolean traffic) {
        try {
            File dir = new File(path);
            File[] files = dir.listFiles();
            Map<Double, AverageCounter> averages = new HashMap<>();

            for(File file : files) {
                String filename = file.getName();
                if(file.isFile() && filename.contains(day) && filename.startsWith(id + "_")) {
                    readData(path + filename, averages, traffic);
                }
            }

            Map<Double, Double> results = new HashMap<>();
            for(Double key : averages.keySet()) {
                results.put(key, averages.get(key).getAverage());
            }

            return results;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void readData(String filename, Map<Double, AverageCounter> averages, boolean traffic) throws IOException {
        FileReader fileReader = new FileReader(filename);
        BufferedReader br = new BufferedReader(fileReader);

        String line = br.readLine();
        while(isLineCorrect(line)) {
            String[] values = line.split(" ");
            Double time = Double.parseDouble(values[0]);
            if(!averages.containsKey(time)) {
                averages.put(time, new AverageCounter());
            }
            int index = traffic ? 1 : 2;
            averages.get(time).addValue(Integer.valueOf(values[index]));

            line = br.readLine();
        }

        br.close();
        fileReader.close();
    }

    private boolean isLineCorrect(String line) {
        return line != null && !line.equals("");
    }

}