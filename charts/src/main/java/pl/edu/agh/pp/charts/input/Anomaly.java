package pl.edu.agh.pp.charts.input;

import javafx.scene.chart.XYChart;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dawid on 2016-10-23.
 */
public class Anomaly {
    private String screenMessage;
    private String anomalyId;
    private String startDate;
    private String lastDate;
    private String routeId;
    private String route;
    private String duration;
    private String severity;
    private String percent;
    private Map<String, String> durationHistory;
    private XYChart.Series<Number, Number> anomalySeries = null;

    public Anomaly(AnomalyOperationProtos.AnomalyMessage anomalyMessage){
        this.anomalyId = String.valueOf(anomalyMessage.getAnomalyID());
        this.startDate = anomalyMessage.getDate();
        this.lastDate = anomalyMessage.getDate();
        this.routeId = String.valueOf(anomalyMessage.getRouteIdx());
        this.route = RoutesLoader.getRoute(routeId);
        this.duration = String.valueOf(anomalyMessage.getDuration());
        durationHistory = new HashMap<>();
        durationHistory.put(this.lastDate,this.duration);
        buildScreenMessage();
    }

    public String getDuration() {
        return duration;
    }

    public String getLastDate() {
        return lastDate;
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

    public String getScreenMessage(){
        return screenMessage;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getAnomalyId(){
        return anomalyId;
    }

    void addMessage(AnomalyOperationProtos.AnomalyMessage anomalyMessage){
        this.lastDate = anomalyMessage.getDate();
        this.duration = String.valueOf(anomalyMessage.getDuration());
        durationHistory.put(this.lastDate,this.duration);
    }

    public Map<String,String> getDurationHistory(){
        return durationHistory;
    }

    private void buildScreenMessage(){
        this.screenMessage = routeId + "              " + startDate;
    }

    XYChart.Series<Number, Number> getChartSeries(){
        return anomalySeries;
    }
    void setChartSeries(XYChart.Series<Number, Number> series){
        this.anomalySeries = series;
    }
}
