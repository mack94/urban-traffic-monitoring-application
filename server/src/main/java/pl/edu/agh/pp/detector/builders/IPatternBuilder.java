package pl.edu.agh.pp.detector.builders;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import pl.edu.agh.pp.detector.enums.DayOfWeek;
import pl.edu.agh.pp.detector.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.detector.records.Record;

import java.util.List;
import java.util.Map;

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

    void setBaseline(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline);

}
