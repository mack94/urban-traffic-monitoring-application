package pl.edu.agh.pp.detector;

import org.jfree.ui.RefineryUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.detector.adapters.Server;
import pl.edu.agh.pp.detector.builders.PolynomialPatternBuilder;
import pl.edu.agh.pp.detector.charts.LineChart_AWT;
import pl.edu.agh.pp.detector.charts.XYLineChart_AWT;
import pl.edu.agh.pp.detector.detectors.Detector;
import pl.edu.agh.pp.detector.enums.DayOfWeek;
import pl.edu.agh.pp.detector.loaders.FilesLoader;
import pl.edu.agh.pp.detector.loaders.InputParser;
import pl.edu.agh.pp.detector.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.detector.records.Record;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Maciej on 18.07.2016.
 * 21:11
 * Project: detector.
 */
public class DetectorManager {

    private static final String ANOMALY_SEARCH_LOGS_PATH = "C:\\Users\\Krzysztof Węgrzyński\\Desktop\\UCZE SIĘ + DRUKARKA\\inzynierka\\dane_z_agegacja\\poniedzialek\\rok_szkolny\\TrafficLog_1_8___Mon_16-09-05.log";
    private static final PolynomialPatternBuilder polynomialPatternBuilder = PolynomialPatternBuilder.getInstance();
    private static final FilesLoader anomalySearchFilesLoader = new FilesLoader(ANOMALY_SEARCH_LOGS_PATH, "C:\\Users\\Krzysztof Węgrzyński\\Desktop\\UCZE SIĘ + DRUKARKA\\inzynierka\\dane_z_agegacja\\wtorek\\rok_szkolny\\TrafficLog_1_8___Tue_16-09-06.log");
    private static Detector detector;
    private final Logger logger = (Logger) LoggerFactory.getLogger(DetectorManager.class);
    private FilesLoader baselineFilesLoader;
    //    private static ChannelReceiver client = new ChannelReceiver();
    private Server server;

