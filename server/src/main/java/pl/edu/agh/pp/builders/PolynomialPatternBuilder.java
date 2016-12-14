package pl.edu.agh.pp.builders;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.adapters.AnomaliesServer;
import pl.edu.agh.pp.adapters.Server;
import pl.edu.agh.pp.detectors.BaselineNameHolder;
import pl.edu.agh.pp.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.serializers.FileBaselineSerializer;
import pl.edu.agh.pp.serializers.IBaselineSerializer;
import pl.edu.agh.pp.trackers.AnomalyTracker;
import pl.edu.agh.pp.trackers.IAnomalyTracker;
import pl.edu.agh.pp.utils.*;
import pl.edu.agh.pp.utils.enums.DayOfWeek;

import java.util.*;

/**
 * Created by Maciej on 18.07.2016.
 * 21:35
 * Project: detector.
 */
public final class PolynomialPatternBuilder implements IPatternBuilder, Strategy {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(IPatternBuilder.class);
    private static final double DECAY_STEP = 0.05;
    private static final double MAX_DECAY = 0.3;
    private static IAnomalyTracker anomalyTracker = AnomalyTracker.getInstance();
    private static IBaselineSerializer baselineSerializer = FileBaselineSerializer.getInstance();
    private static Map<DayOfWeek, Map<Integer, PolynomialFunction>> polynomialFunctions = new HashMap<>();
    private static LeverInfoHelper leverInfoHelper = LeverInfoHelper.getInstance();
    private static BaselineWindowSizeInfoHelper baselineWindowSizeInfoHelper = BaselineWindowSizeInfoHelper.getInstance();
    private static int polymonialDegree = 17;

    private PolynomialPatternBuilder() {
    }

    public static PolynomialPatternBuilder getInstance() {
        return Holder.INSTANCE;
    }

    private static double function(DayOfWeek dayOfWeek, int routeIdx, int second) {
        return polynomialFunctions.get(dayOfWeek).get(routeIdx).value(second);
    }

