package pl.agh.edu.pp.detector.distributions;

import java.util.Random;

/**
 * Created by Maciej on 19.07.2016.
 * 20:58
 * Project: detector.
 */

/**
 Generate pseudo-random floating point values, with an
 approximately Gaussian (normal) distribution.

 Many physical measurements have an approximately Gaussian
 distribution; this provides a way of simulating such values.
 */
public final class GaussianDistribution {

    private Random fRandom = new Random();

    public double getGaussian(double aMean, double aVariance){
        return aMean + fRandom.nextGaussian() * aVariance;
    }

    private static void log(Object aMsg){
        System.out.println(String.valueOf(aMsg));
    }
}
