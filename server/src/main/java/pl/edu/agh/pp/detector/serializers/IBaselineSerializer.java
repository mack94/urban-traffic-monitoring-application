package pl.edu.agh.pp.detector.serializers;

import java.util.Map;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import pl.edu.agh.pp.detector.enums.DayOfWeek;

/**
 * Created by Jakub Janusz on 31.10.2016.
 * 19:10
 * server
 */
public interface IBaselineSerializer
{
    void serialize(Map<DayOfWeek, Map<Integer, PolynomialFunction>> baseline);

    Map<DayOfWeek, Map<Integer, PolynomialFunction>> deserialize(String filename);
}
