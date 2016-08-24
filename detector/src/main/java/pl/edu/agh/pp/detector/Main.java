package pl.edu.agh.pp.detector;

import org.jfree.ui.RefineryUtilities;
import pl.edu.agh.pp.detector.builders.PolynomialPatternBuilder;
import pl.edu.agh.pp.detector.charts.LineChart_AWT;
import pl.edu.agh.pp.detector.detectors.BasicDetector;
import pl.edu.agh.pp.detector.enums.DayOfWeek;
import pl.edu.agh.pp.detector.loaders.FilesLoader;

import java.io.IOException;

/**
 * Created by Maciej on 18.07.2016.
 * 21:11
 * Project: detector.
 */
public class Main {

    private static PolynomialPatternBuilder polynomialPatternBuilder = PolynomialPatternBuilder.getInstance();
    private static BasicDetector basicDetector = new BasicDetector();
    private static FilesLoader filesLoader = new FilesLoader("C:\\Users\\Maciej\\Documents\\Programowanie\\Praca_Inzynierska\\detector\\src\\main\\java\\pl\\edu\\agh\\pp\\detector\\loadtest.txt");

    public static void main(String[] args) {
        try {
            filesLoader.processLineByLine();
            PolynomialPatternBuilder.computePolynomial(filesLoader.getRecords());

            LineChart_AWT chart = new LineChart_AWT("-", "-", PolynomialPatternBuilder.getValueForEachSecondOfDay(DayOfWeek.MONDAY, 0));

            chart.pack();
            RefineryUtilities.centerFrameOnScreen(chart);
            chart.setVisible(true);

            System.out.println(basicDetector.isAnomaly(DayOfWeek.MONDAY, 1, 24300, 781)); // FIXME: To not hardcoded.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
