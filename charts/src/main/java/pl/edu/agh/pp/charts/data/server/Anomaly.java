package pl.edu.agh.pp.charts.data.server;

import javafx.scene.chart.XYChart;
import pl.edu.agh.pp.charts.data.local.RoutesLoader;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dawid on 2016-10-23.
 */
public class Anomaly {
    private String anomalyId;
    private String startDate;
    private String lastDate;
    private String routeId;
    private String route;
    private String dayOfWeek;
    private String previousDuration;
    private String duration;
    private String severity;
    private String percent;
    private Map<String, String> durationHistory;
    private int anomaliesNumber;
    private Baseline baseline = null;

    Anomaly(AnomalyOperationProtos.AnomalyMessage anomalyMessage) {
        this.anomalyId = String.valueOf(anomalyMessage.getAnomalyID());
        this.startDate = anomalyMessage.getDate();
        this.lastDate = anomalyMessage.getDate();
        this.routeId = String.valueOf(anomalyMessage.getRouteIdx());
        this.route = RoutesLoader.getRoute(routeId);
        this.duration = String.valueOf(anomalyMessage.getDuration());
        this.dayOfWeek = String.valueOf(anomalyMessage.getDayOfWeek());
        durationHistory = new HashMap<>();
        durationHistory.put(this.lastDate, this.duration);
        anomaliesNumber = 1;
    }

    public String getDuration() {
        return duration;
    }

    public String  getPreviousDuration() {
        return previousDuration;
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

    public String getAnomaliesNumber() {
        return String.valueOf(anomaliesNumber);
    }

    public String getStartDate() {
        return startDate;
    }

    public String getAnomalyId() {
        return anomalyId;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getTrend() {
        if(duration!=null && previousDuration != null){
            if(Integer.valueOf(duration)<Integer.valueOf(previousDuration)){
                return "↓";
            }
            else if (Integer.valueOf(duration)>Integer.valueOf(previousDuration)){
                return "↑";
            }
        }
        return "-";
    }

    void addMessage(AnomalyOperationProtos.AnomalyMessage anomalyMessage) {
        this.lastDate = anomalyMessage.getDate();
        this.previousDuration = this.duration;
        this.duration = String.valueOf(anomalyMessage.getDuration());
        durationHistory.put(this.lastDate, this.duration);
        anomaliesNumber++;
    }

    public Map<String, String> getDurationHistory() {
        return durationHistory;
    }

    XYChart.Series<Number, Number> getBaselineSeries() {
        if (baseline != null) {
            return baseline.getBaselineSeries();
        } else {
            this.baseline = BaselineManager.getBaseline(Integer.valueOf(routeId), DayOfWeek.of(Integer.parseInt(getDayOfWeek())),"");
            if (baseline != null) {
                return baseline.getBaselineSeries();
            }
        }
        return null;
    }
}