    public DetectorManager(Server server, String ... logFiles) {
        FilesLoader baselineFilesLoader = new FilesLoader(logFiles);

        try {
            baselineFilesLoader.processLineByLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        detector = polynomialPatternBuilder;
        PolynomialPatternBuilder.computePolynomial(baselineFilesLoader.getRecords());
        this.server = server;
    }

    public void doSomething(String logEntry) {
        try {
            LineChart_AWT chart;

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            InputParser inputParser = new InputParser();

            Record record = inputParser.parse(logEntry);

            if (!Objects.equals(logEntry, "")) {
//                chart = new LineChart_AWT("-", "-", PolynomialPatternBuilder.getValueForEachMinuteOfDay(record.getDayOfWeek(), record.getRouteID() - 1));
//                chart.pack();
//                RefineryUtilities.centerFrameOnScreen(chart);
//                chart.setVisible(true);
            }
            AnomalyOperationProtos.AnomalyMessage isAnomaly = detector.isAnomaly(record.getDayOfWeek(), record.getRouteID() - 1, record.getTimeInSeconds(), record.getDurationInTraffic());
            // TODO: Need to send the modified message - message should have ID assigned after AnomalyTracker analyse.
            // TODO: It need to be in the isAnomaly method.
            if (isAnomaly != null) {
                server.send(ByteBuffer.wrap(isAnomaly.toByteArray()));
            }
            Thread.sleep(100);

        } catch (InterruptedException e) {
            logger.error("DetectorManager :: InterruptedException " + e);
        }
    }

    public void displayAnomaliesForRoute(int routeId) {
        try {
            anomalySearchFilesLoader.processLineByLine();
            XYLineChart_AWT chart;

//            while (true) {
//                String incoming = br.readLine();
            //Record record = inputParser.parse(logEntry);
            List<Record> recordsTestedForAnomalies = anomalySearchFilesLoader.getRecords();
            List<Record> anomalousRecords = new ArrayList<>();
            int counter = 0;
            for (Record record : recordsTestedForAnomalies) {
                if (record.getRouteID() == routeId) {
                    if (detector.isAnomaly(record.getDayOfWeek(), record.getRouteID() - 1, record.getTimeInSeconds(), record.getDurationInTraffic()) != null) {
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
            for (Record record : recordsTestedForAnomalies) {
                if (record.getRouteID() == routeId) {
                    chart = new XYLineChart_AWT(file, "Baseline i anomalie dla trasy " + record.getRouteID(), PolynomialPatternBuilder.getValueForEachMinuteOfDay(record.getDayOfWeek(), record.getRouteID() - 1), anomalousRecords);
                    chart.pack();
                    RefineryUtilities.centerFrameOnScreen(chart);
                    chart.setVisible(true);
                    break;
                }
            }

            Thread.sleep(100);

        } catch (InterruptedException e) {
            logger.error("DetectorManager :: InterruptedException " + e);
        } catch (IOException e) {
            logger.error("DetectorManager :: IOException " + e);
        }
    }

    public void displayAnomaliesForFile() {
        try {
            anomalySearchFilesLoader.processLineByLine();
            XYLineChart_AWT chart;

//            while (true) {
//                String incoming = br.readLine();
            //Record record = inputParser.parse(logEntry);
            List<Record> recordsTestedForAnomalies = anomalySearchFilesLoader.getRecords();
            Map<DayOfWeek, Map<Integer, List<Record>>> anomalousRecords = new HashMap<DayOfWeek, Map<Integer,List<Record>>>();
            Map<Integer, List<Record>> dayOfWeekRecords;
            List<Record> routeAndDayRecords;
            int startingRouteId = recordsTestedForAnomalies.get(0).getRouteID() - 1;
            for(DayOfWeek dayOfWeek: DayOfWeek.values()) {
                dayOfWeekRecords = new HashMap<Integer, List<Record>>();
                for(int routeId = startingRouteId; routeId < 8+startingRouteId; routeId++) {
                    routeAndDayRecords = new ArrayList<Record>();
                    dayOfWeekRecords.put(routeId, routeAndDayRecords);
                }
                anomalousRecords.put(dayOfWeek, dayOfWeekRecords);
            }


            int counter = 0;
            for (Record record : recordsTestedForAnomalies) {
                if (detector.isAnomaly(record.getDayOfWeek(), record.getRouteID() - 1, record.getTimeInSeconds(), record.getDurationInTraffic()) != null) {
                    System.out.println("-------------------------------");
                    System.out.println("Day: " + record.getDayOfWeek());
                    System.out.println("When: " + record.getDateTime());
                    System.out.println("RouteId: " + record.getRouteID());
                    System.out.println("-------------------------------");

                    anomalousRecords.get(record.getDayOfWeek()).get(record.getRouteID() - 1).add(record);
                    counter++;
                }

            }
            System.out.println("Number of anomalies: " + counter);

//            Path p = Paths.get(ANOMALY_SEARCH_LOGS_PATH);
//            String anomaly_search_logs_file_name = p.getFileName().toString();
//            p = Paths.get(BASELINE_LOGS_PATH);
//            String baseline_logs_file_name = p.getFileName().toString();
            for(DayOfWeek dayOfWeek: DayOfWeek.values()) {
                for(int routeId = startingRouteId; routeId < 8+startingRouteId; routeId++) {
                    if(anomalousRecords.get(dayOfWeek).get(routeId).size() != 0) {
                        //TODO: inferring from which file baseline and anomalies are from, non trivial
                        chart = new XYLineChart_AWT("Anomaly and baseline chart", "Baseline: " + "baseline_file_name" + System.lineSeparator()
                                + "Anomalie: " + "anomaly_file_name"  + System.lineSeparator()
                                + "Trasa: " + (routeId+1), PolynomialPatternBuilder.getValueForEachMinuteOfDay(dayOfWeek, routeId), anomalousRecords.get(dayOfWeek).get(routeId));
                        chart.pack();
                        RefineryUtilities.centerFrameOnScreen(chart);
                        chart.setVisible(true);
                        //TODO: saving charts to file system instead of showing all of them at once
                    }
                }
            }
//            for (Record record : recordsTestedForAnomalies) {
//                if (record.getRouteID() == routeId) {
//                    chart = new XYLineChart_AWT(file, "Baseline i anomalie dla trasy " + record.getRouteID(), PolynomialPatternBuilder.getValueForEachMinuteOfDay(record.getDayOfWeek(), record.getRouteID() - 1), anomalousRecords);
//                    chart.pack();
//                    RefineryUtilities.centerFrameOnScreen(chart);
//                    chart.setVisible(true);
//                    break;
//                }
//            }

            Thread.sleep(100);

        } catch (InterruptedException e) {
            logger.error("DetectorManager :: InterruptedException " + e);
        } catch (IOException e) {
            logger.error("DetectorManager :: IOException " + e);
        }
    }

}
