package pl.edu.agh.pp.detectors;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.jfree.ui.RefineryUtilities;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.adapters.AnomaliesServer;
import pl.edu.agh.pp.builders.BuilderContext;
import pl.edu.agh.pp.builders.MeanPatternBuilder;
import pl.edu.agh.pp.builders.PolynomialPatternBuilder;
import pl.edu.agh.pp.builders.SupportVectorRegressionPatternBuilder;
import pl.edu.agh.pp.charts.XYLineChart_AWT;
import pl.edu.agh.pp.commandline.CommandLineManager;
import pl.edu.agh.pp.halfroute.HalfRouteManager;
import pl.edu.agh.pp.loaders.FilesLoader;
import pl.edu.agh.pp.loaders.InputParser;
import pl.edu.agh.pp.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.utils.Record;
import pl.edu.agh.pp.utils.enums.DayOfWeek;

/**
 * Created by Maciej on 18.07.2016.
 *
 * @author Maciej Makówka
 *         21:11
 *         Project: detector.
 */
public class DetectorManager
{

    private static final String BASELINE_LOGS_PATH = "C:\\Inz\\appended_file.txt";
    private static final String ANOMALY_SEARCH_LOGS_PATH = "C:\\Inz\\f.log";
    private static final String LOG_FILES_DIRECTORY_PATH = "./logs";
    private static final FilesLoader anomalySearchFilesLoader = new FilesLoader(ANOMALY_SEARCH_LOGS_PATH);
    private final InputParser inputParser;
    private final Logger logger = LoggerFactory.getLogger(DetectorManager.class);
    private final Logger trafficLogger = LoggerFactory.getLogger("traffic");
    private AnomaliesServer anomaliesServer;
    private static FilesLoader baselineFilesLoader;
    private static File[] listOfFiles;
    private static BuilderContext builderContext;

    public DetectorManager(AnomaliesServer anomaliesServer, Boolean isThis)
    {
        this.anomaliesServer = anomaliesServer;
        this.inputParser = new InputParser();
        File folder = new File(LOG_FILES_DIRECTORY_PATH);
        listOfFiles = folder.listFiles();
    }

