package pl.edu.agh.pp.detector.loaders;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.detector.records.Record;

/**
 * Created by Maciej on 18.07.2016.
 * 21:36
 * Project: detector.
 */
public class FilesLoader {
    private final static Charset ENCODING = StandardCharsets.UTF_8;
    private final Logger logger = (Logger) LoggerFactory.getLogger(FilesLoader.class);

    /**
     * TODO: Load all selected files.
     * Regex could be useful in accessing the day of week (the log files has date und day of week)
     * This could be parsed and well set.
     * Regex could be also useful in reading each data from files.
     * All data are temporary saved in computer memory.
     * <p>
     * Firstly, load only one file - hardcoded path.
     */

    private List<Path> listFilePath;
    private List<Record> records = new ArrayList<>();
    private InputParser inputParser = new InputParser();

    public FilesLoader(String ... fileNames) {
        if(fileNames.length > 0){
            listFilePath = new ArrayList<Path>();
            for(String aFileName: fileNames) {
                if(aFileName!= null && !aFileName.equals(""))
                    listFilePath.add(Paths.get(aFileName));
            }
        }else{
            //TODO: some sort of error message or notification for user(bad input)
        }
    }

    public final void processLineByLine() throws IOException {
        for(Path aFilePath: listFilePath) {
            try (Scanner scanner = new Scanner(aFilePath, ENCODING.name())) {
                while (scanner.hasNextLine()) {
                    processLine(scanner.nextLine());
                }
            }
        }
    }

    private void processLine(String aLine) {
        //\"\d+-\d+-\d+ \d+:\d+:\d+,\d+": \{ \{
        Scanner scanner = new Scanner(aLine);
        Pattern regex = Pattern.compile(" },");

        scanner.useDelimiter(regex);
        if (scanner.hasNext()) {
            Record record;
            String buffer = scanner.next();
            if(buffer.contains("\"Status\": \"NOT_FOUND\"")) {
                logger.error("FilesLoader :: bad record found: " + buffer);
                return;
            }
            record = inputParser.parse(buffer);
            if(record != null) {
                records.add(record);
            }
        }
    }

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }

    private String quote(String aText) {
        String QUOTE = "'";
        return QUOTE + aText + QUOTE;
    }

    public List<Path> getListFilePath() {
        return listFilePath;
    }

    public void setListFilePath(List<Path> listFilePath) {
        this.listFilePath = listFilePath;
    }
}
