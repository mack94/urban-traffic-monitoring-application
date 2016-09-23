package pl.edu.agh.pp.detector;

import org.jfree.ui.RefineryUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.detector.adapters.Server;
import pl.edu.agh.pp.detector.builders.PolynomialPatternBuilder;
import pl.edu.agh.pp.detector.charts.LineChart_AWT;
import pl.edu.agh.pp.detector.charts.XYLineChart_AWT;
import pl.edu.agh.pp.detector.detectors.Detector;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Maciej on 18.07.2016.
 * 21:11
 * Project: detector.
 */
public class DetectorManager {

    private static final String ANOMALY_SEARCH_LOGS_PATH = "C:\\Inz\\appended_file.txt";
    private static final PolynomialPatternBuilder polynomialPatternBuilder = PolynomialPatternBuilder.getInstance();
    private static final FilesLoader anomalySearchFilesLoader = new FilesLoader(ANOMALY_SEARCH_LOGS_PATH);
    private static Detector detector;
    private final Logger logger = (Logger) LoggerFactory.getLogger(DetectorManager.class);
    private String BASELINE_LOGS_PATH;
    private FilesLoader baselineFilesLoader;
    //    private static ChannelReceiver client = new ChannelReceiver();
    private Server server;

    public DetectorManager(Server server, String logFile) {
        BASELINE_LOGS_PATH = logFile;
        FilesLoader baselineFilesLoader = new FilesLoader(BASELINE_LOGS_PATH);

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

}
