package pl.edu.agh.pp.serializers;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.joda.time.DateTime;
import pl.edu.agh.pp.utils.enums.DayOfWeek;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Jakub Janusz on 31.10.2016.
 * 19:10
 * server
 */
public interface ISerializer {
    String serializeBaseline(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline);

    Map<DayOfWeek, Map<Integer, PolynomialFunction>> deserializeBaseline(String filename);

    boolean doesBaselineFitConditions(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline, DayOfWeek dayOfWeek, int id);

    void serializeAnomalyTime(Map<Integer, DateTime> anomalyTime);

    void serializeAnomalyId(Map<Integer, String> anomalyId);

    ConcurrentHashMap<Integer, DateTime> deserializeAnomalyTime();

    ConcurrentHashMap<Integer, String> deserializeAnomalyId();
}
