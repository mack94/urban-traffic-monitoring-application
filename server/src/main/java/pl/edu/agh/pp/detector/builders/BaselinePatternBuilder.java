package pl.edu.agh.pp.detector.builders;

import pl.edu.agh.pp.detector.detectors.Detector;
import pl.edu.agh.pp.detector.enums.DayOfWeek;
import pl.edu.agh.pp.detector.operations.AnomalyOperationProtos;

/**
 * Created by Maciej on 18.07.2016.
 * 21:36
 * Project: detector.
 */
public class BaselinePatternBuilder implements Detector {
    @Override
    public AnomalyOperationProtos.AnomalyMessage isAnomaly(DayOfWeek dayOfWeek, int routeIdx, long secondOfDay, long travelDuration) {
        return null;
    }
}
