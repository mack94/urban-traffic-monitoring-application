package pl.edu.agh.pp.builders;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.jgroups.util.Average;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.adapters.Server;
import pl.edu.agh.pp.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.utils.Record;
import pl.edu.agh.pp.utils.enums.DayOfWeek;
import weka.classifiers.functions.LibSVM;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

import java.awt.geom.Point2D;
import java.util.*;

/**
 * Created by Krzysztof Węgrzyński on 2016-12-06.
 */
public class MeanPatternBuilder implements Strategy {

    private final Logger logger = (Logger) LoggerFactory.getLogger(IPatternBuilder.class);
    private static Map<DayOfWeek, Map<Integer, PolynomialSplineFunction>> meanMap = new HashMap<>();

    public static void computeFunction(List<Record> records, boolean shouldSetAfterComputing) throws Exception {
        Map<DayOfWeek, Map<Integer, PolynomialSplineFunction>> baseline = new HashMap<>();

        List<Record> _records = new LinkedList<>();
        Map<Integer, List<Point2D.Double>> pointsMap = new HashMap<>();
        _records.addAll(records);


        int recordRouteID;
        List<Point2D.Double> points;


        for (DayOfWeek day : DayOfWeek.values()) {

            pointsMap.clear();

            for (Record record : _records) {
                if(!record.getAnomalyID().equals("")) continue;

                recordRouteID = record.getRouteID();
                points = pointsMap.get(recordRouteID);
                if (points == null) {
                    points = new LinkedList<>();
                    pointsMap.put(recordRouteID, points);
                }
                if (record.getDayOfWeek().compareTo(day) == 0) {
                    points.add(new Point2D.Double(record.getTimeInSeconds(), record.getDurationInTraffic()));
                }
            }

            final Map<Integer, PolynomialSplineFunction> meanRoutes = new HashMap<>();

            LinearInterpolator li = new LinearInterpolator();
            //li.interpolate();

            pointsMap.keySet()
                    .stream()
                    .filter(routeID -> pointsMap.get(routeID).size() != 0)
                    .forEach(routeID -> {
                        List<Point2D.Double> pom = pointsMap.get(routeID);
                        pom.sort(Comparator.comparingDouble(Point2D.Double::getX));
                        Map<Double, AverageCounter> averages = new TreeMap<>();

                        int len = pom.size();


                        for(Point2D.Double point: pom){
                            double time = point.getX();
                            double duration = point.getY();
                            if(!averages.containsKey(time)) {
                                averages.put(time, new AverageCounter());
                            }
                            averages.get(time).addValue(duration);
                        }
                        double[] x = new double[averages.keySet().size()];
                        double[] y = new double[averages.keySet().size()];
                        int i = 0;
                        for(Double time: averages.keySet()) {
                            x[i] = time;
                            y[i] = averages.get(time).getAverage();
                            i++;
                        }

                        meanRoutes.put(routeID, new LinearInterpolator().interpolate(x, y));

                    });
            baseline.put(day, meanRoutes);

        }

        if (shouldSetAfterComputing)
            meanMap = baseline;
    }

    private static double function(DayOfWeek dayOfWeek, int routeIdx, int second) {
        if(meanMap.get(dayOfWeek).get(routeIdx).isValidPoint(second))
            return meanMap.get(dayOfWeek).get(routeIdx).value(second);
        else {
            return -1;
        }
    }

    public static double[] getValueForEachMinuteOfDay(DayOfWeek dayOfWeek, int routeIdx) {
        double[] values = new double[1440];
        int idx = 0;
        for (int i = 0; i < 86400; i = i + 60) {
            double value = function(dayOfWeek, routeIdx, i);
            if(value == -1) continue;
            values[idx] = value;
            idx++;
        }
        return values;
    }


    @Override
    public AnomalyOperationProtos.AnomalyMessage isAnomaly(DayOfWeek dayOfWeek, int routeIdx, long secondOfDay, long travelDuration) {
        return null;
    }

    @Override
    public void setServer(Server server) {

    }

    static class AverageCounter {

        private double total;
        private int amount;

        AverageCounter() {
            this.total = 0;
            this.amount = 0;
        }

        void addValue(double value) {
            total += value;
            amount++;
        }

        double getAverage() {
            return total / amount;
        }

        public int getAmount() {
            return amount;
        }

    }
}
