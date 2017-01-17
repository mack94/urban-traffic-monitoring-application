package pl.edu.agh.pp.detectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.jfree.ui.RefineryUtilities;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
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
import pl.edu.agh.pp.loaders.FilesLoader;
import pl.edu.agh.pp.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.serializers.FileSerializer;
import pl.edu.agh.pp.utils.*;
import pl.edu.agh.pp.utils.enums.DayOfWeek;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Created by Maciej on 18.07.2016.
 *
 * @author Maciej Makówka
 *         21:11
 *         Project: detector.
 */
public class DetectorManager {
    private static final String LOG_FILES_DIRECTORY_PATH = "./data";
    private static FilesLoader baselineFilesLoader;
    private static File[] listOfFiles;
    private static BuilderContext builderContext;
    private final Logger logger = LoggerFactory.getLogger(DetectorManager.class);
    private String prevAnomalyID = "";
    private int prevSecondOfDay = 0;
    private AnomaliesServer anomaliesServer;

    public DetectorManager(AnomaliesServer anomaliesServer) {
        this.anomaliesServer = anomaliesServer;
        File folder = new File(LOG_FILES_DIRECTORY_PATH);
        listOfFiles = folder.listFiles();
    }

    public DetectorManager(AnomaliesServer anomaliesServer, String... logFiles) {
        File folder = new File(LOG_FILES_DIRECTORY_PATH);
        listOfFiles = folder.listFiles();
        if (folder.isDirectory() && listOfFiles != null) {
            String newLogFiles[] = new String[logFiles.length + listOfFiles.length];
            int i = 0;
            for (String file : logFiles) {
                if (!file.trim().equals("")) {
                    newLogFiles[i] = file;
                    i++;
                }
            }
            for (File file : listOfFiles) {
                if (file.isFile() && file.getAbsolutePath().endsWith(".log")) {
                    newLogFiles[i] = file.getAbsolutePath();
                    i++;
                }
            }
            baselineFilesLoader = new FilesLoader(newLogFiles);
        } else {
            baselineFilesLoader = new FilesLoader(logFiles);
        }

        builderContext = new BuilderContext(PolynomialPatternBuilder.getInstance());
        try {
            PolynomialPatternBuilder.computePolynomial(baselineFilesLoader.processLineByLine(), true);
        } catch (Exception e) {
            logger.error("DetectorManager: Exception in constructor occurred: " + e, e);
        }
        new CommandLineManager().start();
        this.anomaliesServer = anomaliesServer;
        builderContext.setServer(anomaliesServer);
    }

    public DetectorManager(String dirName) {
        File specifiedDir = new File(dirName);
        File folder;
        if (specifiedDir.isDirectory()) {
            folder = specifiedDir;
        } else {
            folder = new File(LOG_FILES_DIRECTORY_PATH);
        }

        listOfFiles = folder.listFiles();
        if (folder.isDirectory() && folder.listFiles() != null) {
            String newLogFiles[] = new String[folder.listFiles().length];
            int i = 0;
            for (File file : folder.listFiles()) {
                if (file.isFile() && file.getAbsolutePath().endsWith(".log")) {
                    newLogFiles[i] = file.getAbsolutePath();
                    i++;
                }
            }
            baselineFilesLoader = new FilesLoader(newLogFiles);
        }
    }

    public static void computeBaselineFromDefaultLogsLocation() throws IOException {
        refreshBaselineFilesLoader();
        PolynomialPatternBuilder.computePolynomial(baselineFilesLoader.processLineByLine(), true);
    }

    public static void refreshBaselineFilesLoader() throws IOException {
        File folder = new File(LOG_FILES_DIRECTORY_PATH);
        listOfFiles = folder.listFiles();
        if (folder.isDirectory() && listOfFiles != null) {
            String newLogFiles[] = new String[listOfFiles.length];
            int i = 0;
            for (File file : listOfFiles) {
                if (file.isFile() && file.getAbsolutePath().endsWith(".log")) {
                    newLogFiles[i] = file.getAbsolutePath();
                    i++;
                }
            }
            baselineFilesLoader = new FilesLoader(newLogFiles);
            baselineFilesLoader.processLineByLine();
        } else {
            throw new FileNotFoundException("DetectorManager: data/ directory missing or no log files detected inside");
        }
    }

