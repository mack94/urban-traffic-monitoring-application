package pl.edu.agh.pp.charts.input;

import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;

/**
 * Created by Dawid on 2016-10-23.
 */
public class Anomaly {
    private String screenId;
    private String anomalyId;
    private String startDate;
    private String endDate;
    private String routeId;
    private String route;
    private String severity;
    private String percent;
    private String standardTime;
    private String driveTime;

    public Anomaly(AnomalyOperationProtos.AnomalyMessage anomalyMessage){

    }

    public String getScreenId(){
        return screenId;
    }
    public String getAnomalyId(){
        return anomalyId;
    }

    public void addMessage(AnomalyOperationProtos.AnomalyMessage anomalyMessage){

    }
}
