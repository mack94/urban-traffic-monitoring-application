package pl.edu.agh.pp.detector.loaders;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import pl.edu.agh.pp.detector.records.Record;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Maciej on 18.07.2016.
 * 21:36
 * Project: detector.
 */
public class FilesLoader {
    /**
     * TODO: Load all selected files.
     * Regex could be useful in accessing the day of week (the log files has date und day of week)
     * This could be parsed and well set.
     * Regex could be also useful in reading each data from files.
     * All data are temporary saved in computer memory.
     *
     * Firstly, load only one file - hardcoded path.
     *
     */

    private final Path fFilePath;
    private final static Charset ENCODING = StandardCharsets.UTF_8;
    private List<Record> records = new ArrayList<>();

    public FilesLoader (String aFileName) {
        fFilePath = Paths.get(aFileName);
    }

    public final void processLineByLine() throws IOException {
        try (Scanner scanner = new Scanner(fFilePath, ENCODING.name())) {
            while (scanner.hasNextLine()) {
                processLine(scanner.nextLine());
            }
            System.out.println(records.size());
        }

    }

    private void processLine(String aLine) {
        //\"\d+-\d+-\d+ \d+:\d+:\d+,\d+": \{ \{
        Scanner scanner = new Scanner(aLine);
        Pattern regex = Pattern.compile(" },");

        scanner.useDelimiter(regex);
        if (scanner.hasNext()) {
            Record record = new Record();
            String buffer = scanner.next();

            record.setDateTime(ConvertStringDateToDateTime(buffer.substring(0,buffer.indexOf('{')-2)));
            buffer = buffer.substring(buffer.indexOf('{'));
            record.setRouteID(Integer.parseInt(buffer.substring(buffer.indexOf('"') + 1, buffer.indexOf(':') - 1)));
            buffer = buffer.substring(buffer.indexOf(','));
            buffer = buffer.substring(buffer.indexOf('{'));
            buffer = buffer.substring(buffer.indexOf(':'));
            record.setDistance(Integer.parseInt(buffer.substring(5, buffer.indexOf('m') - 2)));
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
            records.add(record);
        }
    }

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }

    private String quote(String aText){
        String QUOTE = "'";
        return QUOTE + aText + QUOTE;
    }

    private DateTime ConvertStringDateToDateTime(String Date) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("\"yyyy-MM-dd HH:mm:ss,SSS\"");
        return formatter.parseDateTime(Date);
    }
}
