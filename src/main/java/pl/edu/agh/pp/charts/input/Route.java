package pl.edu.agh.pp.charts.input;

/**
 * Created by Dawid on 2016-06-06.
 */
public class Route {
    private String id;
    private String origin;
    private String destination;

    public Route(String id, String origin, String destination){
        this.id = id;
        this.origin = origin;
        this.destination = destination;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String toString(){
        return id +" - "+origin+" - "+destination;
    }

    public String getID(String txt){
        return null;
    }
}
