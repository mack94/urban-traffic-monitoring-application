package pl.edu.agh.pp.builders;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.adapters.AnomaliesServer;
import pl.edu.agh.pp.adapters.Server;
import pl.edu.agh.pp.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.serializers.FileSerializer;
import pl.edu.agh.pp.serializers.ISerializer;
import pl.edu.agh.pp.trackers.AnomalyTracker;
import pl.edu.agh.pp.trackers.IAnomalyTracker;
import pl.edu.agh.pp.utils.BaselineWindowSizeInfoHelper;
import pl.edu.agh.pp.utils.LeverInfoHelper;
import pl.edu.agh.pp.utils.Record;
import pl.edu.agh.pp.utils.enums.DayOfWeek;
import weka.classifiers.functions.LibSVM;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.awt.geom.Point2D;
import java.util.*;

/**
 * Created by Krzysztof Węgrzyński on 2016-04-21.
 */
public class SupportVectorRegressionPatternBuilder implements Strategy {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(IPatternBuilder.class);
    private static int DAY_INTERVALS = 4;
    private static int INTERVAL = 1200;
    private static IAnomalyTracker anomalyTracker = AnomalyTracker.getInstance();
    private static Map<DayOfWeek, Map<Integer, List<LibSVM>>> svrMap = new HashMap<>();
    private static ISerializer baselineSerializer = FileSerializer.getInstance();
    private static Map<LibSVM, Instances> svmDatasets = new HashMap<>();
    private static BaselineWindowSizeInfoHelper baselineWindowSizeInfoHelper = BaselineWindowSizeInfoHelper.getInstance();
    private static LeverInfoHelper leverInfoHelper = LeverInfoHelper.getInstance();

    private static double classify(DayOfWeek dayOfWeek, int routeIdx, int second) throws Exception {
        LibSVM svr = null;
        int interval = 86400 / DAY_INTERVALS;
        for (int i = 0; i < DAY_INTERVALS; i++) {
            if (i + 1 == DAY_INTERVALS) {
                svr = svrMap.get(dayOfWeek).get(routeIdx).get(i);
            } else if (second < interval * (i + 1)) {
                svr = svrMap.get(dayOfWeek).get(routeIdx).get(i);
                break;
            }
        }

        Attribute timeInSeconds = new Attribute("time");
        Instance instance = new Instance(1);
        instance.setDataset(svmDatasets.get(svrMap.get(dayOfWeek).get(routeIdx).get(0)));
        instance.setValue(svmDatasets.get(svrMap.get(dayOfWeek).get(routeIdx).get(0)).attribute(0), second);
        return svr.classifyInstance(instance);
    }

    private static void addInstance(int intervalMargin, Point2D.Double point, List<Instances> datasets, Attribute timeInSeconds, Attribute durationInTraffic) {
        int interval = 86400 / DAY_INTERVALS;
        for (int i = 0; i < DAY_INTERVALS; i++) {
            Instance instance = new Instance(2);
            instance.setValue(timeInSeconds, point.getX());
            instance.setValue(durationInTraffic, point.getY());
            if (i + 1 == DAY_INTERVALS) {
                datasets.get(i).add(instance);
            } else if (point.getX() < interval * (i + 1)) {
                if (point.getX() > interval * (i + 1) - intervalMargin && datasets.get(i + 1) != null) {
                    datasets.get(i + 1).add(instance);
                }
                datasets.get(i).add(instance);
                break;
            }
        }
    }

    public static void computeClassifier(List<Record> records, boolean shouldSetAfterComputing) throws Exception {
        Map<DayOfWeek, Map<Integer, List<LibSVM>>> baseline = new HashMap<>();

        List<Record> _records = new LinkedList<>();
        Map<Integer, List<Point2D.Double>> pointsMap = new HashMap<>();
        _records.addAll(records);


        int recordRouteID;
        List<Point2D.Double> points;
        Attribute timeInSeconds = new Attribute("time");
        Attribute durationInTraffic = new Attribute("durationInTraffic");
        //ArrayList<Attribute> attrs = new ArrayList<>();
        FastVector attrs = new FastVector();
        attrs.addElement(timeInSeconds);
        attrs.addElement(durationInTraffic);


        for (DayOfWeek day : DayOfWeek.values()) {

            pointsMap.clear();

            for (Record record : _records) {
                if (!record.getAnomalyID().equals("")) continue;

                recordRouteID = record.getRouteID();
                points = pointsMap.get(recordRouteID);
                if (points == null) {
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

            final Map<Integer, List<LibSVM>> svmRoutes = new HashMap<>();

            pointsMap.keySet()
                    .stream()
                    .filter(routeID -> pointsMap.get(routeID).size() != 0)
                    .forEach(routeID -> {
                        List<Instances> datasets = new ArrayList<>();
                        List<LibSVM> classifiers = new LinkedList<>();
                        for (int i = 0; i < DAY_INTERVALS; i++) datasets.add(new Instances("routes_dataset", attrs, 0));
                        datasets.forEach(instances -> instances.setClassIndex(1));

                        for (int i = 0; i < DAY_INTERVALS; i++) classifiers.add(new LibSVM());


                        classifiers.forEach(classifier -> {
                            try {
                                classifier.setOptions("-S 3 -K 2 -Z -C 1000 -P 0.01".split(" "));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });


                        pointsMap.get(routeID).forEach(point -> {
                            addInstance(INTERVAL, point, datasets, timeInSeconds, durationInTraffic);
                        });
                        try {
                            for (int i = 0; i < DAY_INTERVALS; i++) classifiers.get(i).buildClassifier(datasets.get(i));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        svmDatasets.put(classifiers.get(0), datasets.get(0));
                        svmRoutes.put(routeID, classifiers);
                    });
            baseline.put(day, svmRoutes);

        }

        //TODO: poniższe nie działa, nie zgadzają się typy
//        String baselineFilename = baselineSerializer.serializeBaseline(baseline);
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

    public static void setDayIntervals(int dayIntervals) {
        DAY_INTERVALS = dayIntervals;
    }

    public static void setInterval(int INTERVAL) {
        SupportVectorRegressionPatternBuilder.INTERVAL = INTERVAL;
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


        if ((travelDuration > predictedTravelDurationMaximum + errorDelta)) {

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

    @Override
    public void setServer(Server server) {
        anomalyTracker.setAnomaliesServer((AnomaliesServer) server);
    }
}
