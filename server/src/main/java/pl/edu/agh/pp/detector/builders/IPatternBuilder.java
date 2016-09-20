package pl.edu.agh.pp.detector.builders;

import pl.edu.agh.pp.detector.enums.DayOfWeek;
import pl.edu.agh.pp.detector.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.detector.records.Record;

import java.util.List;

/**
 * Created by Maciej on 24.08.2016.
 * 20:01
 * Project: detector.
 */
public interface IPatternBuilder {

    static void computePolynomial(List<Record> records) {
    }

    ;

    @Deprecated
    static double[] getValueForEachSecondOfDay(DayOfWeek dayOfWeek, int routeIdx) {
        return new double[0];
    }

    AnomalyOperationProtos.AnomalyMessage isAnomaly(DayOfWeek dayOfWeek, int routeIdx, long secondOfDay, long travelDuration);

}
