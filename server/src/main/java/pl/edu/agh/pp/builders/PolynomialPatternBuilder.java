package pl.edu.agh.pp.builders;

import java.util.*;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.adapters.Server;
import pl.edu.agh.pp.detectors.Detector;
import pl.edu.agh.pp.utils.enums.DayOfWeek;
import pl.edu.agh.pp.utils.AvailableHistoricalInfoHelper;
import pl.edu.agh.pp.utils.HistoricalInfoHelper;
import pl.edu.agh.pp.utils.LeverInfoHelper;
import pl.edu.agh.pp.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.utils.Record;
import pl.edu.agh.pp.serializers.FileBaselineSerializer;
import pl.edu.agh.pp.serializers.IBaselineSerializer;
import pl.edu.agh.pp.trackers.AnomalyTracker;
import pl.edu.agh.pp.trackers.IAnomalyTracker;

/**
 * Created by Maciej on 18.07.2016.
 * 21:35
 * Project: detector.
 */
public final class PolynomialPatternBuilder implements IPatternBuilder, Detector
{

    private static IAnomalyTracker anomalyTracker = AnomalyTracker.getInstance();
    private static IBaselineSerializer baselineSerializer = FileBaselineSerializer.getInstance();
    private static Map<DayOfWeek, List<Record>> recordsOfDay = new HashMap<>();
    private static Map<DayOfWeek, Map<Integer, PolynomialFunction>> polynomialFunctions = new HashMap<>();
    private static LeverInfoHelper leverInfoHelper = new LeverInfoHelper();
    private static final Logger logger = (Logger) LoggerFactory.getLogger(IPatternBuilder.class);
    private Server server;

    private PolynomialPatternBuilder()
    {
    }

    public static PolynomialPatternBuilder getInstance()
    {
        return Holder.INSTANCE;
    }

    private static double function(DayOfWeek dayOfWeek, int routeIdx, int second)
    {
        return polynomialFunctions.get(dayOfWeek).get(routeIdx).value(second);
    }

    public static void computePolynomial(List<Record> records, boolean shouldSetAfterComputing)
    {
        Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline = new HashMap<>();
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(17);

        List<Record> _records = new LinkedList<>();
        _records.addAll(records);
        HistoricalInfoHelper.addRecords(_records);

        for (DayOfWeek day : DayOfWeek.values())
        {

            Map<Integer, List<WeightedObservedPoint>> weightedObservedPointsMap = new HashMap<>();

            for (Record record : _records)
            {
                int recordRouteID = record.getRouteID();
                List<WeightedObservedPoint> points = weightedObservedPointsMap.get(recordRouteID);
                if (points == null)
                {
                    weightedObservedPointsMap.put(recordRouteID, new ArrayList<>());
                    points = weightedObservedPointsMap.get(recordRouteID);
                }
                if (record.getDayOfWeek().compareTo(day) == 0)
                {
                    points.add(new WeightedObservedPoint(1, record.getTimeInSeconds(), record.getDurationInTraffic()));
                    weightedObservedPointsMap.put(recordRouteID, points);
                }
                AvailableHistoricalInfoHelper.addAvailableDateRoute(
                        record.getDateTime().toString("yyyy-MM-dd"),
                        record.getRouteID()
                );
            }

            Map<Integer, PolynomialFunction> polynomialFunctionRoutes = new HashMap<>();

            weightedObservedPointsMap.keySet()
                    .stream()
                    .filter(routeID -> weightedObservedPointsMap.get(routeID).size() != 0)
                    .forEach(routeID -> polynomialFunctionRoutes.put(routeID, new PolynomialFunction(fitter.fit(weightedObservedPointsMap.get(routeID)))));

            baseline.put(day, polynomialFunctionRoutes);
        }

        String baselineFilename = baselineSerializer.serialize(baseline);
        if (baselineFilename != null)
        {
            logger.info("Baseline has been serialized in {} file", baselineFilename);
        }
        else
        {
            logger.debug("Error occurred while serializing baseline");
        }

        if (shouldSetAfterComputing)
            polynomialFunctions = baseline;

    }

    // It should be discussed.
    // Firstly whether the function is necessary.
    // Secondly whether each 'second' or 'minute' or different time interval.
    public static double[] getValueForEachMinuteOfDay(DayOfWeek dayOfWeek, int routeIdx)
    {
        double[] values = new double[1440];
        int idx = 0;
        for (int i = 0; i < 86400; i = i + 60)
        {
            double value = function(dayOfWeek, routeIdx, i);
            values[idx] = value;
            idx++;
        }
        return values;
    }

