package pl.edu.agh.pp.detector.builders;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.detector.detectors.Detector;
import pl.edu.agh.pp.detector.enums.DayOfWeek;
import pl.edu.agh.pp.detector.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.detector.records.Record;
import pl.edu.agh.pp.detector.trackers.AnomalyTracker;
import pl.edu.agh.pp.detector.trackers.IAnomalyTracker;

import java.util.*;

/**
 * Created by Maciej on 18.07.2016.
 * 21:35
 * Project: detector.
 */
public final class PolynomialPatternBuilder implements IPatternBuilder, Detector {

    public double errorSensitivity = 0.0;

    private static IAnomalyTracker anomalyTracker = AnomalyTracker.getInstance();

    // consider records for each day independently
    private static Map<DayOfWeek, List<Record>> recordsOfDay = new HashMap<>();
    // WeightedObservedPoint list
    private static Map<WeightedObservedPoint, DayOfWeek> points = new HashMap<>();
    // or ...
    //private Map<Integer, List<WeightedObservedPoint>> points = new HashMap<>();
//    private static PolynomialFunction polynomialFunction; // TODO:Should be list of poly function - for each day and for each route.
    private static Map<DayOfWeek, List<PolynomialFunction>> polynomialFunctions = new HashMap<>();
    private final Logger logger = (Logger) LoggerFactory.getLogger(IPatternBuilder.class);
    // allocate memory for each day of week
    private String[] days = new String[7];

    public static PolynomialPatternBuilder getInstance() {
        return Holder.INSTANCE;
    }

    private static double function(DayOfWeek dayOfWeek, int routeIdx, int second) {
        return polynomialFunctions.get(dayOfWeek).get(routeIdx).value(second);
    }

    public static void computePolynomial(List<Record> records) {
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(15);

        List<Record> _records = new LinkedList<>();
        _records.addAll(records);

        for (DayOfWeek day : DayOfWeek.values()) {

            Map<Integer, List<WeightedObservedPoint>> weightedObservedPointsMap = new HashMap<>();

            //TODO: It defined the number of routes.
            // The value 100 is hardcoded and it should be changed.
            // Try to change the list of routes by map.
            for (int i = 0; i < 100; i++) {
                weightedObservedPointsMap.put(i, new ArrayList<>());
            }
            // END TODO

            for (Record record : _records) {
                if (record.getDayOfWeek().compareTo(day) == 0) {
                    int recordRouteID = record.getRouteID();
                    List<WeightedObservedPoint> points = weightedObservedPointsMap.get(recordRouteID);
                    points.add(new WeightedObservedPoint(1, record.getTimeInSeconds(), record.getDurationInTraffic()));
                    weightedObservedPointsMap.put(recordRouteID, points);
//                    _records.remove(record);
                }
            }

            List<PolynomialFunction> polynomialFunctionRoutes = new LinkedList<>();

            for (Integer routeID : weightedObservedPointsMap.keySet()) {
                //System.out.println("DAY= " + day + " routeID " + routeID + " = " + weightedObservedPointsMap.get(routeID).size());
                if (weightedObservedPointsMap.get(routeID).size() != 0)
                    polynomialFunctionRoutes.add(new PolynomialFunction(fitter.fit(weightedObservedPointsMap.get(routeID))));
            }

            polynomialFunctions.put(day, polynomialFunctionRoutes);
        }
    }

    @Override
    public void setErrorSensitivity(double errorSensitivity) {
        this.errorSensitivity = errorSensitivity;
    }

    @Override
    public double getErrorSensitivity() {
        return errorSensitivity;
    }

    // It should be discussed.
    // Firstly whether the function is necessary.
    // Secondly whether each 'second' or 'minute' or different time interval.
    public static double[] getValueForEachMinuteOfDay(DayOfWeek dayOfWeek, int routeIdx) {
        double[] values = new double[1440];
        int idx = 0;
        for (int i = 0; i < 86400; i = i + 60) {
            double value = function(dayOfWeek, routeIdx, i);
            values[idx] = value;
            idx++;
        }
        return values;
    }

    public void addRecord(DayOfWeek dayOfWeek, Record record) {
//        recordsOfDay.put(dayOfWeek, record); TODO
    }

    public Map<DayOfWeek, List<Record>> getRecordsOfDay() {
        return recordsOfDay;
    }

    // TODO
    // Please notice that, we don't want to put here method responsible for deciding whether the value is an anomaly or not.
    // I think so...

    public void setRecordsOfDay(Map<DayOfWeek, List<Record>> recordsOfDay) {
        PolynomialPatternBuilder.recordsOfDay = recordsOfDay;
    }

    //TODO
    public AnomalyOperationProtos.AnomalyMessage isAnomaly(DayOfWeek dayOfWeek, int routeIdx, long secondOfDay, long travelDuration) {

        double predictedTravelDuration = function(dayOfWeek, routeIdx, (int) secondOfDay);
        double bounds = 0.10 + Math.abs(polynomialFunctions.get(dayOfWeek).get(routeIdx).polynomialDerivative().value(secondOfDay)) + (errorSensitivity) % 1; //%
        double errorDelta = predictedTravelDuration * bounds;

        logger.info("#####################");
        logger.info("Error rate: " + errorDelta);
        logger.info(String.valueOf(predictedTravelDuration - errorDelta));
        logger.info(String.valueOf(predictedTravelDuration + errorDelta));

        double errorRate = 0.0;
        
        if ((travelDuration > predictedTravelDuration + errorDelta) || (travelDuration < predictedTravelDuration - errorDelta)) {

            if (travelDuration > predictedTravelDuration + errorDelta)
                errorRate = Math.abs(predictedTravelDuration + errorDelta) / travelDuration;
            else
                errorRate = Math.abs(predictedTravelDuration - errorDelta) / travelDuration;

            long anomalyID = anomalyTracker.put(routeIdx, DateTime.now());

            AnomalyOperationProtos.AnomalyMessage message =
                    AnomalyOperationProtos.AnomalyMessage.newBuilder()
                            .setDayOfWeek(dayOfWeek.ordinal())
                            .setRouteIdx(routeIdx)
                            .setSecondOfDay((int) secondOfDay)
                            .setDuration((int) travelDuration) // TODO: Cast remove?
                            .setSeverity(1) // TODO: Fix it
                            .setMessage(String.format("Error rate: %d %%", (int) (100 - errorRate * 100)))
                            .setAnomalyID(anomalyID)
                            .setDate(DateTime.now().toString("yyyy-MM-dd HH:mm:ss"))
                            .build();
            return message;
        }
        return null;
    }

    public static class Holder {
        static final PolynomialPatternBuilder INSTANCE = new PolynomialPatternBuilder();
    }
}
