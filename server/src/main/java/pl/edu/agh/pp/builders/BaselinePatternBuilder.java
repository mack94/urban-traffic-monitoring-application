package pl.edu.agh.pp.builders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.adapters.Server;
import pl.edu.agh.pp.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.utils.enums.DayOfWeek;

/**
 * Created by Maciej on 18.07.2016.
 * 21:36
 * Project: detector.
 */
public class BaselinePatternBuilder implements Strategy {

    // Manual baseline <?>

    private final Logger logger = (Logger) LoggerFactory.getLogger(IPatternBuilder.class);

    @Override
    public AnomalyOperationProtos.AnomalyMessage isAnomaly(DayOfWeek dayOfWeek, int routeIdx, long secondOfDay, long travelDuration) {
        return null;
    }

    @Override
    public void setServer(Server server) {

    }
}
