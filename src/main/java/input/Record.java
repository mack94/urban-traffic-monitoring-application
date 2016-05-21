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
    private String time;

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
        return id.replace("\"","");
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDistance() {
        return distance.replace("\"", "");
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDuration() {
        return duration.replace("\"","");
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDurationInTraffic() {
        return durationInTraffic.replace("\"","");
    }

    public void setDurationInTraffic(String durationInTrafic) {
        this.durationInTraffic = durationInTrafic;
    }

    public void setTime(){
        time = date.substring(date.indexOf(' ')+1, date.length()-1);
    }

    public String getTime() {
        return time;
    }

    public double getTimeForChart(){
        double hour = Double.valueOf(time.substring(0,2));
        double minute = Double.valueOf(time.substring(3,5));
        return (hour + (minute /60));
    }

    public String getDay(){
        return date.substring(1,11);
    }

}