    public DetectorManager(AnomaliesServer anomaliesServer, String... logFiles)
    {
        this.inputParser = new InputParser();
        File folder = new File(LOG_FILES_DIRECTORY_PATH);
        listOfFiles = folder.listFiles();
        if (folder.isDirectory() && listOfFiles != null)
        {
            String newLogFiles[] = new String[logFiles.length + listOfFiles.length];
            int i = 0;
            for (String file : logFiles)
            {
                if (!file.trim().equals(""))
                {
                    newLogFiles[i] = file;
                    i++;
                }
            }
            for (File file : listOfFiles)
            {
                if (file.isFile() && file.getAbsolutePath().endsWith(".log"))
                {
                    newLogFiles[i] = file.getAbsolutePath();
                    i++;
                }
            }
            baselineFilesLoader = new FilesLoader(newLogFiles);
        }
        else
        {
            folder = new File(logFiles[0]);
            if (folder.isDirectory() && folder.listFiles() != null)
            {
                String newLogFiles[] = new String[folder.listFiles().length];
                int i = 0;
                for (File file : folder.listFiles())
                {
                    if (file.isFile() && file.getAbsolutePath().endsWith(".log"))
                    {
                        newLogFiles[i] = file.getAbsolutePath();
                        i++;
                    }
                }
                baselineFilesLoader = new FilesLoader(newLogFiles);
            }
            baselineFilesLoader = new FilesLoader(logFiles);
        }

        builderContext = new BuilderContext(PolynomialPatternBuilder.getInstance());
        try
        {
            PolynomialPatternBuilder.computePolynomial(baselineFilesLoader.processLineByLine(), true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        new CommandLineManager().start();
        this.anomaliesServer = anomaliesServer;
        builderContext.setServer(anomaliesServer);
    }

    public static void computeBaselineFromDefaultLogsLocation() throws IOException
    {
        refreshBaselineFilesLoader();
        PolynomialPatternBuilder.computePolynomial(baselineFilesLoader.processLineByLine(), true);
    }

    public static void refreshBaselineFilesLoader() throws IOException
    {
        File folder = new File(LOG_FILES_DIRECTORY_PATH);
        listOfFiles = folder.listFiles();
        if (folder.isDirectory() && listOfFiles != null)
        {
            String newLogFiles[] = new String[listOfFiles.length];
            int i = 0;
            for (File file : listOfFiles)
            {
                if (file.isFile() && file.getAbsolutePath().endsWith(".log"))
                {
                    newLogFiles[i] = file.getAbsolutePath();
                    i++;
                }
            }
            baselineFilesLoader = new FilesLoader(newLogFiles);
            baselineFilesLoader.processLineByLine();
        }
        else
        {
            throw new FileNotFoundException("DetectorManager: logs directory missing or no log files detected inside");
        }
    }

    public String isAnomaly(String logEntry, String defaultWaypoints)
    {
        try
        {
            String anomalyId = StringUtils.EMPTY;
            boolean areWaypointsDefault = true;
            Record record = inputParser.parse(logEntry);
            if (!"default".equals(record.getWaypoints()))
            {
                HalfRouteManager halfRouteManager = new HalfRouteManager(record, defaultWaypoints);
                logEntry = halfRouteManager.splitRoute();
                record = inputParser.parse(logEntry);
                areWaypointsDefault = false;
            }

            AnomalyOperationProtos.AnomalyMessage isAnomaly = builderContext.isAnomaly(record.getDayOfWeek(), record.getRouteID(), record.getTimeInSeconds(), record.getDurationInTraffic());

            if (!areWaypointsDefault)
            {
                if (isAnomaly != null)
                {
                    anomalyId = isAnomaly.getAnomalyID();
                    logEntry = new JSONObject(logEntry).put("anomalyId", anomalyId).toString();

                    anomaliesServer.send(isAnomaly.toByteArray());
                }
                trafficLogger.error(logEntry);
                logger.error(logEntry);
            }

            return anomalyId;
        }
        catch (Exception e)
        {
            logger.error("Some Error occurred", e);
        }
        return StringUtils.EMPTY;
    }

    public Map<String, Map<Integer, Integer>> getAnomalyForDateAndRoute(String date, int routeID) throws IOException
    {

        Map<String, Map<Integer, Integer>> result = new HashMap<>();
        DateTime dateTime = DateTime.parse(date);
        int year = dateTime.getYear();
        int month = dateTime.getMonthOfYear();
        int day = dateTime.getDayOfMonth();
        String filenameFormat = String.format("%d-%d-%d", (year % 2000), month, day);
        System.out.println("SEARCH DATE FILENAME: " + filenameFormat);

        if (listOfFiles != null)
        {
            for (File file : listOfFiles)
            {
                if (file.getName().contains(filenameFormat))
                {
                    FilesLoader filesLoader = new FilesLoader();
                    List<Record> records = filesLoader.processFile(file.getPath());
                    for (Record record : records)
                    {
                        String anomalyID = record.getAnomalyID();
                        if (anomalyID != null && anomalyID.length() != 0 && record.getRouteID() == routeID)
                        {
                            if (result.containsKey(anomalyID))
                            {
                                Map<Integer, Integer> currentRecord = result.get(anomalyID);
                                currentRecord.put(record.getTimeInSeconds(), record.getDurationInTraffic());
                                result.replace(anomalyID, currentRecord);
                            }
                            else
                            {
                                Map<Integer, Integer> newRecord = new HashMap<>();
                                newRecord.put(record.getTimeInSeconds(), record.getDurationInTraffic());
                                result.put(anomalyID, newRecord);
                            }
                        }
                    }
                }
            }
        }
        System.out.println(result);
        return result;
    }

    @Deprecated
    public void displayAnomaliesForRoute(int routeId)
    {
        try
        {
            XYLineChart_AWT chart;

            List<Record> recordsTestedForAnomalies = anomalySearchFilesLoader.processLineByLine();
            List<Record> anomalousRecords = new ArrayList<>();
            int counter = 0;
            for (Record record : recordsTestedForAnomalies)
            {
                if (record.getRouteID() == routeId)
                {
                    if (builderContext.isAnomaly(record.getDayOfWeek(), record.getRouteID() - 1, record.getTimeInSeconds(), record.getDurationInTraffic()) != null)
                    {
                        System.out.println("-------------------------------");
                        System.out.println("Day: " + record.getDayOfWeek());
                        System.out.println("When: " + record.getDateTime());
                        System.out.println("RouteId: " + record.getRouteID());
                        System.out.println("-------------------------------");
                        anomalousRecords.add(record);
                        counter++;
                    }
                }
            }
            System.out.println("Number of anomalies: " + counter);

            Path p = Paths.get(ANOMALY_SEARCH_LOGS_PATH);
            String file = p.getFileName().toString();
            for (Record record : recordsTestedForAnomalies)
            {
                if (record.getRouteID() == routeId)
                {
                    chart = new XYLineChart_AWT(file, "Baseline i anomalie dla trasy " + record.getRouteID(), PolynomialPatternBuilder.getValueForEachMinuteOfDay(record.getDayOfWeek(), record.getRouteID() - 1),
                            anomalousRecords);
                    chart.pack();
                    RefineryUtilities.centerFrameOnScreen(chart);
                    chart.setVisible(true);
                    break;
                }
            }

            Thread.sleep(100);

        }
        catch (InterruptedException e)
        {
            logger.error("DetectorManager :: InterruptedException " + e);
        }
        catch (IOException e)
        {
            logger.error("DetectorManager :: IOException " + e);
        }
    }

    public void buildAndShowBaseline(int routeID, DayOfWeek day, String arg, String[] algorithm_options) throws Exception
    {
        XYLineChart_AWT chart;
        if (Objects.equals(arg, "poly") || Objects.equals(arg, "p"))
        {
            for (int i = 0; i < algorithm_options.length; i++)
            {
                if (Objects.equals(algorithm_options[i], "-d") && algorithm_options.length >= i + 1)
                {
                    PolynomialPatternBuilder.setPolymonialDegree(Integer.parseInt(algorithm_options[i + 1]));
                    i++;
                }
            }
            logger.info("Building baseline with method: poly");
            PolynomialPatternBuilder.computePolynomial(baselineFilesLoader.processLineByLine(), true);

            chart = new XYLineChart_AWT("Polymonial", "Baseline dla trasy " + routeID, PolynomialPatternBuilder.getValueForEachMinuteOfDay(day, routeID), new ArrayList<>());
            chart.pack();
            RefineryUtilities.centerFrameOnScreen(chart);
            chart.setVisible(true);
            chart.addWindowListener(new WindowAdapter()
            {
                public void windowClosing(WindowEvent we)
                {
                    System.exit(0);
                }
            });
        }
        else if (Objects.equals(arg, "svr") || Objects.equals(arg, "s"))
        {
            for (int i = 0; i < algorithm_options.length; i++)
            {
                if (Objects.equals(algorithm_options[i], "-n") && algorithm_options.length >= i + 2)
                {
                    SupportVectorRegressionPatternBuilder.setDayIntervals(Integer.parseInt(algorithm_options[i + 1]));
                    i++;
                }
                if (Objects.equals(algorithm_options[i], "-i") && algorithm_options.length >= i + 2)
                {
                    SupportVectorRegressionPatternBuilder.setInterval(Integer.parseInt(algorithm_options[i + 1]));
                    i++;
                }
            }
            logger.info("Building baseline with method: svr");
            SupportVectorRegressionPatternBuilder.computeClassifier(baselineFilesLoader.processLineByLine(), true);

            chart = new XYLineChart_AWT("SVR", "Baseline dla trasy " + routeID, SupportVectorRegressionPatternBuilder.getValueForEachMinuteOfDay(day, routeID), new ArrayList<>());
            chart.pack();
            RefineryUtilities.centerFrameOnScreen(chart);
            chart.setVisible(true);
            chart.addWindowListener(new WindowAdapter()
            {
                public void windowClosing(WindowEvent we)
                {
                    System.exit(0);
                }
            });
        }
        else if (Objects.equals(arg, "mean") || Objects.equals(arg, "average") || Objects.equals(arg, "simple"))
        {
            logger.info("Building baseline with method: simple historical mean");
            MeanPatternBuilder.computeFunction(baselineFilesLoader.processLineByLine(), true);

            chart = new XYLineChart_AWT("Historical mean", "Baseline dla trasy " + routeID, MeanPatternBuilder.getValueForEachMinuteOfDay(day, routeID), new ArrayList<>());
            chart.pack();
            RefineryUtilities.centerFrameOnScreen(chart);
            chart.setVisible(true);
            chart.addWindowListener(new WindowAdapter()
            {
                public void windowClosing(WindowEvent we)
                {
                    System.exit(0);
                }
            });
        }
    }

    @Deprecated
    public void displayAnomaliesForFile()
    {
        try
        {
            XYLineChart_AWT chart;

            List<Record> recordsTestedForAnomalies = anomalySearchFilesLoader.processLineByLine();
            Map<DayOfWeek, Map<Integer, List<Record>>> anomalousRecords = new HashMap<>();
            Map<Integer, List<Record>> dayOfWeekRecords;
            List<Record> routeAndDayRecords;
            int startingRouteId = recordsTestedForAnomalies.get(0).getRouteID();
            for (DayOfWeek dayOfWeek : DayOfWeek.values())
            {
                dayOfWeekRecords = new HashMap<>();
                for (int routeId = startingRouteId; routeId <= 8 + startingRouteId; routeId++)
                {
                    routeAndDayRecords = new ArrayList<>();
                    dayOfWeekRecords.put(routeId, routeAndDayRecords);
                }
                anomalousRecords.put(dayOfWeek, dayOfWeekRecords);
            }

            int counter = 1;
            for (Record record : recordsTestedForAnomalies)
            {
                if (builderContext.isAnomaly(record.getDayOfWeek(), record.getRouteID(), record.getTimeInSeconds(), record.getDurationInTraffic()) != null)
                {
                    System.out.println("-------------------------------");
                    System.out.println("Day: " + record.getDayOfWeek());
                    System.out.println("When: " + record.getDateTime());
                    System.out.println("RouteId: " + record.getRouteID());
                    System.out.println("-------------------------------");

                    anomalousRecords.get(record.getDayOfWeek()).get(record.getRouteID()).add(record);
                    counter++;
                }

            }
            System.out.println("Number of anomalies: " + counter);

            for (DayOfWeek dayOfWeek : DayOfWeek.values())
            {
                for (int routeId = startingRouteId; routeId < 8 + startingRouteId; routeId++)
                {
                    if (anomalousRecords.get(dayOfWeek).get(routeId).size() != 0)
                    {
                        chart = new XYLineChart_AWT("Anomaly and baseline chart", "Baseline: " + "Baseline_time" + System.lineSeparator()
                                + "Anomalie: " + "anomaly search day" + System.lineSeparator()
                                + "Trasa: " + (routeId), PolynomialPatternBuilder.getValueForEachMinuteOfDay(dayOfWeek, routeId), anomalousRecords.get(dayOfWeek).get(routeId));
                        chart.pack();
                        RefineryUtilities.centerFrameOnScreen(chart);
                        chart.setVisible(true);
                    }
                }
            }

            Thread.sleep(100);

        }
        catch (InterruptedException e)
        {
            logger.error("DetectorManager :: InterruptedException " + e);
        }
        catch (IOException e)
        {
            logger.error("DetectorManager :: IOException " + e);
        }
    }

    public boolean areAllRoutesIncluded(JSONArray loadedRoutes)
    {
        Map<String, Set<DayOfWeek>> list = baselineFilesLoader.getLoadedRoutes();
        boolean contains;
        for (int i = 0; i < loadedRoutes.length(); i++)
        {
            contains = false;
            JSONObject route = loadedRoutes.getJSONObject(i);
            String id = route.get("id").toString();
            for (Map.Entry<String, Set<DayOfWeek>> entry : list.entrySet())
            {
                if (entry.getKey().equals(id))
                {
                    for (DayOfWeek dayOfWeek : entry.getValue())
                    {
                        if (dayOfWeek == DayOfWeek.fromValue(DateTime.now().getDayOfWeek()))
                        {
                            contains = true;
                            break;
                        }
                    }
                }
                if (contains)
                {
                    break;
                }
            }
            if (!contains)
            {
                return false;
            }
        }
        return true;
    }

}
