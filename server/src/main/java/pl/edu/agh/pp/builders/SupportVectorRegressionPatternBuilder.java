package pl.edu.agh.pp.builders;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.detectors.Detector;
import pl.edu.agh.pp.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.serializers.FileBaselineSerializer;
import pl.edu.agh.pp.serializers.IBaselineSerializer;
import pl.edu.agh.pp.trackers.AnomalyTracker;
import pl.edu.agh.pp.trackers.IAnomalyTracker;
import pl.edu.agh.pp.utils.*;
import pl.edu.agh.pp.utils.enums.DayOfWeek;
import weka.classifiers.functions.LibSVM;
import weka.core.*;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

/**
 * Created by Krzysztof Węgrzyński on 2016-04-21.
 */
public class SupportVectorRegressionPatternBuilder implements Detector {

    private static IAnomalyTracker anomalyTracker = AnomalyTracker.getInstance();
    private static Map<DayOfWeek, Map<Integer, LibSVM>> svrMap = new HashMap<>();
    private static IBaselineSerializer baselineSerializer = FileBaselineSerializer.getInstance();
    private static Map<LibSVM, Instances> svmDatasets = new HashMap<>();
    private static BaselineWindowSizeInfoHelper baselineWindowSizeInfoHelper = BaselineWindowSizeInfoHelper.getInstance();
    private static LeverInfoHelper leverInfoHelper = LeverInfoHelper.getInstance();
    private static final Logger logger = (Logger) LoggerFactory.getLogger(IPatternBuilder.class);

    private static double classify(DayOfWeek dayOfWeek, int routeIdx, int second) throws Exception {
        LibSVM svr;
        svr = svrMap.get(dayOfWeek).get(routeIdx);
        Attribute timeInSeconds = new Attribute("time");
        Instance instance = new DenseInstance(1);
        instance.setDataset(svmDatasets.get(svr));
        instance.setValue(svmDatasets.get(svr).attribute(0), second);
        return svr.classifyInstance(instance);
    }

    public static void computeClassifier(List<Record> records, boolean shouldSetAfterComputing) throws Exception {
        Map<DayOfWeek, Map<Integer, LibSVM>> baseline = new HashMap<>();

        List<Record> _records = new LinkedList<>();
        Map<Integer, List<Point2D.Double>> pointsMap = new HashMap<>();
        _records.addAll(records);



        int recordRouteID;
        List<Point2D.Double> points;
        Attribute timeInSeconds = new Attribute("time");
        Attribute durationInTraffic = new Attribute("durationInTraffic");
        ArrayList<Attribute> attrs = new ArrayList<>();
        attrs.add(timeInSeconds);
        attrs.add(durationInTraffic);


        for (DayOfWeek day : DayOfWeek.values()) {

            pointsMap.clear();

            for (Record record : _records) {
                recordRouteID = record.getRouteID();
                points = pointsMap.get(recordRouteID);
                if(points == null) {
                    points = new LinkedList<>();
                    pointsMap.put(recordRouteID, points);
                }
                if (record.getDayOfWeek().compareTo(day) == 0) {
                    points.add(new Point2D.Double(record.getTimeInSeconds(), record.getDurationInTraffic()));
                }
                //TODO: sprawdzić, czy kod poniżej będzie kompatybilny z nowym baseline, nie jestem tego pewien
//                AvailableHistoricalInfoHelper.addAvailableDateRoute(
//                        record.getDateTime().toString("yyyy-MM-dd"),
//                        record.getRouteID()
//                );
            }

            final Map<Integer, LibSVM> svmRoutes = new HashMap<>();

            pointsMap.keySet()
                    .stream()
                    .filter(routeID -> pointsMap.get(routeID).size() != 0)
                    .forEach(routeID -> {
                        Instances dataset = new Instances("routes_dataset", attrs, 0);
                        dataset.setClassIndex(dataset.numAttributes() - 1);
                        LibSVM svm = new LibSVM();
                        try {
                            svm.setOptions("-S 3 -K 2 -Z -C 1000 -P 0.01".split(" "));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        pointsMap.get(routeID).stream().forEach(point -> {
                            Instance instance = new DenseInstance(2);
                            instance.setValue(timeInSeconds, point.getX());
                            instance.setValue(durationInTraffic, point.getY());
                            dataset.add(instance);
                        });
                        try {
                            svm.buildClassifier( dataset );
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        svmDatasets.put(svm, dataset);
                        svmRoutes.put(routeID, svm);
                    });
            baseline.put(day, svmRoutes);

        }

        //TODO: poniższe nie działa, nie zgadzają się typy
//        String baselineFilename = baselineSerializer.serialize(baseline);
//        if (baselineFilename != null) {
//            logger.info("Baseline has been serialized in {} file", baselineFilename);
//        } else {
//            logger.debug("Error occurred while serializing baseline");
//        }

        if (shouldSetAfterComputing)
            svrMap = baseline;
    }

    public static double[] getValueForEachMinuteOfDay(DayOfWeek dayOfWeek, int routeIdx) throws Exception {
        double[] values = new double[1440];
        int idx = 0;
        double value;
        for (int i = 0; i < 86400; i = i + 60) {
            value = classify(dayOfWeek, routeIdx, i);
            values[idx] = value;
            idx++;
        }
        return values;
    }

    public AnomalyOperationProtos.AnomalyMessage isAnomaly(DayOfWeek dayOfWeek, int routeIdx, long secondOfDay, long travelDuration) {
        double predictedTravelDuration = 0;
        try {
            predictedTravelDuration = classify(dayOfWeek, routeIdx, (int) secondOfDay);
        } catch (Exception e) {
            e.printStackTrace();
        }
        double errorSensitivity = leverInfoHelper.getLeverValue();
        double bounds = 0.25 + errorSensitivity; // %
        double errorDelta = predictedTravelDuration * bounds;
        int baselineWindowSize = baselineWindowSizeInfoHelper.getBaselineWindowSizeValue();
        double predictedTravelDurationMinimum = Double.MAX_VALUE;
        double predictedTravelDurationMaximum = Double.MIN_VALUE;
        double errorRate = 0.0;

        for (int unitDiff = -baselineWindowSize; unitDiff <= baselineWindowSize; unitDiff++) {
            double tempDuration = 0;
            try {
                tempDuration = classify(dayOfWeek, routeIdx, (int) secondOfDay + (unitDiff * 60));
            } catch (Exception e) {
                e.printStackTrace();
            }
            predictedTravelDurationMinimum = predictedTravelDurationMinimum < tempDuration ? predictedTravelDurationMinimum : tempDuration;
            predictedTravelDurationMaximum = predictedTravelDurationMaximum < tempDuration ? tempDuration : predictedTravelDurationMaximum;
        }

        logger.info("#####################");
        logger.info("Error rate: " + errorDelta);
        logger.info(String.valueOf(predictedTravelDurationMinimum - errorDelta));
        logger.info(String.valueOf(predictedTravelDurationMaximum + errorDelta));


        if ((travelDuration > predictedTravelDurationMaximum + errorDelta) || (travelDuration < predictedTravelDurationMinimum - errorDelta)) {

            if (travelDuration > predictedTravelDuration + errorDelta)
                errorRate = travelDuration / predictedTravelDuration;
            else
                errorRate = travelDuration / predictedTravelDuration;

            String anomalyID = anomalyTracker.put(routeIdx, DateTime.now());
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
}
