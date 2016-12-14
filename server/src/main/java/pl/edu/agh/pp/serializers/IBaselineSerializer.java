package pl.edu.agh.pp.serializers;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import pl.edu.agh.pp.utils.enums.DayOfWeek;

import java.util.Map;

/**
 * Created by Jakub Janusz on 31.10.2016.
 * 19:10
 * server
 */
public interface IBaselineSerializer {
    String serialize(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline);

    Map<DayOfWeek, Map<Integer, PolynomialFunction>> deserialize(String filename);

    boolean doesBaselineFitConditions(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline, DayOfWeek dayOfWeek, int id);
}
