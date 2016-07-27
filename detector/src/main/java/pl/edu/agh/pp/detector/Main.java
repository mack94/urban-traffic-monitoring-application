package pl.edu.agh.pp.detector;

import org.jfree.ui.RefineryUtilities;
import pl.edu.agh.pp.detector.builders.PolynomialPatternBuilder;
import pl.edu.agh.pp.detector.charts.LineChart_AWT;

/**
 * Created by Maciej on 18.07.2016.
 * 21:11
 * Project: detector.
 */
public class Main {

    private static PolynomialPatternBuilder polynomialPatternBuilder = PolynomialPatternBuilder.getInstance();

    public static void main(String[] args) {
        PolynomialPatternBuilder.computePolynomial();

        LineChart_AWT chart = new LineChart_AWT("-", "-", PolynomialPatternBuilder.getValueForEachSecondOfDay());

        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);
    }
}
