package pl.edu.agh.pp.detector;

import java.io.File;
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
import pl.edu.agh.pp.cron.HalfRouteManager;
import pl.edu.agh.pp.detector.adapters.Server;
import pl.edu.agh.pp.detector.builders.PolynomialPatternBuilder;
import pl.edu.agh.pp.detector.charts.XYLineChart_AWT;
import pl.edu.agh.pp.detector.detectors.Detector;
import pl.edu.agh.pp.detector.enums.DayOfWeek;
import pl.edu.agh.pp.detector.loaders.FilesLoader;
import pl.edu.agh.pp.detector.loaders.InputParser;
import pl.edu.agh.pp.detector.managers.CommandLineManager;
import pl.edu.agh.pp.detector.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.detector.records.Record;

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
    private static final String ANOMALY_SEARCH_LOGS_PATH = "C:\\Inz\\appended_file.txt";
    private static final String LOG_FILES_DIRECTORY_PATH = "./logs";
    private static final PolynomialPatternBuilder polynomialPatternBuilder = PolynomialPatternBuilder.getInstance();
    private static final FilesLoader anomalySearchFilesLoader = new FilesLoader(ANOMALY_SEARCH_LOGS_PATH, "C:\\Inz\\appended_file.txt");
    private final InputParser inputParser;
    private static Detector detector;
    private final Logger logger = (Logger) LoggerFactory.getLogger(DetectorManager.class);
    // private static ChannelReceiver client = new ChannelReceiver();
    private Server server;
    private FilesLoader baselineFilesLoader;

    public DetectorManager(Server server, String... logFiles)
    {
        this.inputParser = new InputParser();
        File folder = new File(LOG_FILES_DIRECTORY_PATH);
        File[] listOfFiles = folder.listFiles();
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
            baselineFilesLoader = new FilesLoader(logFiles);
        }

        detector = polynomialPatternBuilder;
        try
        {

            PolynomialPatternBuilder.computePolynomial(baselineFilesLoader.processLineByLine(), true);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        new CommandLineManager().start();
        this.server = server;
        polynomialPatternBuilder.setServer(server);
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

            AnomalyOperationProtos.AnomalyMessage isAnomaly = detector.isAnomaly(record.getDayOfWeek(), record.getRouteID(), record.getTimeInSeconds(), record.getDurationInTraffic());

            if (isAnomaly != null)
            {
                anomalyId = String.valueOf(isAnomaly.getAnomalyID());
                if (!areWaypointsDefault)
                {
                    logEntry = new JSONObject(logEntry).put("anomalyId", anomalyId).toString();
                    logger.error(logEntry);
                }
                server.send(isAnomaly.toByteArray());
            }

            return anomalyId;
        }
        catch (Exception e)
        {
            logger.error("Some Error occurred", e);
        }
        return StringUtils.EMPTY;
    }

    public void displayAnomaliesForRoute(int routeId)
    {
        try
        {
            XYLineChart_AWT chart;

            // while (true) {
            // String incoming = br.readLine();
            // Record record = inputParser.parse(logEntry);
            List<Record> recordsTestedForAnomalies = anomalySearchFilesLoader.processLineByLine();
            List<Record> anomalousRecords = new ArrayList<>();
            int counter = 0;
            for (Record record : recordsTestedForAnomalies)
            {
                if (record.getRouteID() == routeId)
                {
                    if (detector.isAnomaly(record.getDayOfWeek(), record.getRouteID() - 1, record.getTimeInSeconds(), record.getDurationInTraffic()) != null)
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

    public void displayAnomaliesForFile()
    {
        try
        {
            XYLineChart_AWT chart;

            // while (true) {
            // String incoming = br.readLine();
            // Record record = inputParser.parse(logEntry);
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
                if (detector.isAnomaly(record.getDayOfWeek(), record.getRouteID(), record.getTimeInSeconds(), record.getDurationInTraffic()) != null)
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

            // Path p = Paths.get(ANOMALY_SEARCH_LOGS_PATH);
            // String anomaly_search_logs_file_name = p.getFileName().toString();
            // p = Paths.get(BASELINE_LOGS_PATH);
            // String baseline_logs_file_name = p.getFileName().toString();
            for (DayOfWeek dayOfWeek : DayOfWeek.values())
            {
                for (int routeId = startingRouteId; routeId < 8 + startingRouteId; routeId++)
                {
                    if (anomalousRecords.get(dayOfWeek).get(routeId).size() != 0)
                    {
                        // TODO: inferring from which file baseline and anomalies are from, non trivial
                        chart = new XYLineChart_AWT("Anomaly and baseline chart", "Baseline: " + "baseline_file_name" + System.lineSeparator()
                                + "Anomalie: " + "anomaly_file_name" + System.lineSeparator()
                                + "Trasa: " + (routeId + 1), PolynomialPatternBuilder.getValueForEachMinuteOfDay(dayOfWeek, routeId), anomalousRecords.get(dayOfWeek).get(routeId));
                        chart.pack();
                        RefineryUtilities.centerFrameOnScreen(chart);
                        chart.setVisible(true);
                        // TODO: saving charts to file system instead of showing all of them at once
                    }
                }
            }
            // for (Record record : recordsTestedForAnomalies) {
            // if (record.getRouteID() == routeId) {
            // chart = new XYLineChart_AWT(file, "Baseline i anomalie dla trasy " + record.getRouteID(), PolynomialPatternBuilder.getValueForEachMinuteOfDay(record.getDayOfWeek(), record.getRouteID() - 1),
            // anomalousRecords);
            // chart.pack();
            // RefineryUtilities.centerFrameOnScreen(chart);
            // chart.setVisible(true);
            // break;
            // }
            // }

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
