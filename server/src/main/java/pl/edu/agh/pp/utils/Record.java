package pl.edu.agh.pp.utils;

import org.joda.time.DateTime;
import pl.edu.agh.pp.utils.enums.DayOfWeek;

/**
 * Created by Maciej on 18.07.2016.
 * 21:40
 * Project: detector.
 */
public class Record {

    private int routeID;
    private DateTime dateTime;
    private int duration;
    private int durationInTraffic;
    private String distance;
    private String waypoints;
    private String anomalyID;

    public int getRouteID() {
        return routeID;
    }

    public void setRouteID(int routeID) {
        this.routeID = routeID;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDurationInTraffic() {
        return durationInTraffic;
    }

    public void setDurationInTraffic(int durationInTraffic) {
        this.durationInTraffic = durationInTraffic;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(String waypoints) {
        this.waypoints = waypoints;
    }

    public DayOfWeek getDayOfWeek() {
        return DayOfWeek.fromValue(getDateTime().getDayOfWeek());
    }

    public int getTimeInSeconds() {
        return getDateTime().getSecondOfDay();
    }

    public String getAnomalyID() {
        return anomalyID;
    }

    public void setAnomalyID(String anomalyID) {
        this.anomalyID = anomalyID;
    }
}
