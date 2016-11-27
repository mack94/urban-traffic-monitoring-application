package pl.edu.agh.pp.builders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.adapters.Server;
import pl.edu.agh.pp.utils.enums.DayOfWeek;
import pl.edu.agh.pp.operations.AnomalyOperationProtos;

/**
 * Created by Maciej on 18.07.2016.
 * 21:36
 * Project: detector.
 */
public class NeuralNetworksPatternBuilder implements Strategy {

    private final Logger logger = (Logger) LoggerFactory.getLogger(IPatternBuilder.class);

    @Override
    public AnomalyOperationProtos.AnomalyMessage isAnomaly(DayOfWeek dayOfWeek, int routeIdx, long secondOfDay, long travelDuration) {
        return null;
    }

    @Override
    public void setServer(Server server) {

    }
}