    public String isAnomaly(Record record) {
        try {
            String anomalyId = StringUtils.EMPTY;

            AnomalyOperationProtos.AnomalyMessage anomalyMessage = builderContext.isAnomaly(record.getDayOfWeek(), record.getRouteID(), record.getTimeInSeconds(), record.getDurationInTraffic());

            if (anomalyMessage != null) {
                anomalyId = anomalyMessage.getAnomalyID();
                anomaliesServer.send(anomalyMessage.toByteArray());
                CurrentAnomaliesHelper.getInstance().putLastMessage(anomalyMessage);

                String baseline = BaselineNameHolder.getBaseline(record.getDayOfWeek(), record.getRouteID());
                baseline = StringUtils.removeStart(baseline, "baseline/");
                baseline = StringUtils.removeEnd(baseline, ".ser");
                anomalyId += "#" + baseline;
            }

            return anomalyId;
        } catch (Exception e) {
            logger.error("Some Error occurred", e);
        }
        return StringUtils.EMPTY;
    }

    public Map<String, Map<Integer, Integer>> getAnomalyForDateAndRoute(String date, int routeID) throws IOException {

        Map<String, Map<Integer, Integer>> result = new HashMap<>();
        DateTime dateTime = DateTime.parse(date);
        String monthFormat = "%d";
        String dayFormat = "%d";
        int year = dateTime.getYear();
        int month = dateTime.getMonthOfYear();
        int day = dateTime.getDayOfMonth();
        if(month < 10){
            monthFormat = "0%d";
        }
        if(day < 10){
            dayFormat = "0%d";
        }
        String format = "%d-" + monthFormat + "-" + dayFormat;
        String filenameFormat = String.format(format, (year % 2000), month, day);

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.getName().contains(filenameFormat) && file.getName().endsWith(".log")) {
                    FilesLoader filesLoader = new FilesLoader();
                    List<Record> records = filesLoader.processFile(file.getPath());
                    for (Record record : records) {
                        String anomalyID = record.getAnomalyID();
                        if (anomalyID != null && anomalyID.length() != 0 && record.getRouteID() == routeID) {
                            if (result.containsKey(anomalyID)) {
                                Map<Integer, Integer> currentRecord = result.get(anomalyID);
                                currentRecord.put(record.getTimeInSeconds(), record.getDurationInTraffic());
                                result.replace(anomalyID, currentRecord);
                            } else {
                                Map<Integer, Integer> newRecord = new HashMap<>();
                                newRecord.put(record.getTimeInSeconds(), record.getDurationInTraffic());
                                result.put(anomalyID, newRecord);
                            }
                        }
                    }
                }
            }
        }

        if (result.isEmpty()) {
            fillAnomaliesOnHistoricalBaseline(result, date, routeID, dateTime, filenameFormat);
        }
        return result;
    }

    private void fillAnomaliesOnHistoricalBaseline(Map<String, Map<Integer, Integer>> result, String date, int routeID, DateTime dateTime, String filenameFormat) throws IOException {
        AnomalyOperationProtos.DemandBaselineMessage.Day protosFormatDay;
        protosFormatDay = AnomalyOperationProtos.DemandBaselineMessage.Day.forNumber(dateTime.getDayOfWeek());

        Map<DayOfWeek, Map<Integer, PolynomialFunction>> baselines;
        baselines = FileSerializer.getInstance().searchAndDeserializeBaseline(date, routeID, protosFormatDay);
        PolynomialFunction function = baselines.get(DayOfWeek.fromValue(dateTime.getDayOfWeek())).get(routeID);
        Map<Integer, Integer> newRecord;

        if (function != null) {
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    if (file.getName().contains(filenameFormat) && file.getName().endsWith(".log")) {
                        FilesLoader filesLoader = new FilesLoader();
                        List<Record> records = filesLoader.processFile(file.getPath());
                        for (Record record : records) {
                            if (record.getRouteID() == routeID) {
                                prevAnomalyID = checkForAnomaly(function, record.getDateTime().getSecondOfDay(), record.getDurationInTraffic(), routeID);
                                if (!prevAnomalyID.isEmpty()) {
                                    if (result.containsKey(prevAnomalyID)) {
                                        Map<Integer, Integer> currentRecord = result.get(prevAnomalyID);
                                        currentRecord.put(record.getTimeInSeconds(), record.getDurationInTraffic());
                                        result.replace(prevAnomalyID, currentRecord);
                                    } else {
                                        newRecord = new HashMap<>();
                                        newRecord.put(record.getTimeInSeconds(), record.getDurationInTraffic());
                                        result.put(prevAnomalyID, newRecord);
                                    }
                                }
                            }
                        }
                    }
                }
            }


        }

    }

    private String checkForAnomaly(PolynomialFunction function, int secondOfDay, int travelDuration, int routeID) {
        double predictedTravelDuration = function.value(secondOfDay);
        double errorSensitivity = LeverInfoHelper.getInstance().getLeverValue();
        double bounds = 0.25 + errorSensitivity; // %
        double errorDelta = predictedTravelDuration * bounds;
        int baselineWindowSize = BaselineWindowSizeInfoHelper.getInstance().getBaselineWindowSizeValue();
        int anomalyLifeTime = AnomalyLifeTimeInfoHelper.getInstance().getAnomalyLifeTimeValue();
        double predictedTravelDurationMinimum = Double.MAX_VALUE;
        double predictedTravelDurationMaximum = Double.MIN_VALUE;

        for (int unitDiff = -baselineWindowSize; unitDiff <= baselineWindowSize; unitDiff++) {
            double tempDuration = function.value(secondOfDay + (unitDiff * 60));
            predictedTravelDurationMinimum = predictedTravelDurationMinimum < tempDuration ? predictedTravelDurationMinimum : tempDuration;
            predictedTravelDurationMaximum = predictedTravelDurationMaximum < tempDuration ? tempDuration : predictedTravelDurationMaximum;
        }

        if ((travelDuration > predictedTravelDurationMaximum + errorDelta)) {
            DateTime dateTimeNew = DateTime.now().withMillisOfDay(secondOfDay * 1000);
            DateTime dateTimePrev = DateTime.now().withMillisOfDay(prevSecondOfDay * 1000);
            if (!prevAnomalyID.isEmpty() && Seconds.secondsBetween(dateTimeNew, dateTimePrev).getSeconds() < anomalyLifeTime) {
                return prevAnomalyID;
            }
            String newAnomalyID = String.format("%04d", routeID) + "_" + dateTimeNew.toLocalDate() + "_" + dateTimeNew.getHourOfDay() + "-" + dateTimeNew.getMinuteOfHour();
            return newAnomalyID;
        }
        return "";
    }

    public void buildAndShowBaseline(int routeID, DayOfWeek day, String arg, String[] algorithm_options) throws Exception {
        XYLineChart_AWT chart;
        try {
            if (Objects.equals(arg, "poly") || Objects.equals(arg, "p")) {
                for (int i = 0; i < algorithm_options.length; i++) {
                    if (Objects.equals(algorithm_options[i], "-d") && algorithm_options.length >= i + 1) {
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
                chart.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent we) {
                        System.exit(0);
                    }
                });
            } else if (Objects.equals(arg, "svr") || Objects.equals(arg, "s")) {
                for (int i = 0; i < algorithm_options.length; i++) {
                    if (Objects.equals(algorithm_options[i], "-n") && algorithm_options.length >= i + 2) {
                        SupportVectorRegressionPatternBuilder.setDayIntervals(Integer.parseInt(algorithm_options[i + 1]));
                        i++;
                    }
                    if (Objects.equals(algorithm_options[i], "-i") && algorithm_options.length >= i + 2) {
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
                chart.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent we) {
                        System.exit(0);
                    }
                });
            } else if (Objects.equals(arg, "mean") || Objects.equals(arg, "average") || Objects.equals(arg, "simple")) {
                logger.info("Building baseline with method: simple historical mean");
                MeanPatternBuilder.computeFunction(baselineFilesLoader.processLineByLine(), true);

                chart = new XYLineChart_AWT("Historical mean", "Baseline dla trasy " + routeID, MeanPatternBuilder.getValueForEachMinuteOfDay(day, routeID), new ArrayList<>());
                chart.pack();
                RefineryUtilities.centerFrameOnScreen(chart);
                chart.setVisible(true);
                chart.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent we) {
                        System.exit(0);
                    }
                });
            }
        } catch (NullPointerException e) {
            logger.error("WARNING! Historical data for specified day and route missing. Baseline was not computed.");
        }
    }

    public List<String> areAllRoutesIncluded(JSONArray loadedRoutes) {
        Map<String, Set<DayOfWeek>> list = baselineFilesLoader.getLoadedRoutes();
        boolean contains;
        List<String> missingRoutes = new LinkedList<>();
        for (int i = 0; i < loadedRoutes.length(); i++) {
            contains = false;
            JSONObject route = loadedRoutes.getJSONObject(i);
            String id = route.get("id").toString();
            for (Map.Entry<String, Set<DayOfWeek>> entry : list.entrySet()) {
                if (entry.getKey().equals(id)) {
                    for (DayOfWeek dayOfWeek : entry.getValue()) {
                        if (dayOfWeek == DayOfWeek.fromValue(DateTime.now().getDayOfWeek())) {
                            contains = true;
                            break;
                        }
                    }
                }
                if (contains) {
                    break;
                }
            }
            if (!contains) {
                missingRoutes.add(id);
            }
        }
        return missingRoutes;
    }

}