    public static void computePolynomial(List<Record> records, boolean shouldSetAfterComputing) {
        Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline = new HashMap<>();
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(polymonialDegree);

        List<Record> _records = new LinkedList<>();
        _records.addAll(records);
        HistoricalInfoHelper.addRecords(_records);
        DateTime now = DateTime.now();

        for (DayOfWeek day : DayOfWeek.values()) {

            Map<Integer, List<WeightedObservedPoint>> weightedObservedPointsMap = new HashMap<>();

            for (Record record : _records) {
                if (!"".equals(record.getAnomalyID()) || !"default".equals(record.getWaypoints()))
                    continue;

                int recordRouteID = record.getRouteID();
                List<WeightedObservedPoint> points = weightedObservedPointsMap.get(recordRouteID);
                if (points == null) {
                    weightedObservedPointsMap.put(recordRouteID, new ArrayList<>());
                    points = weightedObservedPointsMap.get(recordRouteID);
                }
                if (record.getDayOfWeek().compareTo(day) == 0) {
                    points.add(new WeightedObservedPoint(1 - computeWeightDecay(now, record), record.getTimeInSeconds(), record.getDurationInTraffic()));
                    weightedObservedPointsMap.put(recordRouteID, points);
                }
                AvailableHistoricalInfoHelper.addAvailableDateRoute(
                        record.getDateTime().toString("yyyy-MM-dd"),
                        record.getRouteID());
            }

            Map<Integer, PolynomialFunction> polynomialFunctionRoutes = new HashMap<>();

            weightedObservedPointsMap.keySet()
                    .stream()
                    .filter(routeID -> weightedObservedPointsMap.get(routeID).size() != 0)
                    .forEach(routeID -> polynomialFunctionRoutes.put(routeID, new PolynomialFunction(fitter.fit(weightedObservedPointsMap.get(routeID)))));

            baseline.put(day, polynomialFunctionRoutes);
        }

        String baselineFilename = baselineSerializer.serialize(baseline);
        if (baselineFilename != null) {
            logger.info("Baseline has been serialized in {} file", baselineFilename);
        } else {
            logger.debug("Error occurred while serializing baseline");
        }

        if (shouldSetAfterComputing) {
            polynomialFunctions = baseline;
            setBaselineNames(baseline, baselineFilename);
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

    private static double computeWeightDecay(DateTime now, Record record) {
        int dayDiff = Days.daysBetween(record.getDateTime(), now).getDays();
        int counter = -1;
        while (dayDiff > 0) {
            dayDiff -= 7;
            counter++;
        }

        return DECAY_STEP * counter < MAX_DECAY ? DECAY_STEP * counter : MAX_DECAY;
    }

    private static void setBaselineNames(Map<DayOfWeek, Map<Integer, PolynomialFunction>> map, String filename) {
        BaselineNameHolder.clear();
        for (DayOfWeek day : map.keySet()) {
            for (Integer route : map.get(day).keySet()) {
                BaselineNameHolder.addBaseline(day, route, filename);
            }
        }
    }

    public static void setPolymonialDegree(int newDegree) {
        polymonialDegree = newDegree;
    }

    @Override
    public void setBaseline(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline, String filename) {
        polynomialFunctions = baseline;
        setBaselineNames(baseline, filename);
    }

    @Override
    public void setPartialBaseline(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline, DayOfWeek dayOfWeek, int id, String filename) {
        PolynomialFunction function = baseline.get(dayOfWeek).get(id);
        polynomialFunctions.get(dayOfWeek).put(id, function);
        BaselineNameHolder.addBaseline(dayOfWeek, id, filename);
    }

    @Override
    public void updateBaseline(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline, String filename) {
        for (DayOfWeek dayOfWeek : baseline.keySet()) {
            for (Map.Entry<Integer, PolynomialFunction> entry : baseline.get(dayOfWeek).entrySet()) {
                polynomialFunctions.get(dayOfWeek).put(entry.getKey(), entry.getValue());
                BaselineNameHolder.addBaseline(dayOfWeek, entry.getKey(), filename);
            }
        }
    }

    public AnomalyOperationProtos.AnomalyMessage isAnomaly(DayOfWeek dayOfWeek, int routeIdx, long secondOfDay, long travelDuration) {
        double predictedTravelDuration = function(dayOfWeek, routeIdx, (int) secondOfDay);
        double errorSensitivity = leverInfoHelper.getLeverValue();
        double bounds = 0.25 + errorSensitivity; // %
        double errorDelta = predictedTravelDuration * bounds;
        int baselineWindowSize = baselineWindowSizeInfoHelper.getBaselineWindowSizeValue();
        double predictedTravelDurationMinimum = Double.MAX_VALUE;
        double predictedTravelDurationMaximum = Double.MIN_VALUE;
        double errorRate;

        for (int unitDiff = -baselineWindowSize; unitDiff <= baselineWindowSize; unitDiff++) {
            double tempDuration = function(dayOfWeek, routeIdx, (int) secondOfDay + (unitDiff * 60));
            predictedTravelDurationMinimum = predictedTravelDurationMinimum < tempDuration ? predictedTravelDurationMinimum : tempDuration;
            predictedTravelDurationMaximum = predictedTravelDurationMaximum < tempDuration ? tempDuration : predictedTravelDurationMaximum;
        }

        logger.info("#####################");
        logger.info("Error rate: " + errorDelta);
        logger.info(String.valueOf(predictedTravelDurationMinimum - errorDelta));
        logger.info(String.valueOf(predictedTravelDurationMaximum + errorDelta));

        if ((travelDuration > predictedTravelDurationMaximum + errorDelta)) {

            if (travelDuration > predictedTravelDuration + errorDelta)
                errorRate = travelDuration / predictedTravelDuration;
            else
                errorRate = travelDuration / predictedTravelDuration;

            String anomalyID = anomalyTracker.put(routeIdx, DateTime.now());
            String baseline = BaselineNameHolder.getBaseline(dayOfWeek, routeIdx);
            baseline = StringUtils.removeStart(baseline, "baseline/");
            baseline = StringUtils.removeEnd(baseline, ".ser");
            anomalyID += "#" + baseline;
            int severity = (int) ((Math.abs(predictedTravelDuration / travelDuration) * 3) % 6);
            System.out.println("Exceed - " + errorRate * 100);
            return AnomalyOperationProtos.AnomalyMessage.newBuilder()
                    .setDayOfWeek(dayOfWeek.ordinal())
                    .setRouteIdx(routeIdx)
                    .setSecondOfDay((int) secondOfDay)
                    .setDuration((int) travelDuration)
                    .setSeverity(1) // TODO: Fix it
                    .setMessage(String.format("Error rate: > %f <", errorRate))
                    .setAnomalyID(anomalyID)
                    .setDate(DateTime.now().toString("yyyy-MM-dd HH:mm:ss"))
                    .setIsActive(true)
                    .setNormExceed((int) (errorRate * 100) - 100)
                    .build();
        } else if (anomalyTracker.has(routeIdx)) {
            anomalyTracker.remove(routeIdx);
        }
        return null;
    }

    @Override
    public void setServer(Server server) {
        anomalyTracker.setAnomaliesServer((AnomaliesServer) server);
    }

    public static class Holder {
        static final PolynomialPatternBuilder INSTANCE = new PolynomialPatternBuilder();
    }
}
