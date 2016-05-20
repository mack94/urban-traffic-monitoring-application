package main.java.input;

/**
 * Created by Dawid on 2016-05-20.
 */
public class Record {
    private String date;
    private String id;
    private String distance;
    private String duration;
    private String durationInTraffic;

    public String toString(){
        return "Date: "+date+" ID: "+id+" Distance: "+distance+" Duration: "+duration+" Duration in traffic: "+durationInTraffic;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDurationInTraffic() {
        return durationInTraffic;
    }

    public void setDurationInTraffic(String durationInTrafic) {
        this.durationInTraffic = durationInTrafic;
    }
}
