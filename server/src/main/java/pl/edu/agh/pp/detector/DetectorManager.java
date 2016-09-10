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
    private static FilesLoader filesLoader = new FilesLoader("C:\\Users\\Maciej\\Downloads\\logs_16-09-03_Sat\\TrafficLog_1_8___Sat_16-09-03.log");

    public DetectorManager() {
        try {
            filesLoader.processLineByLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        detector = polynomialPatternBuilder;
        PolynomialPatternBuilder.computePolynomial(filesLoader.getRecords());
    }

    public void doSomething(String logEntry) {
        try {
            LineChart_AWT chart;

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
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

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
