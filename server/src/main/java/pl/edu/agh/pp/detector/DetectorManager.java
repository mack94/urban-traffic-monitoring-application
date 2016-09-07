package pl.edu.agh.pp.detector;

import org.jfree.ui.RefineryUtilities;
import pl.edu.agh.pp.detector.builders.PolynomialPatternBuilder;
import pl.edu.agh.pp.detector.charts.LineChart_AWT;
import pl.edu.agh.pp.detector.detectors.Detector;
import pl.edu.agh.pp.detector.enums.DayOfWeek;
import pl.edu.agh.pp.detector.loaders.FilesLoader;
import pl.edu.agh.pp.detector.loaders.InputParser;
import pl.edu.agh.pp.detector.records.Record;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Maciej on 18.07.2016.
 * 21:11
 * Project: detector.
 */
public class DetectorManager {

    private static PolynomialPatternBuilder polynomialPatternBuilder = PolynomialPatternBuilder.getInstance();
    private static Detector detector;
    private static FilesLoader filesLoader = new FilesLoader("C:\\Users\\drdrz\\Downloads\\logs_16-08-24_Wed\\TrafficLog_1_8___Wed_16-08-24.log");

    public void doSomething(String logEntry) {
        try {
            filesLoader.processLineByLine();
            detector = PolynomialPatternBuilder.getInstance();
            PolynomialPatternBuilder.computePolynomial(filesLoader.getRecords());

            LineChart_AWT chart = new LineChart_AWT("-", "-", PolynomialPatternBuilder.getValueForEachSecondOfDay(DayOfWeek.WEDNESDAY, 0));
            chart.pack();
            RefineryUtilities.centerFrameOnScreen(chart);
            chart.setVisible(true);

            System.out.println(detector.isAnomaly(DayOfWeek.WEDNESDAY, 0, 10200, 518)); // FIXME: To not hardcoded.

//            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            InputParser inputParser = new InputParser();

//            while (true) {
//                String incoming = br.readLine();
            Record record = inputParser.parse(logEntry);

            if (logEntry != "") {
                chart = new LineChart_AWT("-", "-", PolynomialPatternBuilder.getValueForEachSecondOfDay(record.getDayOfWeek(), record.getRouteID() - 1));
                chart.pack();
                RefineryUtilities.centerFrameOnScreen(chart);
                chart.setVisible(true);
            }

            System.out.println(detector.isAnomaly(record.getDayOfWeek(), record.getRouteID() - 1, record.getTimeInSeconds(), record.getDurationInTraffic()));
            Thread.sleep(100);
//            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
