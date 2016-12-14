package pl.edu.agh.pp.builders;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import pl.edu.agh.pp.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.utils.enums.DayOfWeek;

import java.util.Map;

/**
 * Created by Maciej on 24.08.2016.
 * 20:01
 * Project: detector.
 */
public interface IPatternBuilder {
    AnomalyOperationProtos.AnomalyMessage isAnomaly(DayOfWeek dayOfWeek, int routeIdx, long secondOfDay, long travelDuration);

    void setBaseline(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline, String filename);

    void setPartialBaseline(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline, DayOfWeek dayOfWeek, int id, String filename);

    void updateBaseline(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline, String filename);
}
