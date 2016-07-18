package pl.agh.edu.pp.detector.builders;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.PolynomialFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import pl.agh.edu.pp.detector.records.Record;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Maciej on 18.07.2016.
 * 21:35
 * Project: detector.
 */
public class PolynomialPatternBuilder {


    // allocate memory for each day of week
    private String[] days = new String[7];
    // consider records for each day independently
    // 0 - sunday ... 6 - saturday
    private Map<Integer, Record> recordsOfDay;
    // WeightedObservedPoint list
    private List<WeightedObservedPoint> points = new LinkedList<>();
    // or ...
    //private Map<Integer, List<WeightedObservedPoint>> points = new HashMap<>();

    public static class Holder {
        static final PolynomialPatternBuilder INSTANCE = new PolynomialPatternBuilder();
    }

    public static PolynomialPatternBuilder getInstance() {
        return Holder.INSTANCE;
    }

//    public void addRecord(Record record) {
//        records.add(record);
//    }

    private static void loadRecords() {
        // use loader
        // if (day == MONDAY) recordsOfDay.add(<MONDAY, RECORD>)
        String s1 = "";
        WeightedObservedPoint w1 = new WeightedObservedPoint(1, 90, 650);

    }

    private void computePolynomial() {
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(5);

        PolynomialFunction function = new PolynomialFunction(fitter.fit(points));
    }
}
