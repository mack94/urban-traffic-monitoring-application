package pl.edu.agh.pp.builders;

import pl.edu.agh.pp.adapters.Server;
import pl.edu.agh.pp.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.utils.enums.DayOfWeek;

/**
 * Created by Maciej on 27.11.2016.
 * 00:48
 * Project: server.
 */
public class BuilderContext {

    private Strategy strategy;

    public BuilderContext(Strategy strategy) {
        this.strategy = strategy;
    }

    public AnomalyOperationProtos.AnomalyMessage isAnomaly(DayOfWeek dayOfWeek, int routeIdx, long secondOfDay, long travelDuration) {
        return strategy.isAnomaly(dayOfWeek, routeIdx, secondOfDay, travelDuration);
    }

    public void setServer(Server server) {
        strategy.setServer(server);
    }

}