    @Override
    public void setBaseline(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline)
    {
        this.polynomialFunctions = baseline;
    }

    @Override
    public void setPartialBaseline(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline, DayOfWeek dayOfWeek, int id)
    {
        PolynomialFunction function = baseline.get(dayOfWeek).get(id);
        this.polynomialFunctions.get(dayOfWeek).put(id, function);
    }

    @Override
    public void updateBaseline(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline)
    {
        for (DayOfWeek dayOfWeek : baseline.keySet())
        {
            for (Map.Entry<Integer, PolynomialFunction> entry : baseline.get(dayOfWeek).entrySet())
            {
                polynomialFunctions.get(dayOfWeek).put(entry.getKey(), entry.getValue());
            }
        }
    }

    public void addRecord(DayOfWeek dayOfWeek, Record record)
    {
        // recordsOfDay.put(dayOfWeek, record); TODO
    }

    public Map<DayOfWeek, List<Record>> getRecordsOfDay()
    {
        return recordsOfDay;
    }

    // TODO
    // Please notice that, we don't want to put here method responsible for deciding whether the value is an anomaly or not.
    // I think so...

    public void setRecordsOfDay(Map<DayOfWeek, List<Record>> recordsOfDay)
    {
        PolynomialPatternBuilder.recordsOfDay = recordsOfDay;
    }

    public AnomalyOperationProtos.AnomalyMessage isAnomaly(DayOfWeek dayOfWeek, int routeIdx, long secondOfDay, long travelDuration)
    {

        double predictedTravelDuration = function(dayOfWeek, routeIdx, (int) secondOfDay);
        double errorSensitivity = leverInfoHelper.getLeverValue();
        double bounds = 0.25 + errorSensitivity; // %
        double errorDelta = predictedTravelDuration * bounds;

        // TODO: It's a problematic thing, because it is not precise at 00:00 and next 4 minutes of next day. (it took last day)
        // TODO: But at 12am there is no big differences between days. - so this difference might be omitted.

        double predictedTravelDurationMinimum = Double.MAX_VALUE;
        double predictedTravelDurationMaximum = Double.MIN_VALUE;
        for (int unitDiff = -5; unitDiff < 6; unitDiff++)
        {
            double tempDuration = function(dayOfWeek, routeIdx, (int) secondOfDay + (unitDiff * 60)); // unitDiff * 60 = 1 * minDiff
            predictedTravelDurationMinimum = predictedTravelDurationMinimum < tempDuration ? predictedTravelDurationMinimum : tempDuration;
            predictedTravelDurationMaximum = predictedTravelDurationMaximum < tempDuration ? tempDuration : predictedTravelDurationMaximum;
        }

        logger.info("#####################");
        logger.info("Error rate: " + errorDelta);
        logger.info(String.valueOf(predictedTravelDurationMinimum - errorDelta));
        logger.info(String.valueOf(predictedTravelDurationMaximum + errorDelta));

        double errorRate = 0.0;

        if ((travelDuration > predictedTravelDurationMaximum + errorDelta) || (travelDuration < predictedTravelDurationMinimum - errorDelta))
        {

            if (travelDuration > predictedTravelDuration + errorDelta)
                errorRate = (predictedTravelDuration) / travelDuration;
            else
                errorRate = (predictedTravelDuration) / travelDuration;

            String anomalyID = anomalyTracker.put(routeIdx, DateTime.now());
            int severity = (int) ((Math.abs(predictedTravelDuration / travelDuration) * 3) % 6);

            return AnomalyOperationProtos.AnomalyMessage.newBuilder()
                    .setDayOfWeek(dayOfWeek.ordinal())
                    .setRouteIdx(routeIdx)
                    .setSecondOfDay((int) secondOfDay)
                    .setDuration((int) travelDuration) // TODO: Cast remove?
                    .setSeverity(1) // TODO: Fix it
                    .setMessage(String.format("Error rate: > %f <", errorRate))
                    .setAnomalyID(anomalyID)
                    .setDate(DateTime.now().toString("yyyy-MM-dd HH:mm:ss"))
                    .setIsActive(true)
                    .setNormExceed((int) (errorRate*100) - 100)
                    .build();
        }
        else if (anomalyTracker.has(routeIdx))
        {
            anomalyTracker.remove(routeIdx);
        }
        return null;
    }

    public void setServer(Server server)
    {
        this.server = server;
        anomalyTracker.setServer(server);
    }

    public static class Holder
    {
        static final PolynomialPatternBuilder INSTANCE = new PolynomialPatternBuilder();
    }
}
