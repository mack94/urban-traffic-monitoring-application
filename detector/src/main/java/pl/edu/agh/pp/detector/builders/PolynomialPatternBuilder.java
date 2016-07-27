package pl.edu.agh.pp.detector.builders;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import pl.edu.agh.pp.detector.distributions.GaussianDistribution;
import pl.edu.agh.pp.detector.enums.DayOfWeek;
import pl.edu.agh.pp.detector.records.Record;

import java.util.*;

/**
 * Created by Maciej on 18.07.2016.
 * 21:35
 * Project: detector.
 */
public final class PolynomialPatternBuilder {


    // allocate memory for each day of week
    private String[] days = new String[7];
    // consider records for each day independently
    private Map<DayOfWeek, Record> recordsOfDay;
    // WeightedObservedPoint list
    private static List<WeightedObservedPoint> points = new LinkedList<>();
    // or ...
    //private Map<Integer, List<WeightedObservedPoint>> points = new HashMap<>();
    private static PolynomialFunction function;

    public static class Holder {
        static final PolynomialPatternBuilder INSTANCE = new PolynomialPatternBuilder();
    }

    public static PolynomialPatternBuilder getInstance() {
        return Holder.INSTANCE;
    }

    public void addRecord(DayOfWeek dayOfWeek, Record record) {
        recordsOfDay.put(dayOfWeek, record);
    }

    public Map<DayOfWeek, Record> getRecordsOfDay() {
        return recordsOfDay;
    }

    public void setRecordsOfDay(Map<DayOfWeek, Record> recordsOfDay) {
        this.recordsOfDay = recordsOfDay;
    }

    // rather to parse point to WeightedObservedPoint
    private static void loadRecords() {
        // use loader
        // if (day == MONDAY) recordsOfDay.add(<MONDAY, RECORD>)
        String s1 = "";

        GaussianDistribution gaussian = new GaussianDistribution();
        double MEAN = 0.0f;
        double VARIANCE = 1.000f;
        Collection<WeightedObservedPoint> weightedObservedPoints = new LinkedList<>();
        for (int idx = 1; idx <= 7; ++idx){
            System.out.println(gaussian.getGaussian(MEAN, VARIANCE));
            //weightedObservedPoints.add(new WeightedObservedPoint(1, idx, gaussian.getGaussian(MEAN, VARIANCE)));
        }

        weightedObservedPoints.add(new WeightedObservedPoint(1, 0, 500));
        weightedObservedPoints.add(new WeightedObservedPoint(1, 14400, 501));
        weightedObservedPoints.add(new WeightedObservedPoint(1, 28800, 898));
        weightedObservedPoints.add(new WeightedObservedPoint(1, 36000, 720));
        weightedObservedPoints.add(new WeightedObservedPoint(1, 46000, 690));
        weightedObservedPoints.add(new WeightedObservedPoint(1, 57200, 907));
        weightedObservedPoints.add(new WeightedObservedPoint(1, 57800, 898));
        weightedObservedPoints.add(new WeightedObservedPoint(1, 59300, 917));
        weightedObservedPoints.add(new WeightedObservedPoint(1, 72000, 625));
        weightedObservedPoints.add(new WeightedObservedPoint(1, 82000, 540));
        weightedObservedPoints.add(new WeightedObservedPoint(1, 85000, 528));

        points.addAll(weightedObservedPoints);
    }

    // computes the value expected
    private static double function(int second) {
        return function.value(second);
    }

    // Please notice that, we don't want to put here method responsible for deciding whether the value is an anomaly or not.

    public static void computePolynomial() {
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(7);

        loadRecords();

        function = new PolynomialFunction(fitter.fit(points));
        System.out.println(function);
    }

    @Deprecated
    public static double[] getValueForEachSecondOfDay() {
        double[] values = new double[1440];
        int idx = 0;
        for (int i = 0; i < 86400; i = i + 60) {
            double value = function(i);
            values[idx] = value;
            idx++;
        }
        return values;
    }
}
