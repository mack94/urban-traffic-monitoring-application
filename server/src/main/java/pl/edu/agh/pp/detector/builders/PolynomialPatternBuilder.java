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

    private static IAnomalyTracker anomalyTracker = AnomalyTracker.getInstance();
    private static Map<DayOfWeek, List<Record>> recordsOfDay = new HashMap<>();
    private static Map<DayOfWeek, Map<Integer, PolynomialFunction>> polynomialFunctions = new HashMap<>();
    private final Logger logger = (Logger) LoggerFactory.getLogger(IPatternBuilder.class);
    public double errorSensitivity = 0.0;

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

            for (Record record : _records) {
                int recordRouteID = record.getRouteID();
                List<WeightedObservedPoint> points = weightedObservedPointsMap.get(recordRouteID);
                if (points == null) {
                    weightedObservedPointsMap.put(recordRouteID, new ArrayList<>());
                    points = weightedObservedPointsMap.get(recordRouteID);
                }
                if (record.getDayOfWeek().compareTo(day) == 0) {
                    points.add(new WeightedObservedPoint(1, record.getTimeInSeconds(), record.getDurationInTraffic()));
                    weightedObservedPointsMap.put(recordRouteID, points);
                }
            }

            Map<Integer, PolynomialFunction> polynomialFunctionRoutes = new HashMap<>();

            weightedObservedPointsMap.keySet().stream()
                    .filter(routeID -> weightedObservedPointsMap.get(routeID).size() != 0)
                    .forEach(routeID -> polynomialFunctionRoutes.put(routeID, new PolynomialFunction(fitter.fit(weightedObservedPointsMap.get(routeID)))));

            polynomialFunctions.put(day, polynomialFunctionRoutes);
        }
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

    @Override
    public double getErrorSensitivity() {
        return errorSensitivity;
    }

    @Override
    public void setErrorSensitivity(double errorSensitivity) {
        this.errorSensitivity = errorSensitivity;
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

    public AnomalyOperationProtos.AnomalyMessage isAnomaly(DayOfWeek dayOfWeek, int routeIdx, long secondOfDay, long travelDuration) {

        double predictedTravelDuration = function(dayOfWeek, routeIdx, (int) secondOfDay);
        double bounds = 0.35;// + Math.abs(polynomialFunctions.get(dayOfWeek).get(routeIdx).polynomialDerivative().value(secondOfDay)) + 1.0* (errorSensitivity) % 1.0; //%
        double errorDelta = predictedTravelDuration * bounds;

        // TODO: It's a problematic thing, because it is not precise at 00:00 and next 4 minutes of next day. (it took last day)
        // TODO: But at 12am there is no big differences between days. - so this difference might be omitted.

        double predictedTravelDurationMinimum = Double.MAX_VALUE;
        double predictedTravelDurationMaximum = Double.MIN_VALUE;
        for (int unitDiff = -5; unitDiff < 6; unitDiff++) {
            double tempDuration = function(dayOfWeek, routeIdx, (int) secondOfDay + (unitDiff * 60)); // unitDiff * 60 = 1 * minDiff
            predictedTravelDurationMinimum = predictedTravelDurationMinimum < tempDuration ? predictedTravelDurationMinimum : tempDuration;
            predictedTravelDurationMaximum = predictedTravelDurationMaximum < tempDuration ? tempDuration : predictedTravelDurationMaximum;
        }

        logger.info("#####################");
        logger.info("Error rate: " + errorDelta);
        logger.info(String.valueOf(predictedTravelDurationMinimum - errorDelta));
        logger.info(String.valueOf(predictedTravelDurationMaximum + errorDelta));

        double errorRate = 0.0;

        if ((travelDuration > predictedTravelDurationMaximum + errorDelta) || (travelDuration < predictedTravelDurationMinimum - errorDelta)) {

            if (travelDuration > predictedTravelDuration + errorDelta)
                errorRate = (predictedTravelDuration) / travelDuration;
            else
                errorRate = (predictedTravelDuration) / travelDuration;

            long anomalyID = anomalyTracker.put(routeIdx, DateTime.now());
            int severity = (int) ((Math.abs(predictedTravelDuration/travelDuration) * 3) % 6);

            return AnomalyOperationProtos.AnomalyMessage.newBuilder()
                    .setDayOfWeek(dayOfWeek.ordinal())
                    .setRouteIdx(routeIdx)
                    .setSecondOfDay((int) secondOfDay)
                    .setDuration((int) travelDuration) // TODO: Cast remove?
                    .setSeverity(severity) // TODO: Fix it
                    .setMessage(String.format("Error rate: %d %%", (int) (100 - errorRate * 100)))
                    .setAnomalyID(anomalyID)
                    .setDate(DateTime.now().toString("yyyy-MM-dd HH:mm:ss"))
                    .build();
        } else if (anomalyTracker.has(routeIdx)) {
            anomalyTracker.remove(routeIdx);
        }
        // TODO: else if anomaly is in map, "remove" it
        return null;
    }

    public static class Holder {
        static final PolynomialPatternBuilder INSTANCE = new PolynomialPatternBuilder();
    }
}
