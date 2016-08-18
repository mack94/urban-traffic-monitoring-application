package pl.edu.agh.pp.detector;

import org.jfree.ui.RefineryUtilities;
import pl.edu.agh.pp.detector.builders.PolynomialPatternBuilder;
import pl.edu.agh.pp.detector.charts.LineChart_AWT;
import pl.edu.agh.pp.detector.detectors.BasicDetector;
import pl.edu.agh.pp.detector.enums.DayOfWeek;

/**
 * Created by Maciej on 18.07.2016.
 * 21:11
 * Project: detector.
 */
public class Main {

    private static PolynomialPatternBuilder polynomialPatternBuilder = PolynomialPatternBuilder.getInstance();
    private static BasicDetector basicDetector = new BasicDetector();

    public static void main(String[] args) {
        PolynomialPatternBuilder.computePolynomial();

        LineChart_AWT chart = new LineChart_AWT("-", "-", PolynomialPatternBuilder.getValueForEachSecondOfDay(DayOfWeek.MONDAY, 10));

        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);

        System.out.println(basicDetector.isAnomaly(DayOfWeek.MONDAY, 10, 24300, 900)); // FIXME: To not hardcoded.
    }
}
