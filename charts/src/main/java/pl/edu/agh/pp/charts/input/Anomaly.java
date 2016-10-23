package pl.edu.agh.pp.charts.input;

import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;

/**
 * Created by Dawid on 2016-10-23.
 */
public class Anomaly {
    private String screenMessage;
    private String anomalyId;
    private String startDate;
    private String endDate;
    private String routeId;
    private String route;

    public String getEndDate() {
        return endDate;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getRoute() {
        return route;
    }

    public String getSeverity() {
        return severity;
    }

    public String getPercent() {
        return percent;
    }

    public String getStandardTime() {
        return standardTime;
    }

    public String getDriveTime() {
        return driveTime;
    }

    private String severity;
    private String percent;
    private String standardTime;
    private String driveTime;

    public Anomaly(AnomalyOperationProtos.AnomalyMessage anomalyMessage){
        this.anomalyId = String.valueOf(anomalyMessage.getAnomalyID());
        this.startDate = anomalyMessage.getDate();
        this.endDate = anomalyMessage.getDate();
        this.routeId = String.valueOf(anomalyMessage.getRouteIdx());
        this.route = RoutesLoader.getRoute(routeId);
        buildScreenMessage();
    }

    public String getScreenMessage(){
        return screenMessage;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getAnomalyId(){
        return anomalyId;
    }

    public void addMessage(AnomalyOperationProtos.AnomalyMessage anomalyMessage){

    }

    private void buildScreenMessage(){
        this.screenMessage = routeId + "              " + startDate;
    }
}
