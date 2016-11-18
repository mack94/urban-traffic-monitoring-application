package pl.edu.agh.pp.detector.builders;

import java.util.Map;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import pl.edu.agh.pp.detector.enums.DayOfWeek;
import pl.edu.agh.pp.detector.operations.AnomalyOperationProtos;

/**
 * Created by Maciej on 24.08.2016.
 * 20:01
 * Project: detector.
 */
public interface IPatternBuilder
{
    AnomalyOperationProtos.AnomalyMessage isAnomaly(DayOfWeek dayOfWeek, int routeIdx, long secondOfDay, long travelDuration);

    void setBaseline(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline);

    void setPartialBaseline(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline, DayOfWeek dayOfWeek, int id);

    void updateBaseline(Map<DayOfWeek, Map<Integer,PolynomialFunction>> baseline);
}
