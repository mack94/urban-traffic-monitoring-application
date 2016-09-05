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

/**
 * Created by Maciej on 18.07.2016.
 * 21:11
 * Project: detector.
 */
public class Main {

    private static PolynomialPatternBuilder polynomialPatternBuilder = PolynomialPatternBuilder.getInstance();
    private static Detector detector;
    private static FilesLoader filesLoader = new FilesLoader("C:\\Users\\Maciej\\Downloads\\9-16(1)\\9-16.txt");

    public static void main(String[] args) {
        try {
            filesLoader.processLineByLine();
            detector = PolynomialPatternBuilder.getInstance();
//            PolynomialPatternBuilder.computePolynomial(filesLoader.getRecords());

            LineChart_AWT chart = new LineChart_AWT("-", "-", PolynomialPatternBuilder.getValueForEachSecondOfDay(DayOfWeek.FRIDAY, 0));
            chart.pack();
            RefineryUtilities.centerFrameOnScreen(chart);
            chart.setVisible(true);

            System.out.println(detector.isAnomaly(DayOfWeek.FRIDAY, 0, 10200, 518)); // FIXME: To not hardcoded.

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            InputParser inputParser = new InputParser();

            while (true) {
                String incoming = br.readLine();
                Record record = inputParser.parse(incoming);

                if (incoming != "") {
                    chart = new LineChart_AWT("-", "-", PolynomialPatternBuilder.getValueForEachSecondOfDay(record.getDayOfWeek(), record.getRouteID() - 9));
                    chart.pack();
                    RefineryUtilities.centerFrameOnScreen(chart);
                    chart.setVisible(true);
                }

                System.out.println(detector.isAnomaly(record.getDayOfWeek(), record.getRouteID() - 9, record.getTimeInSeconds(), record.getDurationInTraffic()));
                Thread.sleep(100);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
