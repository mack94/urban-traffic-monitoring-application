package pl.edu.agh.pp.builders;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.jfree.ui.RefineryUtilities;
import pl.edu.agh.pp.charts.LineChart_AWT;
import pl.edu.agh.pp.charts.XYLineChart_AWT;
import pl.edu.agh.pp.utils.AvailableHistoricalInfoHelper;
import pl.edu.agh.pp.utils.HistoricalInfoHelper;
import pl.edu.agh.pp.utils.Record;
import pl.edu.agh.pp.utils.enums.DayOfWeek;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.core.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

/**
 * Created by Krzysztof Węgrzyński on 2016-04-21.
 */
public class SupportVectorRegressionPatternBuilder {
    private static final int SECONDS_IN_24_HOURS = 86400;

    private static double normalize(double min, double max, double value) {
        return (value-min)/(max-min);
    }

    private static int denormalize(double min, double max, double normalizedValue) {
        return (int)(normalizedValue*(max-min) + min);
    }
    public static void computePolynomial(List<Record> records, boolean shouldSetAfterComputing) throws Exception {
        Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline = new HashMap<>();

        List<Record> _records = new LinkedList<>();
        List<Integer> durationsInTraffic = new LinkedList<>();
        Map<Integer, List<Point2D.Double>> pointsMap = new HashMap<>();
        _records.addAll(records);
        _records.stream().forEach(record -> durationsInTraffic.add(record.getDurationInTraffic()));
        int minDurationInTraffic = Collections.min(durationsInTraffic);
        int maxDurationInTraffic = Collections.max(durationsInTraffic);
        LibSVM svm = new LibSVM();


        int recordRouteID;
        List<Point2D.Double> points;
        for (Record record : _records) {
            points = pointsMap.get(record.getRouteID());
            if(points == null) {
                points = new LinkedList<>();
                pointsMap.put(record.getRouteID(), points);
            }
            //points.add(new Point2D.Double((double)record.getTimeInSeconds()/SECONDS_IN_24_HOURS, normalize(minDurationInTraffic, maxDurationInTraffic, record.getDurationInTraffic())));
            points.add(new Point2D.Double(record.getTimeInSeconds(), record.getDurationInTraffic()));
            //System.out.println("Time: " + (double)record.getTimeInSeconds()/SECONDS_IN_24_HOURS + " Duration in traffic: " + normalize(minDurationInTraffic, maxDurationInTraffic, record.getDurationInTraffic()));
        }

        Attribute timeInSeconds = new Attribute("time");
        Attribute durationInTraffic = new Attribute("durationInTraffic");
        ArrayList<Attribute> attrs = new ArrayList<>();
        attrs.add(timeInSeconds);
        attrs.add(durationInTraffic);
        final Instances dataset = new Instances("my_dataset", attrs, 0);
        dataset.setClassIndex(dataset.numAttributes() - 1);
        svm.setOptions("-S 3 -K 2 -Z -C 1000 -P 0.01".split(" "));
//        svm.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_RBF, LibSVM.TAGS_KERNELTYPE));
//        svm.setSVMType(new SelectedTag(LibSVM.SVMTYPE_EPSILON_SVR, LibSVM.TAGS_SVMTYPE)); // -S 3=epsilon-SVR


        pointsMap.get(1).stream().forEach(point -> {
            Instance instance = new DenseInstance(2);
            instance.setValue(timeInSeconds, point.getX());
            instance.setValue(durationInTraffic, point.getY());
            dataset.add(instance);
        });




        svm.buildClassifier( dataset );
        int trainSize = dataset.numInstances()-287;
        int testSize = dataset.numInstances() - trainSize;
        Instances train = new Instances(dataset, 0, trainSize);
        train.setClassIndex(train.numAttributes() - 1);
        Instances test = new Instances(dataset, trainSize, testSize);
        test.setClassIndex(test.numAttributes() - 1);

        Evaluation eval = new Evaluation(train); //trainset
        eval.evaluateModel(svm, test); //testset
        System.out.println(eval.toSummaryString());
        eval.predictions().stream().forEach(prediction -> {
            System.out.println("predicted: " + prediction.predicted() + " actual: " + prediction.actual());
        });

        LineChart_AWT chart;
        double[] values = new double[1440];
        int idx = 0;

        for (int i = 0; i < 86400; i = i + 60) {
            Instance instance = new DenseInstance(1);
            instance.setValue(timeInSeconds, i);
            instance.setDataset(dataset);
            double value = svm.classifyInstance(instance);
            System.out.println("Time: " + instance.value(0) + " predicted: " + value);
            values[idx] = value;
            idx++;
        }

        chart = new LineChart_AWT("test", "Baseline i anomalie dla trasy " + 1, values);
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);
        //System.out.println(eval.weightedFMeasure());
        //System.out.println(eval.weightedPrecision());
        //System.out.println(eval.weightedRecall());

        System.out.println("min: " + minDurationInTraffic + " max: " + maxDurationInTraffic);
    }
}
