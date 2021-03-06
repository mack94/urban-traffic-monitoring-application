package pl.edu.agh.pp.trackers;

import org.joda.time.DateTime;
import pl.edu.agh.pp.adapters.AnomaliesServer;
import pl.edu.agh.pp.utils.JodaTimeHelper;

import java.util.List;

/**
 * Created by Maciej on 05.10.2016.
 *
 * @author Maciej Makówka
 *         11:13
 *         Project: server.
 */
public interface IAnomalyTracker {

    /**
     * Puts the anomaly to the structure with current anomalies.
     *
     * @param routeID  The ID / Index of the route on which the anomaly occurred.
     * @param dateTime The Joda DateTime object with the Date and Time when the anomaly occurred.
     * @return The ID of persisted anomaly.
     */
    String put(int routeID, DateTime dateTime);

    /**
     * Get the ID of the last anomaly on the desired route.
     *
     * @param routeID The Index/ID of the route.
     * @return The ID of last anomaly on this route, or -1 if the anomaly never occurred there.
     */
    String get(int routeID);

    /**
     * Marks anomaly at given route ID as finished.
     *
     * @param routeID The Index/ID of the route.
     */
    void remove(int routeID);

    /**
     * Get the last update time of anomalies for the route which ID is passed by value in argument.
     *
     * @param routeID The Index/ID of the route.
     * @return The DateTime object that represents the Date and Time of last update. If its the first
     * update then MINIMUM_ANOMALY_DATE is returned.
     * @see JodaTimeHelper
     */
    DateTime getLastUpdate(int routeID);

    /**
     * Check, if there was an anomaly on given route, since server application was started.
     *
     * @param routeID The Index/ID of the route.
     * @return True if an anomaly occurred at given route, false otherwise.
     */
    boolean has(int routeID);

    /**
     * Get list of routes, on which an anomaly was detected, since server application
     * was started.
     *
     * @return List of type Integer, which contains routes ID number.
     */
    List<Integer> getCurrentAnomaliesRoutesIds();

    /**
     * Set the anomaly server - the server where the results of anomaly tracking will be send.
     *
     * @param anomaliesServer The AnomaliesServer that us clients are connected to.
     * @see AnomaliesServer
     * @see pl.edu.agh.pp.adapters.Server
     */
    void setAnomaliesServer(AnomaliesServer anomaliesServer);
}
