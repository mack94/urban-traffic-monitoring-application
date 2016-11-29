package pl.edu.agh.pp.builders;

import pl.edu.agh.pp.adapters.Server;
import pl.edu.agh.pp.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.utils.enums.DayOfWeek;

/**
 * Created by Maciej on 27.11.2016.
 * 00:50
 * Project: server.
 */
public interface Strategy {

    AnomalyOperationProtos.AnomalyMessage isAnomaly(DayOfWeek dayOfWeek, int routeIdx, long secondOfDay, long travelDuration);

    void setServer(Server server);

}
