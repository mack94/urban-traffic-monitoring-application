package pl.edu.agh.pp.detectors;

import pl.edu.agh.pp.utils.enums.DayOfWeek;
import pl.edu.agh.pp.operations.AnomalyOperationProtos;

/**
 * Created by Dawid on 2016-09-05.
 */
public interface Detector {
    AnomalyOperationProtos.AnomalyMessage isAnomaly(DayOfWeek dayOfWeek, int routeIdx, long secondOfDay, long travelDuration);
}
