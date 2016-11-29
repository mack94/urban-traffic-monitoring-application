package pl.edu.agh.pp.detectors;

import pl.edu.agh.pp.builders.PolynomialPatternBuilder;
import pl.edu.agh.pp.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.utils.enums.DayOfWeek;

/**
 * Created by Maciej on 02.08.2016.
 * 20:26
 * Project: detector.
 */

// For instance the Basic Detector uses only PolynomialPattern Builder.
public class BasicDetector implements Detector {

    //TODO przerobić na interfejs
    private PolynomialPatternBuilder polynomialPatternBuilder = PolynomialPatternBuilder.getInstance();

    public BasicDetector() {
    }

    public AnomalyOperationProtos.AnomalyMessage isAnomaly(DayOfWeek dayOfWeek, int routeIdx, long secondOfDay, long travelDuration) {
        return polynomialPatternBuilder.isAnomaly(dayOfWeek, routeIdx, secondOfDay, travelDuration);
    }

    /**
     * TODO: Modify and make an interface
     * Could be PolynomialDetector, BaselineDetector, NeuralNetworksDetector, BayesianDetected made.
     * Each could have an possibility to detect an anomaly and what is more important, the ability,
     * to communicate with the data logger.
     * It's due to the fact that we need to have a good data interchange and anomaly detection in real-time.
     * Moreover, the logger should log faster if anomaly detector want this (it want this if first-time anomaly detected).
     * So, this should be like an server application, and logger like a client application.
     * This could be build based on the JGroups architecture.
     */
}
