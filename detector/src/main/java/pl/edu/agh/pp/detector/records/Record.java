package pl.edu.agh.pp.detector.records;

import org.joda.time.DateTime;

/**
 * Created by Maciej on 18.07.2016.
 * 21:40
 * Project: detector.
 */
public class Record {

    private int id;
    private DateTime dateTime;
    private int duration;
    private int durationInTraffic;
    private int distance;
    private String waypoints;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(String waypoints) {
        this.waypoints = waypoints;
    }
}
