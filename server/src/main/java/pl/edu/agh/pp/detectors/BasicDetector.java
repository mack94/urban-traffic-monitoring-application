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

    private PolynomialPatternBuilder polynomialPatternBuilder = PolynomialPatternBuilder.getInstance();

    public BasicDetector() {
    }

    public AnomalyOperationProtos.AnomalyMessage isAnomaly(DayOfWeek dayOfWeek, int routeIdx, long secondOfDay, long travelDuration) {
        return polynomialPatternBuilder.isAnomaly(dayOfWeek, routeIdx, secondOfDay, travelDuration);
    }

}
