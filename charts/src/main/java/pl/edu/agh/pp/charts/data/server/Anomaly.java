package pl.edu.agh.pp.charts.data.server;

import javafx.scene.chart.XYChart;
import pl.edu.agh.pp.charts.data.local.RoutesLoader;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.TreeMap;

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
    private TreeMap<String, String> durationHistory;
    private TreeMap<String, String> percentHistory;
    private int anomaliesNumber;
    private Baseline baseline = null;

    Anomaly(AnomalyOperationProtos.AnomalyMessage anomalyMessage) {
        this.anomalyId = anomalyMessage.getAnomalyID();
        this.startDate = anomalyMessage.getDate();
        this.lastDate = anomalyMessage.getDate();
        this.routeId = String.valueOf(anomalyMessage.getRouteIdx());
        this.route = RoutesLoader.getRoute(routeId);
        this.duration = String.valueOf(anomalyMessage.getDuration());
        this.dayOfWeek = String.valueOf(anomalyMessage.getDayOfWeek());
        this.percent = String.valueOf(anomalyMessage.getNormExceed());
        durationHistory = new TreeMap<>(String::compareTo);
        percentHistory = new TreeMap<>(String::compareTo);
        durationHistory.put(this.lastDate, this.duration);
        percentHistory.put(this.lastDate, this.percent);
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
        int score = 0;
        Map.Entry<String,String> currentEntry = percentHistory.lastEntry();
        if(percentHistory.size()>1) {
            Map.Entry<String,String> previousEntry = percentHistory.lowerEntry(currentEntry.getKey());
            if(currentEntry.getValue().compareTo(previousEntry.getValue())>0) score += 2;
            else if(currentEntry.getValue().compareTo(previousEntry.getValue())<0) score -= 2;
            if(percentHistory.size()>2) {
                if(previousEntry.getValue().compareTo(percentHistory.lowerEntry(previousEntry.getKey()).getValue())>0) score += 1;
                else if(previousEntry.getValue().compareTo(percentHistory.lowerEntry(previousEntry.getKey()).getValue())<0) score -= 1;
            }
        }
        if(score == 2 || score == 1) return "↗";
        if(score == 3) return "↑";
        if(score == -2 || score == -1) return "↘";
        if(score == -3) return "↓";
        return "-";
    }

    void addMessage(AnomalyOperationProtos.AnomalyMessage anomalyMessage) {
        this.lastDate = anomalyMessage.getDate();
        this.previousDuration = this.duration;
        this.duration = String.valueOf(anomalyMessage.getDuration());
        this.percent = String.valueOf(anomalyMessage.getNormExceed());
        durationHistory.put(this.lastDate, this.duration);
        percentHistory.put(this.lastDate, this.percent);
        anomaliesNumber++;
    }

    public Map<String, String> getDurationHistory() {
        return durationHistory;
    }

    public Map<String, String> getPercentHistory() {
        return percentHistory;
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
