package pl.edu.agh.pp.loaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.utils.Record;
import pl.edu.agh.pp.utils.enums.DayOfWeek;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private InputParser inputParser = new InputParser();
    private Map<String, Set<DayOfWeek>> loadedRoutes;

    public FilesLoader() {
    }

    public FilesLoader(String... fileNames) {
        loadedRoutes = new HashMap<>();
        if (fileNames.length > 0) {
            listFilePath = new ArrayList<>();
            for (String aFileName : fileNames) {
                if (aFileName != null && !aFileName.equals(""))
                    listFilePath.add(Paths.get(aFileName));
            }
        } else {
            logger.error("FilesLoader: Cannot retrieve data due 0 files given.");
        }
    }

    public final List<Record> processLineByLine() throws IOException {
        List<Record> records = new ArrayList<>();
        for (Path aFilePath : listFilePath) {
            try (Scanner scanner = new Scanner(aFilePath, ENCODING.name())) {
                while (scanner.hasNextLine()) {
                    records.add(processLine(scanner.nextLine()));
                }
            }
        }

        records = records.stream()
                .filter(r -> r != null)
                .collect(Collectors.toList());

        setLoadedRoutes(records);

        return records;
    }

    public final List<Record> processFile(String filePath) throws IOException {
        List<Record> records = new ArrayList<>();
        Path aFilePath = Paths.get(filePath);

        try (Scanner scanner = new Scanner(aFilePath, ENCODING.name())) {
            while (scanner.hasNextLine()) {
                records.add(processLine(scanner.nextLine()));
            }
        }

        records = records.stream()
                .filter(r -> r != null)
                .collect(Collectors.toList());

        return records;
    }

    private Record processLine(String aLine) {
        Scanner scanner = new Scanner(aLine);
        Pattern regex = Pattern.compile(" },");

        scanner.useDelimiter(regex);
        if (scanner.hasNext()) {
            Record record;
            String buffer = scanner.next();
            record = inputParser.parse(buffer);
            if (record != null) {
                return record;
            }
        }
        return null;
    }

    private void setLoadedRoutes(List<Record> records) {
        for (Record record : records) {
            String id = String.valueOf(record.getRouteID());
            if (!loadedRoutes.containsKey(id)) {
                loadedRoutes.put(id, new HashSet<>());
            }
            DayOfWeek dayOfWeek = record.getDayOfWeek();
            loadedRoutes.get(id).add(dayOfWeek);
        }
    }

    public Map<String, Set<DayOfWeek>> getLoadedRoutes() {
        return loadedRoutes;
    }
}
